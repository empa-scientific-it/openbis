/*
 * Copyright 2014 ETH Zuerich, SIS
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;

/**
 * @author Franz-Josef Elmer
 */
public class BasicEntityDescriptionPredicateTest extends AuthorizationTestCase
{
    @Test
    public void testSuccessfulEvaluationForExperiment()
    {
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.EXPERIMENT,
                "/" + SPACE_CODE + "/P/E1");
        BasicEntityDescriptionPredicate predicate = new BasicEntityDescriptionPredicate();
        prepareProvider(createSpaces());
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        predicate.init(provider);

        Status status = predicate.doEvaluation(createPerson(), createRoles(false), entityDescription);

        assertEquals(Status.OK, status);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailedEvaluationForExperiment()
    {
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.EXPERIMENT,
                "/" + ANOTHER_SPACE_CODE + "/P/E1");
        BasicEntityDescriptionPredicate predicate = new BasicEntityDescriptionPredicate();
        prepareProvider(createSpaces());
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        predicate.init(provider);

        Status status = predicate.doEvaluation(createPerson(), createRoles(false), entityDescription);

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"", status.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfulEvaluationForSample()
    {
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.SAMPLE,
                "/" + SPACE_CODE + "/S1");
        BasicEntityDescriptionPredicate predicate = new BasicEntityDescriptionPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);

        Status status = predicate.doEvaluation(createPerson(), createRoles(false), entityDescription);

        assertEquals(Status.OK, status);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailedEvaluationForSample()
    {
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.SAMPLE,
                "/" + ANOTHER_SPACE_CODE + "/S1");
        BasicEntityDescriptionPredicate predicate = new BasicEntityDescriptionPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));
                }
            });

        Status status = predicate.doEvaluation(createPerson(), createRoles(false), entityDescription);

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"", status.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfulEvaluationForDataSet()
    {
        final DataSetAccessPE accessData =
                DataSetAccessPE.createDataSetAccessPEForTest("1", "D1", SPACE_CODE);
        context.checking(new Expectations()
            {
                {
                    allowing(provider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));

                    one(provider).getDatasetCollectionAccessDataByCodes(Arrays.asList("D1"));
                    will(returnValue(Collections.singleton(accessData)));
                }
            });

        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.DATA_SET, "D1");
        BasicEntityDescriptionPredicate predicate = new BasicEntityDescriptionPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);

        Status status = predicate.doEvaluation(createPerson(), createRoles(false), entityDescription);

        assertEquals(Status.OK, status);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailedEvaluationForDataSet()
    {
        final DataSetAccessPE accessData =
                DataSetAccessPE.createDataSetAccessPEForTest("1", "D1", ANOTHER_SPACE_CODE);
        context.checking(new Expectations()
            {
                {
                    allowing(provider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));

                    one(provider).getDatasetCollectionAccessDataByCodes(Arrays.asList("D1"));
                    will(returnValue(Collections.singleton(accessData)));
                }
            });

        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.DATA_SET, "D1");
        BasicEntityDescriptionPredicate predicate = new BasicEntityDescriptionPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);

        Status status = predicate.doEvaluation(createPerson(), createRoles(false), entityDescription);

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"", status.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfulEvaluationForMaterial()
    {
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.MATERIAL, "M1 (MT)");
        BasicEntityDescriptionPredicate predicate = new BasicEntityDescriptionPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);

        Status status = predicate.doEvaluation(createPerson(), createRoles(false), entityDescription);

        assertEquals(Status.OK, status);
        context.assertIsSatisfied();
    }
}
