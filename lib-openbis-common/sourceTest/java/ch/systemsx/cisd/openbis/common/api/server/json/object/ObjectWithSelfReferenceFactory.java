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

/**
 * @author pkupczyk
 */
public class ObjectWithSelfReferenceFactory extends ObjectFactory<ObjectWithSelfReference>
{

    public static final String TYPE = "ObjectWithSelfReference";

    public static final String CLASS = ".LegacyObjectWithSelfReference";

    public static final String SELF_REFERENCE = "selfReference";

    @Override
    public ObjectWithSelfReference createObject()
    {
        ObjectWithSelfReference object = new ObjectWithSelfReference();
        object.selfReference = object;
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        int objectId = objectCounter.current();
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(SELF_REFERENCE, objectId);
        return map.toMap();
    }

}
