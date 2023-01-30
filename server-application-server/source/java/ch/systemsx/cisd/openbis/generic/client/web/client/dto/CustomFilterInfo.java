/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;

/**
 * Stores information about selected custom filter.
 * 
 * @author Izabela Adamczyk
 */
public class CustomFilterInfo<T> implements IsSerializable
{
    private String name;

    private String expression;

    private Set<ParameterWithValue> parameters;

    public CustomFilterInfo()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public Set<ParameterWithValue> getParameters()
    {
        return parameters;
    }

    public void setParameters(Set<ParameterWithValue> parameters)
    {
        this.parameters = parameters;
    }

}
