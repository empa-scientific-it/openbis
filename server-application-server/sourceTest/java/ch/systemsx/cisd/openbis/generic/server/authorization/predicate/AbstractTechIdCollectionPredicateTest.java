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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ExperimentTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ProjectTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.SpaceTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Franz-Josef Elmer
 */
public class AbstractTechIdCollectionPredicateTest extends AuthorizationTestCase
{
    @Test
    public void testProjectTechIdCollectionPredicateIsSuccessful()
    {
        ProjectTechIdCollectionPredicate predicate = new ProjectTechIdCollectionPredicate();
        prepareProvider(createSpaces());
        final List<TechId> techIds = TechId.createList(1L, 2L);

        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        context.checking(new Expectations()
            {
                {
                    one(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, techIds);
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(createSpace()))));
                }
            });
        predicate.init(provider);

        Status result = predicate.evaluate(createPerson(), createRoles(false), techIds);

        assertEquals("OK", result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSpaceTechIdCollectionPredicateSucceedsForInstanceAdmin()
    {
        SpaceTechIdCollectionPredicate predicate = new SpaceTechIdCollectionPredicate();
        prepareProvider(createSpaces());
        final List<TechId> techIds = TechId.createList(1L, 2L);
        context.checking(new Expectations()
            {
                {
                    one(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.SPACE, techIds);
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(createAnotherSpace()))));
                }
            });
        predicate.init(provider);

        Status result =
                predicate.evaluate(createPerson(), createRoles(true), techIds);

        assertTrue(result.toString(), result.isOK());
        context.assertIsSatisfied();
    }

    @Test
    public void testSpaceTechIdCollectionPredicateSucceedsForSpaceAdmin()
    {
        SpaceTechIdCollectionPredicate predicate = new SpaceTechIdCollectionPredicate();
        prepareProvider(createSpaces());
        final List<TechId> techIds = TechId.createList(1L, 2L);
        context.checking(new Expectations()
            {
                {
                    one(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.SPACE, techIds);
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(createAnotherSpace()))));
                }
            });
        predicate.init(provider);

        Status result =
                predicate.evaluate(createPerson(), createAnotherSpaceAdminRole(), techIds);

        assertTrue(result.toString(), result.isOK());
        context.assertIsSatisfied();
    }

    @Test
    public void testSpaceTechIdCollectionPredicateFails()
    {
        SpaceTechIdCollectionPredicate predicate = new SpaceTechIdCollectionPredicate();
        prepareProvider(createSpaces());
        final List<TechId> techIds = TechId.createList(1L, 2L);
        context.checking(new Expectations()
            {
                {
                    one(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.SPACE, techIds);
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(createAnotherSpace()))));
                }
            });
        predicate.init(provider);

        Status result = predicate.evaluate(createPerson(), createRoles(false), techIds);

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testExperimentTechIdCollectionPredicateFails()
    {
        ExperimentTechIdCollectionPredicate predicate = new ExperimentTechIdCollectionPredicate();
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        prepareProvider(createSpaces());
        final List<TechId> techIds = TechId.createList(1L, 2L);
        context.checking(new Expectations()
            {
                {
                    one(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.EXPERIMENT, techIds);
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(createAnotherSpace()))));
                }
            });
        predicate.init(provider);

        Status result = predicate.evaluate(createPerson(), createRoles(false), techIds);

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }
}
