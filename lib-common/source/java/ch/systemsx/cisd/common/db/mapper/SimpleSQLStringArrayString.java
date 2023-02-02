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

/**
 * A helper class for converting a <code>String[]</code> to a valid array String in SQL.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SimpleSQLStringArrayString
{

    private final String[] value;

    public SimpleSQLStringArrayString(String[] array)
    {
        value = array;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        for (String v : value)
        {
            builder.append("\'");
            builder.append(v);
            builder.append("\'");
            builder.append(',');
        }
        if (value.length > 0)
        {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

}
