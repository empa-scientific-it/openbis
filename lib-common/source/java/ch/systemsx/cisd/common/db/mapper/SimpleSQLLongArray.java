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

import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

/**
 * A simple implementation of {@link java.sql.Array} for <code>long[]</code> and {@link Set} of {@link Long} which only supports {@link #getArray()}
 * for retrieval.
 * 
 * @author Bernd Rinn
 */
class SimpleSQLLongArray extends AbstractSQLArray
{

    private final long[] value;

    SimpleSQLLongArray(long[] array)
    {
        value = array;
    }

    SimpleSQLLongArray(Set<Long> set)
    {
        if (set instanceof LongSet)
        {
            value = ((LongSet) set).toLongArray();
        } else
        {
            value = new long[set.size()];
            int idx = 0;
            for (Long l : set)
            {
                value[idx++] = l;
            }
        }
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
     * @return BIGINT
     */
    @Override
    public int getBaseType()
    {
        return Types.BIGINT;
    }

    /**
     * Get the base type name of this array.
     * 
     * @return "int8"
     */
    @Override
    public String getBaseTypeName()
    {
        return "int8";
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
        builder.append('{');
        for (long v : value)
        {
            builder.append(Long.toString(v));
            builder.append(',');
        }
        if (value.length > 0)
        {
            builder.setLength(builder.length() - 1);
        }
        builder.append('}');
        return builder.toString();
    }

}
