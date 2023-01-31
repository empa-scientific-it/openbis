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

package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

/**
 * @author Pawel Glyzewski
 */
public class SortableFilterableTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = -7363632554479395042L;

    private static enum Direction
    {
        ASC, DESC
    }

    private static class RowComparator implements Comparator<String[]>
    {
        private final int columnNumber;

        private final boolean reverse;

        private RowComparator(int columnNumber, boolean reverse)
        {
            this.columnNumber = columnNumber;
            this.reverse = reverse;
        }

        @Override
        public int compare(String[] o1, String[] o2)
        {
            if (reverse)
            {
                return -o1[columnNumber].compareTo(o2[columnNumber]);

            } else
            {
                return o1[columnNumber].compareTo(o2[columnNumber]);
            }
        }
    }

    private int sortByColumn = -1;

    private Direction sortingOrder = Direction.ASC;

    private final String[] headers;

    private final List<String[]> data;

    private List<String[]> viewData;

    private String filter = "";

    public SortableFilterableTableModel(List<String[]> data, String[] headers)
    {
        this.data = data;
        this.headers = headers;
    }

    @Override
    public String getColumnName(int col)
    {
        return headers[col];
    }

    @Override
    public int getRowCount()
    {
        if (viewData != null)
        {
            return viewData.size();
        } else
        {
            return data.size();
        }
    }

    @Override
    public int getColumnCount()
    {
        return headers.length;
    }

    public void filter(@SuppressWarnings("hiding") String filter)
    {
        this.filter = "(?i)" + filter;
        Pattern pattern;

        try
        {
            pattern = Pattern.compile(this.filter);
        } catch (RuntimeException e)
        {
            pattern = Pattern.compile(".*");
        }
        viewData = new ArrayList<String[]>();
        for (String[] row : data)
        {
            boolean matches = false;
            for (String value : row)
            {
                matches = matches || pattern.matcher(value).find();
                if (matches)
                {
                    viewData.add(row);
                    break;
                }
            }
        }

        if (sortByColumn > -1)
        {
            Collections.sort(viewData, new RowComparator(sortByColumn,
                    Direction.ASC != sortingOrder));
        }

        fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (viewData != null)
        {
            return viewData.get(rowIndex)[columnIndex];
        } else
        {
            return data.get(rowIndex)[columnIndex];
        }
    }

    public boolean isAscending(int column)
    {
        return Direction.ASC == sortingOrder;
    }

    public boolean isSorting(int column)
    {
        return column == sortByColumn;
    }

    public synchronized void toggleSort(int column)
    {
        if (column == sortByColumn)
        {
            switch (sortingOrder)
            {
                case ASC:
                    sortingOrder = Direction.DESC;
                    break;
                case DESC:
                    sortByColumn = -1;
                    break;
            }
        } else
        {
            sortByColumn = column;
            sortingOrder = Direction.ASC;
        }

        filter(filter);
    }
}
