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
                    null, null, null, null, null, null, null, null, null, "/"
            },
            {
                    SPACE_CODE, null, null, null, null, null, null, null, null,
                    String.format("%s/", SPACE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, null, null, null, null, EXTENSION,
                    String.format("%s%s", SPACE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, null, SAMPLE_CODE, null, null, null,
                    String.format("%s/%s/", SPACE_CODE, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, null, SAMPLE_CODE, null, null, EXTENSION,
                    String.format("%s/%s%s", SPACE_CODE, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("%s/%s (%s)/", SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("%s/%s (%s)%s", SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, null, SAMPLE_CODE, null, DATA_SET_CODE, null,
                    String.format("%s/%s/%s/", SPACE_CODE, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, null, null, null, null, SAMPLE_CODE, null, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s%s", SPACE_CODE, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("%s/%s (%s)/%s/", SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, null, null, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s (%s)/%s%s", SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, null, null, null,
                    String.format("%s/%s/", SPACE_CODE, PROJECT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, null, null, EXTENSION,
                    String.format("%s/%s%s", SPACE_CODE, PROJECT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("%s/%s/%s (%s)/", SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("%s/%s/%s (%s)%s", SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, SAMPLE_CODE, null, null, null,
                    String.format("%s/%s/%s/", SPACE_CODE, PROJECT_CODE, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, SAMPLE_CODE, null, null, EXTENSION,
                    String.format("%s/%s/%s%s", SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("%s/%s/%s (%s)/%s/", SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s (%s)/%s%s", SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE,
                            EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, SAMPLE_CODE, null, DATA_SET_CODE, null,
                    String.format("%s/%s/%s/%s/", SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, SAMPLE_CODE, null, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s/%s%s", SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, null, null, null, null, null, null,
                    String.format("%s/%s/%s/", SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, null, null, null, null, null, EXTENSION,
                    String.format("%s/%s/%s%s", SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, null, null,
                    String.format("%s/%s/%s (%s)/", SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, null, EXTENSION,
                    String.format("%s/%s/%s (%s)%s", SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, DATA_SET_CODE, null,
                    String.format("%s/%s/%s (%s)/%s/", SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s (%s)/%s%s", SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, DATA_SET_CODE,
                            EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("%s/%s/%s (%s)/%s (%s)/", SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, SAMPLE_NAME,
                            SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("%s/%s/%s (%s)/%s (%s)%s", SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("%s/%s/%s (%s)/%s (%s)/%s/", SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("%s/%s/%s (%s)/%s (%s)/%s%s", SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    null, null, null, null, null, SAMPLE_CODE, null, null, null,
                    String.format("%s/", SAMPLE_CODE)
            },
            {
                    null, null, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("%s (%s)/", SAMPLE_NAME, SAMPLE_CODE)
            },
            {
                    null, null, null, null, null, SAMPLE_CODE, null, null, EXTENSION,
                    String.format("%s%s", SAMPLE_CODE, EXTENSION)
            },
            {
                    null, null, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("%s (%s)%s", SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
    };

    private static final Object[][] ERRONEOUS_NEXT_ZIP_ENTRY_DATA = {
            {
                    null, PROJECT_CODE, null, null, null, null, null, null, null
            },
            {
                    null, null, EXPERIMENT_CODE, null, null, null, null, null, null
            },
            {
                    null, null, null, null, null, null, null, DATA_SET_CODE, null
            },
            {
                    null, null, null, null, null, null, null, null, EXTENSION
            },
            {
                    null, PROJECT_CODE, EXPERIMENT_CODE, null, null, SAMPLE_CODE, null, DATA_SET_CODE, null
            },
            {
                    null, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION
            },
            {
                    SPACE_CODE, null, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, null, EXTENSION
            },
            {
                    SPACE_CODE, null, null, null, null, null, null, DATA_SET_CODE, EXTENSION
            },
            {
                    SPACE_CODE, null, null, null, null, null, null, DATA_SET_CODE, null
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, null, DATA_SET_CODE, EXTENSION
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, null, DATA_SET_CODE, null
            },
    };

    private static final Object[][] FOLDER_NAME_DATA = {
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", null,
                    "O+DEFAULT_LAB_NOTEBOOK+DEFAULT_PROJECT+OBJ1+ANALYZED_DATA+my dataset (20231201115004780-27)"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file",
                    "O+DEFAULT_LAB_NOTEBOOK+DEFAULT_PROJECT+OBJ1+ANALYZED_DATA+my dataset (20231201115004780-27)/file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", null, "file",
                    "O+DEFAULT_LAB_NOTEBOOK+DEFAULT_PROJECT+OBJ1+ANALYZED_DATA+20231201115004780-27/file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", null, null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file",
                    "O+DEFAULT_LAB_NOTEBOOK+OBJ1+ANALYZED_DATA+my dataset (20231201115004780-27)/file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", null, null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", null, "file",
                    "O+DEFAULT_LAB_NOTEBOOK+OBJ1+ANALYZED_DATA+20231201115004780-27/file"
            },
            {
                    'O', null, null, null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file",
                    "O+OBJ1+ANALYZED_DATA+my dataset (20231201115004780-27)/file"
            },
            {
                    'O', null, null, null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", null, "file",
                    "O+OBJ1+ANALYZED_DATA+20231201115004780-27/file"
            },
            {
                    'E', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "EXP1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file",
                    "E+DEFAULT_LAB_NOTEBOOK+DEFAULT_PROJECT+EXP1+ANALYZED_DATA+my dataset (20231201115004780-27)/file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", "OBJ_CONTAINER", "OBJ1", "ANALYZED_DATA", "20231201115004780-27", "my dataset",
                    "file", "O+DEFAULT_LAB_NOTEBOOK+DEFAULT_PROJECT+OBJ_CONTAINER*OBJ1+ANALYZED_DATA+my dataset (20231201115004780-27)/file"
            },
    };

    private static final Object[][] ERRONEOUS_FOLDER_NAME_DATA = {
            {
                    'P', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file"
            },
            {
                    'O', null, "DEFAULT_PROJECT", null, "OBJ1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, null, "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", "OBJ_CONTAINER", null, "ANALYZED_DATA", "20231201115004780-27", "my dataset",
                    "file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "OBJ1", null, "20231201115004780-27", "my dataset", "file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "OBJ1", "ANALYZED_DATA", null, "my dataset", "file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "OBJ1", "ANALYZED_DATA", null, null, "file"
            },
            {
                    'O', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", null, "OBJ1", "ANALYZED_DATA", null, null, null
            },

            {
                    // Experiments cannot have containers
                    'E', "DEFAULT_LAB_NOTEBOOK", "DEFAULT_PROJECT", "EXP_CONTAINER", "EXP1", "ANALYZED_DATA", "20231201115004780-27", "my dataset",
                    "file"
            },
            {
                    // Experiments cannot be on the space level
                    'E', "DEFAULT_LAB_NOTEBOOK", null, null, "EXP1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file"
            },
            {
                    // Experiments cannot be shared (w/o space)
                    'E', null, null, null, "EXP1", "ANALYZED_DATA", "20231201115004780-27", "my dataset", "file"
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
    public void testGetNextDirectoryName(final String spaceCode, final String projectCode, final String experimentCode, final String experimentName,
             final String containerCode, final String sampleCode, final String sampleName, final String dataSetCode, final String extension,
            final String expectedResult)
    {
        assertEquals(ExportExecutor.getNextDocDirectoryName(spaceCode, projectCode, experimentCode, experimentName, containerCode, sampleCode,
                sampleName, dataSetCode, extension), expectedResult);
    }

    @Test(dataProvider = ERRONEOUS_NEXT_ZIP_ENTRY_DATA_PROVIDER, expectedExceptions = IllegalArgumentException.class)
    public void testGetNextDirectoryNameError(final String spaceCode, final String projectCode, final String experimentCode,
            final String experimentName, final String containerCode, final String sampleCode, final String sampleName, final String dataSetCode,
            final String extension)
    {
        ExportExecutor.getNextDocDirectoryName(spaceCode, projectCode, experimentCode, experimentName, containerCode, sampleCode, sampleName, dataSetCode,
                extension);
    }

    @Test(dataProvider = FOLDER_NAME_DATA_PROVIDER)
    public void testGetDataDirectoryName(final char prefix, final String spaceCode, final String projectCode,
            final String containerCode, final String entityCode, final String dataSetTypeCode, final String dataSetCode,
            final String dataSetName, final String fileName, final String expectedResult)
    {
        assertEquals(ExportExecutor.getDataDirectoryName(prefix, spaceCode, projectCode, containerCode, entityCode, dataSetTypeCode, dataSetCode,
                dataSetName, fileName), expectedResult);
    }

    @Test(dataProvider = ERRONEOUS_FOLDER_NAME_DATA_PROVIDER, expectedExceptions = IllegalArgumentException.class)
    public void testGetDataDirectoryNameError(final char prefix, final String spaceCode, final String projectCode,
            final String containerCode, final String entityCode, final String dataSetTypeCode, final String dataSetCode,
            final String dataSetName, final String fileName)
    {
        ExportExecutor.getDataDirectoryName(prefix, spaceCode, projectCode, containerCode, entityCode, dataSetTypeCode, dataSetCode, dataSetName, fileName);
    }

    @Test()
    public void testEscapeUnsafeCharacters()
    {
        final String input = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 $!#%'()+,-.;=@[]^_{}~\\/:*?\"<>|`";
        final String expectedOutput = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 $!#%'()+,-.;=@[]^_{}~__________";
        assertEquals(ExportExecutor.escapeUnsafeCharacters(input), expectedOutput);
    }

}