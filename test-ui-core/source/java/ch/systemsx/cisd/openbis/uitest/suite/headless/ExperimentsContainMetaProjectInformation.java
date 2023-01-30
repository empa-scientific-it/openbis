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

import ch.systemsx.cisd.openbis.uitest.rmi.Identifiers;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Project;

/**
 * @author anttil
 */
public class ExperimentsContainMetaProjectInformation extends HeadlessSuite
{

    @Test
    public void experimentListingContainsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, experiment);

        List<Experiment> listResult = listExperiments(Identifiers.get(experiment).toString());

        assertThat(listResult, containsExactly(experiment));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void experimentsOfProjectsListingContainsMetaProjectInformation() throws Exception
    {
        Project project = create(aProject());
        Experiment experiment = create(anExperiment().in(project));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, experiment);
        List<Experiment> listResult = listExperimentsOfProjects(project);

        assertThat(listResult, containsExactly(experiment));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void experimentsHavingSamplesListingContainsMetaProjectInformation() throws Exception
    {
        Project project = create(aProject());
        Experiment experiment = create(anExperiment().in(project));
        create(aSample().in(experiment));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, experiment);
        List<Experiment> listResult = listExperimentsHavingSamplesOfProjects(project);

        assertThat(listResult, containsExactly(experiment));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void experimentsHavingDataSetsListingContainsMetaProjectInformation() throws Exception
    {
        Project project = create(aProject());
        Experiment experiment = create(anExperiment().in(project));
        create(aDataSet().in(experiment));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, experiment);
        List<Experiment> listResult = listExperimentsHavingDataSetsOfProjects(project);

        assertThat(listResult, containsExactly(experiment));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

}
