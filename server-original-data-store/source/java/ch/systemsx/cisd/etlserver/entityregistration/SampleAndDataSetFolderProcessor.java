/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.filefilter.RegexFileFilter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.EMailAddress;

/**
 * Utitlity class for registering all the samples/datasets in a folder
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetFolderProcessor extends AbstractSampleAndDataSetProcessor
{
    static final String ERRORS_FILENAME = "errors.txt";

    private final ArrayList<File> controlFiles = new ArrayList<File>();

    private final HashMap<File, Exception> errorMap = new HashMap<File, Exception>();

    SampleAndDataSetFolderProcessor(SampleAndDataSetRegistrationGlobalState globalState, File folder)
    {
        super(globalState, folder);
    }

    /**
     * Check that the folder passes validation, try to register all the entities in each control file. Any errors that occur in processing are sent as
     * an email.
     */
    public void register() throws UserFailureException, EnvironmentFailureException
    {
        try
        {
            checkFolderIsFolder();
            collectControlFilesOrThrowError();

            for (File controlFile : controlFiles)
            {
                try
                {
                    SampleAndDataSetControlFileProcessor controlFileProcessor =
                            new SampleAndDataSetControlFileProcessor(globalState, folder,
                                    controlFile);
                    controlFileProcessor.register();
                } catch (UserFailureException e)
                {
                    e.printStackTrace();
                    errorMap.put(controlFile, e);
                }
            }

            checkErrorMapIsEmpty();
        } catch (UserFailureException e)
        {
            sendEmailWithErrorMessage(e.getMessage());
        } catch (Exception e)
        {
            sendEmailWithErrorMessage(e.getMessage());
            throw new CheckedExceptionTunnel(e);
        }
    }

    private void checkFolderIsFolder() throws UserFailureException
    {
        // file should be a directory
        if (false == folder.isDirectory())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(folder.getName());
            sb.append(" is an ordinary file. It must be a folder containing a control file and data subfolders.");
            throw new UserFailureException(sb.toString());
        }
    }

    private FileFilter getFileFilter()
    {
        return new RegexFileFilter(getControlFilePattern());
    }

    private String getControlFilePattern()
    {
        return globalState.getControlFilePattern();
    }

    private void collectControlFilesOrThrowError() throws UserFailureException
    {
        File[] files = folder.listFiles(getFileFilter());
        Collections.addAll(controlFiles, files);

        if (controlFiles.isEmpty())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Folder (");
            sb.append(folder.getName());
            sb.append(") for sample/dataset registration contains no control files matching the configured pattern: ");
            sb.append(getControlFilePattern());
            sb.append(".");
            sb.append("\nFolder contents:");
            for (String filename : folder.list())
            {
                sb.append("\n\t");
                sb.append(filename);
            }
            sb.append("\n");
            throw new UserFailureException(sb.toString());
        }
    }

    private void checkErrorMapIsEmpty()
    {
        if (errorMap.isEmpty())
        {
            return;
        }

        // We encountered some errors in processing
        StringBuffer sb = new StringBuffer();
        for (File file : errorMap.keySet())
        {
            sb.append("Encountered error processing ");
            sb.append(file.getName());
            sb.append(" :\n\t");
            sb.append(errorMap.get(file));
            sb.append("\n");
        }

        throw new UserFailureException(sb.toString());
    }

    protected void sendEmailWithErrorMessage(String message)
    {
        logError(message);

        // Create an email and send it.
        try
        {
            String subject = createErrorEmailSubject();
            String content = createErrorEmailContent();
            String filename = ERRORS_FILENAME;
            EMailAddress[] recipients = globalState.getErrorEmailRecipients();
            DataSource dataSource = new ByteArrayDataSource(message, "text/plain");

            globalState.getMailClient().sendEmailMessageWithAttachment(subject, content, filename,
                    new DataHandler(dataSource), null, null, recipients);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private String createErrorEmailSubject()
    {
        return String.format("Sample / Data Set Registration Error -- %s", folder);
    }

    private String createErrorEmailContent()
    {
        return String
                .format("When trying to process the files in the folder, %s, errors were encountered. These errors are detailed in the attachment.",
                        folder);
    }

}
