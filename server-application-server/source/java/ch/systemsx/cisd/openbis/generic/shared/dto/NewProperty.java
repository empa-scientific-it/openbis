/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * @author Izabela Adamczyk
 */
public class NewProperty implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    String property;

    Serializable value;

    public NewProperty()
    {
    }

    public NewProperty(String name, Serializable value)
    {
        this.property = name;
        this.value = value;
    }

    public String getPropertyCode()
    {
        return property;
    }

    @BeanProperty(label = "property", optional = false)
    public void setPropertyCode(String name)
    {
        this.property = name;
    }

    public String getValue()
    {
        return getPropertyAsString(value);
    }

    @BeanProperty(label = "value", optional = false)
    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof NewProperty)
        {
            NewProperty that = (NewProperty) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(property, that.property);
            builder.append(value, that.value);
            return builder.isEquals();
        }
        return false;
    }

    private String getPropertyAsString(Serializable propertyValue) {
        if(propertyValue == null) {
            return null;
        } else {
            if(propertyValue.getClass().isArray()) {
                Serializable[] values = (Serializable[]) propertyValue;
                StringBuilder builder = new StringBuilder("[");
                for(Serializable value : values) {
                    if(builder.length() > 1) {
                        builder.append(", ");
                    }
                    builder.append(value);
                }
                builder.append("]");
                return builder.toString();
            } else {
                return (String) propertyValue;
            }
        }
    }

}
