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

import static ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeFactory.BASE;
import static ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeFactory.BASE_VALUE;

import java.util.Map;

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
public class ObjectWithKnownButNotUniqueClassFactory extends ObjectFactory<Object>
{

    public static final String TYPE = "ObjectWithKnownButNotUniqueClass";

    public static final String CLASS = ".LegacyObjectWithTypeA";

    public static final String A = "a";

    public static final String A_VALUE = "aValue";

    @Override
    public ObjectWithKnownButNotUniqueClass createObjectToSerialize()
    {
        ObjectWithKnownButNotUniqueClass object = new ObjectWithKnownButNotUniqueClass();
        object.base = BASE_VALUE;
        object.a = A_VALUE;
        return object;
    }

    @Override
    public Map<String, Object> createExpectedMapAfterSerialization(ObjectCounter objectCounter)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, ObjectType.TYPE);
        map.putField(BASE, BASE_VALUE);
        map.putField(A, A_VALUE);
        return map.toMap();
    }

    @Override
    public Object createMapToDeserialize(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putType(TYPE, CLASS, ObjectType.CLASS);
        map.putField(BASE, BASE_VALUE);
        map.putField(A, A_VALUE);
        return map.toMap();
    }

    @Override
    public Object createExpectedObjectAfterDeserialization()
    {
        ObjectMap map = new ObjectMap();
        map.putType(TYPE, CLASS, ObjectType.CLASS);
        map.putField(BASE, BASE_VALUE);
        map.putField(A, A_VALUE);
        return map.toMap();
    }

}
