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

import java.util.Map;

/**
 * @author pkupczyk
 */

public abstract class ObjectFactory<T>
{

    public T createObjectToSerialize()
    {
        return createObject();
    }

    public Object createExpectedMapAfterSerialization(ObjectCounter objectCounter)
    {
        return createMap(objectCounter, ObjectType.TYPE);
    }

    public Object createMapToDeserialize(ObjectCounter objectCounter, ObjectType objectType)
    {
        return createMap(objectCounter, objectType);
    }

    public T createExpectedObjectAfterDeserialization()
    {
        return createObject();
    }

    protected T createObject()
    {
        throw new UnsupportedOperationException();
    }

    protected Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        throw new UnsupportedOperationException();
    }

}
