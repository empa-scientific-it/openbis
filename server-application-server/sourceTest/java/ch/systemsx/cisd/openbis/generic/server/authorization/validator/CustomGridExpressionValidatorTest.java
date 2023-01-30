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

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator.GridCustomFilterTranslator;

/**
 * Test cases for corresponding {@link ExpressionValidator} class.
 * 
 * @author Piotr Buczek
 */
@Friend(toClasses = ExpressionValidator.class)
public final class CustomGridExpressionValidatorTest extends AuthorizationTestCase
{
    @Test
    public final void testWithPublicFilter()
    {
        final PersonPE person = createPerson("A");
        final PersonPE registrator = createPerson("B");
        final boolean isPublic = true;
        final GridCustomFilterPE filter = createFilter(registrator, isPublic);
        final ExpressionValidator validator = new ExpressionValidator();
        assertEquals(true, validator.isValid(person, GridCustomFilterTranslator.translate(filter)));
    }

    @Test
    public final void testWithTheRightRegistrator()
    {
        // registrators are equal when they have the same userId AND db instance
        final PersonPE person = createPerson("A");
        final PersonPE registrator = person;
        final boolean isPublic = false;
        final GridCustomFilterPE filter = createFilter(registrator, isPublic);
        final ExpressionValidator validator = new ExpressionValidator();
        assertEquals(true, validator.isValid(person, GridCustomFilterTranslator.translate(filter)));
    }

    @Test
    public final void testWithTheRightInstanceAdmin()
    {
        final PersonPE person = createPerson("A");
        assignRoles(person);
        final PersonPE registrator = createPerson("B");
        final boolean isPublic = false;
        final GridCustomFilterPE filter = createFilter(registrator, isPublic);
        final ExpressionValidator validator = new ExpressionValidator();
        assertEquals(true, validator.isValid(person, GridCustomFilterTranslator.translate(filter)));
    }
}
