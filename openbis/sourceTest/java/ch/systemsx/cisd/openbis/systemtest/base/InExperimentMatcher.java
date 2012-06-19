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

package ch.systemsx.cisd.openbis.systemtest.base;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

public class InExperimentMatcher extends TypeSafeMatcher<Object>
{

    private Experiment experiment;

    public InExperimentMatcher(Experiment experiment)
    {
        this.experiment = experiment;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("An entity in experiment " + experiment.getCode());
    }

    @Override
    public boolean matchesSafely(Object actual)
    {
        if (actual instanceof Sample)
        {
            return ((Sample) actual).getExperiment().getId() == experiment.getId();
        } else
        {
            return ((DataSet) actual).getExperiment().getId() == experiment.getId();
        }
    }

}