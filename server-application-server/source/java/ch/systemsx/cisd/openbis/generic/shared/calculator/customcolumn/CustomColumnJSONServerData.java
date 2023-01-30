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

package ch.systemsx.cisd.openbis.generic.shared.calculator.customcolumn;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.customcolumn.core.CustomColumnMethod;

/**
 * @author pkupczyk
 */
public class CustomColumnJSONServerData
{

    private CustomColumnMethod method;

    private Map<String, Object> methodParams;

    public CustomColumnJSONServerData(CustomColumnMethod method)
    {
        if (method == null)
        {
            throw new IllegalArgumentException("Method was null");
        }
        this.method = method;
    }

    public CustomColumnMethod getMethod()
    {
        return method;
    }

    public void addParam(String name, Object value)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name was null");
        }
        if (methodParams == null)
        {
            methodParams = new HashMap<String, Object>();
        }
        methodParams.put(name, value);
    }

    public Map<String, Object> getParams()
    {
        return methodParams;
    }
}
