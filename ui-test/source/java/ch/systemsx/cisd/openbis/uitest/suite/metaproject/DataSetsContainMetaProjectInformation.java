/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.suite.metaproject;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class DataSetsContainMetaProjectInformation extends MetaProjectSuite
{

    @Test
    public void searchedDataSetContainsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, dataSet);

        DataSet searchResult = searchDataSet(dataSet);
        assertThat(metaProjectsOf(searchResult), containExactly(metaProject));
    }

    @Test
    public void listedDataSetContainsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, dataSet);

        DataSet listResult = listDataSet(dataSet);
        assertThat(metaProjectsOf(listResult), containExactly(metaProject));
    }
}
