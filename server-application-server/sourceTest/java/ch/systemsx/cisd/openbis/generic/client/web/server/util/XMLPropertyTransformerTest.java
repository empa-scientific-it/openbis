/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Franz-Josef Elmer
 */
public class XMLPropertyTransformerTest extends AssertJUnit
{
    private static final String XSLT =
            "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
                    + "<xsl:template match='/'><b><xsl:value-of select='.'/></b></xsl:template>"
                    + "</xsl:stylesheet>";

    @Test
    public void test()
    {
        Sample sample = new Sample();
        IEntityProperty p1 = createProperty(DataTypeCode.XML, "<root>hello world</root>", XSLT);
        IEntityProperty p2 = createProperty(DataTypeCode.XML, "<root>hello earth</root>", null);
        IEntityProperty p3 = createProperty(DataTypeCode.VARCHAR, "hello", null);
        sample.setProperties(Arrays.<IEntityProperty> asList(p1, p2, p3));

        new XMLPropertyTransformer().transformXMLProperties(Arrays.asList(sample));

        IEntityProperty transformed1 = sample.getProperties().get(0);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><b>hello world</b>",
                transformed1.tryGetAsString());
        assertEquals("<root>hello world</root>", transformed1.tryGetOriginalValue());
        IEntityProperty transformed2 = sample.getProperties().get(1);
        assertEquals("<root>hello earth</root>", transformed2.tryGetAsString());
        assertEquals("<root>hello earth</root>", transformed2.tryGetOriginalValue());
        IEntityProperty transformed3 = sample.getProperties().get(2);
        assertEquals("hello", transformed3.tryGetAsString());
        assertEquals("hello", transformed3.tryGetOriginalValue());
    }

    private GenericEntityProperty createProperty(DataTypeCode dataSetTypeCode, String value,
            String xsltOrNull)
    {
        GenericEntityProperty property = new GenericEntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setDataType(new DataType(dataSetTypeCode));
        propertyType.setTransformation(xsltOrNull);
        property.setPropertyType(propertyType);
        property.setValue(value);
        return property;
    }
}
