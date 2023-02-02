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
public class ObjectWithRenamedPropertiesFactory extends ObjectFactory<ObjectWithRenamedProperties>
{

    public static final String TYPE = "ObjectWithRenamedProperties";

    public static final String CLASS = ".LegacyObjectWithRenamedProperties";

    public static final String PROPERTY = "property";

    public static final String PROPERTY_VALUE = "propertyValue";

    public static final String PROPERTY_RENAMED = "propertyRenamed";

    public static final String PROPERTY_RENAMED_VALUE = "propertyRenamedValue";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER = "propertyWithGetterAndSetter";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_VALUE =
            "propertyWithGetterAndSetterValue";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_RENAMED =
            "propertyWithGetterAndSetterRenamed";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_RENAMED_VALUE =
            "propertyWithGetterAndSetterRenamedValue";

    @Override
    public ObjectWithRenamedProperties createObject()
    {
        ObjectWithRenamedProperties object = new ObjectWithRenamedProperties();
        object.property = PROPERTY_VALUE;
        object.x = PROPERTY_RENAMED_VALUE;
        object.setPropertyWithGetterAndSetter(PROPERTY_WITH_GETTER_AND_SETTER_VALUE);
        object.setY(PROPERTY_WITH_GETTER_AND_SETTER_RENAMED_VALUE);
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(PROPERTY, PROPERTY_VALUE);
        map.putField(PROPERTY_RENAMED, PROPERTY_RENAMED_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER, PROPERTY_WITH_GETTER_AND_SETTER_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER_RENAMED,
                PROPERTY_WITH_GETTER_AND_SETTER_RENAMED_VALUE);
        return map.toMap();
    }

}
