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

import java.util.ArrayList;

/**
 * Generic part of all entity type initializers. Such classes are used to create immutable entity type classes.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Franz-Josef Elmer
 */
public class EntityTypeInitializer
{
    private String code;

    private String description;

    private ValidationPluginInfo validationPluginInfo;

    private ArrayList<PropertyTypeGroup> propertyTypeGroups =
            new ArrayList<PropertyTypeGroup>();

    EntityTypeInitializer()
    {
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public ValidationPluginInfo getValidationPluginInfo()
    {
        return validationPluginInfo;
    }

    public void setValidationPluginInfo(ValidationPluginInfo validationPluginInfo)
    {
        this.validationPluginInfo = validationPluginInfo;
    }

    public ArrayList<PropertyTypeGroup> getPropertyTypeGroups()
    {
        return propertyTypeGroups;
    }

    public void addPropertyTypeGroup(PropertyTypeGroup propertyType)
    {
        propertyTypeGroups.add(propertyType);
    }

}
