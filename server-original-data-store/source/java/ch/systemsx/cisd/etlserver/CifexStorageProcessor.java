/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * Removes the artificial structure of data sets.
 * <p>
 * The incoming data set structure is scanned for files which match specified ( {@link #KEEP_FILE_REGEX_KEY}) regex. If only one such file has been
 * found, it is kept and all the other files including the directory are removed. If more files match the specified regular expression, they are all
 * kept together with the directory structure and files not matching the regex are deleted.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public class CifexStorageProcessor extends AbstractDelegatingStorageProcessor
{

    private final static Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            CifexStorageProcessor.class);

    public static final String KEEP_FILE_REGEX_KEY = "keep-file-regex";

    public static final String MOVE_TO_ERROR_FOLDER_KEY = "move-to-error-folder";

    private final String keepFileRegex;

    private final boolean moveToErrorFolder;

    public CifexStorageProcessor(Properties properties)
    {
        super(properties);
        keepFileRegex = PropertyUtils.getProperty(properties, KEEP_FILE_REGEX_KEY);
        moveToErrorFolder = PropertyUtils.getBoolean(properties, MOVE_TO_ERROR_FOLDER_KEY, false);
    }

    @Override
    public IStorageProcessorTransaction createTransaction(
            StorageProcessorTransactionParameters parameters)
    {

        IStorageProcessorTransaction superTransaction = super.createTransaction(parameters);
        return new CifexStorageProcessorTransaction(parameters, superTransaction, keepFileRegex,
                moveToErrorFolder);
    }

    private static final class CifexStorageProcessorTransaction extends
            AbstractDelegatingStorageProcessorTransaction
    {
        private static final long serialVersionUID = 1L;

        private final String keepFileRegex;

        private final boolean moveToErrorFolder;

        private transient File dirToRestore;

        private transient File fileToMove;

        private transient boolean dirDeleted = false;

        private CifexStorageProcessorTransaction(StorageProcessorTransactionParameters parameters,
                IStorageProcessorTransaction transaction, String keepFileRegex,
                boolean moveErrorToFolder)
        {
            super(parameters, transaction);
            this.keepFileRegex = keepFileRegex;
            this.moveToErrorFolder = moveErrorToFolder;
        }

        @Override
        protected File executeStoreData(ITypeExtractor typeExtractor, IMailClient mailClient)
        {
            File newIncomingDataSetDirectory = incomingDataSetDirectory;
            if (StringUtils.isBlank(keepFileRegex) == false)
            {
                newIncomingDataSetDirectory = clean(incomingDataSetDirectory, keepFileRegex);
            }
            nestedTransaction.storeData(typeExtractor, mailClient, newIncomingDataSetDirectory);

            return nestedTransaction.getStoredDataDirectory();
        }

        @Override
        protected void executeCommit()
        {
            nestedTransaction.commit();
        }

        @Override
        protected UnstoreDataAction executeRollback(Throwable ex)
        {
            nestedTransaction.rollback(ex);
            if (dirDeleted)
            {
                FileOperations.getMonitoredInstanceForCurrentThread().mkdir(dirToRestore);
                FileOperations.getMonitoredInstanceForCurrentThread().moveToDirectory(fileToMove,
                        dirToRestore);
            }
            return moveToErrorFolder ? UnstoreDataAction.MOVE_TO_ERROR : UnstoreDataAction.DELETE;
        }

        @Private
        public File clean(File dir, String pattern)
        {
            assert dir != null;
            if (StringUtils.isBlank(pattern) || dir.isDirectory() == false)
            {
                return dir;
            }
            FileFilter filter = new RegexFileFilter(pattern);
            List<File> matchingFiles = Arrays.asList(dir.listFiles(filter));
            File newDataDir = dir;
            try
            {
                removeUnmatchedFiles(dir, matchingFiles);
                if (matchingFiles.size() == 1)
                {
                    File matchingFile = matchingFiles.get(0);
                    String root = dir.getParent();
                    File destinationFile = new File(root, matchingFile.getName());
                    File tmpFile = FileUtilities.createNextNumberedFile(destinationFile, null);
                    boolean movedToTmp =
                            FileOperations.getMonitoredInstanceForCurrentThread().rename(
                                    matchingFile, tmpFile);
                    if (movedToTmp)
                    {
                        newDataDir = tmpFile;
                        fileToMove = tmpFile;
                    }
                    dirDeleted =
                            movedToTmp
                                    && FileOperations.getMonitoredInstanceForCurrentThread()
                                            .delete(dir);
                    if (dirDeleted)
                    {
                        dirToRestore = dir;
                    }
                    boolean movedToDestPlace =
                            movedToTmp
                                    && FileOperations.getMonitoredInstanceForCurrentThread()
                                            .rename(tmpFile, destinationFile);
                    if (movedToDestPlace)
                    {
                        newDataDir = destinationFile;
                        fileToMove = destinationFile;
                    }
                    if ((movedToDestPlace && dirDeleted) == false)
                    {
                        createCleaningErrorMessage(dir, pattern);
                    }
                }
            } catch (IOExceptionUnchecked ex)
            {
                createCleaningErrorMessage(dir, pattern);
            }
            return newDataDir;
        }

        private void createCleaningErrorMessage(File dir, String pattern)
        {
            notificationLog.error(String.format("Cleaning '%s' by file name pattern '%s' failed.",
                    dir.getPath(), pattern));
        }

        private void removeUnmatchedFiles(File dir, List<File> matchingFiles)
        {
            for (File f : dir.listFiles())
            {
                if (matchingFiles.contains(f) == false)
                {
                    FileOperations.getMonitoredInstanceForCurrentThread().deleteRecursively(f);
                }
            }
        }

    }
}
