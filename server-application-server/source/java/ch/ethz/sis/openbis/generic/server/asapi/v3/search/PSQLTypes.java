/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * PostgreSQL types and their Java counterparts.
 */
public enum PSQLTypes
{

    VARCHAR("varchar"),

    TIMESTAMP_WITHOUT_TZ("timestamp"),

    TIMESTAMP_WITH_TZ("timestamptz"),

    DATE("date"),

    BOOLEAN("boolean"),

    CHARACTER("character"),

    FLOAT8("float8"),

    FLOAT4("float4"),

    INT2("int2"),

    INT4("int4"),

    INT8("int8"),

    NUMERIC("numeric");

    private static final Map<Class<?>, PSQLTypes> JAVA_TO_SQL_TYPE_MAP = new HashMap<>();

    private static final Map<Class<?>, Set<PSQLTypes>> JAVA_TO_COMPATIBLE_SQL_TYPES_MAP = new HashMap<>();

    private static final Map<String, Class<?>> SQL_TYPE_TO_JAVA_MAP = new HashMap<>();

    static
    {
        JAVA_TO_SQL_TYPE_MAP.put(String.class, VARCHAR);
        JAVA_TO_SQL_TYPE_MAP.put(Date.class, TIMESTAMP_WITH_TZ);
        JAVA_TO_SQL_TYPE_MAP.put(boolean.class, BOOLEAN);
        JAVA_TO_SQL_TYPE_MAP.put(Boolean.class, BOOLEAN);
        JAVA_TO_SQL_TYPE_MAP.put(char.class, CHARACTER);
        JAVA_TO_SQL_TYPE_MAP.put(Character.class, CHARACTER);
        JAVA_TO_SQL_TYPE_MAP.put(double.class, FLOAT8);
        JAVA_TO_SQL_TYPE_MAP.put(Double.class, FLOAT8);
        JAVA_TO_SQL_TYPE_MAP.put(float.class, FLOAT4);
        JAVA_TO_SQL_TYPE_MAP.put(Float.class, FLOAT4);
        JAVA_TO_SQL_TYPE_MAP.put(byte.class, INT2);
        JAVA_TO_SQL_TYPE_MAP.put(Byte.class, INT2);
        JAVA_TO_SQL_TYPE_MAP.put(short.class, INT2);
        JAVA_TO_SQL_TYPE_MAP.put(Short.class, INT2);
        JAVA_TO_SQL_TYPE_MAP.put(int.class, INT4);
        JAVA_TO_SQL_TYPE_MAP.put(Integer.class, INT4);
        JAVA_TO_SQL_TYPE_MAP.put(long.class, INT8);
        JAVA_TO_SQL_TYPE_MAP.put(Long.class, INT8);

        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(String.class, EnumSet.of(VARCHAR));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Date.class, EnumSet.of(TIMESTAMP_WITH_TZ));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(boolean.class, EnumSet.of(BOOLEAN));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Boolean.class, EnumSet.of(BOOLEAN));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(char.class, EnumSet.of(CHARACTER, VARCHAR));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Character.class, EnumSet.of(CHARACTER, VARCHAR));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(double.class, EnumSet.of(FLOAT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Double.class, EnumSet.of(FLOAT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(float.class, EnumSet.of(FLOAT4, FLOAT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Float.class, EnumSet.of(FLOAT4, FLOAT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(byte.class, EnumSet.of(INT2, INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Byte.class, EnumSet.of(INT2, INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(short.class, EnumSet.of(INT2, INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Short.class, EnumSet.of(INT2, INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(int.class, EnumSet.of(INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Integer.class, EnumSet.of(INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(long.class, EnumSet.of(INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Long.class, EnumSet.of(INT8));

        SQL_TYPE_TO_JAVA_MAP.put("smallint", Short.class);
        SQL_TYPE_TO_JAVA_MAP.put("int2", Short.class);
        SQL_TYPE_TO_JAVA_MAP.put("smallserial", Short.class);
        SQL_TYPE_TO_JAVA_MAP.put("serial2", Short.class);

        SQL_TYPE_TO_JAVA_MAP.put("integer", Integer.class);
        SQL_TYPE_TO_JAVA_MAP.put("int", Integer.class);
        SQL_TYPE_TO_JAVA_MAP.put("int4", Integer.class);
        SQL_TYPE_TO_JAVA_MAP.put("serial", Integer.class);
        SQL_TYPE_TO_JAVA_MAP.put("serial4", Integer.class);

        SQL_TYPE_TO_JAVA_MAP.put("bigint", Long.class);
        SQL_TYPE_TO_JAVA_MAP.put("int8", Long.class);
        SQL_TYPE_TO_JAVA_MAP.put("bigserial", Long.class);
        SQL_TYPE_TO_JAVA_MAP.put("serial8", Long.class);
        SQL_TYPE_TO_JAVA_MAP.put("pg_lsn", Long.class);

        SQL_TYPE_TO_JAVA_MAP.put("money", Float.class);
        SQL_TYPE_TO_JAVA_MAP.put("real", Float.class);
        SQL_TYPE_TO_JAVA_MAP.put("float4", Float.class);

        SQL_TYPE_TO_JAVA_MAP.put("double precision", Double.class);
        SQL_TYPE_TO_JAVA_MAP.put("float8", Double.class);
        SQL_TYPE_TO_JAVA_MAP.put("numeric", Double.class);
        SQL_TYPE_TO_JAVA_MAP.put("decimal", Double.class);

        SQL_TYPE_TO_JAVA_MAP.put("bit", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("bit varying", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("varbit", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("character", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("char", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("character varying", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("varchar", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("cidr", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("inet", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("macaddr", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("json", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("jsonb", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("text", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("tsquery", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("tsvector", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("txid_snapshot", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("uuid", String.class);
        SQL_TYPE_TO_JAVA_MAP.put("xml", String.class);

        SQL_TYPE_TO_JAVA_MAP.put("bytea", Byte[].class);

        SQL_TYPE_TO_JAVA_MAP.put("boolean", Boolean.class);
        SQL_TYPE_TO_JAVA_MAP.put("bool", Boolean.class);

        SQL_TYPE_TO_JAVA_MAP.put("date", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("interval", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("time", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("time without time zone", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("time with time zone", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("timetz", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("timestamp", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("timestamp without time zone", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("timestamp with time zone", Date.class);
        SQL_TYPE_TO_JAVA_MAP.put("timestamptz", Date.class);

        SQL_TYPE_TO_JAVA_MAP.put("box", Object.class);
        SQL_TYPE_TO_JAVA_MAP.put("circle", Object.class);
        SQL_TYPE_TO_JAVA_MAP.put("line", Object.class);
        SQL_TYPE_TO_JAVA_MAP.put("lseg", Object.class);
        SQL_TYPE_TO_JAVA_MAP.put("path", Object.class);
        SQL_TYPE_TO_JAVA_MAP.put("point", Object.class);
        SQL_TYPE_TO_JAVA_MAP.put("polygon", Object.class);
    }

    private String name;

    PSQLTypes(final String name)
    {
        this.name = name;
    }

    public static PSQLTypes javaClassToSQLType(final Class<?> klass)
    {
        return JAVA_TO_SQL_TYPE_MAP.get(klass);
    }

    public static Class<?> javaClassToSQLType(final PSQLTypes type)
    {
        return SQL_TYPE_TO_JAVA_MAP.get(type);
    }

    /**
     * Returns a set of SQL types compatible with the given Java class, i.e. which can be stored in the DB with no loss. Strings are
     * excluded, so that they are only assignable from strings (char is an exception).
     *
     * @param klass the class to which compatible SQL types should be found.
     * @return a set of SQL types which correspond to the given Java type.
     */
    public static Set<PSQLTypes> javaClassToCompatibleForSaveSQLTypes(final Class<?> klass)
    {
        return JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.get(klass);
    }

    public static Class<?> sqlTypeToJavaClass(final String sqlType) {
        return SQL_TYPE_TO_JAVA_MAP.get(sqlType);
    }

    @Override
    public String toString()
    {
        return name;
    }

}
