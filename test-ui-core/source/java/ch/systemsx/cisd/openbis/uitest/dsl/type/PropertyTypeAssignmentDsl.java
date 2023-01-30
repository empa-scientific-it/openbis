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

import ch.systemsx.cisd.openbis.uitest.type.EntityType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.Script;

/**
 * @author anttil
 */
class PropertyTypeAssignmentDsl extends PropertyTypeAssignment
{

    private PropertyType propertyType;

    private EntityType entityType;

    private boolean mandatory;

    private String initialValue;

    private Script script;

    public PropertyTypeAssignmentDsl(PropertyType propertyType, EntityType entityType,
            boolean mandatory, String initialValue, Script script)
    {
        this.entityType = entityType;
        this.propertyType = propertyType;
        this.mandatory = mandatory;
        this.initialValue = initialValue;
        this.script = script;
    }

    @Override
    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    @Override
    public EntityType getEntityType()
    {
        return entityType;
    }

    @Override
    public boolean isMandatory()
    {
        return mandatory;
    }

    @Override
    public String getInitialValue()
    {
        return initialValue;
    }

    @Override
    public Script getScript()
    {
        return script;
    }

    @Override
    public String toString()
    {
        return "PropertyTypeAssignment [" + this.propertyType + ", " + this.entityType + "]";
    }

}
