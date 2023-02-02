/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.db.mapper;

import java.sql.SQLException;
import java.sql.Types;

/**
 * A simple implementation of {@link java.sql.Array} for <code>String[]</code>.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SimpleSQLStringArray extends AbstractSQLArray
{

    private final String[] value;

    SimpleSQLStringArray(String[] array)
    {
        value = array;
    }

    /**
     * Get the object array.
     * 
     * @return the object array
     */
    @Override
    public Object getArray()
    {
        return value;
    }

    /**
     * Get the base type of this array.
     * 
     * @return VARCHAR
     */
    @Override
    public int getBaseType()
    {
        return Types.VARCHAR;
    }

    /**
     * Get the base type name of this array.
     * 
     * @return "varchar"
     */
    @Override
    public String getBaseTypeName()
    {
        return "varchar";
    }

    @Override
    public void free() throws SQLException
    {
        // do nothing
    }

    @Override
    /*
     * * For the PostgreSQL JDBC driver to work with this class, this method needs to return '{x,y,z,...}'.
     */
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (String v : value)
        {
            builder.append("\"");
            builder.append(v);
            builder.append("\"");
            builder.append(',');
        }
        // Remove the trailing ','
        if (value.length > 0)
        {
            builder.setLength(builder.length() - 1);
        }
        builder.append("}");
        return builder.toString();
    }

}
