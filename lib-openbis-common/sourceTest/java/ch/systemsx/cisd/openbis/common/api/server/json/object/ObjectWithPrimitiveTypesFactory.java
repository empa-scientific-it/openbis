/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
public class ObjectWithPrimitiveTypesFactory extends ObjectFactory<ObjectWithPrimitiveTypes>
{

    public static final String TYPE = "ObjectWithPrimitiveTypes";

    public static final String CLASS = ".LegacyObjectWithPrimitiveTypes";

    public static final String STRING_FIELD = "stringField";

    public static final String STRING_FIELD_VALUE = "stringValue";

    public static final String INTEGER_OBJECT_FIELD = "integerObjectField";

    public static final Integer INTEGER_OBJECT_FIELD_VALUE = Integer.valueOf(1);

    public static final String FLOAT_OBJECT_FIELD = "floatObjectField";

    public static final Float FLOAT_OBJECT_FIELD_VALUE = Float.valueOf(2.5f);

    public static final String DOUBLE_OBJECT_FIELD = "doubleObjectField";

    public static final Double DOUBLE_OBJECT_FIELD_VALUE = Double.valueOf(3.5d);

    public static final String INTEGER_FIELD = "integerField";

    public static final int INTEGER_FIELD_VALUE = 4;

    public static final String FLOAT_FIELD = "floatField";

    public static final float FLOAT_FIELD_VALUE = 5.5f;

    public static final String DOUBLE_FIELD = "doubleField";

    public static final double DOUBLE_FIELD_VALUE = 6.5d;

    @Override
    public ObjectWithPrimitiveTypes createObject()
    {
        ObjectWithPrimitiveTypes object = new ObjectWithPrimitiveTypes();
        object.stringField = STRING_FIELD_VALUE;
        object.integerObjectField = INTEGER_OBJECT_FIELD_VALUE;
        object.floatObjectField = FLOAT_OBJECT_FIELD_VALUE;
        object.doubleObjectField = DOUBLE_OBJECT_FIELD_VALUE;
        object.integerField = INTEGER_FIELD_VALUE;
        object.floatField = FLOAT_FIELD_VALUE;
        object.doubleField = DOUBLE_FIELD_VALUE;
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(STRING_FIELD, STRING_FIELD_VALUE);
        map.putField(INTEGER_OBJECT_FIELD, INTEGER_OBJECT_FIELD_VALUE);
        map.putField(FLOAT_OBJECT_FIELD, FLOAT_OBJECT_FIELD_VALUE);
        map.putField(DOUBLE_OBJECT_FIELD, DOUBLE_OBJECT_FIELD_VALUE);
        map.putField(INTEGER_FIELD, INTEGER_FIELD_VALUE);
        map.putField(FLOAT_FIELD, FLOAT_FIELD_VALUE);
        map.putField(DOUBLE_FIELD, DOUBLE_FIELD_VALUE);
        return map.toMap();
    }

}
