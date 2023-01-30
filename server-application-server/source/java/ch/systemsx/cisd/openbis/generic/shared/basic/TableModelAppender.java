/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * @author pkupczyk
 */
public class TableModelAppender
{

    private boolean first = true;

    private List<TableModelColumnHeader> headers = new ArrayList<TableModelColumnHeader>();

    private List<TableModelRow> rows = new ArrayList<TableModelRow>();

    public void append(TableModel tableModel)
    {
        if (tableModel == null)
        {
            throw new IllegalArgumentException("Table model cannot be null");
        }

        if (first)
        {
            if (tableModel.getHeader() != null)
            {
                headers.addAll(tableModel.getHeader());
            }
            if (tableModel.getRows() != null)
            {
                rows.addAll(tableModel.getRows());
            }
            first = false;
        } else
        {
            checkColumnCount(tableModel);
            checkColumnIds(tableModel);
            checkColumnTypes(tableModel);
            rows.addAll(tableModel.getRows());
        }
    }

    private void checkColumnCount(TableModel tableModel)
    {
        int expectedCount = headers.size();
        int appendedCount = tableModel.getHeader() != null ? tableModel.getHeader().size() : 0;

        if (expectedCount != appendedCount)
        {
            throw new TableModelWithDifferentColumnCountException(expectedCount, appendedCount);
        }
    }

    private void checkColumnIds(TableModel tableModel)
    {
        List<String> expectedIds = new ArrayList<String>();
        List<String> appendedIds = new ArrayList<String>();

        for (TableModelColumnHeader expectedHeader : headers)
        {
            expectedIds.add(expectedHeader.getId());
        }

        if (tableModel.getHeader() != null)
        {
            for (TableModelColumnHeader appendedHeader : tableModel.getHeader())
            {
                appendedIds.add(appendedHeader.getId());
            }
        }

        if (expectedIds.equals(appendedIds) == false)
        {
            throw new TableModelWithDifferentColumnIdsException(expectedIds, appendedIds);
        }
    }

    private void checkColumnTypes(TableModel tableModel)
    {
        List<DataTypeCode> expectedTypes = new ArrayList<DataTypeCode>();
        List<DataTypeCode> appendedTypes = new ArrayList<DataTypeCode>();

        for (TableModelColumnHeader expectedHeader : headers)
        {
            expectedTypes.add(expectedHeader.getDataType());
        }

        if (tableModel.getHeader() != null)
        {
            for (TableModelColumnHeader appendedHeader : tableModel.getHeader())
            {
                appendedTypes.add(appendedHeader.getDataType());
            }
        }

        Iterator<DataTypeCode> expectedTypeIter = expectedTypes.iterator();
        Iterator<DataTypeCode> appendedTypeIter = appendedTypes.iterator();

        while (expectedTypeIter.hasNext() && appendedTypeIter.hasNext())
        {
            DataTypeCode expectedType = expectedTypeIter.next();
            DataTypeCode appendedType = appendedTypeIter.next();

            if (expectedType != null && appendedType != null
                    && expectedType.equals(appendedType) == false)
            {
                throw new TableModelWithIncompatibleColumnTypesException(expectedTypes, appendedTypes);
            }
        }
    }

    public TableModel toTableModel()
    {
        return new TableModel(headers, rows);
    }

    public static class IllegalTableModelException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

    }

    public static class TableModelWithDifferentColumnCountException extends IllegalTableModelException
    {

        private static final long serialVersionUID = 1L;

        private int expectedColumnCount;

        private int appendedColumnCount;

        public TableModelWithDifferentColumnCountException(int expectedColumnCount, int appendedColumnCount)
        {
            this.expectedColumnCount = expectedColumnCount;
            this.appendedColumnCount = appendedColumnCount;
        }

        public int getExpectedColumnCount()
        {
            return expectedColumnCount;
        }

        public int getAppendedColumnCount()
        {
            return appendedColumnCount;
        }

    }

    public static class TableModelWithDifferentColumnIdsException extends IllegalTableModelException
    {

        private static final long serialVersionUID = 1L;

        private List<String> expectedColumnIds;

        private List<String> appendedColumnIds;

        public TableModelWithDifferentColumnIdsException(List<String> expectedColumnIds, List<String> appendedColumnIds)
        {
            this.expectedColumnIds = expectedColumnIds;
            this.appendedColumnIds = appendedColumnIds;
        }

        public List<String> getExpectedColumnIds()
        {
            return expectedColumnIds;
        }

        public List<String> getAppendedColumnIds()
        {
            return appendedColumnIds;
        }

    }

    public static class TableModelWithIncompatibleColumnTypesException extends IllegalTableModelException
    {

        private static final long serialVersionUID = 1L;

        private List<DataTypeCode> expectedColumnTypes;

        private List<DataTypeCode> appendedColumnTypes;

        public TableModelWithIncompatibleColumnTypesException(List<DataTypeCode> expectedColumnTypes, List<DataTypeCode> appendedColumnTypes)
        {
            this.expectedColumnTypes = expectedColumnTypes;
            this.appendedColumnTypes = appendedColumnTypes;
        }

        public List<DataTypeCode> getExpectedColumnTypes()
        {
            return expectedColumnTypes;
        }

        public List<DataTypeCode> getAppendedColumnTypes()
        {
            return appendedColumnTypes;
        }

    }

}
