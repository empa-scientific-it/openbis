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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Value object with name and optional description of a validation plugin.
 *
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("ValidationPluginInfo")
public class ValidationPluginInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    public ValidationPluginInfo(String name, String descriptionOrNull)
    {
        this.name = name;
        this.description = descriptionOrNull;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ValidationPluginInfo == false)
        {
            return false;
        }
        ValidationPluginInfo that = (ValidationPluginInfo) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.name, that.name);
        builder.append(this.description, that.description);
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(name);
        builder.append(description);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(name);
        builder.append(description);
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private ValidationPluginInfo()
    {
    }

    private void setName(String name)
    {
        this.name = name;
    }

    private void setDescription(String description)
    {
        this.description = description;
    }

}
