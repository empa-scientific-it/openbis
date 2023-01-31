/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;

/**
 * @author Franz-Josef Elmer
 */
public class IdentifierAttributeMappingManagerTest extends AbstractFileSystemTestCase
{
    private File as1;

    private File as2;

    private File as3;

    private DatasetDescription dataSetDescription;

    private DatasetDescription bigDataSetDescription;

    private DatasetDescription smallDataSetDescription;

    private PhysicalDataSet dataSet;

    private PhysicalDataSet bigDataSet;

    private PhysicalDataSet smallDataSet;

    @BeforeMethod
    public void prepareTestFiles()
    {
        as1 = new File(workingDirectory, "a-s1");
        as2 = new File(workingDirectory, "a-s2");
        as3 = new File(workingDirectory, "a-s3");

        dataSet = createDataSet("/S1/P1/E1", 100L);
        bigDataSet = createDataSet("/S1/P1/E1", 1001L);
        smallDataSet = createDataSet("/S1/P1/E1", 10L);

        dataSetDescription = DataSetTranslator.translateToDescription(dataSet);
        bigDataSetDescription = DataSetTranslator.translateToDescription(bigDataSet);
        smallDataSetDescription = DataSetTranslator.translateToDescription(smallDataSet);
    }

    private PhysicalDataSet createDataSet(String experimentIdentifier, long dataSetSize)
    {
        return new DataSetBuilder()
                .code("DS1").store(new DataStoreBuilder("DSS").getStore()).type("MY-TYPE").fileFormat("ABC").size(dataSetSize)
                .experiment(new ExperimentBuilder().identifier(experimentIdentifier).getExperiment())
                .getDataSet();
    }

