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

package ch.systemsx.cisd.etlserver.hdf5;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessor;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.HierarchicalStructureDuplicatorFileToHDF5;

/**
 * Storage processor that keeps data in an HDF5 container on the file system.
 * <p>
 * In the data store, the data set is stored in a file named <code>container.h5ar</code>.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class HDF5StorageProcessor extends AbstractStorageProcessor
{
    private static final String HDF5_CONTAINER_FILE_NAME = "container.h5ar";

    public final static String COMPRESS_DATA_PROPERTY = "compress-data";

    private final boolean isDataCompressed;

    /**
     * Constructor.
     * 
     * @param properties
     */
    public HDF5StorageProcessor(Properties properties)
    {
        super(properties);
        isDataCompressed = PropertyUtils.getBoolean(properties, COMPRESS_DATA_PROPERTY, false);
    }

    @Override
    public IStorageProcessorTransaction createTransaction(
            StorageProcessorTransactionParameters parameters)
    {
        return new HDF5StorageProcessorTransaction(parameters, isDataCompressed,
                getDefaultUnstoreDataAction(null));

    }

    /**
     * Given a directory in the store, return the HDF5 container file.
     * 
     * @return A file with HDF5 content
     */
    public static File getHDF5ContainerFile(final File storedDataDirectory)
    {
        return new File(storedDataDirectory, HDF5_CONTAINER_FILE_NAME);
    }

    /**
     * Given a directory in the store, return an {@link HDF5Container} object wrapping the file.
     * 
     * @return An HDF5Container object.
     */
    public static HDF5Container getHdf5Container(final File storedDataDirectory)
    {
        return new HDF5Container(getHDF5ContainerFile(storedDataDirectory));
    }

    public static class HDF5StorageProcessorTransaction extends AbstractStorageProcessorTransaction
    {
        private static final long serialVersionUID = 1L;

        // The file that we are currently processing -- need to store this to implement
        // commit
        private transient File fileBeingProcessed;

        private final boolean isDataCompressed;

        private final UnstoreDataAction unstoreDataAction;

        public HDF5StorageProcessorTransaction(StorageProcessorTransactionParameters parameters,
                boolean isDataCompressed, UnstoreDataAction unstoreAction)
        {
            super(parameters);
            checkParameters(parameters.getIncomingDataSetDirectory(), parameters.getRootDir());

            this.storedDataDirectory = parameters.getRootDir();
            this.isDataCompressed = isDataCompressed;
            this.unstoreDataAction = unstoreAction;
        }

        /**
         * Write the content of the incomingDataSetDirectory into an HDF5 container with the same hierarchical structure as the
         * incomingDataSetDirectory. Don't delete the incomingDataSetDirectory yet (this happens in @link{#commit}).
         */
        @Override
        public File executeStoreData(ITypeExtractor typeExtractor, IMailClient mailClient)
        {

            HDF5Container container = getHdf5Container(storedDataDirectory);
            container.runWriterClient(isDataCompressed,
                    new HierarchicalStructureDuplicatorFileToHDF5.DuplicatorWriterClient(
                            incomingDataSetDirectory));

            fileBeingProcessed = incomingDataSetDirectory;
            return storedDataDirectory;
        }

        @Override
        public UnstoreDataAction executeRollback(Throwable exception)
        {
            // Just delete the file in the store -- no need to touch the incomingDataSet
            // because we haven't done anything to it.
            File storedFile = getHDF5ContainerFile(storedDataDirectory);
            storedFile.delete();

            fileBeingProcessed = null;

            return unstoreDataAction;
        }

        @Override
        public void executeCommit()
        {
            // fileBeingProcessed cannot be null at this point
            FileUtilities.deleteRecursively(fileBeingProcessed);
            fileBeingProcessed = null;
        }

        @Override
        public File tryGetProprietaryData()
        {
            return null;
        }
    }

}
