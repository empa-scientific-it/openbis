/*
 * Copyright 2015 ETH Zuerich, SIS
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.IMailClientProvider;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ITimeAndWaitingProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * Class doing clean ups of corrupted multi-data-set container files. Depending on the file path a container file is deleted immediately or later
 * (using a {@link FileDeleter}).
 *
 * @author Franz-Josef Elmer
 */
class MultiDataSetArchiveCleaner implements IMultiDataSetArchiveCleaner
{
    static final String FILE_PATH_PREFIXES_FOR_ASYNC_DELETION_KEY = "file-path-prefixes-for-async-deletion";

    static final String DELETION_REQUESTS_DIR_KEY = "deletion-requests-dir";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiDataSetArchiveCleaner.class);

    private static Map<File, FileDeleter> globalDeleters = new HashMap<File, FileDeleter>();

    private final List<String> filePathPrefixesForAsyncDeletion;

    private File deletionRequestsDir;

    private final Map<File, FileDeleter> deleters;

    MultiDataSetArchiveCleaner(Properties properties)
    {
        this(properties, SystemTimeProvider.SYSTEM_TIME_PROVIDER, globalDeleters);
    }

    MultiDataSetArchiveCleaner(Properties properties, ITimeAndWaitingProvider timeProvider, Map<File, FileDeleter> deleters)
    {
        this.deleters = deleters;
        this.filePathPrefixesForAsyncDeletion = getPathPrefixesForAsyncDeletion(properties);

        if (filePathPrefixesForAsyncDeletion.isEmpty())
        {
            return;
        }
        deletionRequestsDir = new File(PropertyUtils.getMandatoryProperty(properties, DELETION_REQUESTS_DIR_KEY));
        if (deletionRequestsDir.isFile())
        {
            throw new ConfigurationFailureException("Property '" + DELETION_REQUESTS_DIR_KEY
                    + "' denotes an existing file instead of a directory: " + deletionRequestsDir);
        }
        if (deletionRequestsDir.exists() == false)
        {
            if (deletionRequestsDir.mkdirs() == false)
            {
                throw new ConfigurationFailureException("Couldn't create directory: " + deletionRequestsDir);
            }
        }
        synchronized (deleters)
        {
            FileDeleter deleter = deleters.get(deletionRequestsDir);
            if (deleter == null)
            {
                deleter = new FileDeleter(deletionRequestsDir, timeProvider, new IMailClientProvider()
                    {
                        @Override
                        public IMailClient getMailClient()
                        {
                            return ServiceProvider.getDataStoreService().createEMailClient();
                        }
                    }, properties);
                deleters.put(deletionRequestsDir, deleter);
            }
            deleter.start();
        }
    }

    private List<String> getPathPrefixesForAsyncDeletion(Properties properties)
    {
        List<String> relativeFilePathPrefixesForAsyncDeletion = PropertyUtils.getList(properties, FILE_PATH_PREFIXES_FOR_ASYNC_DELETION_KEY);
        ArrayList<String> result = new ArrayList<String>();
        for (String path : relativeFilePathPrefixesForAsyncDeletion)
        {
            result.add(new File(path).getAbsolutePath());
        }
        return result;
    }

    @Override
    public void delete(File file)
    {
        if (isFileForAsyncDeletion(file))
        {
            FileDeleter deleter = deleters.get(deletionRequestsDir);
            deleter.requestDeletion(file);
        } else
        {
            deleteSync(file);
        }
    }

    private void deleteSync(File file)
    {
        if (file.delete())
        {
            operationLog.info("File immediately deleted: " + file);
        } else
        {
            operationLog.warn("Failed to delete file immediately: " + file);
        }
    }

    private boolean isFileForAsyncDeletion(File file)
    {
        String path = file.getAbsolutePath();
        for (String prefix : filePathPrefixesForAsyncDeletion)
        {
            if (path.startsWith(prefix))
            {
                return true;
            }
        }
        return false;
    }
}
