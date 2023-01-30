/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Test cases for corresponding {@link AbstractTechIdPredicate} class implementation.
 * 
 * @see SpaceIdentifierPredicateTest
 * @author Piotr Buczek
 */
public final class AbstractTechIdPredicateTest extends AuthorizationTestCase
{
    private static TechId TECH_ID = CommonTestUtils.TECH_ID;

    private static SpaceOwnerKind ENTITY_KIND = SpaceOwnerKind.EXPERIMENT;

    private AbstractTechIdPredicate createPredicate()
    {
        return AbstractTechIdPredicate.create(ENTITY_KIND);
    }

    @Test
    public final void testDoEvaluationWithoutDAOFactory()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        boolean fail = true;
        try
        {
            predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        } catch (final Exception e)
        {
            assertEquals("Data provider cannot be null", e.getMessage());
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testExceptionBecauseGroupDoesNotExist()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        prepareProvider(Collections.<SpacePE> emptyList(), createSpace(), ENTITY_KIND, TECH_ID);
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        predicate.init(provider);
        assertTrue(predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID).isError());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluation()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        prepareProvider(createSpaces(), createSpace(), ENTITY_KIND, TECH_ID);
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationWithHomeGroup()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        final PersonPE person = createPerson();
        person.setHomeSpace(createSpace());
        final SpacePE homeGroup = createSpace();
        prepareProvider(createSpaces(), homeGroup, ENTITY_KIND, TECH_ID);
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        predicate.init(provider);
        final Status evaluation = predicate.doEvaluation(person, createRoles(false), TECH_ID);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testFailedEvaluation()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        prepareProvider(createSpaces(), createAnotherSpace(), ENTITY_KIND, TECH_ID);
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals("User 'megapixel' does not have enough privileges.", evaluation
                .tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAccessAnotherGroup()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        final List<SpacePE> groups = createSpaces();
        final SpacePE anotherGroup = createSpace(ANOTHER_SPACE_CODE);
        groups.add(anotherGroup);
        prepareProvider(groups, anotherGroup, ENTITY_KIND, TECH_ID);
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals("User 'megapixel' does not have enough privileges.", evaluation
                .tryGetErrorMessage());
        context.assertIsSatisfied();
    }

}
