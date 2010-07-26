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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

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
                    .doEvaluation(createPerson(), createRoles(false), GroupIdentifier.createHome());
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void testExceptionBecauseInstanceDoesNotExist()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(INSTANCE_CODE, null, Collections.<GroupPE> emptyList());
        predicate.init(provider);
        predicate.doEvaluation(createPerson(), createRoles(false), new GroupIdentifier(
                INSTANCE_CODE, SPACE_CODE)).isError();
        context.assertIsSatisfied();
    }

    @Test
    public final void testExceptionBecauseGroupDoesNotExist()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), Collections.<GroupPE> emptyList());
        predicate.init(provider);
        assertTrue(predicate.doEvaluation(createPerson(), createRoles(false), new GroupIdentifier(
                INSTANCE_CODE, SPACE_CODE)).isError());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluation()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), new GroupIdentifier(
                        INSTANCE_CODE, SPACE_CODE));
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationWithHomeGroup()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);
        final PersonPE person = createPerson();
        final GroupPE homeGroup = createGroup();
        person.setHomeGroup(homeGroup);
        final GroupIdentifier groupIdentifier = new GroupIdentifier(INSTANCE_CODE, null);
        final Status evaluation =
                predicate.doEvaluation(person, createRoles(false), groupIdentifier);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testFailedEvaluation()
    {
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createGroups());
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), new GroupIdentifier(
                        ANOTHER_INSTANCE_CODE, ANOTHER_GROUP_CODE));
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals("User 'megapixel' does not have enough privileges.", evaluation
                .tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAccessAnotherGroup()
    {
        final DatabaseInstancePE homeDatabaseInstance = createDatabaseInstance();
        final SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();
        final List<GroupPE> groups = createGroups();
        groups.add(createGroup(ANOTHER_GROUP_CODE, homeDatabaseInstance));
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), groups);
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), new GroupIdentifier(
                        INSTANCE_CODE, ANOTHER_GROUP_CODE));
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals("User 'megapixel' does not have enough privileges.", evaluation
                .tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
