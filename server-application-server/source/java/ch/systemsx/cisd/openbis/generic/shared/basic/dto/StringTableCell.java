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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * Table cell wrapping a string.
 * 
 * @author Franz-Josef Elmer
 */
public class StringTableCell implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final StringTableCell EMPTY_CELL = new StringTableCell("");

    public static StringTableCell wrap(String stringOrNull)
    {
        return StringUtils.isBlank(stringOrNull) ? EMPTY_CELL : new StringTableCell(stringOrNull);
    }

    private String string;

    public StringTableCell(String string)
    {
        if (string == null)
        {
            throw new IllegalArgumentException("Unspecified string.");
        }
        this.string = string;
    }

    @Override
    public int compareTo(ISerializableComparable o)
    {
        return string.compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj
                || (obj instanceof StringTableCell && string.equals(((StringTableCell) obj).string));
    }

    @Override
    public int hashCode()
    {
        return string.hashCode();
    }

    @Override
    public String toString()
    {
        return string;
    }

    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private StringTableCell()
    {
    }
}
