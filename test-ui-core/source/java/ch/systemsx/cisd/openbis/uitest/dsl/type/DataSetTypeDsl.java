/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.Collection;

import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;

/**
 * @author anttil
 */
class DataSetTypeDsl extends DataSetType
{
    private final String code;

    private String description;

    private Collection<PropertyTypeAssignment> propertyTypeAssignments;

    public DataSetTypeDsl(String code, String description,
            Collection<PropertyTypeAssignment> propertyTypeAssignments)
    {
        this.code = code;
        this.description = description;
        this.propertyTypeAssignments = propertyTypeAssignments;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public Collection<PropertyTypeAssignment> getPropertyTypeAssignments()
    {
        return propertyTypeAssignments;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setPropertyTypeAssignments(Collection<PropertyTypeAssignment> propertyTypeAssignments)
    {
        this.propertyTypeAssignments = propertyTypeAssignments;
    }

    @Override
    public String toString()
    {
        return "DataSetType " + this.code;
    }
}
