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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.Arrays;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class NewProjectPredicateTest extends CommonPredicateTest<NewProject>
{

    @Override
    protected void expectWithAll(IAuthorizationConfig config, NewProject object)
    {
        expectAuthorizationConfig(config);
        prepareProvider(ALL_SPACES_PE);
    }

    @Override
    protected NewProject createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        return new NewProject("/" + spacePE.getCode() + "/" + projectPE.getCode(), "description");
    }

    @Override
    protected Status evaluateObject(NewProject object, RoleWithIdentifier... roles)
    {
        NewProjectPredicate predicate = new NewProjectPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), object);
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertEquals(UserFailureException.class, t.getClass());
        assertEquals("No new project specified.", t.getMessage());
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

}
