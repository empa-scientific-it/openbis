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
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Test cases for corresponding {@link SpaceIdentifierPredicate} class.
 * 
 * @author Christian Ribeaud
 */
public final class SpaceIdentifierPredicateTest extends AuthorizationTestCase
{
    @Test
    public final void testDoEvaluationWithoutDAOFactory()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        boolean fail = true;
        try
        {
            predicate
                    .doEvaluation(createPerson(), createRoles(false), SpaceIdentifier.createHome());
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testExceptionBecauseGroupDoesNotExist()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(Collections.<SpacePE> emptyList());
        predicate.init(provider);
        assertTrue(predicate.doEvaluation(createPerson(), createRoles(false), new SpaceIdentifier(
                SPACE_CODE)).isError());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluation()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), new SpaceIdentifier(
                        SPACE_CODE));
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationWithHomeGroup()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);
        final PersonPE person = createPerson();
        final SpacePE homeGroup = createSpace();
        person.setHomeSpace(homeGroup);
        final SpaceIdentifier groupIdentifier = new SpaceIdentifier(null);
        final Status evaluation =
                predicate.doEvaluation(person, createRoles(false), groupIdentifier);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testFailedEvaluation()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), new SpaceIdentifier(
                        ANOTHER_SPACE_CODE));
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals("User 'megapixel' does not have enough privileges.", evaluation
                .tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAccessAnotherGroup()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        final List<SpacePE> groups = createSpaces();
        groups.add(createSpace(ANOTHER_SPACE_CODE));
        prepareProvider(groups);
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), new SpaceIdentifier(ANOTHER_SPACE_CODE));
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals("User 'megapixel' does not have enough privileges.", evaluation
                .tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
