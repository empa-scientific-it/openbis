/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;
import ch.systemsx.cisd.openbis.common.hdf5.FileToHDF5DuplicationVerifier;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container.IHDF5ReaderClient;
import ch.systemsx.cisd.openbis.common.hdf5.IHDF5ContainerReader;

/**
 * Tests for {@link HDF5StorageProcessor}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractHDF5StorageProcessorTest extends AbstractFileSystemTestCase
{
    protected final HDF5StorageProcessor storageProcessor;

    protected IStorageProcessorTransaction transaction;

    protected AbstractHDF5StorageProcessorTest(Properties properties)
    {
        super();
        storageProcessor = new HDF5StorageProcessor(properties);
        storageProcessor.setStoreRootDirectory(workingDirectory);
    }

    private File createDirectory(final String directoryName)
    {
        final File file = new File(workingDirectory, directoryName);
        file.mkdir();
        return file;
    }

    protected void testStoreData()
    {
        final File incomingDataSetDirectory = createDirectory("incoming");
        FileUtilities.writeToFile(new File(incomingDataSetDirectory, "read.me"), "hello world");
        File rootDir = createDirectory("root");
        transaction = createTransaction(incomingDataSetDirectory, rootDir);
        transaction.storeData(null, null, incomingDataSetDirectory);
        assertTrue(incomingDataSetDirectory.exists());
        assertTrue(transaction.getStoredDataDirectory().isDirectory());

        File hdf5ContainerFile = HDF5StorageProcessor.getHDF5ContainerFile(rootDir);
        assertTrue(hdf5ContainerFile.exists());
        assertTrue(hdf5ContainerFile.isFile());

        final HDF5Container container = HDF5StorageProcessor.getHdf5Container(rootDir);

        container.runReaderClient(new IHDF5ReaderClient()
            {

                @Override
                public void runWithSimpleReader(IHDF5ContainerReader reader)
                {
                    FileToHDF5DuplicationVerifier verifier =
                            new FileToHDF5DuplicationVerifier(incomingDataSetDirectory, container,
                                    reader);
                    verifier.verifyDuplicate();
                }
            });

        transaction.commit();

        assertFalse(incomingDataSetDirectory.exists());
    }

    protected void testUnstoreData()
    {
        File rootDir = createDirectory("root");
        File incomingDataSetDirectory = createDirectory("incoming");
        File readMeFile = new File(incomingDataSetDirectory, "read.me");
        FileUtilities.writeToFile(readMeFile, "hi");
        transaction = createTransaction(incomingDataSetDirectory, rootDir);
        transaction.storeData(null, null, incomingDataSetDirectory);
        assertTrue(incomingDataSetDirectory.exists());
        assertTrue(transaction.getStoredDataDirectory().isDirectory());

        File hdf5ContainerFile = HDF5StorageProcessor.getHDF5ContainerFile(rootDir);
        assertTrue(hdf5ContainerFile.exists());
        assertTrue(hdf5ContainerFile.isFile());

        transaction.rollback(null);
        assertEquals(true, incomingDataSetDirectory.exists());
        assertEquals("hi", FileUtilities.loadToString(readMeFile).trim());
    }

    protected void testGetStoreRootDirectory()
    {
        File storeRootDirectory = storageProcessor.getStoreRootDirectory();
        assertEquals(workingDirectory.getAbsolutePath(), storeRootDirectory.getAbsolutePath());
    }

    protected void testStoreDataNullValues()
    {
        createTransaction(null, null).storeData(null, null, null);
    }

    private IStorageProcessorTransaction createTransaction(File incoming, File rootDir)
    {
        StorageProcessorTransactionParameters parameters = new StorageProcessorTransactionParameters(null, incoming, rootDir);
        return storageProcessor.createTransaction(parameters);

    }

}
