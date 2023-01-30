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

import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class SamplesContainMetaProjectInformation extends HeadlessSuite
{

    @Test
    public void sampleSearchReturnsMetaProjectInformation() throws Exception
    {
        Sample sample = create(aSample());
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, sample);

        List<Sample> searchResult = searchFor(samples().withCode(sample.getCode()));

        assertThat(searchResult, containsExactly(sample));
        assertThat(metaProjectsOf(searchResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void sampleSearchOnBehalfOfAnotherUserReturnsMetaProjectInformation() throws Exception
    {
        Sample sample = create(aSample());
        User john = create(aUser());
        MetaProject metaProject = as(user(john), create(aMetaProject()));
        as(user(john), tagWith(metaProject, sample));

        List<Sample> searchResult =
                searchFor(samples().withCode(sample.getCode()).onBehalfOf(john));

        assertThat(searchResult, containsExactly(sample));
        assertThat(metaProjectsOf(searchResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void samplesOfExperimentListingContainsMetaProjectInformation() throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, sample);

        List<Sample> listResult = listSamplesOfExperiment(experiment);

        assertThat(listResult, containsExactly(sample));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void samplesOfExperimentListingOnBehalfOfAnotherUserContainsMetaProjectInformation()
            throws Exception
    {
        Experiment experiment = create(anExperiment());
        Sample sample = create(aSample().in(experiment));
        User john = create(aUser());
        MetaProject metaProject = as(user(john), create(aMetaProject()));
        as(user(john), tagWith(metaProject, sample));

        List<Sample> listResult = listSamplesOfExperimentOnBehalfOf(experiment, john);

        assertThat(listResult, containsExactly(sample));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }

}
