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
import java.util.Collections;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class SampleUpdatesCollectionPredicateTest extends AuthorizationTestCase
{
    @Test
    public void testEvaluateOK()
    {
        SampleUpdatesDTO sampleWithId =
                new SampleUpdatesDTO(new TechId(42L), null, null, null, null, 0, null, null, null);
        SampleUpdatesDTO sampleWithIdAndExperiment =
                new SampleUpdatesDTO(new TechId(43L), null, ExperimentIdentifierFactory.parse("/"
                        + SPACE_CODE + "/B/E"), null, null, 0, null, null, null);
        SampleUpdatesDTO sampleWithIdAndIdentifer =
                new SampleUpdatesDTO(new TechId(44L), null, null, null, null, 0,
                        SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S1"), null, null);
        prepareProvider(createSpaces());
        SampleUpdatesCollectionPredicate predicate = new SampleUpdatesCollectionPredicate();
        predicate.init(provider);

        SampleAccessPE sampleAccess = new SampleAccessPE();
        sampleAccess.setSpaceCode(SPACE_CODE);
        sampleAccess.setSampleCode("TEST");

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));

                    one(provider).getSampleCollectionAccessDataByTechIds(TechId.createList(42L, 43L, 44L), true);
                    will(returnValue(Collections.singleton(sampleAccess)));
                }
            });

        Status result =
                predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(sampleWithId,
                        sampleWithIdAndExperiment, sampleWithIdAndIdentifer));

        assertEquals("OK", result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testEvaluateFailedBecauseOfSampleId()
    {
        SampleUpdatesDTO sampleWithId =
                new SampleUpdatesDTO(new TechId(42L), null, null, null, null, 0, null, null, null);
        SampleUpdatesDTO sampleWithIdAndExperiment =
                new SampleUpdatesDTO(new TechId(43L), null, ExperimentIdentifierFactory.parse("/"
                        + SPACE_CODE + "/B/E"), null, null, 0, null, null, null);
        SampleUpdatesDTO sampleWithIdAndIdentifer =
                new SampleUpdatesDTO(new TechId(44L), null, null, null, null, 0,
                        SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S1"), null, null);
        prepareProvider(createSpaces());
        SampleUpdatesCollectionPredicate predicate = new SampleUpdatesCollectionPredicate();
        predicate.init(provider);

        SampleAccessPE sampleAccess = new SampleAccessPE();
        sampleAccess.setSampleCode("TEST");

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));

                    one(provider).getSampleCollectionAccessDataByTechIds(TechId.createList(42L, 43L, 44L), true);
                    will(returnValue(Collections.singleton(sampleAccess)));
                }
            });

        Status result =
                predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(sampleWithId,
                        sampleWithIdAndExperiment, sampleWithIdAndIdentifer));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges to modify "
                + "instance level entities.\"", result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testEvaluateFailedBecauseOfExperiment()
    {
        SampleUpdatesDTO sampleWithId =
                new SampleUpdatesDTO(new TechId(42L), null, null, null, null, 0, null, null, null);
        SampleUpdatesDTO sampleWithIdAndExperiment =
                new SampleUpdatesDTO(new TechId(43L), null, ExperimentIdentifierFactory.parse("/"
                        + ANOTHER_SPACE_CODE + "/B/E"), null, null, 0, null, null, null);
        SampleUpdatesDTO sampleWithIdAndIdentifer =
                new SampleUpdatesDTO(new TechId(44L), null, null, null, null, 0,
                        SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S1"), null, null);
        prepareProvider(createSpaces());

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));
                }
            });

        SampleUpdatesCollectionPredicate predicate = new SampleUpdatesCollectionPredicate();
        predicate.init(provider);

        Status result =
                predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(sampleWithId,
                        sampleWithIdAndExperiment, sampleWithIdAndIdentifer));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testEvaluateFailedBecauseOfSampleIdentifier()
    {
        SampleUpdatesDTO sampleWithId =
                new SampleUpdatesDTO(new TechId(42L), null, null, null, null, 0, null, null, null);
        SampleUpdatesDTO sampleWithIdAndExperiment =
                new SampleUpdatesDTO(new TechId(43L), null, ExperimentIdentifierFactory.parse("/"
                        + SPACE_CODE + "/B/E"), null, null, 0, null, null, null);
        SampleUpdatesDTO sampleWithIdAndIdentifer =
                new SampleUpdatesDTO(new TechId(44L), null, null, null, null, 0,
                        SampleIdentifierFactory.parse("/" + ANOTHER_SPACE_CODE + "/S1"), null, null);

        prepareProvider(createSpaces());
        SampleUpdatesCollectionPredicate predicate = new SampleUpdatesCollectionPredicate();
        predicate.init(provider);

        SampleAccessPE sampleAccess = new SampleAccessPE();
        sampleAccess.setSpaceCode(ANOTHER_SPACE_CODE);
        sampleAccess.setSampleCode("S1");

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));

                    one(provider).getSampleCollectionAccessDataByTechIds(TechId.createList(42L, 43L, 44L), true);
                    will(returnValue(Collections.singleton(sampleAccess)));
                }
            });

        Status result =
                predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(sampleWithId,
                        sampleWithIdAndExperiment, sampleWithIdAndIdentifer));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }
}
