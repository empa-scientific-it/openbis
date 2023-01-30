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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;

/**
 * error test cases for {@link ElementFactory}.
 * 
 * @author Kaloyan Enimanev
 */
public class ElementFactoryTest extends AssertJUnit
{

    private IElementFactory factory = new ElementFactory();

    @Test(expectedExceptions = AssertionError.class)
    public void testInvalidName()
    {
        factory.createElement("-name");

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDoNotAllowSampleTagCreation()
    {
        factory.createElement("Sample");

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDoNotAllowMaterialTagCreation()
    {
        factory.createElement("Material");

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetNonExistentAttribute()
    {
        IElement element = factory.createElement("test");
        element.getAttribute("non-existent");
    }

    @Test
    public void testGetAttributeWithDefaultValue()
    {
        IElement element = factory.createElement("test");

        String existentAttribute = "attr";
        String value = "value";
        element.addAttribute(existentAttribute, value);

        String defaultValue = "def_value";
        assertEquals(value, element.getAttribute(existentAttribute, defaultValue));
        assertEquals(defaultValue, element.getAttribute("non-existent", defaultValue));
    }

}
