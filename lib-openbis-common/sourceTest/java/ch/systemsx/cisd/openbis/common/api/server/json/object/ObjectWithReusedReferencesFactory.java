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
public class ObjectWithReusedReferencesFactory extends ObjectFactory<ObjectWithReusedReferences>
{

    public static final String TYPE = "ObjectWithReusedReferences";

    public static final String CLASS = ".LegacyObjectWithReusedReferences";

    public static final String REFERENCE_1 = "reference1";

    public static final String REFERENCE_2 = "reference2";

    @Override
    public ObjectWithReusedReferences createObject()
    {
        ObjectWithReusedReferences object = new ObjectWithReusedReferences();

        ObjectWithType reference = new ObjectWithTypeFactory().createObject();
        object.reference1 = reference;
        object.reference2 = reference;

        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);

        Integer referenceId = objectCounter.current();
        Object reference = new ObjectWithTypeFactory().createMap(objectCounter, objectType);

        map.putField(REFERENCE_1, reference);
        map.putField(REFERENCE_2, referenceId);

        return map.toMap();
    }

}
