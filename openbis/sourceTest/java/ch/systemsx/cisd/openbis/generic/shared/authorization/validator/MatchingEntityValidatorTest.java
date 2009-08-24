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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.translator.SearchHitTranslator;

/**
 * Test cases for corresponding {@link MatchingEntityValidator} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = MatchingEntityValidator.class)
public final class MatchingEntityValidatorTest extends AuthorizationTestCase
{
    private static MatchingEntity asHit(IMatchingEntity matchingEntity)
    {
        return SearchHitTranslator.translate(new SearchHit(matchingEntity,
                "unimportant", "?"));
    }

    @Test
    public final void testIsValidFailed()
    {
        boolean fail = true;
        try
        {
            new MatchingEntityValidator().isValid(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testWithExperimentInTheRightDatabase()
    {
        final PersonPE person = createPersonWithRoleAssignments();
        final ExperimentPE experiment = createExperiment(createGroup());
        final MatchingEntityValidator validator = new MatchingEntityValidator();
        assertEquals(true, validator.isValid(person, asHit(experiment)));
    }

    @Test
    public final void testWithExperimentInTheRightGroup()
    {
        final PersonPE person = createPersonWithRoleAssignments();
        final ExperimentPE experiment = createExperiment(createAnotherGroup());
        final MatchingEntityValidator validator = new MatchingEntityValidator();
        assertEquals(true, validator.isValid(person, asHit(experiment)));
    }

    @Test
    public final void testWithExperimentInTheWrongGroup()
    {
        final PersonPE person = createPersonWithRoleAssignments();
        GroupPE group = createGroup("blabla", createAnotherDatabaseInstance());
        final ExperimentPE experiment = createExperiment(group);
        final MatchingEntityValidator validator = new MatchingEntityValidator();
        assertEquals(false, validator.isValid(person, asHit(experiment)));
    }

    @Test
    public final void testWithSampleInTheRightGroup()
    {
        final PersonPE person = createPersonWithRoleAssignments();
        final SamplePE sample = createSample(createGroup());
        final MatchingEntityValidator validator = new MatchingEntityValidator();
        assertEquals(true, validator.isValid(person, asHit(sample)));
    }

    @Test
    public final void testWithSampleInTheWrongGroup()
    {
        final PersonPE person = createPersonWithRoleAssignments();
        GroupPE group = createGroup("blabla", createAnotherDatabaseInstance());
        final SamplePE sample = createSample(group);
        final MatchingEntityValidator validator = new MatchingEntityValidator();
        assertEquals(false, validator.isValid(person, asHit(sample)));
    }

    @Test
    public final void testWithInstanceSample()
    {
        final PersonPE person = createPersonWithRoleAssignments();
        final SamplePE sample = createSample(createDatabaseInstance());
        final MatchingEntityValidator validator = new MatchingEntityValidator();
        assertEquals(true, validator.isValid(person, asHit(sample)));
    }

}
