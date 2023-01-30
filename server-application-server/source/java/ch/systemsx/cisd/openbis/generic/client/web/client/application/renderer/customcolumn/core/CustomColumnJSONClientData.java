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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.customcolumn.core;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.customcolumn.core.CustomColumnMethod;

/**
 * @author pkupczyk
 */
public class CustomColumnJSONClientData
{

    private CustomColumnMethod method;

    private JSONObject params;

    public CustomColumnJSONClientData(CustomColumnMethod method, JSONObject params)
    {
        if (method == null)
        {
            throw new IllegalArgumentException("Method was null");
        }
        this.method = method;
        this.params = params;
    }

    public CustomColumnMethod getMethod()
    {
        return method;
    }

    public JSONObject getParams()
    {
        return params;
    }

    public String getStringParam(String name)
    {
        if (getParams() == null)
        {
            return null;
        }
        JSONValue value = getParams().get(name);
        if (value == null || value.isNull() != null || value.isString() == null)
        {
            return null;
        } else
        {
            return ((JSONString) value).stringValue();
        }
    }

    public JSONObject getObjectParam(String name)
    {
        if (getParams() == null)
        {
            return null;
        }
        JSONValue value = getParams().get(name);
        if (value == null || value.isNull() != null || value.isObject() == null)
        {
            return null;
        } else
        {
            return (JSONObject) value;
        }

    }

}
