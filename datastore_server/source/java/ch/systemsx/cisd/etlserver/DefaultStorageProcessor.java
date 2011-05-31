/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.utils.Unzipper;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A default {@link IStorageProcessorTransactional} implementation. The data set is stored in
 * subfolder {@link #ORIGINAL_DIR}.
 * 
 * @author Christian Ribeaud
 */
public class DefaultStorageProcessor extends AbstractStorageProcessor
{
    public static final String ORIGINAL_DIR = "original";

    static final String NO_RENAME = "Couldn't rename '%s' to '%s'.";

    static final String UNZIP_CRITERIA_KEY = "unzip";

    static final String DELETE_UNZIPPED_KEY = "delete_unzipped";

    private final boolean unzip;

    private final boolean deleteUnzipped;

    public DefaultStorageProcessor(final Properties properties)
    {
        super(properties);

        unzip = PropertyUtils.getBoolean(properties, UNZIP_CRITERIA_KEY, false);
        deleteUnzipped = PropertyUtils.getBoolean(properties, DELETE_UNZIPPED_KEY, true);
    }

    //
    // AbstractStorageProcessor
    //

    public IStorageProcessorTransaction createTransaction()
    {
        return new DefaultStorageProcessorTransaction();
    }

    private class DefaultStorageProcessorTransaction extends AbstractStorageProcessorTransaction
    {

        @Override
        protected File storeData(DataSetInformation dataSetInformation,
                ITypeExtractor typeExtractor, IMailClient mailClient)
        {
            checkParameters(incomingDataSetDirectory, rootDirectory);
            File originalDir = getOriginalDirectory(rootDirectory);
            if (originalDir.mkdir() == false)
            {
                throw new EnvironmentFailureException("Couldn't create "
                        + originalDir.getAbsolutePath());
            }
            final File targetFile = new File(originalDir, incomingDataSetDirectory.getName());
            if (FileRenamer.renameAndLog(incomingDataSetDirectory, targetFile) == false)
            {
                throw new EnvironmentFailureException(String.format(NO_RENAME,
                        incomingDataSetDirectory, targetFile));
            }
            // Set the stored data directory in case unzip throws an exception.
            this.storedDataDirectory = rootDirectory;

            unzipIfMatching(targetFile, originalDir);
            return rootDirectory;
        }

        @Override
        protected void executeCommit()
        {
            // nothing to do
        }

        @Override
        protected UnstoreDataAction executeRollback(Throwable ex)
        {
            checkParameters(incomingDataSetDirectory, storedDataDirectory);
            File targetFile =
                    new File(getOriginalDirectory(storedDataDirectory),
                            incomingDataSetDirectory.getName());
            // Note that this will move back <code>targetFilePath</code> to its original place but
            // the
            // directory structure will persist. Right now, we consider this is fine as these empty
            // directories will not disturb the running application.
            FileRenamer.renameAndLog(targetFile, incomingDataSetDirectory);
            return getDefaultUnstoreDataAction(ex);
        }

        /**
         * returns the only file or directory which is expected to be found inside original
         * directory
         */
        public final File tryGetProprietaryData()
        {
            File originalDir = getOriginalDirectory(storedDataDirectory);
            List<File> files = FileUtilities.listFilesAndDirectories(originalDir, false, null);
            if (files.size() != 1)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Exactly one file expected in '%s' directory, but %d found.",
                        originalDir.getPath(), files.size());
            }
            return files.get(0);
        }
    }

    public static File getOriginalDirectory(final File storedDataDirectory)
    {
        return new File(storedDataDirectory, ORIGINAL_DIR);
    }

    /**
     * Unzips given archive file to selected output directory.
     */
    protected final Status unzipIfMatching(File archiveFile, File outputDirectory)
    {
        if (unzip && isZipFile(archiveFile))
        {
            return Unzipper.unzip(archiveFile, outputDirectory, deleteUnzipped);
        }
        return Status.OK;
    }
}
