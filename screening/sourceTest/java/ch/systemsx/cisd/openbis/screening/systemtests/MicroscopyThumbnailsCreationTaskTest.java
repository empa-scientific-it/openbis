/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.CountStopCondition;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.RegexCondition;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = { "slow", "systemtest" })
public class MicroscopyThumbnailsCreationTaskTest extends AbstractMicroscopyImageDropboxTestCase
{
    private static SimpleComparator<DataSet, String> CODE_COMPARATOR = new SimpleComparator<DataSet, String>()
        {
            @Override
            public String evaluate(DataSet dataSet)
            {
                return dataSet.getCode();
            }
        };

    @Override
    protected String getDataFolderToDrop()
    {
        return "aarons_drosophila_example";
    }

    @Override
    @BeforeTest
    public void dropAnExampleDataSet() throws Exception
    {
        super.dropAnExampleDataSet();
        waitUntilDataSetImported(new CountStopCondition(new RegexCondition(".* thumbnail data sets have been created."), 3));
    }

    @Test
    public void test()
    {
        List<AbstractExternalData> dataSets = getRegisteredContainerDataSets(getClass());
        int actualNumberOfThumbnailDataSets = 0;
        for (int i = 0; i < dataSets.size(); i++)
        {
            AbstractExternalData dataSet = dataSets.get(i);
            DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
            fetchOptions.withComponents().withExperiment();
            fetchOptions.withComponents().withSample();
            fetchOptions.withComponents().withType();
            DataSetPermId dataSetPermId = new DataSetPermId(dataSet.getCode());
            DataSet containerDataSet = v3api.getDataSets(sessionToken, Arrays.asList(dataSetPermId), fetchOptions).get(dataSetPermId);
            List<DataSet> components = containerDataSet.getComponents();
            Collections.sort(components, CODE_COMPARATOR);

            DataSet mainDataSet = components.get(0);
            assertEquals("Data set " + i, "MICROSCOPY_IMG", mainDataSet.getType().getCode());
            assertEquals("Data set " + i, components.size() > 1 ? 2 : 1, components.size());
            if (components.size() > 1)
            {
                actualNumberOfThumbnailDataSets++;
                DataSet thumbnailDataSet = components.get(1);
                assertEquals("Data set " + i, "MICROSCOPY_IMG_THUMBNAIL", thumbnailDataSet.getType().getCode());
                assertEquals("Data set " + i, mainDataSet.getExperiment().getIdentifier().getIdentifier(),
                        thumbnailDataSet.getExperiment().getIdentifier().getIdentifier());
                assertEquals("Data set " + i, mainDataSet.getSample().getIdentifier().getIdentifier(),
                        thumbnailDataSet.getSample().getIdentifier().getIdentifier());

                IDataStoreServerApi dssApi = ServiceProvider.getDssServiceInternalV3();
                DataSetFileSearchCriteria searchCriteria = new DataSetFileSearchCriteria();
                searchCriteria.withDataSet().withCode().thatEquals(thumbnailDataSet.getCode());
                DataSetFileFetchOptions fileFetchOptions = new DataSetFileFetchOptions();
                List<DataSetFile> files = dssApi.searchFiles(sessionToken, searchCriteria, fileFetchOptions).getObjects();
                List<String> paths = files.stream().map(DataSetFile::getPath).collect(Collectors.toList());
                Collections.sort(paths);
                assertEquals("Data set " + i, "[, thumbnail.png]", paths.toString());
                List<Long> sizes = files.stream().map(DataSetFile::getFileLength).collect(Collectors.toList());
                System.err.println(sizes);
                assertTrue("Size of thumbnail " + i + " is of unexpected size " + sizes.get(1)
                        + ". Should be > 40000.", sizes.get(1) > 40000);
                assertTrue("Size of thumbnail " + i + " is of unexpected size " + sizes.get(1)
                        + ". Should be < 55000.", sizes.get(1) < 55000);
            }
        }
        assertEquals(9, dataSets.size());
        assertEquals(5, actualNumberOfThumbnailDataSets);
    }

}
