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

import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample.SamplePredicateTestService;

/**
 * @author pkupczyk
 */
public class ExistingSampleIdentifierPredicateSystemTest extends SampleIdentifierPredicateSystemTest
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ;
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<SampleIdentifier> objects, Object param)
    {
        getBean(SamplePredicateTestService.class).testExistingSampleIdentifierPredicate(sessionProvider, objects.get(0));
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}
