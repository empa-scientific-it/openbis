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

import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.customcolumn.core.CustomColumnMethod;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.customcolumn.core.CustomColumnParam;

/**
 * @author pkupczyk
 */
public class CustomColumnJSONParser
{

    public static final CustomColumnJSONClientData parse(String str)
    {
        if (str == null || str.length() == 0)
        {
            return null;
        } else
        {
            try
            {
                JSONValue value = JSONParser.parseLenient(str);
                if (value == null || value.isObject() == null)
                {
                    return null;
                } else
                {
                    JSONObject object = (JSONObject) value;

                    try
                    {
                        return new CustomColumnJSONClientData(maybeGetMethod(object),
                                maybeGetParams(object));
                    } catch (CustomColumnJSONException e)
                    {
                        return null;
                    }

                }
            } catch (JSONException e)
            {
                return null;
            }
        }

    }

    private static final CustomColumnMethod maybeGetMethod(JSONObject object)
            throws CustomColumnJSONException
    {
        if (!object.containsKey(CustomColumnParam.$$__METHOD__$$.name()))
        {
            throw new CustomColumnJSONException("Custom column method not defined");
        }

        JSONValue methodValue = object.get(CustomColumnParam.$$__METHOD__$$.name());

        if (methodValue == null || methodValue.isNull() != null)
        {
            return null;
        } else
        {
            if (methodValue.isString() == null)
            {
                throw new CustomColumnJSONException("Custom column method is not a String");
            } else
            {
                JSONString methodString = (JSONString) methodValue;
                try
                {
                    return CustomColumnMethod.valueOf(methodString.stringValue());
                } catch (IllegalArgumentException e)
                {
                    throw new CustomColumnJSONException("Custom column method: "
                            + methodString.stringValue() + " is unknown");
                }
            }
        }
    }

    private static final JSONObject maybeGetParams(JSONObject object)
            throws CustomColumnJSONException
    {
        JSONValue paramsValue = object.get(CustomColumnParam.$$__PARAMS__$$.name());
        if (paramsValue == null || paramsValue.isNull() != null)
        {
            return null;
        } else
        {
            if (paramsValue.isObject() == null)
            {
                throw new CustomColumnJSONException("Custom column method params are not an Object");
            } else
            {
                return (JSONObject) paramsValue;
            }
        }
    }
}
