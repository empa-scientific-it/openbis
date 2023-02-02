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

package ch.systemsx.cisd.openbis.common.api.server.json.object;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject(ObjectWithRenamedPropertiesFactory.TYPE)
public class ObjectWithRenamedProperties
{

    public String property;

    private String propertyWithGetterAndSetter;

    @JsonProperty("propertyRenamed")
    public String x;

    private String y;

    public String getPropertyWithGetterAndSetter()
    {
        return propertyWithGetterAndSetter;
    }

    public void setPropertyWithGetterAndSetter(String propertyWithGetterAndSetter)
    {
        this.propertyWithGetterAndSetter = propertyWithGetterAndSetter;
    }

    public String getY()
    {
        return y;
    }

    @JsonProperty("propertyWithGetterAndSetterRenamed")
    public void setY(String y)
    {
        this.y = y;
    }

    @Override
    public int hashCode()
    {
        return 1;
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }

}
