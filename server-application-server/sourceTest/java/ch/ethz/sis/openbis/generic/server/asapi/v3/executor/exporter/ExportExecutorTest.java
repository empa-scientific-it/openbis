/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.PDF_DIRECTORY;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExportExecutorTest
{

    private static final String NEXT_ZIP_ENTRY_DATA_PROVIDER = "nextZipEntryData";

    private static final String ERRONEOUS_NEXT_ZIP_ENTRY_DATA_PROVIDER = "erroneousNextZipEntryData";

    private static final String FOLDER_NAME_DATA_PROVIDER = "folderNameData";

    private static final String ERRONEOUS_FOLDER_NAME_DATA_PROVIDER = "erroneousFolderNameData";

    private static final String SPACE_CODE = "TEST_SPACE";

    private static final String PROJECT_CODE = "TEST_PROJECT";

    private static final String EXPERIMENT_CODE = "TEST_EXPERIMENT";

    private static final String EXPERIMENT_NAME = "Test experiment name";

    private static final String SAMPLE_CODE = "TEST_SAMPLE";

    private static final String SAMPLE_NAME = "Test sample name";

    private static final String EXTENSION = ".pdf";

    private static final String DATA_SET_CODE = "TEST_DATA_SET";

    private static final Object[][] NEXT_ZIP_ENTRY_DATA = {
            {
                    null, null, null, null, null, null, null, null,
                    String.format("%s/", PDF_DIRECTORY)
            },
            {
                    SPACE_CODE, null, null, null, null, null, null, null,
                    String.format("%s/%s/", PDF_DIRECTORY, SPACE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, null, null, null, EXTENSION,
                    String.format("%s/%s%s", PDF_DIRECTORY, SPACE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, null, null, null,
                    String.format("%s/%s/%s/", PDF_DIRECTORY, SPACE_CODE, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, null, null, EXTENSION,
                    String.format("%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("%s/%s/%s (%s)/", PDF_DIRECTORY, SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("%s/%s/%s (%s)%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, null, DATA_SET_CODE, null,
                    String.format("%s/%s/%s/%s/", PDF_DIRECTORY, SPACE_CODE, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, null, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("%s/%s/%s (%s)/%s/", PDF_DIRECTORY, SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s (%s)/%s%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, null, null,
                    String.format("%s/%s/%s/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, null, EXTENSION,
                    String.format("%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("%s/%s/%s/%s (%s)/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("%s/%s/%s/%s (%s)%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, null, null, null,
                    String.format("%s/%s/%s/%s/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, null, null, EXTENSION,
                    String.format("%s/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("%s/%s/%s/%s (%s)/%s/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s/%s (%s)/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE,
                            EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, null, DATA_SET_CODE, null,
                    String.format("%s/%s/%s/%s/%s/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, null, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, null, null, null, null, null,
                    String.format("%s/%s/%s/%s/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, null, null, null, null, EXTENSION,
                    String.format("%s/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, null,
                    String.format("%s/%s/%s/%s (%s)/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, EXTENSION,
                    String.format("%s/%s/%s/%s (%s)%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, DATA_SET_CODE, null,
                    String.format("%s/%s/%s/%s (%s)/%s/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s/%s (%s)/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, DATA_SET_CODE,
                            EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("%s/%s/%s/%s (%s)/%s (%s)/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, SAMPLE_NAME,
                            SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("%s/%s/%s/%s (%s)/%s (%s)%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("%s/%s/%s/%s (%s)/%s (%s)/%s/", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s/%s (%s)/%s (%s)/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
    };

    private static final Object[][] ERRONEOUS_NEXT_ZIP_ENTRY_DATA = {
            {
                    null, PROJECT_CODE, null, null, null, null, null, null
            },
            {
                    null, null, EXPERIMENT_CODE, null, null, null, null, null
            },
            {
                    null, null, null, null, SAMPLE_CODE, null, null, null
            },
            {
                    null, null, null, null, null, null, DATA_SET_CODE, null
            },
            {
                    null, null, null, null, null, null, null, EXTENSION
            },
            {
                    null, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION
            },
            {
                    SPACE_CODE, null, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, EXTENSION
            },
            {
                    SPACE_CODE, null, null, null, null, null, DATA_SET_CODE, EXTENSION
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, DATA_SET_CODE, EXTENSION
            },
    };

    private static final Object[][] FOLDER_NAME_DATA = {
            {
                    'O', "SPACE", "PROJECT", null, "CODE_A", "NAME_A", "O+SPACE+PROJECT+CODE_A+NAME_A"
            },
            {
                    'O', "SPACE", null, null, "CODE_A", "NAME_A", "O+SPACE+CODE_A+NAME_A"
            },
            {
                    'O', null, null, null, "CODE_A", "NAME_A", "O+CODE_A+NAME_A"
            },
            {
                    'E', "SPACE", "PROJECT", null, "CODE_A", null, "E+SPACE+PROJECT+CODE_A"
            },
            {
                    'O', "SPACE", "PROJECT", "CODE_B", "CODE_A", "NAME_A", "O+SPACE+PROJECT+CODE_B*CODE_A+NAME_A"
            },
    };

    private static final Object[][] ERRONEOUS_FOLDER_NAME_DATA = {
            {
                    'P', "SPACE", "PROJECT", null, "CODE_A", "NAME_A"
            },
            {
                    'O', null, "PROJECT", null, "CODE_A", "NAME_A"
            },
            {
                    'O', "SPACE", "PROJECT", null, null, "NAME_A"
            },
            {
                    'O', "SPACE", "PROJECT", null, null, null
            },
            {
                    'O', "SPACE", "PROJECT", "CODE_B", null, "NAME_A"
            },
            {
                    'O', "SPACE", "PROJECT", "CODE_B", null, null
            },

            {
                    // Experiments cannot have containers
                    'E', "SPACE", "PROJECT", "CODE_B", "CODE_A", null
            },
            {
                    // Experiments cannot have containers
                    'E', "SPACE", "PROJECT", "CODE_B", "CODE_A", "NAME_A"
            },
            {
                    // Experiments cannot be on the space level
                    'E', "SPACE", null, null, "CODE_A", "NAME_A"
            },
            {
                    // Experiments cannot be standalone
                    'E', null, null, null, "CODE_A", "NAME_A"
            },
    };

    @DataProvider
    private Object[][] nextZipEntryData()
    {
        return NEXT_ZIP_ENTRY_DATA;
    }

    @DataProvider
    private Object[][] erroneousNextZipEntryData()
    {
        return ERRONEOUS_NEXT_ZIP_ENTRY_DATA;
    }

    @DataProvider
    private Object[][] folderNameData()
    {
        return FOLDER_NAME_DATA;
    }

    @DataProvider
    private Object[][] erroneousFolderNameData()
    {
        return ERRONEOUS_FOLDER_NAME_DATA;
    }

    @Test(dataProvider = NEXT_ZIP_ENTRY_DATA_PROVIDER)
    public void testGetNextZipEntry(final String spaceCode, final String projectCode, final String experimentCode, final String experimentName,
            final String sampleCode, final String sampleName, final String dataSetCode, final String extension, final String expectedResult)
    {
        assertEquals(ExportExecutor.getNextDocZipEntry(spaceCode, projectCode, experimentCode, experimentName, sampleCode, sampleName, dataSetCode,
                extension), expectedResult);
    }

    @Test(dataProvider = ERRONEOUS_NEXT_ZIP_ENTRY_DATA_PROVIDER, expectedExceptions = IllegalArgumentException.class)
    public void testGetNextZipEntryError(final String spaceCode, final String projectCode, final String experimentCode, final String experimentName,
            final String sampleCode, final String sampleName, final String dataSetCode, final String extension)
    {
        ExportExecutor.getNextDocZipEntry(spaceCode, projectCode, experimentCode, experimentName, sampleCode, sampleName, dataSetCode, extension);
    }

    @Test(dataProvider = FOLDER_NAME_DATA_PROVIDER)
    public void testGetFolderName(final char prefix, final String spaceCode, final String projectCode,
            final String containerCode, final String entityCode, final String entityName, final String expectedResult)
    {
        assertEquals(ExportExecutor.getFolderName(prefix, spaceCode, projectCode, containerCode, entityCode, entityName),
                expectedResult);
    }

    @Test(dataProvider = ERRONEOUS_FOLDER_NAME_DATA_PROVIDER, expectedExceptions = IllegalArgumentException.class)
    public void testGetFolderNameError(final char prefix, final String spaceCode, final String projectCode,
            final String containerCode, final String entityCode, final String entityName)
    {
        ExportExecutor.getFolderName(prefix, spaceCode, projectCode, containerCode, entityCode, entityName);
    }

}