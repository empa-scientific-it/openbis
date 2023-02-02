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

package ch.systemsx.cisd.openbis.common.api.server.json.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * @author pkupczyk
 */
@SuppressWarnings("hiding")
public class ObjectMap
{

    private Integer objectId;

    private ObjectType objectType;

    private String type;

    private String clazz;

    private Map<String, Object> fields = new TreeMap<String, Object>();

    public void putId(ObjectCounter objectCounter)
    {
        if (objectCounter != null)
        {
            this.objectId = objectCounter.next();
        }
    }

    public void putType(String type, String clazz, ObjectType objectType)
    {
        this.type = type;
        this.clazz = clazz;
        this.objectType = objectType;
    }

    public void putField(String name, Object value)
    {
        fields.put(name, value);
    }

    public Map<String, Object> toMap()
    {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        if (ObjectType.TYPE.equals(objectType))
        {
            map.put("@type", type);
        }
        if (ObjectType.CLASS.equals(objectType))
        {
            map.put("@class", clazz);
        }
        if (objectId != null)
        {
            map.put("@id", objectId);
        }

        map.putAll(fields);
        return map;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }

}
