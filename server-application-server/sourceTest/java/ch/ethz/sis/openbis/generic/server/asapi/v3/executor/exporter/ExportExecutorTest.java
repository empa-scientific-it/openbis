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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.NAME_PROPERTY_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.PDF_DIRECTORY;
import static org.testng.Assert.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class ExportExecutorTest
{

    private static final String NEXT_ZIP_ENTRY_DATA_PROVIDER = "nextZipEntryData";

    private static final String ERRONEOUS_NEXT_ZIP_ENTRY_DATA_PROVIDER = "erroneousNextZipEntryData";

    private static final String SPACE_CODE = "TEST_SPACE";

    private static final String PROJECT_CODE = "TEST_PROJECT";

    private static final String EXPERIMENT_CODE = "TEST_EXPERIMENT";

    private static final String EXPERIMENT_NAME = "Test experiment name";

    private static final String SAMPLE_CODE = "TEST_SAMPLE";

    private static final String SAMPLE_NAME = "Test sample name";

    private static final String EXTENSION = ".pdf";

    private static final String DATA_SET_CODE = "TEST_DATA_SET";

    private static final Object[][] DATA = {
            {
                    null, null, null, null, null, null, null, null,
                    String.format("/%s", PDF_DIRECTORY)
            },
            {
                    SPACE_CODE, null, null, null, null, null, null, null,
                    String.format("/%s/%s", PDF_DIRECTORY, SPACE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, null, null, null, EXTENSION,
                    String.format("/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, null, null, null,
                    String.format("/%s/%s/%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, null, null, EXTENSION,
                    String.format("/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("/%s/%s/%s (%s)", PDF_DIRECTORY, SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("/%s/%s/%s (%s)%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, null, DATA_SET_CODE, null,
                    String.format("/%s/%s/%s/%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, null, DATA_SET_CODE, EXTENSION,
                    String.format("/%s/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("/%s/%s/%s (%s)/%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, null, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("/%s/%s/%s (%s)/%s%s", PDF_DIRECTORY, SPACE_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, null, null,
                    String.format("/%s/%s/%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, null, null, null, EXTENSION,
                    String.format("/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("/%s/%s/%s/%s (%s)", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("/%s/%s/%s/%s (%s)%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, null, null, null,
                    String.format("/%s/%s/%s/%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, null, null, EXTENSION,
                    String.format("/%s/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("/%s/%s/%s/%s (%s)/%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("/%s/%s/%s/%s (%s)/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE,
                            EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, null, DATA_SET_CODE, null,
                    String.format("/%s/%s/%s/%s/%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, null, null, SAMPLE_CODE, null, DATA_SET_CODE, EXTENSION,
                    String.format("/%s/%s/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, null, null, null, null, null,
                    String.format("/%s/%s/%s/%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, null, null, null, null, EXTENSION,
                    String.format("/%s/%s/%s/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, null,
                    String.format("/%s/%s/%s/%s (%s)", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, null, EXTENSION,
                    String.format("/%s/%s/%s/%s (%s)%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, DATA_SET_CODE, null,
                    String.format("/%s/%s/%s/%s (%s)/%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, null, null, DATA_SET_CODE, EXTENSION,
                    String.format("/%s/%s/%s/%s (%s)/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, DATA_SET_CODE,
                            EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, null, null,
                    String.format("/%s/%s/%s/%s (%s)/%s (%s)", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE, SAMPLE_NAME,
                            SAMPLE_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, null, EXTENSION,
                    String.format("/%s/%s/%s/%s (%s)/%s (%s)%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, EXTENSION)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, null,
                    String.format("/%s/%s/%s/%s (%s)/%s (%s)/%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE)
            },
            {
                    SPACE_CODE, PROJECT_CODE, EXPERIMENT_CODE, EXPERIMENT_NAME, SAMPLE_CODE, SAMPLE_NAME, DATA_SET_CODE, EXTENSION,
                    String.format("/%s/%s/%s/%s (%s)/%s (%s)/%s%s", PDF_DIRECTORY, SPACE_CODE, PROJECT_CODE, EXPERIMENT_NAME, EXPERIMENT_CODE,
                            SAMPLE_NAME, SAMPLE_CODE, DATA_SET_CODE, EXTENSION)
            },
    };

    private static final Object[][] ERRONEOUS_DATA = {
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

    @DataProvider
    protected Object[][] nextZipEntryData()
    {
        return DATA;
    }

    @DataProvider
    protected Object[][] erroneousNextZipEntryData()
    {
        return ERRONEOUS_DATA;
    }

    @Test(dataProvider = NEXT_ZIP_ENTRY_DATA_PROVIDER)
    public void testGetNextZipEntry(final String spaceCode, final String projectCode, final String experimentCode, final String experimentName,
            final String sampleCode, final String sampleName, final String dataSetCode, final String extension, final String expectedResult)
    {
        assertEquals(ExportExecutor.getNextZipEntry(spaceCode, projectCode, experimentCode, experimentName, sampleCode, sampleName, dataSetCode,
                extension), expectedResult);
    }

    @Test(dataProvider = ERRONEOUS_NEXT_ZIP_ENTRY_DATA_PROVIDER, expectedExceptions = IllegalArgumentException.class)
    public void testGetNextZipEntryError(final String spaceCode, final String projectCode, final String experimentCode, final String experimentName,
            final String sampleCode, final String sampleName, final String dataSetCode, final String extension)
    {
        ExportExecutor.getNextZipEntry(spaceCode, projectCode, experimentCode, experimentName, sampleCode, sampleName, dataSetCode, extension);
    }

}