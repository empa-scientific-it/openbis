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

import java.util.Map;

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedTypes.ObjectNested;

/**
 * @author pkupczyk
 */
public class ObjectNestedFactory extends ObjectFactory<ObjectNested>
{

    public static final String TYPE = "ObjectNested";

    public static final String CLASS = ".LegacyObjectNested";

    public static final String NESTED = "nested";

    public static final String NESTED_VALUE = "nestedValue";

    @Override
    public ObjectNested createObject()
    {
        ObjectNested object = new ObjectNested();
        object.nested = NESTED_VALUE;
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(NESTED, NESTED_VALUE);
        return map.toMap();
    }

}
