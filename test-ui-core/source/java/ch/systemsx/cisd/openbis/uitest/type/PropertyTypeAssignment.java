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

package ch.systemsx.cisd.openbis.uitest.type;

/**
 * @author anttil
 */
public abstract class PropertyTypeAssignment
{

    public abstract PropertyType getPropertyType();

    public abstract EntityType getEntityType();

    public abstract boolean isMandatory();

    public abstract String getInitialValue();

    public abstract Script getScript();

    @Override
    public final boolean equals(Object o)
    {
        if (o instanceof PropertyTypeAssignment)
        {
            PropertyTypeAssignment assignment = (PropertyTypeAssignment) o;
            return assignment.getPropertyType().getCode().equalsIgnoreCase(
                    getPropertyType().getCode())
                    &&
                    assignment.getEntityType().getCode()
                            .equalsIgnoreCase(getEntityType().getCode());
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        return (getPropertyType().getCode().toUpperCase() + "/" + getEntityType().getCode()
                .toUpperCase()).hashCode();
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " between " + getPropertyType() + " and "
                + getEntityType();
    }
}
