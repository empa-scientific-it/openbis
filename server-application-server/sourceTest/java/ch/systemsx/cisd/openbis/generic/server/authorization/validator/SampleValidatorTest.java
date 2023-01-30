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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * Test cases for {@link SampleValidator}.
 * 
 * @author Izabela Adamczyk
 */
public class SampleValidatorTest extends AuthorizationTestCase
{
    private static final String BASE_URL = "baseUrl";

    @Test
    public void testIsValidWithTheRightGroup()
    {
        SampleValidator validator = new SampleValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, SampleTranslator.translate(
                createSample(createAnotherSpace()), BASE_URL, null,
                new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null)));
    }

    @Test
    public void testIsValidSharedWithTheRightInstance()
    {
        SampleValidator validator = new SampleValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, SampleTranslator.translate(
                createSample(), BASE_URL, null,
                new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null)));
    }

    @Test
    public void testIsValidWithTheRightDatabaseInstance()
    {
        SampleValidator validator = new SampleValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, SampleTranslator.translate(
                createSample(createSpace()), BASE_URL, null, new ManagedPropertyEvaluatorFactory(
                        null, new TestJythonEvaluatorPool()), null)));
    }
}
