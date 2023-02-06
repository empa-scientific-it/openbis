/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Common attributes of all entity types.
 *
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
public class EntityType implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String description;

    private ValidationPluginInfo validationPluginInfo;

    private List<PropertyTypeGroup> propertyTypeGroups;

    EntityType(EntityTypeInitializer initializer)
    {
        InitializingChecks.checkValidString(initializer.getCode(), "Unspecified code.");
        code = initializer.getCode();
        description = initializer.getDescription();
        validationPluginInfo = initializer.getValidationPluginInfo();
        propertyTypeGroups = Collections.unmodifiableList(initializer.getPropertyTypeGroups());
    }

    /**
     * Returns the unique entity type code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the description or <code>null</code> if undefined.
     */
    public String getDescription()
    {
        return description;
    }

    public ValidationPluginInfo getValidationPluginInfo()
    {
        return validationPluginInfo;
    }

    /**
     * Return the grouped property types for this entity type. (Groups are referred to as sections elsewhere).
     */
    public List<PropertyTypeGroup> getPropertyTypeGroups()
    {
        return propertyTypeGroups;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityType == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        EntityType other = (EntityType) obj;
        builder.append(getCode(), other.getCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    //
    // JSON-RPC
    //

    EntityType()
    {
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setDescription(String description)
    {
        this.description = description;
    }

    private void setValidationPluginInfo(ValidationPluginInfo validationPluginInfo)
    {
        this.validationPluginInfo = validationPluginInfo;
    }

    private void setPropertyTypeGroups(ArrayList<PropertyTypeGroup> propertyTypeGroups)
    {
        this.propertyTypeGroups = propertyTypeGroups;
    }
}
