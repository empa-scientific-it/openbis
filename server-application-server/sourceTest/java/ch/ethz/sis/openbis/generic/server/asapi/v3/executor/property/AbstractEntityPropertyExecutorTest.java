/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.AbstractExecutorTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;

/**
 * @author pkupczyk
 */
public abstract class AbstractEntityPropertyExecutorTest extends AbstractExecutorTest
{

    public static EntityTypePE createEntityType()
    {
        SampleTypePE entityType = new SampleTypePE();
        entityType.setCode("TEST_ENTITY_TYPE");
        return entityType;
    }

    public static PropertyTypePE createPropertyType(String propertyCode)
    {
        final DataTypePE dataType = new DataTypePE();
        dataType.setCode(DataTypeCode.VARCHAR);

        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setType(dataType);
        propertyType.setCode(propertyCode);

        return propertyType;
    }

    public static EntityTypePropertyTypePE createEntityTypePropertyType(String propertyCode)
    {
        final SampleTypePropertyTypePE entityTypePropertyType = new SampleTypePropertyTypePE();
        entityTypePropertyType.setPropertyType(createPropertyType(propertyCode));

        return entityTypePropertyType;
    }

    public static EntityPropertyPE createEntityProperty(String propertyCode, String propertyValue)
    {
        final SamplePropertyPE entityProperty = new SamplePropertyPE();
        entityProperty.setEntityTypePropertyType(createEntityTypePropertyType(propertyCode));
        entityProperty.setValue(propertyValue);

        return entityProperty;
    }

    public static class PropertyValuesMatcher extends TypeSafeMatcher<Set<? extends EntityPropertyPE>>
    {

        private Set<? extends EntityPropertyPE> expectedProperties;

        public PropertyValuesMatcher(Set<? extends EntityPropertyPE> expectedProperties)
        {
            this.expectedProperties = expectedProperties;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Expected properties " + expectedProperties);
        }

        @Override
        public boolean matchesSafely(Set<? extends EntityPropertyPE> actualProperties)
        {
            Map<String, String> expectedValues = getValues(expectedProperties);
            Map<String, String> actualValues = getValues(actualProperties);
            return expectedValues.equals(actualValues);
        }

        private Map<String, String> getValues(Set<? extends EntityPropertyPE> properties)
        {
            Map<String, String> values = new HashMap<String, String>();

            for (EntityPropertyPE property : properties)
            {
                String code = property.getEntityTypePropertyType().getPropertyType().getCode();
                String value = property.getValue();
                values.put(code, value);
            }

            return values;
        }

    }

}
