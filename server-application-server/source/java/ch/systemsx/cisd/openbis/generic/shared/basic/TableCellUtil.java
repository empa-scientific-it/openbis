/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;

/**
 * Utility functions for table cells.
 * 
 * @author Franz-Josef Elmer
 */
public class TableCellUtil
{
    public static final String USER_PREFIX = "USER-";

    public static final String INTERN_PREFIX = "INTERN-";

    /**
     * Returns an appropriate table cell for the specified string token. If it can be parsed as an integer number a {@link IntegerTableCell} is
     * returned. If it can be parsed as a floating point number a {@link DoubleTableCell} is returned. Otherwise a {@link StringTableCell} is
     * returned.
     */
    public static ISerializableComparable createTableCell(String token)
    {
        if (token == null)
        {
            return StringTableCell.EMPTY_CELL;
        }
        try
        {
            return new IntegerTableCell(Long.parseLong(token));
        } catch (NumberFormatException ex)
        {
            try
            {
                return new DoubleTableCell(Double.parseDouble(token));
            } catch (NumberFormatException ex1)
            {
                // ignored
            }
        }
        return new StringTableCell(token);
    }

    /**
     * Returns the code of specified property type with prefix <code>INTERN-</code> or <code>USER-</code> depending on whether it is internal name
     * space or not.
     */
    public static String getPropertyTypeCode(PropertyType propertyType)
    {
        return (propertyType.isManagedInternally() ? INTERN_PREFIX : USER_PREFIX)
                + propertyType.getSimpleCode();
    }

    public static boolean isEditiableProperty(PropertyType propertyType)
    {
        DataTypeCode dataType = propertyType.getDataType().getCode();
        switch (dataType)
        {
            case REAL:
            case INTEGER:
            case VARCHAR:
            case MULTILINE_VARCHAR:
            case BOOLEAN:
            case DATE:
            case TIMESTAMP:
            case CONTROLLEDVOCABULARY:
            case MATERIAL:
            case SAMPLE:
            case HYPERLINK:
                return true;
            case XML:
                return false;
        }
        throw new UnsupportedOperationException(""); // not possible
    }

    private TableCellUtil()
    {
    }

}
