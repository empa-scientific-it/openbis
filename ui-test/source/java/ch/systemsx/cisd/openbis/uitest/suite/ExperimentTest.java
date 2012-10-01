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

package ch.systemsx.cisd.openbis.uitest.suite;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.Space;

/**
 * @author anttil
 */
@Test(groups =
    { "login-admin" })
public class ExperimentTest extends SeleniumTest
{

    @Test
    public void creatingExperimentWithSampleChangesTheSample() throws Exception
    {
        Space space = create(aSpace());
        Project project = create(aProject().in(space));
        Sample sample = create(aSample().in(space));

        Experiment experiment = create(anExperiment().in(project).withSamples(sample));

        assertThat(browserEntryOf(sample), containsValue("Experiment", experiment.getCode()));
        assertThat(browserEntryOf(sample), containsValue("Project", project.getCode()));
    }
}
