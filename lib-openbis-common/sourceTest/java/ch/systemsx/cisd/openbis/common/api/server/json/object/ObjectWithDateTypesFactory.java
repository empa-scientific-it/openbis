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
public class ObjectWithDateTypesFactory extends ObjectFactory<ObjectWithDateTypes>
{

    private static final long DAY_IN_MILLIS = 24 * 3600 * 1000;

    public static final String TYPE = "ObjectWithDateTypes";

    public static final String CLASS = ".LegacyObjectWithDateTypes";

    public static final String UTIL_DATE = "utilDate";

    public static final java.util.Date UTIL_DATE_VALUE = new java.util.Date(DAY_IN_MILLIS);

    public static final String SQL_DATE = "sqlDate";

    public static final java.sql.Date SQL_DATE_VALUE = new java.sql.Date(DAY_IN_MILLIS * 2);

    public static final String SQL_TIMESTAMP = "sqlTimestamp";

    public static final java.sql.Timestamp SQL_TIMESTAMP_VALUE = new java.sql.Timestamp(
            DAY_IN_MILLIS * 3);

    @Override
    public ObjectWithDateTypes createObject()
    {
        ObjectWithDateTypes object = new ObjectWithDateTypes();
        object.utilDate = UTIL_DATE_VALUE;
        object.sqlDate = SQL_DATE_VALUE;
        object.sqlTimestamp = SQL_TIMESTAMP_VALUE;
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(UTIL_DATE, UTIL_DATE_VALUE);
        map.putField(SQL_DATE, SQL_DATE_VALUE);
        map.putField(SQL_TIMESTAMP, SQL_TIMESTAMP_VALUE);
        return map.toMap();
    }

}
