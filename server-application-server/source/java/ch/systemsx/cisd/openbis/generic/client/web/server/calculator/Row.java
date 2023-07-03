/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermTableCell;

/**
 * Row object used in jython expressions to access column values.
 * <p>
 * All public methods of this class are part of the Filter/Calculated Column API.
 * 
 * @author Franz-Josef Elmer
 */
public final class Row
{
    private final Map<String, List<ColumnDefinition>> definitionsByProperties =
            new HashMap<String, List<ColumnDefinition>>();

    private final ITableDataProvider provider;

    private List<? extends Comparable<?>> rowValues;

    Row(ITableDataProvider provider)
    {
        this.provider = provider;
    }

    void setRowData(List<? extends Comparable<?>> rowValues)
    {
        this.rowValues = rowValues;
    }

    /**
     * Returns the value of the column specified by its ID.
     * 
     * @throws IllegalArgumentException if no column with specified ID exists.
     */
    public Object col(String columnID)
    {
        Comparable<?> value = provider.getValue(columnID, rowValues);
        if (value instanceof ISerializableComparable)
        {
            return ComparableCellValueHelper.unwrap((ISerializableComparable) value);
        } else
        {
            return value;
        }
    }

    /**
     * Returns the value of the column as <code>VocabularyColumn</code> object if the column is a vocabulary. If the value of the column is null or
     * the column is not a vocabulary, then it returns null.
     * 
     * @throws IllegalArgumentException if no column with specified ID exists.
     */
//    public VocabularyColumn colAsVocabulary(String columnID)
//    {
//        Comparable<?> value = provider.getValue(columnID, rowValues);
//
//        if (value instanceof VocabularyTermTableCell)
//        {
//            VocabularyTermTableCell cell = (VocabularyTermTableCell) value;
//
//            if (cell.getVocabularyTerm() != null)
//            {
//                return new VocabularyColumn(cell.getVocabularyTerm());
//            } else
//            {
//                return null;
//            }
//        } else
//        {
//            return null;
//        }
//    }

    /**
     * Returns all column definitions which have a property with specified key.
     * 
     * @param propertyKeyOrNull The key of the property. If <code>null</code> all column definitions are returned.
     * @return an empty list if no column definition found.
     */
    public List<ColumnDefinition> colDefs(String propertyKeyOrNull)
    {
        List<ColumnDefinition> definitions = definitionsByProperties.get(propertyKeyOrNull);
        if (definitions == null)
        {
            definitions = new ArrayList<ColumnDefinition>();
            Collection<String> columnIDs = provider.getAllColumnIDs();
            for (String columnID : columnIDs)
            {
                if (propertyKeyOrNull == null
                        || provider.tryToGetProperty(columnID, propertyKeyOrNull) != null)
                {
                    definitions.add(new ColumnDefinition(columnID, provider));
                }
            }
            definitionsByProperties.put(propertyKeyOrNull, definitions);
        }
        return Collections.unmodifiableList(definitions);
    }

    /**
     * Returns all column values where the column definition has a property with specified key and value.
     * 
     * @return an empty list if no columns found.
     */
    public List<Object> cols(String propertyKey, String propertyValue)
    {
        List<Object> values = new ArrayList<Object>();
        List<ColumnDefinition> definitions = colDefs(propertyKey);
        for (ColumnDefinition definition : definitions)
        {
            String property = definition.property(propertyKey);
            if (propertyValue.equals(property))
            {
                values.add(col(definition.id()));
            }
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Returns all column values grouped by the property value of the property specified by the key.
     * 
     * @return an empty list if no columns found.
     */
    public List<ColumnGroup> colsGroupedBy(String propertyKey)
    {
        Map<String, List<Object>> map = new LinkedHashMap<String, List<Object>>();
        List<ColumnDefinition> definitions = colDefs(propertyKey);
        for (ColumnDefinition definition : definitions)
        {
            String property = definition.property(propertyKey);
            List<Object> values = map.get(property);
            if (values == null)
            {
                values = new ArrayList<Object>();
                map.put(property, values);
            }
            values.add(col(definition.id()));
        }
        Set<Entry<String, List<Object>>> entrySet = map.entrySet();
        List<ColumnGroup> groups = new ArrayList<ColumnGroup>(entrySet.size());
        for (Entry<String, List<Object>> entry : entrySet)
        {
            groups.add(new ColumnGroup(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(groups);
    }

}