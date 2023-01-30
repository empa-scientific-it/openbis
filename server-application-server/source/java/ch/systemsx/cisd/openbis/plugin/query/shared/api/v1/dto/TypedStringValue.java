/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto;

import java.io.Serializable;

/**
 * A bean that stores a primitive value as a string with a type. Used for JSON conversion.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TypedStringValue
{
    private QueryTableColumnDataType type;

    private String value;

    public TypedStringValue()
    {
    }

    public TypedStringValue(QueryTableColumnDataType type, String value)
    {
        this.type = type;
        this.value = value;
    }

    public QueryTableColumnDataType getType()
    {
        return type;
    }

    public void setType(QueryTableColumnDataType type)
    {
        this.type = type;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Serializable toSerializable()
    {
        Serializable serializableValue = null;
        switch (type)
        {
            case DOUBLE:
                serializableValue = Double.parseDouble(value);
                break;
            case LONG:
                serializableValue = Long.parseLong(value);
                break;
            case STRING:
                serializableValue = value;
                break;

        }

        return serializableValue;
    }

    @Override
    public String toString()
    {
        return type + "(" + value + ")";
    }

}
