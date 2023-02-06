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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Represents value together with {@code isModified} flag
 * 
 * @author Jakub Straszewski
 */
@JsonObject("as.dto.common.update.FieldUpdateValue")
public class FieldUpdateValue<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private boolean isModified = false;

    @JsonProperty
    private T value;

    /**
     * value for update
     */
    @JsonIgnore
    public void setValue(T value)
    {
        this.value = value;
        this.isModified = true;
    }

    /**
     * @return {@code true} if the value has been set for update.
     */
    @JsonIgnore
    public boolean isModified()
    {
        return isModified;
    }

    /**
     * @return value for update
     */
    @JsonIgnore
    public T getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        if (isModified())
        {
            return new ObjectToString(this).append("value", value).toString();
        } else
        {
            return new ObjectToString(this).toString();
        }
    }

}