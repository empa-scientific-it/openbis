/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IStructuredPropertyConverter;

/**
 * @author Kaloyan Enimanev
 */
public abstract class AbstractStructuredPropertyConverterTest extends AssertJUnit
{

    protected IElementFactory factory = new ElementFactory();

    protected IStructuredPropertyConverter converter;

    @BeforeClass
    public void fixture()
    {
        converter = createConverter();
    }

    protected abstract IStructuredPropertyConverter createConverter();

    protected static final class ValueManagedProperty implements IManagedProperty
    {
        private static final long serialVersionUID = 1L;

        private final String value;

        ValueManagedProperty(String value)
        {
            this.value = value;
        }

        @Override
        public String getPropertyTypeCode()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isOwnTab()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOwnTab(boolean ownTab)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSpecialValue()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getValue()
        {
            return value;
        }

        @Override
        public void setValue(Serializable value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public IManagedUiDescription getUiDescription()
        {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testNoNestedElements()
    {

        final List<IElement> elements = createElementsNoNestedElements();

        final String persistentValue = converter.convertToString(elements);
        final List<IElement> deserialized =
                converter.convertToElements(new ValueManagedProperty(persistentValue));

        assertEquals(elements, deserialized);
    }

    protected List<IElement> createElementsNoNestedElements()
    {
        final List<IElement> elements =
                Arrays.asList(
                        factory.createElement("testname").addAttribute("attr1", "value1")
                                .addAttribute("attr2", "value2").setData("dummy&<>data"),
                        factory.createSampleLink("permIdSample").addAttribute("sampleAttrKey",
                                "sampleAttrVal"),
                        factory.createMaterialLink("materialCode", "typeCode").addAttribute(
                                "materialAttrKey", "materialAttrVal")
                        );
        return elements;
    }

    @Test
    public void testWithNestedElements()
    {

        final List<IElement> elements = createElementsWithNestedElements();

        final String persistentValue = converter.convertToString(elements);
        final List<IElement> deserialized =
                converter.convertToElements(new ValueManagedProperty(persistentValue));

        assertEquals(elements, deserialized);
    }

    protected List<IElement> createElementsWithNestedElements()
    {
        final List<IElement> elements =
                Arrays.asList(
                        factory.createElement("name1")
                                .addAttribute("attr1", "value1")
                                .addChildren(factory.createSampleLink("nestedPermId1"),
                                        factory.createMaterialLink("code1", "typeCode1")),
                        factory.createSampleLink("permIdSample").addChildren(
                                factory.createElement("nested1").addAttribute("na1", "nav2")),
                        factory.createMaterialLink("materialCode", "typeCode").addChildren(
                                factory.createElement("nested2").addAttribute("na2", "nav2")));
        return elements;
    }
}
