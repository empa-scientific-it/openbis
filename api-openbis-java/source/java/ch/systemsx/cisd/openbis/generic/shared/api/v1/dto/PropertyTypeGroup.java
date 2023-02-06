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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Immutable value object representing a data set type.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("PropertyTypeGroup")
public final class PropertyTypeGroup implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new data set instance. Necessary since all the fields of a DataSet are final.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class PropertyTypeGroupInitializer
    {
        private String name;

        private ArrayList<PropertyType> propertyTypes = new ArrayList<PropertyType>();

        public String getName()
        {
            return name;
        }

        public void setName(String code)
        {
            this.name = code;
        }

        public ArrayList<PropertyType> getPropertyTypes()
        {
            return propertyTypes;
        }

        public void addPropertyType(PropertyType propertyType)
        {
            propertyTypes.add(propertyType);
        }
    }

    private String name;

    private ArrayList<PropertyType> propertyTypes;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public PropertyTypeGroup(PropertyTypeGroupInitializer initializer)
    {
        this.name = initializer.getName();

        this.propertyTypes = initializer.getPropertyTypes();
    }

    /**
     * Returns the name of this group (section). May be null.
     */
    public String getName()
    {
        return name;
    }

    public List<PropertyType> getPropertyTypes()
    {
        return propertyTypes;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof PropertyTypeGroup == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        PropertyTypeGroup other = (PropertyTypeGroup) obj;
        builder.append(getName(), other.getName());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getName());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getName());
        builder.append(getPropertyTypes());
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private PropertyTypeGroup()
    {
    }

    private void setName(String name)
    {
        this.name = name;
    }

    private void setPropertyTypes(ArrayList<PropertyType> propertyTypes)
    {
        this.propertyTypes = propertyTypes;
    }
}
