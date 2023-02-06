/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * @author pkupczyk
 */
@JsonIgnoreType
public class ObjectToString implements Serializable
{

    private static final long serialVersionUID = 1L;

    private ToStringBuilder builder;

    public ObjectToString(Object object)
    {
        builder = new ToStringBuilder(object, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public ObjectToString append(String field, Object value)
    {
        builder.append(field, value);
        return this;
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

}
