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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.ITableDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * @author Franz-Josef Elmer
 */
public class TableDataProviderFactory
{
    private abstract static class AbstractTableDataProvider<T> implements ITableDataProvider
    {
        protected final List<IColumnDefinition<T>> availableColumns;

        protected Map<String, Integer> indexMap;

        public AbstractTableDataProvider(List<IColumnDefinition<T>> availableColumns)
        {
            this.availableColumns = availableColumns;
        }

        @Override
        public String tryToGetProperty(String columnID, String key)
        {
            return getDefinition(columnID).tryToGetProperty(key);
        }

        @Override
        public Comparable<?> getValue(String columnID, List<? extends Comparable<?>> rowValues)
        {
            Integer index = indexMap.get(columnID);
            if (index == null)
            {
                throw new IllegalArgumentException("Unknown column ID: " + columnID);
            }
            return rowValues.get(index);
        }

        @Override
        public Collection<String> getAllColumnIDs()
        {
            List<String> result = new ArrayList<String>();
            for (IColumnDefinition<T> columnDefinition : availableColumns)
            {
                result.add(columnDefinition.getIdentifier());
            }
            return result;
        }

        @Override
        public List<String> getAllColumnTitles()
        {
            List<String> result = new ArrayList<String>();
            for (IColumnDefinition<T> columnDefinition : availableColumns)
            {
                result.add(columnDefinition.getHeader());
            }
            return result;
        }

        protected void createIndexMap(List<IColumnDefinition<T>> definitions)
        {
            indexMap = new HashMap<String, Integer>();
            for (int i = 0; i < definitions.size(); i++)
            {
                IColumnDefinition<T> columnDefinition = definitions.get(i);
                indexMap.put(columnDefinition.getIdentifier(), i);
            }
        }

        protected IColumnDefinition<T> getDefinition(String columnID)
        {
            for (IColumnDefinition<T> definition : availableColumns)
            {
                if (definition.getIdentifier().equals(columnID))
                {
                    return definition;
                }
            }
            throw new IllegalArgumentException("Unknown column ID: " + columnID);
        }
    }

    private static final class TableDataProviderForGridRowModles<T> extends
            AbstractTableDataProvider<T>
    {
        private final GridRowModels<T> rows;

        private TableDataProviderForGridRowModles(List<IColumnDefinition<T>> availableColumns,
                GridRowModels<T> rows)
        {
            super(availableColumns);
            this.rows = rows;
        }

        @Override
        public List<List<? extends Comparable<?>>> getRows()
        {
            List<IColumnDefinition<T>> definitions =
                    new ArrayList<IColumnDefinition<T>>(availableColumns);
            createIndexMap(definitions);

            List<List<? extends Comparable<?>>> result =
                    new ArrayList<List<? extends Comparable<?>>>();
            for (GridRowModel<T> row : rows)
            {
                List<Comparable<?>> rowValues = new ArrayList<Comparable<?>>();
                for (IColumnDefinition<T> definition : definitions)
                {
                    Comparable<?> v = null;
                    // Dependency checker not allow code like
                    // if (definition instanceof GridCustomColumnDefinition)
                    if (definition.getClass().getSimpleName().contains("CustomColumn"))
                    {
                        String identifier = definition.getIdentifier();
                        try
                        {
                            v = row.findColumnValue(identifier);
                        } catch (IllegalStateException ex)
                        {
                            v = null;
                        }
                    } else
                    {
                        v = definition.tryGetComparableValue(row);
                    }
                    rowValues.add(v);
                }
                result.add(rowValues);
            }
            return result;
        }

    }

    private static final class TableDataProviderForListOfRows<T> extends
            AbstractTableDataProvider<T>
    {
        private final List<T> rows;

        private TableDataProviderForListOfRows(List<IColumnDefinition<T>> availableColumns,
                List<T> rows)
        {
            super(availableColumns);
            this.rows = rows;
        }

        @Override
        public List<List<? extends Comparable<?>>> getRows()
        {
            List<IColumnDefinition<T>> definitions =
                    new ArrayList<IColumnDefinition<T>>(availableColumns);
            createIndexMap(definitions);

            List<List<? extends Comparable<?>>> result =
                    new ArrayList<List<? extends Comparable<?>>>();
            for (T row : rows)
            {
                List<Comparable<?>> rowValues = new ArrayList<Comparable<?>>();
                for (IColumnDefinition<T> definition : definitions)
                {
                    Comparable<?> value = null;
                    try
                    {
                        value =
                                definition.tryGetComparableValue(GridRowModel
                                        .createWithoutCustomColumns(row));
                    } catch (Exception ex)
                    {
                        // ignored because this happens hopefully only if definition is a custom
                        // column definition
                    }
                    rowValues.add(value);
                }
                result.add(rowValues);
            }
            return result;
        }

    }

    static <T> ITableDataProvider createDataProvider(final List<T> rows,
            final List<IColumnDefinition<T>> availableColumns)
    {
        return new TableDataProviderForListOfRows<T>(availableColumns, rows);
    }

    public static <T> ITableDataProvider createDataProvider(final GridRowModels<T> rows,
            final List<IColumnDefinition<T>> availableColumns)
    {
        return new TableDataProviderForGridRowModles<T>(availableColumns, rows);
    }
}
