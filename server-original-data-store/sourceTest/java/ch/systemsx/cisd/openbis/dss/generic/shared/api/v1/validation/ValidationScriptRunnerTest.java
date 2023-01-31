/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation;

import java.io.File;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class ValidationScriptRunnerTest extends AssertJUnit
{

    private static final String SCRIPTS_FOLDER =
            "../datastore_server/sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/shared/api/v1/validation/";

    private static final String TEST_DATA_FOLDER = SCRIPTS_FOLDER;

    public static final String BASIC_VALIDATION_SCRIPT = SCRIPTS_FOLDER
            + "basic-validation-script.py";

    public static final String SPLITTED_VALIDATION_SCRIPT_1 = SCRIPTS_FOLDER
            + "validation_script_1_of_2.py";

    public static final String SPLITTED_VALIDATION_SCRIPT_2 = SCRIPTS_FOLDER
            + "validation_script_2_of_2.py";

    public static final String VALID_DATA_SET = TEST_DATA_FOLDER + "/valid-data-set";

    public static final String INVALID_DATA_SET = TEST_DATA_FOLDER + "/invalid-data-set";

    @Test
    public void testBasicValidationOnValidDataSet()
    {
        ValidationScriptRunner scriptRunner =
                ValidationScriptRunner.createValidatorFromScriptPaths(new String[]
                { BASIC_VALIDATION_SCRIPT });
        List<ValidationError> errors = scriptRunner.validate(new File(VALID_DATA_SET));

        assertTrue("The valid data set should have no errors", errors.isEmpty());
    }

    @Test
    public void testMultipleScriptsValidationOnValidDataSet()
    {
        ValidationScriptRunner scriptRunner =
                ValidationScriptRunner.createValidatorFromScriptPaths(new String[]
                { SPLITTED_VALIDATION_SCRIPT_1, SPLITTED_VALIDATION_SCRIPT_2 });
        List<ValidationError> errors = scriptRunner.validate(new File(VALID_DATA_SET));

        assertTrue("The valid data set should have no errors", errors.isEmpty());
    }

    @Test
    public void testBasicValidationOnInvalidDataSet()
    {
        ValidationScriptRunner scriptRunner =
                ValidationScriptRunner.createValidatorFromScriptPaths(new String[]
                { BASIC_VALIDATION_SCRIPT });
        List<ValidationError> errors = scriptRunner.validate(new File(INVALID_DATA_SET));

        assertEquals(1, errors.size());
        ValidationError error = errors.get(0);
        assertEquals("No file named valid-file.txt was found in invalid-data-set",
                error.getErrorMessage());
    }

    @Test
    public void testMultipleScriptsValidationOnInvalidDataSet()
    {
        ValidationScriptRunner scriptRunner =
                ValidationScriptRunner.createValidatorFromScriptPaths(new String[]
                { SPLITTED_VALIDATION_SCRIPT_1, SPLITTED_VALIDATION_SCRIPT_2 });
        List<ValidationError> errors = scriptRunner.validate(new File(INVALID_DATA_SET));

        assertEquals(1, errors.size());
        ValidationError error = errors.get(0);
        assertEquals("No file named valid-file.txt was found in invalid-data-set",
                error.getErrorMessage());
    }
}
