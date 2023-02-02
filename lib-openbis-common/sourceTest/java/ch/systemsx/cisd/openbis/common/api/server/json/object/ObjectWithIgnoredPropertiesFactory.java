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

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
public class ObjectWithIgnoredPropertiesFactory extends ObjectFactory<ObjectWithIgnoredProperties>
{

    public static final String TYPE = "ObjectWithIgnoredProperties";

    public static final String CLASS = ".LegacyObjectWithIgnoredProperties";

    public static final String PROPERTY = "property";

    public static final String PROPERTY_VALUE = "propertyValue";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER = "propertyWithGetterAndSetter";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_VALUE =
            "propertyWithGetterAndSetterValue";

    public static final String PROPERTY_IGNORED = "propertyIgnored";

    public static final String PROPERTY_IGNORED_VALUE = "propertyIgnoredValue";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_IGNORED =
            "propertyWithGetterAndSetterIgnored";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_IGNORED_VALUE =
            "propertyWithGetterAndSetterIgnoredValue";

    @Override
    public ObjectWithIgnoredProperties createObjectToSerialize()
    {
        ObjectWithIgnoredProperties object = new ObjectWithIgnoredProperties();
        object.property = PROPERTY_VALUE;
        object.propertyIgnored = PROPERTY_IGNORED_VALUE;
        object.setPropertyWithGetterAndSetter(PROPERTY_WITH_GETTER_AND_SETTER_VALUE);
        object.setPropertyWithGetterAndSetterIgnored(PROPERTY_WITH_GETTER_AND_SETTER_IGNORED_VALUE);
        return object;
    }

    @Override
    public Object createExpectedMapAfterSerialization(ObjectCounter objectCounter)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, ObjectType.TYPE);
        map.putField(PROPERTY, PROPERTY_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER, PROPERTY_WITH_GETTER_AND_SETTER_VALUE);
        return map.toMap();
    }

    @Override
    public Object createMapToDeserialize(ObjectCounter objectCounter,
            ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(PROPERTY, PROPERTY_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER, PROPERTY_WITH_GETTER_AND_SETTER_VALUE);
        map.putField(PROPERTY_IGNORED, PROPERTY_IGNORED_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER_IGNORED,
                PROPERTY_WITH_GETTER_AND_SETTER_IGNORED_VALUE);
        return map.toMap();
    }

    @Override
    public ObjectWithIgnoredProperties createExpectedObjectAfterDeserialization()
    {
        ObjectWithIgnoredProperties object = new ObjectWithIgnoredProperties();
        object.property = PROPERTY_VALUE;
        object.setPropertyWithGetterAndSetter(PROPERTY_WITH_GETTER_AND_SETTER_VALUE);
        return object;
    }

}
