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
package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Set;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import org.apache.log4j.Logger;

class PlaceholderPropertyCreator implements IPropertyPlaceholderCreator
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PlaceholderPropertyCreator.class);

    /**
     * Adds placeholders for <var>dynamicProperties</var> to <var>definedProperties</var> if they
     * don't exist yet.
     */
    @Override
    public void addDynamicPropertiesPlaceholders(Set<IEntityProperty> definedProperties,
            Set<String> dynamicProperties)
    {
        addPlaceholders(definedProperties, dynamicProperties,
                BasicConstant.DYNAMIC_PROPERTY_PLACEHOLDER_VALUE);
    }

    /**
     * Adds placeholders for <var>managedProperties</var> to <var>definedProperties</var> if they
     * don't exist yet.
     */
    @Override
    public void addManagedPropertiesPlaceholders(Set<IEntityProperty> definedProperties,
            Set<String> managedProperties)
    {
        addPlaceholders(definedProperties, managedProperties,
                BasicConstant.MANAGED_PROPERTY_PLACEHOLDER_VALUE);
    }

    /**
     * Adds <var>placeholderProperties</var> with specified <var>placeholderValue</var>to
     * <var>definedProperties</var> if they don't exist yet.
     */
    private void addPlaceholders(Set<IEntityProperty> definedProperties,
            Set<String> placeholderProperties, String placeholderValue)
    {
        for (String p : placeholderProperties)
        {
            if (definedProperties.stream()
                    .anyMatch(x -> x.getPropertyType().getCode().equals(p)) == false)
            {
                definedProperties.forEach(operationLog::info);
                final IEntityProperty entityProperty = new EntityProperty();
                entityProperty.setValue(placeholderValue);
                PropertyType propertyType = new PropertyType();
                propertyType.setCode(p);
                entityProperty.setPropertyType(propertyType);
                definedProperties.add(entityProperty);
                definedProperties.forEach(operationLog::info);
            }
        }
    }

}