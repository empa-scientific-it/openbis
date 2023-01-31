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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Test cases for corresponding {@link NamedDataStrategy} class.
 * 
 * @author Christian Ribeaud
 */
public final class NamedDataStrategyTest extends AbstractFileSystemTestCase
{
    private static final String SHARE_ID = "share-1";

    private static final DataStoreStrategyKey UNIDENTIFIED = DataStoreStrategyKey.UNIDENTIFIED;

    private final static String FILE_NAME = "AX14";

    private final static String TEST_FILENAME = "test";

    private final static NamedDataStrategy strategy = new NamedDataStrategy(UNIDENTIFIED);

    private final void createSomeFiles() throws IOException
    {
        createNumberedFiles(workingDirectory);
        assert workingDirectory.list().length == 3;
        assert new File(workingDirectory, FILE_NAME + "_[1]").exists();
    }

    private final static void createNumberedFiles(final File dir) throws IOException
    {
        for (int i = 0; i < 3; i++)
        {
            FileUtils.touch(new File(dir, FILE_NAME + "_[" + (i + 1) + "]"));
        }
    }

    @Test
    public void testCreateTargetPathNoFileExists() throws IOException
    {
        assertEquals(TEST_FILENAME, NamedDataStrategy.createTargetPath(
                new File(workingDirectory, TEST_FILENAME)).getName());
    }

    @Test
    public void testCreateTargetPathOriginalFileExists() throws IOException
    {
        FileUtils.touch(new File(workingDirectory, TEST_FILENAME));
        assertEquals(TEST_FILENAME + "_[1]", NamedDataStrategy.createTargetPath(
                new File(workingDirectory, TEST_FILENAME)).getName());
    }

    @Test
    public void testCreateTargetPathSomeFilesExist() throws IOException
    {
        FileUtils.touch(new File(workingDirectory, TEST_FILENAME));
        FileUtils.touch(new File(workingDirectory, TEST_FILENAME + "_[1]"));
        FileUtils.touch(new File(workingDirectory, TEST_FILENAME + "_[2]"));
        FileUtils.touch(new File(workingDirectory, TEST_FILENAME + "_[3]"));
        assertEquals(TEST_FILENAME + "_[4]", NamedDataStrategy.createTargetPath(
                new File(workingDirectory, TEST_FILENAME)).getName());
    }

    @Test
    public final void testGetBaseDirectory() throws IOException
    {
        createSomeFiles();
        boolean exceptionThrown = false;
        try
        {
            strategy.getBaseDirectory(null, null, null);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Base directory can not be null", exceptionThrown);
        final DataSetType dataSetType = new DataSetType("DataSet");
        DataSetInformation dataSetInfo = new DataSetInformation();
        dataSetInfo.setShareId(SHARE_ID);
        final File baseDirectory =
                strategy.getBaseDirectory(workingDirectory, dataSetInfo, dataSetType);
        assertEquals(new File(new File(new File(workingDirectory, SHARE_ID), NamedDataStrategy
                .getDirectoryName(UNIDENTIFIED)), IdentifiedDataStrategy
                .createDataSetTypeDirectory(dataSetType)), baseDirectory);
    }

    @Test(dependsOnMethods = "testGetBaseDirectory")
    public final void testGetTargetPath() throws IOException
    {
        createSomeFiles();
        boolean exceptionThrown = false;
        try
        {
            strategy.getTargetPath(null, null);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Base directory and incoming data set can not be null", exceptionThrown);
        File targetPath = strategy.getTargetPath(workingDirectory, new File(FILE_NAME));
        assertEquals(new File(workingDirectory, FILE_NAME), targetPath);
        FileUtils.touch(targetPath);
        targetPath = strategy.getTargetPath(workingDirectory, new File(FILE_NAME));
        assertEquals(new File(workingDirectory, FILE_NAME + "_[4]"), targetPath);
    }
}