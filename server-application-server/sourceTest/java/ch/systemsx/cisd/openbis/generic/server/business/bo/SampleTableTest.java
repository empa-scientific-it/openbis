/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Test cases for corresponding {@link SampleTable} class.
 * 
 * @author Piotr Buczek
 */
public final class SampleTableTest extends AbstractBOTest
{

    private static final String DB = "DB";

    @Test
    public void testDelete()
    {
        final List<TechId> sampleIds = new ArrayList<TechId>();
        sampleIds.add(new TechId(1L));
        sampleIds.add(new TechId(2L));
        sampleIds.add(new TechId(3L));
        final String reason = "reason";
        context.checking(new Expectations()
            {
                {
                    PersonPE registrator = EXAMPLE_SESSION.tryGetPerson();
                    one(sampleDAO).delete(sampleIds, registrator, reason);
                }
            });
        final SampleTable sampleTableBO = createSampleTableBO();
        sampleTableBO.deleteByTechIds(sampleIds, reason);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    @SuppressWarnings("unchecked")
    public void testPrepareForRegistrationWithAuthorizationFailure()
    {
        context.checking(new Expectations()
            {
                {
                    one(entityOperationChecker).assertInstanceSampleCreationAllowed(
                            with(any(IAuthSession.class)), with(any(List.class)));
                    will(throwException(new AuthorizationFailureException("Creation not allowed")));
                }
            });

        NewSample newSample = new NewSample();
        newSample.setIdentifier(getSharedSampleIdentifier("sampleCode").toString());

        final SampleTable sampleTableBO = createSampleTableBO();
        sampleTableBO.prepareForRegistration(Collections.singletonList(newSample), null);
        context.assertIsSatisfied();
    }

    private final SampleTable createSampleTableBO()
    {
        return new SampleTable(daoFactory, EXAMPLE_SESSION, null, entityOperationChecker, null,
                managedPropertyEvaluatorFactory, null);
    }

    private final static SampleIdentifier getSharedSampleIdentifier(final String code)
    {
        return new SampleIdentifier(code);
    }

}
