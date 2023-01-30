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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType.DataSetTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType.PropertyTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup.PropertyTypeGroupInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetTypeTest extends AssertJUnit
{
    private static final String DATA_SET_TYPE_CODE = "dataSet-type";

    private DataSetType dataSetType;

    @BeforeMethod
    public void setUp()
    {
        DataSetTypeInitializer initializer = new DataSetTypeInitializer();
        initializer.setCode(DATA_SET_TYPE_CODE);
        initializer.setDescription("hello");
        initializer.setValidationPluginInfo(new ValidationPluginInfo("test", "validation test"));
        initializer.setMainDataSetPattern(".*");
        initializer.setMainDataSetPath("/a/b/c");
        PropertyTypeGroupInitializer groupInitializer = new PropertyTypeGroupInitializer();

        PropertyTypeInitializer propTypeInitializer = new PropertyTypeInitializer();
        propTypeInitializer.setDataType(DataTypeCode.VARCHAR);
        propTypeInitializer.setCode("PROP1");
        propTypeInitializer.setLabel("Property 1");
        groupInitializer.addPropertyType(new PropertyType(propTypeInitializer));

        propTypeInitializer.setCode("PROP2");
        propTypeInitializer.setLabel("Property 2");
        propTypeInitializer.setDescription("Property 2 Description");

        groupInitializer.addPropertyType(new PropertyType(propTypeInitializer));

        initializer.addPropertyTypeGroup(new PropertyTypeGroup(groupInitializer));
        dataSetType = new DataSetType(initializer);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testInitialization()
    {
        DataSetTypeInitializer initializer = new DataSetTypeInitializer();
        new DataSetType(initializer);
    }

    @Test
    public void testEquals()
    {
        DataSetTypeInitializer initializer = new DataSetTypeInitializer();
        initializer.setCode(DATA_SET_TYPE_CODE);
        PropertyTypeGroupInitializer groupInitializer = new PropertyTypeGroupInitializer();
        PropertyTypeInitializer propTypeInitializer = new PropertyTypeInitializer();
        propTypeInitializer.setDataType(DataTypeCode.VARCHAR);
        propTypeInitializer.setCode("PROP200");
        propTypeInitializer.setLabel("Property 200");
        groupInitializer.addPropertyType(new PropertyType(propTypeInitializer));
        initializer.addPropertyTypeGroup(new PropertyTypeGroup(groupInitializer));

        DataSetType myDataSetType = new DataSetType(initializer);
        assertTrue("Data sets with the same code should be equal.",
                dataSetType.equals(myDataSetType));
        assertEquals(dataSetType.hashCode(), myDataSetType.hashCode());

        initializer = new DataSetTypeInitializer();
        initializer.setCode(DATA_SET_TYPE_CODE);
        myDataSetType = new DataSetType(initializer);
        assertTrue("Data sets with the same code should be equal.",
                dataSetType.equals(myDataSetType));
        assertEquals(dataSetType.hashCode(), myDataSetType.hashCode());

        initializer = new DataSetTypeInitializer();
        initializer.setCode("code-2");
        groupInitializer = new PropertyTypeGroupInitializer();
        propTypeInitializer = new PropertyTypeInitializer();
        propTypeInitializer.setDataType(DataTypeCode.VARCHAR);
        propTypeInitializer.setCode("PROP1");
        propTypeInitializer.setLabel("Property 1");
        groupInitializer.addPropertyType(new PropertyType(propTypeInitializer));
        initializer.addPropertyTypeGroup(new PropertyTypeGroup(groupInitializer));

        propTypeInitializer.setCode("PROP2");
        propTypeInitializer.setLabel("Property 2");
        propTypeInitializer.setDescription("Property 2 Description");
        myDataSetType = new DataSetType(initializer);
        assertFalse("Data sets with the different ids should not be equal.",
                dataSetType.equals(myDataSetType));
    }

    @Test
    public void testToString()
    {
        String stringRepresentation = dataSetType.toString();
        assertEquals("DataSetType[dataSet-type,hello,deletionDisallowed=false,mainDataSetPattern=.*,"
                + "mainDataSetPath=/a/b/c,{PropertyTypeGroup[<null>,{PropertyType[VARCHAR,PROP1,"
                + "Property 1,<null>,optional],PropertyType[VARCHAR,PROP2,Property 2,"
                + "Property 2 Description,optional]}]}]",
                stringRepresentation);
    }
}
