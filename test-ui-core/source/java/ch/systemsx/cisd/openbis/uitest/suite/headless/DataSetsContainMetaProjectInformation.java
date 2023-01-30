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

package ch.systemsx.cisd.openbis.uitest.suite.headless;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class DataSetsContainMetaProjectInformation extends HeadlessSuite
{

    @Test
    public void dataSetSearchReturnsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, dataSet);

        List<DataSet> searchResult = searchFor(dataSets().withCode(dataSet.getCode()));

        assertThat(searchResult, containsExactly(dataSet));
        assertThat(metaProjectsOf(searchResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void dataSetSearchOnBehalfOfAnotherUserReturnsMetaProjectInformation()
            throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));

        User john = create(aUser());
        MetaProject metaProject = as(user(john), create(aMetaProject()));
        as(user(john), tagWith(metaProject, dataSet));

        List<DataSet> searchResult =
                searchFor(dataSets().withCode(dataSet.getCode()).onBehalfOf(john));

        assertThat(searchResult, containsExactly(dataSet));
        assertThat(metaProjectsOf(searchResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void dataSetsOfSamplesListingContainsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, dataSet);

        List<DataSet> listResult = listDataSetsOfSamples(sample);

        assertThat(listResult, containsExactly(dataSet));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void dataSetsOfSamplesWithConnectionsListingContainsMetaProjectInformation()
            throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, dataSet);

        List<DataSet> listResult = listDataSetsOfSamplesWithConnections(sample);

        assertThat(listResult, containsExactly(dataSet));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void dataSetsOfExperimentsListingContainsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, dataSet);

        List<DataSet> listResult = listDataSetsOfExperiments(experiment);

        assertThat(listResult, containsExactly(dataSet));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void dataSetsOfSampleListingContainsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, dataSet);

        List<DataSet> listResult = listDataSetsOfSample(sample);

        assertThat(listResult, containsExactly(dataSet));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void dataSetsOfSamplesListingOnBehalfOfAnotherUserContainsMetaProjectInformation()
            throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));

        User john = create(aUser());
        MetaProject metaProject = as(user(john), create(aMetaProject()));
        as(user(john), tagWith(metaProject, dataSet));

        List<DataSet> listResult = listDataSetsOfSamplesOnBehalfOfUser(john, sample);

        assertThat(listResult, containsExactly(dataSet));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void dataSetsOfExperimentsListingOnBehalfOfAnotherUserContainsMetaProjectInformation()
            throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));

        User john = create(aUser());
        MetaProject metaProject = as(user(john), create(aMetaProject()));
        as(user(john), tagWith(metaProject, dataSet));

        List<DataSet> listResult = listDataSetsOfExperimentsOnBehalfOfUser(john, experiment);

        assertThat(listResult, containsExactly(dataSet));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void dataSetMetaDataContainsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        DataSet dataSet = create(aDataSet().in(sample));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, dataSet);

        List<DataSet> result = getDataSetMetaData(dataSet.getCode());

        assertThat(result, containsExactly(dataSet));
        assertThat(metaProjectsOf(result.get(0)), containExactly(metaProject));
    }

}
