/*
 * Copyright 2009 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Bernd Rinn
 */
public class ListSampleCriteriaPredicateTest extends AuthorizationTestCase
{

    @Test
    public final void testDoEvaluationWithoutDAOFactory()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        boolean fail = true;
        try
        {
            predicate.doEvaluation(createPerson(), createRoles(false),
                    ListSampleCriteria.createForExperiment(new TechId(1L)));
        } catch (final Exception e)
        {
            assertEquals("Data provider cannot be null", e.getMessage());
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testNoSpaces()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(Collections.<SpacePE> emptyList());
        predicate.init(provider);
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSpaceCode(SPACE_CODE);
        criteria.setIncludeSpace(true);
        assertTrue(predicate.doEvaluation(createPerson(), createRoles(false), criteria).isOK());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSpaceDoesNotExist()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(Arrays.asList(createAnotherSpace()));
        predicate.init(provider);
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSpaceCode(SPACE_CODE);
        criteria.setIncludeSpace(true);
        assertTrue(predicate.doEvaluation(createPerson(), createRoles(false), criteria).isOK());
        context.assertIsSatisfied();
    }

    public final void testExceptionBecauseSpaceUnauthorized()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSpaceCode(ANOTHER_SPACE_CODE);
        criteria.setIncludeSpace(true);
        assertTrue(predicate.doEvaluation(createPerson(), createRoles(false), criteria).isError());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationForSpaceSamples()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSpaceCode(SPACE_CODE);
        criteria.setIncludeSpace(true);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), criteria);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationForSharedSamples()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(createSpaces());
        predicate.init(provider);
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setIncludeInstance(true);
        final Status evaluation =
                predicate.doEvaluation(createPerson(),
                        Collections.<RoleWithIdentifier> emptyList(), criteria);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationForExperimentSamples()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(createSpaces());
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        context.checking(new Expectations()
            {
                {
                    one(provider).tryGetSpace(SpaceOwnerKind.EXPERIMENT, new TechId(17L));
                    will(returnValue(createSpace()));
                }
            });
        predicate.init(provider);
        final ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(new TechId(17L));
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), criteria);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testExceptionBecauseExperimentUnauthorized()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(createSpaces());
        expectAuthorizationConfig(new TestAuthorizationConfig(false, false));
        context.checking(new Expectations()
            {
                {
                    one(provider).tryGetSpace(SpaceOwnerKind.EXPERIMENT, new TechId(17L));
                    will(returnValue(createAnotherSpace()));
                }
            });
        predicate.init(provider);
        final ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(new TechId(17L));
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), criteria);
        assertTrue(evaluation.isError());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationForContainerSamples()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(createSpaces());

        SampleAccessPE sampleAccess = new SampleAccessPE();
        sampleAccess.setSpaceCode(SPACE_CODE);
        sampleAccess.setSampleCode("TEST");

        context.checking(new Expectations()
            {
                {
                    one(provider).getSampleCollectionAccessDataByTechIds(Arrays.asList(new TechId(42L)), true);
                    will(returnValue(Collections.singleton(sampleAccess)));
                }
            });
        predicate.init(provider);
        final ListSampleCriteria criteria = ListSampleCriteria.createForContainer(new TechId(42L));
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), criteria);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testExceptionBecauseContainerUnauthorized()
    {
        final ListSampleCriteriaPredicate predicate = new ListSampleCriteriaPredicate();
        prepareProvider(createSpaces());

        SampleAccessPE containerAccess = new SampleAccessPE();
        containerAccess.setSpaceCode(ANOTHER_SPACE_CODE);
        containerAccess.setSampleCode("TEST");

        context.checking(new Expectations()
            {
                {
                    one(provider).getSampleCollectionAccessDataByTechIds(Arrays.asList(new TechId(42L)), true);
                    will(returnValue(Collections.singleton(containerAccess)));
                }
            });
        predicate.init(provider);
        final ListSampleCriteria criteria = ListSampleCriteria.createForContainer(new TechId(42L));
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), criteria);
        assertTrue(evaluation.isError());
        context.assertIsSatisfied();
    }

}
