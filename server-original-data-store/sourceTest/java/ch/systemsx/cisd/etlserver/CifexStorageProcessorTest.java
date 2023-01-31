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
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessorTest.TestProcedureAndDataTypeExtractor;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;

/**
 * Test cases for {@link CifexStorageProcessor}.
 * 
 * @author Izabela Adamczyk
 */
public class CifexStorageProcessorTest extends AbstractFileSystemTestCase
{
    // TODO 2009-07-09, IA: Add more test cases

    private final static ITypeExtractor TYPE_EXTRACTOR = new TestProcedureAndDataTypeExtractor();

    private static final String UNDEFINED = null;

    private final CifexStorageProcessor createStorageProcessorWithRegex(String keepFileRegex)
    {
        Properties properties = new Properties();
        properties.setProperty(
                AbstractDelegatingStorageProcessor.DELEGATE_PROCESSOR_CLASS_PROPERTY,
                DefaultStorageProcessor.class.getName());
        if (keepFileRegex != null)
        {
            properties.setProperty(CifexStorageProcessor.KEEP_FILE_REGEX_KEY, keepFileRegex);
        }
        final CifexStorageProcessor storageProcessor = new CifexStorageProcessor(properties);
        storageProcessor.setStoreRootDirectory(workingDirectory);
        return storageProcessor;
    }

    private File createDirectory(final String directoryName)
    {
        final File file = new File(workingDirectory, directoryName);
        file.mkdir();
        assertEquals(true, file.isDirectory());
        return file;
    }

    @Test
    public final void testStoreDataWithoutPatternSpecified()
    {
        final CifexStorageProcessor storageProcessor = createStorageProcessorWithRegex(UNDEFINED);
        final File incomingDataSetDirectory = createDirectory("incoming");
        FileUtilities.writeToFile(new File(incomingDataSetDirectory, "read.me"), "hello world");
        final File rootDir = createDirectory("root");
        final File storeData = storeData(storageProcessor, incomingDataSetDirectory, rootDir);
        assertEquals(false, incomingDataSetDirectory.exists());
        assertEquals(true, storeData.isDirectory());
        assertEquals(rootDir.getAbsolutePath(), storeData.getAbsolutePath());
        assertEquals(
                "hello world",
                FileUtilities.loadToString(
                        new File(storeData, DefaultStorageProcessor.ORIGINAL_DIR
                                + "/incoming/read.me")).trim());
    }

    @Test
    public final void testStoreDataIncomingDirIsFile()
    {
        final CifexStorageProcessor storageProcessor =
                createStorageProcessorWithRegex("dummy-pattern");
        File incoming = new File(workingDirectory, "read.me");
        FileUtilities.writeToFile(incoming, "hello world");
        final File rootDir = createDirectory("root");
        final File storeData = storeData(storageProcessor, incoming, rootDir);
        assertEquals(false, incoming.exists());
        assertEquals(true, storeData.isDirectory());
        File fileName = new File(rootDir, DefaultStorageProcessor.ORIGINAL_DIR + "/read.me");
        assertEquals(rootDir.getAbsolutePath(), storeData.getAbsolutePath());
        assertEquals("hello world", FileUtilities.loadToString(fileName).trim());
    }

    private File storeData(final CifexStorageProcessor storageProcessor, File incoming,
            final File rootDir)
    {
        StorageProcessorTransactionParameters parameters =
                new StorageProcessorTransactionParameters(null, incoming, rootDir);
        IStorageProcessorTransaction transaction = storageProcessor.createTransaction(parameters);
        transaction.storeData(TYPE_EXTRACTOR, null, incoming);
        return transaction.getStoredDataDirectory();
    }

}