    @Test
    public void testGetArchiveFolder()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1\t2\t" + as1 + "\n"
                + "/S1/P1\t2\t" + as1 + "\n"
                + "/S1\t3\t\n"
                + "/S2\t4\t" + as2);
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), true, null);

        List<File> folders = new ArrayList<File>(mappingManager.getAllFolders());

        Collections.sort(folders);
        assertEquals("[" + as1 + ", " + as2 + "]", folders.toString());
    }

    @Test
    public void testGetArchiveFolderForBigDataSetWhenTwoFoldersConfigured()
    {
        testGetArchiveFolderWhenTwoFoldersConfigured(bigDataSetDescription, as1);
    }

    @Test
    public void testGetArchiveFolderForSmallDataSetWhenTwoFoldersConfigured()
    {
        testGetArchiveFolderWhenTwoFoldersConfigured(smallDataSetDescription, as2);
    }

    private void testGetArchiveFolderWhenTwoFoldersConfigured(DatasetDescription description, File expectedArchiveFolder)
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S2\t4\t" + as3 + "\n"
                + "/S1/P1\t2\t" + as1 + ", " + as2);
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), true, 1000L);

        List<File> folders = new ArrayList<File>(mappingManager.getAllFolders());

        Collections.sort(folders);
        assertEquals("[" + as1 + ", " + as2 + ", " + as3 + "]", folders.toString());

        File archiveFolder = mappingManager.getArchiveFolder(description, null);

        assertEquals(expectedArchiveFolder.getPath(), archiveFolder.getPath());
    }

    @Test
    public void testGetArchiveFolderForBigDataSetWhenOneFolderConfigured()
    {
        testGetArchiveFolderWhenOneFolderConfigured(bigDataSetDescription, as1);
    }

    @Test
    public void testGetArchiveFolderForSmallDataSetWhenOneFolderConfigured()
    {
        testGetArchiveFolderWhenOneFolderConfigured(smallDataSetDescription, as1);
    }

    private void testGetArchiveFolderWhenOneFolderConfigured(DatasetDescription description, File expectedArchiveFolder)
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S2\t4\t" + as3 + "\n"
                + "/S1/P1\t2\t" + as1);
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), true, 1000L);

        List<File> folders = new ArrayList<File>(mappingManager.getAllFolders());

        Collections.sort(folders);
        assertEquals("[" + as1 + ", " + as3 + "]", folders.toString());

        File archiveFolder = mappingManager.getArchiveFolder(description, null);

        assertEquals(expectedArchiveFolder.getPath(), archiveFolder.getPath());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Small data set size limit cannot be null")
    public void testCreateWhenTwoFoldersConfiguredButNoSmallDataSetSizeLimitDefined()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S2\t4\t" + as3 + "\n"
                + "/S1/P1\t2\t" + as1 + ", " + as2);
        new IdentifierAttributeMappingManager(mappingFile.getPath(), true, null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Found 3 archive folders.*")
    public void testCreateWhenMoreThanTwoFoldersConfigured()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S2\t4\t" + as3 + "\n"
                + "/S1/P1\t2\t" + as1 + "," + as2 + ", " + as3);
        new IdentifierAttributeMappingManager(mappingFile.getPath(), true, null);
    }

    @Test
    public void testCreateWithInvalidIdentifierRegex()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/s1/p[\t2\t" + as1 + "," + as2);
        try
        {
            new IdentifierAttributeMappingManager(mappingFile.getPath(), true, null);
        } catch (PatternSyntaxException ex)
        {
            AssertionUtil.assertStarts("Unclosed character class", ex.getMessage());
        }
    }

    @Test
    public void testGetArchiveFolderFromExperimentMapping()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1/E1\t2\t" + as1 + "\n"
                + "/S1/P1/E2\t2\t" + as2 + "\n"
                + "/S1/P1\t2\t" + as2 + "\n"
                + "\n"
                + " \n"
                + "\t \n"
                + "\t\t\n"
                + "\t\t\t \n"
                + "/S1\t2\t" + as2 + "\n");
        as1.mkdirs();
        as2.mkdirs();
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);

        File archiveFolder = mappingManager.getArchiveFolder(dataSetDescription, as2);

        assertEquals(as1.getPath(), archiveFolder.getPath());
    }

    @Test
    public void testGetArchiveFolderFromProjectMapping()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1\t2\t" + as1 + "\n"
                + "/S1/P2\t2\t" + as2 + "\n"
                + "/S1\t2\t" + as2 + "\n");
        as1.mkdirs();
        as2.mkdirs();
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
        File archiveFolder = mappingManager.getArchiveFolder(dataSetDescription, as2);

        assertEquals(as1.getPath(), archiveFolder.getPath());
    }

    @Test
    public void testGetArchiveFolderFromSpaceMapping()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1\t2\t" + as1);
        as1.mkdirs();
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);

        File archiveFolder = mappingManager.getArchiveFolder(dataSetDescription, as2);

        assertEquals(as1.getPath(), archiveFolder.getPath());
    }

    @Test
    public void testGetArchiveFolderFromSpaceMappingArchiveFolderDoesNotExist()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1\t2\t" + as1);

        try
        {
            new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Archive folder '" + as1 + "' doesn't exists or is a file.", ex.getMessage());
        }
    }

    @Test
    public void testGetArchiveFolderFromSpaceMappingCreateArchiveFolder()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1\t2\t" + as1 + "\n"
                + "/S2\t4\t" + as2);
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), true, null);

        File archiveFolder = mappingManager.getArchiveFolder(dataSetDescription, as2);

        assertEquals(as1.getPath(), archiveFolder.getPath());
    }

    @Test
    public void testGetArchiveFolderFromSpaceMappingMissingFolder()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1\t2\t\n"
                + "/S2\t4\t" + as2);
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), true, null);

        File archiveFolder = mappingManager.getArchiveFolder(dataSetDescription, as2);

        assertEquals(as2.getPath(), archiveFolder.getPath());
    }

    @Test
    public void testGetArchiveFolderFromSpaceMappingMissingEntry()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S2\t4\t" + as2);
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), true, null);

        File archiveFolder = mappingManager.getArchiveFolder(dataSetDescription, as2);

        assertEquals(as2.getPath(), archiveFolder.getPath());
    }

    @Test
    public void testGetArchiveFolderFromNonexistingMappingFile()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");

        try
        {
            new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Mapping file '" + mappingFile + "' does not exist.", ex.getMessage());
        }
    }

    @Test
    public void testGetArchiveFolderFromUndefinedMappingFile()
    {
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(null, true, null);

        File archiveFolder = mappingManager.getArchiveFolder(dataSetDescription, as2);

        assertEquals(as2.getPath(), archiveFolder.getPath());
    }

    @Test
    public void testGetShareIdsOnExperimentLevel()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1/E1\t2, 1,3\t\n"
                + "/S1/P1\t1,3\t\n"
                + "/S1\t2,3\t\n"
                + "/S2\t4,5\t");
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
        SimpleDataSetInformationDTO ds = SimpleDataSetHelper.filterAndTranslate(Arrays.<AbstractExternalData> asList(dataSet)).get(0);

        List<String> shareIds = mappingManager.getShareIds(ds);

        assertEquals("[2, 1, 3]", shareIds.toString());
    }

    @Test
    public void testGetShareIdsOnExperimentLevelWithRegex()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1/E.*\t2, 1,3\t\n"
                + "/S1/P1\t1,3\t\n"
                + "/S1\t2,3\t\n"
                + "/S2\t4,5\t");
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
        SimpleDataSetInformationDTO ds = SimpleDataSetHelper.filterAndTranslate(Arrays.<AbstractExternalData> asList(dataSet)).get(0);

        List<String> shareIds = mappingManager.getShareIds(ds);

        assertEquals("[2, 1, 3]", shareIds.toString());
    }

    @Test
    public void testGetShareIdsOnProjectLevel()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1/E2\t2, 1,3\t\n"
                + "/S1/P1\t1,3\t\n"
                + "/S1\t2,3\t\n"
                + "/S2\t4,5\t");
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
        SimpleDataSetInformationDTO ds = SimpleDataSetHelper.filterAndTranslate(Arrays.<AbstractExternalData> asList(dataSet)).get(0);

        List<String> shareIds = mappingManager.getShareIds(ds);

        assertEquals("[1, 3]", shareIds.toString());
    }

    @Test
    public void testGetShareIdsOnProjectLevelWithRegex()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1/E2\t2, 1,3\t\n"
                + "/S1/P.*\t1,3\t\n"
                + "/S1\t2,3\t\n"
                + "/S2\t4,5\t");
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
        SimpleDataSetInformationDTO ds = SimpleDataSetHelper.filterAndTranslate(Arrays.<AbstractExternalData> asList(dataSet)).get(0);

        List<String> shareIds = mappingManager.getShareIds(ds);

        assertEquals("[1, 3]", shareIds.toString());
    }

    @Test
    public void testGetShareIdsOnSpaceLevel()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1/E2\t2, 1,3\t\n"
                + "/S1/P2\t1,3\t\n"
                + "/S1\t2,3\t\n"
                + "/S2\t4,5\t");
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
        SimpleDataSetInformationDTO ds = SimpleDataSetHelper.filterAndTranslate(Arrays.<AbstractExternalData> asList(dataSet)).get(0);

        List<String> shareIds = mappingManager.getShareIds(ds);

        assertEquals("[2, 3]", shareIds.toString());
    }

    @Test
    public void testGetShareIdsOnSpaceLevelWithRegex()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1/E2\t2, 1,3\t\n"
                + "/S1/P2\t1,3\t\n"
                + "/S.*\t2,3\t\n"
                + "/S2\t4,5\t");
        IdentifierAttributeMappingManager mappingManager = new IdentifierAttributeMappingManager(mappingFile.getPath(), false, null);
        SimpleDataSetInformationDTO ds = SimpleDataSetHelper.filterAndTranslate(Arrays.<AbstractExternalData> asList(dataSet)).get(0);

        List<String> shareIds = mappingManager.getShareIds(ds);

        assertEquals("[2, 3]", shareIds.toString());
    }

}
