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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.structured;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.BasicPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.JythonManagedPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyFunctions;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;

/**
 * @author Kaloyan Enimanev
 */
public class StructuredPropertyConverterPythonTest extends AssertJUnit
{

    private static final String SCRIPT_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/openbis/generic/shared/managed_property/structured/";

    /**
     * test the API for creating {@link IElement} is usable from Jython.
     */
    @Test
    public void testAPIUsageFromJython()
    {
        IManagedProperty managedProperty = new ManagedEntityProperty(new EntityProperty());
        String script =
                CommonTestUtils.getResourceAsString(SCRIPT_FOLDER, "structured-property-test.py");
        JythonManagedPropertyEvaluator evaluator = new JythonManagedPropertyEvaluator(script);

        evaluator.configureUI(managedProperty, new BasicPropertyAdaptor("CODE", "value",
                new SamplePropertyPE()));

        // the script will create several elements and serialize them in the property value
        List<IElement> elements =
                ManagedPropertyFunctions.propertyConverter().convertToElements(managedProperty);

        assertEquals(3, elements.size());
    }
}
