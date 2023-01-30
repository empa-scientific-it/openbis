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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * @author Franz-Josef Elmer
 */
public class ExternalDataValidatorTest extends AuthorizationTestCase
{
    private PhysicalDataSet createData(SpacePE space)
    {
        PhysicalDataSet data = new PhysicalDataSet();
        data.setExperiment(ExperimentTranslator.translate(createExperiment(space),
                "http://someURL", null, new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null));
        return data;
    }

    private PhysicalDataSet createDataForSample(SpacePE space)
    {
        PhysicalDataSet data = new PhysicalDataSet();
        data.setSample(SampleTranslator.translate(createSample(space),
                "http://someURL", null, new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null));
        return data;
    }

    @Test
    public void testIsValidWithDataInTheRightGroup()
    {
        ExternalDataValidator validator = new ExternalDataValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, createData(createAnotherSpace())));
    }

    @Test
    public void testIsValidWithDataForSampleInTheRightGroup()
    {
        ExternalDataValidator validator = new ExternalDataValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, createDataForSample(createAnotherSpace())));
    }

    @Test
    public void testIsValidWithDataInTheRightDatabaseInstance()
    {
        ExternalDataValidator validator = new ExternalDataValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, createData(createSpace())));
    }

}
