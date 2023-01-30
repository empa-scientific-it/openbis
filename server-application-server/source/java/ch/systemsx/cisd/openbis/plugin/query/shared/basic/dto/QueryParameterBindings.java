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

package ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Encapsulation of bindings of values to parameters used in a query.
 * 
 * @author Piotr Buczek
 */
public class QueryParameterBindings implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Map<String, String> bindings = new HashMap<String, String>();

    public Map<String, String> getBindings()
    {
        return bindings;
    }

    public void setBindings(Map<String, String> bindings)
    {
        this.bindings = bindings;
    }

    public void addBinding(String parameter, String value)
    {
        bindings.put(parameter, value);
    }

    public void addBinding(ParameterWithValue parameterWithValue)
    {
        bindings.put(parameterWithValue.getParameter(), parameterWithValue.getValue());
    }

    public String tryGetParameterValue(String parameter)
    {
        return bindings.get(parameter);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : bindings.entrySet())
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
