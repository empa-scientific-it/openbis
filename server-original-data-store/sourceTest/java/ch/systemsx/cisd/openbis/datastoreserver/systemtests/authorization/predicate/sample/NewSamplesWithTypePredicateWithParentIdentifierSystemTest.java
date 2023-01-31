/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.sample;

import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample.SamplePredicateTestService;

/**
 * @author pkupczyk
 */
public class NewSamplesWithTypePredicateWithParentIdentifierSystemTest extends NewSamplePredicateWithParentIdentifierSystemTest
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<NewSample> objects, Object param)
    {
        NewSamplesWithTypes newSamples = new NewSamplesWithTypes();
        newSamples.setNewEntities(objects);
        getBean(SamplePredicateTestService.class).testNewSamplesWithTypePredicate(user.getSessionProvider(), newSamples);
    }

    @Override
    protected CommonPredicateSystemTestAssertions<NewSample> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<NewSample>(super.getAssertions())
            {
                @Override
                public void assertWithNullCollection(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertException(t, NullPointerException.class, null);
                    }
                }
            };
    }

}
