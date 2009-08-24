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

import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;

/**
 * @author Franz-Josef Elmer
 */
public class DatabaseInstanceValidatorTest extends AuthorizationTestCase
{
    @Test
    public void testIsValidWithTheRightDatabaseInstance()
    {
        PersonPE person = createPersonWithRoleAssignments();
        DatabaseInstanceValidator validator = new DatabaseInstanceValidator();
        assertEquals(true, validator.isValid(person, DatabaseInstanceTranslator
                .translate(createDatabaseInstance())));
    }

    @Test
    public void testIsValidWithTheWrongDatabaseInstance()
    {
        PersonPE person = createPersonWithRoleAssignments();
        DatabaseInstanceValidator validator = new DatabaseInstanceValidator();
        assertEquals(false, validator.isValid(person, DatabaseInstanceTranslator
                .translate(createDatabaseInstance("blabla"))));
    }

}
