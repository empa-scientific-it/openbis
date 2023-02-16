/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.io.FileFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.IMailClientProvider;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeAndWaitingProvider;

/**
 * Class starting a daemon for deleting files.
 * <p>
 * A deletion is requested by creating a deletion request file in a specified directory. Such a file contains the absolute path of the file to be
 * deleted. It will be created by invoking {@link #requestDeletion(File)}.
 *
 * @author Franz-Josef Elmer
 */
public class FileDeleter
{
    /** File type of deletion request files. **/
    public static final String FILE_TYPE = ".deletionrequest";

    /** Format of the time stamp of deletion request. **/
    public static final String TIMESTAMP_FORMAT = "yyyyMMdd-HHmmss";

    /** Property: Time interval in milliseconds the daemon tries to handle deletion requests. */
    public static final String DELETION_POLLING_TIME_KEY = "deletion-polling-time";

    private static final long DEFAULT_DELETION_POLLING_TIME = 10 * DateUtils.MILLIS_PER_MINUTE;

    /** Property: Time out in milliseconds of deletion requests. */
    public static final String DELETION_TIME_OUT_KEY = "deletion-time-out";

    private static final long DEFAULT_DELETION_TIME_OUT = 24 * DateUtils.MILLIS_PER_HOUR;

    /** Property: Email address for notifying time-outed deletion requests. */
    public static final String EMAIL_ADDRESS_KEY = "email-address";

    /** Property: Email address for the 'from' field. */
    public static final String EMAIL_FROM_ADDRESS_KEY = "email-from-address";

    /** Property: Email subject. */
    public static final String EMAIL_SUBJECT_KEY = "email-subject";

    /** Property: Template with variable ${file-list}. */
    public static final String EMAIL_TEMPLATE_KEY = "email-template";

    /** Name of the file list variable in {@link #EMAIL_TEMPLATE_KEY} property. */
    public static final String FILE_LIST_VARIABLE = "file-list";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileDeleter.class);

    static final Template THREAD_NAME_TEMPLATE = new Template("File Deleter (${directory})");

    private static final FileFilter FILE_FILTER = new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                return file.getName().endsWith(FileDeleter.FILE_TYPE);
            }
        };

    private final SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);

    private final File directory;

    private final ITimeAndWaitingProvider timeProvider;

    private final IMailClientProvider eMailClientProvider;

    private final long pollingTime;

    private final long timeOut;

    private String emailOrNull;

    private String emailTemplate;

    private String emailSubject;

    private String emailFromAddressOrNull;

    private int counter;

    private boolean started;

    private long threadStartTime;

    /**
     * Creates an instance for the specified directory, time provider, email client and properties.
     */
    public FileDeleter(File directory, ITimeAndWaitingProvider timeProvider, IMailClientProvider eMailClientProvider, Properties properties)
    {
        this.eMailClientProvider = eMailClientProvider;
        assert directory.isDirectory();

        this.directory = directory;
        this.timeProvider = timeProvider;
        pollingTime = DateTimeUtils.getDurationInMillis(properties, DELETION_POLLING_TIME_KEY, DEFAULT_DELETION_POLLING_TIME);
        timeOut = DateTimeUtils.getDurationInMillis(properties, DELETION_TIME_OUT_KEY, DEFAULT_DELETION_TIME_OUT);
        emailOrNull = PropertyUtils.getProperty(properties, EMAIL_ADDRESS_KEY);
        if (emailOrNull != null)
        {
            emailTemplate = PropertyUtils.getMandatoryProperty(properties, EMAIL_TEMPLATE_KEY);
            emailSubject = PropertyUtils.getMandatoryProperty(properties, EMAIL_SUBJECT_KEY);
            emailFromAddressOrNull = properties.getProperty(EMAIL_FROM_ADDRESS_KEY);
        }
    }

    /**
     * Starts daemon.
     */
    public synchronized void start()
    {
        if (started)
        {
            return;
        }
        started = true;
        Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    polling();
                }
            };
        Template template = THREAD_NAME_TEMPLATE.createFreshCopy();
        template.attemptToBind("directory", directory.toString());
        new Thread(null, runnable, template.createText()).start();
    }

    /**
     * Requests deletion of specified file.
     */
    public void requestDeletion(File file)
    {
        operationLog.info("Schedule for deletion: " + file);
        String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date(timeProvider.getTimeInMilliseconds()));
        String deletionRequestFileName = timestamp + "_" + (++counter) + FILE_TYPE;
        File deletionRequestFile = new File(directory, deletionRequestFileName);
        File stagedDeletionRequestFile = new File(directory, deletionRequestFileName + ".staged");
        FileUtilities.writeToFile(stagedDeletionRequestFile, file.getAbsolutePath());
        stagedDeletionRequestFile.renameTo(deletionRequestFile);
        operationLog.info("Deletion request file for '" + file + "': " + deletionRequestFile);
    }

    /**
     * Stops daemon.
     */
    public void stop()
    {
        started = false;
    }

    private void polling()
    {
        threadStartTime = timeProvider.getTimeInMilliseconds();
        while (started)
        {
            File[] files = directory.listFiles(FILE_FILTER);
            Arrays.sort(files);
            StringBuilder builder = new StringBuilder();
            for (File deletionRequestFile : files)
            {
                handle(deletionRequestFile, builder);
            }
            if (emailOrNull != null && builder.length() > 0)
            {
                Template template = new Template(emailTemplate);
                template.attemptToBind(FILE_LIST_VARIABLE, builder.toString());
                String emailMessage = template.createText();
                IMailClient mailClient = eMailClientProvider.getMailClient();
                mailClient.sendEmailMessage(emailSubject, emailMessage,
                        new EMailAddress(emailOrNull), new EMailAddress(emailFromAddressOrNull));
            }
            timeProvider.sleep(pollingTime);
        }
    }

    private void handle(File deletionRequestFile, StringBuilder builder)
    {
        Long fileTime = getFileTime(deletionRequestFile);
        if (fileTime != null)
        {
            long time = timeProvider.getTimeInMilliseconds();
            long deletionTime = time - fileTime;
            File file = new File(FileUtilities.loadToString(deletionRequestFile).trim());
            if (delete(file))
            {
                operationLog.info("Successfully deleted: " + file);
                if (deletionRequestFile.delete() == false)
                {
                    operationLog.warn("Couldn't delete deletion request file: " + deletionRequestFile);
                }
            } else if (deletionTime > timeOut)
            {
                String message = file + "\n   Deletion requested at "
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fileTime)
                        + " (" + DateTimeUtils.renderDuration(deletionTime) + " elapsed)";
                operationLog.warn(message);
                long timeSinceStart = time - threadStartTime;
                if (timeSinceStart > timeOut && (timeSinceStart % timeOut) < pollingTime)
                {
                    builder.append(message).append('\n');
                }
            }
        }
    }

    private Long getFileTime(File trackingFile)
    {
        try
        {
            return timestampFormat.parse(trackingFile.getName().split("_")[0]).getTime();
        } catch (ParseException ex)
        {
            return null;
        }
    }

    /**
     * Deletes the specified file. Can be overridden.
     * 
     * @return <code>true</code> if the file has been successfully deleted.
     */
    protected boolean delete(File file)
    {
        try
        {
            IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
            if (fileOperations.delete(file))
            {
                return true;
            }
            return fileOperations.exists(file) == false;
        } catch (Throwable t)
        {
            return false;
        }
    }
}