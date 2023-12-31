/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * @author Piotr Buczek
 */
public class ParameterValue implements Comparable<ParameterValue>, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    String value;

    String description;

    public ParameterValue()
    {
    }

    public ParameterValue(String value, String description)
    {
        this.value = value;
        this.description = description;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return value + " (" + description + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ParameterValue == false)
        {
            return false;
        }
        ParameterValue other = (ParameterValue) obj;
        return this.toString().equals(other.toString());
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    //
    // Comparable
    //

    //
    // Comparable
    //

    @Override
    public int compareTo(ParameterValue o)
    {
        return toString().compareTo(o.toString());
    }

}
