/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimplePersonRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableCellUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermTableCell;

/**
 * Builder class for creating an instance of {@link TypedTableModel}.
 *
 * @author Franz-Josef Elmer
 */
public class TypedTableModelBuilder<T extends Serializable>
{
    private static final StringTableCell EMPTY_CELL = new StringTableCell("");

    private static final class ColumnMetaData implements IColumnMetaData
    {
        private final Column column;

        ColumnMetaData(Column column)
        {
            this.column = column;
        }

        @Override
        public IColumnMetaData withTitle(String title)
        {
            column.getHeader().setTitle(title);
            return this;
        }

        @Override
        public IColumnMetaData withDefaultWidth(int width)
        {
            column.getHeader().setDefaultColumnWidth(width);
            return this;
        }

        @Override
        public IColumnMetaData withDataType(DataTypeCode dataType)
        {
            column.getHeader().setDataType(dataType);
            return this;
        }

        @Override
        public IColumnMetaData hideByDefault()
        {
            column.getHeader().setHidden(true);
            return this;
        }

        @Override
        public IColumnMetaData editable()
        {
            column.getHeader().setEditable(true);
            return this;
        }

        @Override
        public IColumnMetaData linkEntitiesOnly()
        {
            column.getHeader().setLinkEntitiesOnly(true);
            return this;
        }

    }

    private static interface IIndexProvider
    {
        public int getIndex();
    }

    private static interface IColumnItem
    {
        public List<Column> getColumns();
    }

    private final class ColumnGroup implements IColumnGroup, IColumnItem
    {
        private final String groupKey;

        private final Set<Column> cols = new LinkedHashSet<TypedTableModelBuilder.Column>();

        private boolean uneditablePropertyColumns;

        private ColumnGroup(String groupKey)
        {
            this.groupKey = groupKey;
        }

        @Override
        public List<Column> getColumns()
        {
            return new ArrayList<TypedTableModelBuilder.Column>(cols);
        }

        @Override
        public IColumn column(String id)
        {
            Column column = getOrCreateColumn(id);
            cols.add(column);
            return column;
        }

        @Override
        public IColumnGroup uneditablePropertyColumns()
        {
            uneditablePropertyColumns = true;
            return this;
        }

        @Override
        public void addColumnsForAssignedProperties(EntityType entityType)
        {
            addColumnsForAssignedProperties(groupKey, entityType);
        }

        @Override
        public void addColumnsForAssignedProperties(String idPrefix, EntityType entityType)
        {
            List<? extends EntityTypePropertyType<?>> propertyTypes =
                    entityType.getAssignedPropertyTypes();
            if (propertyTypes != null)
            {
                for (EntityTypePropertyType<?> propertyType : propertyTypes)
                {
                    IColumn column = addColumn(idPrefix, propertyType.getPropertyType(), false);
                    column.property(entityType.getCode(), Boolean.TRUE.toString());
                    setEditableFlag(column, propertyType.getPropertyType());
                    setVocabulary(column, propertyType.getPropertyType().getVocabulary());
                }
            }
        }

        @Override
        public void addColumnsForPropertyTypes(List<PropertyType> propertyTypes)
        {
            addColumnsForPropertyTypes(groupKey, propertyTypes, false);
        }

        @Override
        public void addColumnsForPropertyTypesForUpdate(List<PropertyType> propertyTypes)
        {
            addColumnsForPropertyTypes(groupKey, propertyTypes, true);
        }

        @Override
        public void addColumnsForPropertyTypes(String idPrefix, List<PropertyType> propertyTypes,
                boolean forUpdate)
        {
            for (PropertyType propertyType : propertyTypes)
            {
                addColumn(idPrefix, propertyType, forUpdate);
            }
        }

        private IColumn addColumn(String idPrefix, PropertyType propertyType,
                boolean useOriginalPropertyTypeCode)
        {
            String label = propertyType.getLabel();
            String code =
                    idPrefix
                            + (useOriginalPropertyTypeCode ? propertyType.getCode() : TableCellUtil
                            .getPropertyTypeCode(propertyType));
            DataTypeCode dataType = propertyType.getDataType().getCode();
            IColumn column = column(code).withTitle(label).withDataType(dataType);
            return column;
        }

        @Override
        public void addProperties(Collection<IEntityProperty> properties)
        {
            addProperties(groupKey, properties);
        }

        @Override
        public void addProperties(String idPrefix, Collection<IEntityProperty> properties)
        {
            addProperties(idPrefix, properties, false);
        }

        @Override
        public void addPropertiesForUpdate(Collection<IEntityProperty> properties)
        {
            addProperties("", properties, true);
        }

        private void addProperties(String idPrefix, Collection<IEntityProperty> properties,
                boolean forUpdate)
        {
            for (IEntityProperty property : properties)
            {
                PropertyType propertyType = property.getPropertyType();
                IColumn column = addColumn(idPrefix, propertyType, forUpdate);
                DataTypeCode dataType = propertyType.getDataType().getCode();
                ISerializableComparable value;
                switch (dataType)
                {
                    case MATERIAL:
                        Material material = property.getMaterial();
                        if (material == null) // if not yet calculated dynamic property
                        {
                            value = new StringTableCell("");
                        } else if (forUpdate)
                        {
                            value = new StringTableCell(material.getIdentifier());
                        } else
                        {
                            value =
                                    new EntityTableCell(EntityKind.MATERIAL,
                                            material.getIdentifier());
                        }
                        break;
                    case CONTROLLEDVOCABULARY:
                        VocabularyTerm vocabularyTerm = property.getVocabularyTerm();
                        if (vocabularyTerm == null) // if not yet calculated dynamic property
                        {
                            value = new StringTableCell("");
                        } else if (forUpdate)
                        {
                            value = new StringTableCell(vocabularyTerm.getCode());
                        } else
                        {
                            value = new VocabularyTermTableCell(vocabularyTerm);
                        }
                        break;
                    default:
                        if (forUpdate)
                        {
                            value = new StringTableCell(property.tryGetAsString());
                        } else
                        {
                            value = DataTypeUtils.convertTo(dataType, property.tryGetAsString());
                        }
                }
                setEditableFlag(column, propertyType);
                setDynamicFlag(column, property);
                column.addValue(value);
            }
        }

        private void setEditableFlag(IColumn column, PropertyType propertyType)
        {
            if (uneditablePropertyColumns)
            {
                return;
            }
            if (TableCellUtil.isEditiableProperty(propertyType))
            {
                column.editable();
            }
        }

        private void setDynamicFlag(IColumn column, IEntityProperty property)
        {
            if (property.isDynamic())
            {
                column.dynamicProperty();
            }
        }

        private void setVocabulary(IColumn column, Vocabulary vocabularyOrNull)
        {
            column.setVocabulary(vocabularyOrNull);
        }
    }

    private static final class Column implements IColumn, IColumnItem
    {
        private final TableModelColumnHeader header;

        private final List<ISerializableComparable> values =
                new ArrayList<ISerializableComparable>();

        private final IIndexProvider indexProvider;

        Column(String id, IIndexProvider indexProvider)
        {
            header = new TableModelColumnHeader(null, id, 0);
            this.indexProvider = indexProvider;
        }

        public TableModelColumnHeader getHeader()
        {
            return header;
        }

        public ISerializableComparable getValue(int index)
        {
            return index < values.size() ? values.get(index) : EMPTY_CELL;
        }

        @Override
        public List<Column> getColumns()
        {
            return new ArrayList<TypedTableModelBuilder.Column>(Arrays.asList(this));
        }

        @Override
        public IColumn withTitle(String title)
        {
            header.setTitle(title);
            return this;
        }

        @Override
        public IColumn withDefaultWidth(int width)
        {
            header.setDefaultColumnWidth(width);
            return this;
        }

        @Override
        public IColumn withDataType(DataTypeCode dataType)
        {
            header.setDataType(dataType);
            return this;
        }

        @Override
        public IColumn withEntityKind(EntityKind entityKind)
        {
            header.setEntityKind(entityKind);
            return this;
        }

        @Override
        public IColumn editable()
        {
            header.setEditable(true);
            return this;
        }

        @Override
        public IColumn dynamicProperty()
        {
            header.setDynamicProperty(true);
            return this;
        }

        @Override
        public IColumn property(String key, String value)
        {
            header.setProperty(key, value);
            return this;
        }

        @Override
        public void addValue(ISerializableComparable valueOrNull)
        {
            int index = indexProvider.getIndex();
            if (index < 0)
            {
                throw new IllegalStateException(
                        "Row index is < 0: This is most likely caused by missing invocation of builder.addRow().");
            }
            while (index > values.size())
            {
                values.add(EMPTY_CELL);
            }
            if (index == values.size())
            {
                values.add(index, valueOrNull);
            } else
            {
                ISerializableComparable v = values.get(index);
                if (v instanceof VocabularyTermTableCell)
                {
                    VocabularyTermTableCell vc = (VocabularyTermTableCell) v;
                    vc.append(valueOrNull);

                }
            }
        }

        @Override
        public void addString(String valueOrNull)
        {
            setDataType(DataTypeCode.VARCHAR);
            StringTableCell value =
                    valueOrNull == null ? EMPTY_CELL : new StringTableCell(valueOrNull);
            addValue(value);
        }

        @Override
        public void addMatch(MatchingEntity valueOrNull)
        {
            setDataType(DataTypeCode.MULTILINE_VARCHAR);
            ISerializableComparable value =
                    valueOrNull == null ? EMPTY_CELL : new MatchingEntityTableCell(valueOrNull);
            addValue(value);
        }

        @Override
        public void addInteger(Long valueOrNull)
        {
            setDataType(DataTypeCode.INTEGER);
            ISerializableComparable value =
                    valueOrNull == null ? EMPTY_CELL : new IntegerTableCell(valueOrNull);
            addValue(value);
        }

        @Override
        public void addDouble(Double valueOrNull)
        {
            setDataType(DataTypeCode.REAL);
            ISerializableComparable value =
                    valueOrNull == null ? EMPTY_CELL : new DoubleTableCell(valueOrNull);
            addValue(value);
        }

        @Override
        public void addDate(Date valueOrNull)
        {
            setDataType(DataTypeCode.TIMESTAMP);
            ISerializableComparable value =
                    valueOrNull == null ? EMPTY_CELL : new DateTableCell(valueOrNull);
            addValue(value);
        }

        @Override
        public void addEntityLink(IEntityInformationHolderWithIdentifier entity, String linkText)
        {
            assert entity != null;
            // TODO 2011-06-20, Piotr Buczek: extend DataType with ENTITY and set the type here
            header.setEntityKind(entity.getEntityKind());
            final EntityTableCell cell =
                    new EntityTableCell(entity.getEntityKind(), entity.getPermId(),
                            entity.getIdentifier());
            cell.setInvalid(DeletionUtils.isDeleted(entity));
            cell.setLinkText(linkText);
            addValue(cell);
        }

        @Override
        public void addEntityLink(
                Collection<? extends IEntityInformationHolderWithIdentifier> entities)
        {
            if (entities != null && !entities.isEmpty())
            {
                IEntityInformationHolderWithIdentifier firstEntity = entities.iterator().next();

                if (entities.size() == 1)
                {
                    addEntityLink(firstEntity, firstEntity.getIdentifier());
                } else
                {
                    // WORKAROUND we have no way to create cells with multiple links.
                    // This is an ugly way not to display multiple entities as single link.

                    final int MAX_ENTITIES = 4;

                    StringBuilder builder = new StringBuilder();
                    int counter = 0;

                    for (IEntityInformationHolderWithIdentifier entity : entities)
                    {
                        if (counter == MAX_ENTITIES)
                        {
                            builder.append("... (").append(entities.size() - MAX_ENTITIES)
                                    .append(" more)");
                            break;
                        }
                        builder.append(entity.getIdentifier()).append("\n");
                        counter++;
                    }

                    EntityTableCell fakeEntityTableCell =
                            new EntityTableCell(firstEntity.getEntityKind(), builder.toString());
                    fakeEntityTableCell.setFake(true);
                    addValue(fakeEntityTableCell);
                }
            }
        }

        private void setDataType(DataTypeCode dataType)
        {
            header.setDataType(DataTypeUtils.getCompatibleDataType(header.getDataType(), dataType));
        }

        @Override
        public void addPerson(Person personOrNull)
        {
            addString(SimplePersonRenderer.createPersonName(personOrNull).toString());
        }

        @Override
        public void setVocabulary(Vocabulary vocabularyOrNull)
        {
            header.setVocabulary(vocabularyOrNull);
        }
    }

    private final Map<String, Column> columns = new HashMap<String, Column>();

    private final Map<String, IColumnGroup> columnGroups = new HashMap<String, IColumnGroup>();

    private final List<IColumnItem> columnItems = new ArrayList<IColumnItem>();

    private final List<T> rowObjects = new ArrayList<T>();

    /**
     * Returns the model.
     */
    public TypedTableModel<T> getModel()
    {
        List<Column> orderedColumns = new ArrayList<Column>();
        for (IColumnItem item : columnItems)
        {
            List<Column> itemColumns = item.getColumns();
            Collections.sort(itemColumns, new Comparator<Column>()
            {
                @Override
                public int compare(Column c1, Column c2)
                {
                    String t1 = StringUtils.trimToEmpty(c1.getHeader().getTitle());
                    String t2 = StringUtils.trimToEmpty(c2.getHeader().getTitle());
                    return t1.compareTo(t2);
                }
            });
            orderedColumns.addAll(itemColumns);
        }
        for (int i = 0; i < orderedColumns.size(); i++)
        {
            TableModelColumnHeader header = orderedColumns.get(i).getHeader();
            header.setIndex(i);
        }
        List<TableModelColumnHeader> headers = new ArrayList<TableModelColumnHeader>();
        for (Column column : orderedColumns)
        {
            headers.add(column.getHeader());
        }
        List<TableModelRowWithObject<T>> rows = new ArrayList<TableModelRowWithObject<T>>();
        for (int i = 0, n = rowObjects.size(); i < n; i++)
        {
            T object = rowObjects.get(i);
            List<ISerializableComparable> rowValues =
                    new ArrayList<ISerializableComparable>(headers.size());
            for (Column column : orderedColumns)
            {
                rowValues.add(column.getValue(i));
            }
            rows.add(new TableModelRowWithObject<T>(object, rowValues));
        }
        return new TypedTableModel<T>(headers, rows);
    }

    /**
     * Adds a column with specified id.
     *
     * @return an {@link IColumnMetaData} instance which allows to set title, default width, and/or
     *         data type.
     * @throws IllegalArgumentException if a column with specified is has already been added.
     */
    public IColumnMetaData addColumn(String id)
    {
        if (columns.containsKey(id))
        {
            throw new IllegalArgumentException("There is already a column with id '" + id + "'.");
        }
        Column column = getOrCreateColumnAsColumnItem(id);
        return new ColumnMetaData(column);
    }

    /**
     * Adds a row with optional row object. This method has to be called before adding values to
     * columns.
     */
    public void addRow(T objectOrNull)
    {
        rowObjects.add(objectOrNull);
    }

    public IColumnGroup columnGroup(final String groupKey)
    {
        IColumnGroup columnGroup = columnGroups.get(groupKey);
        if (columnGroup == null)
        {
            ColumnGroup group = new ColumnGroup(groupKey);
            columnItems.add(group);
            columnGroup = group;
            columnGroups.put(groupKey, columnGroup);
        }
        return columnGroup;
    }

    public IColumn column(String id)
    {
        return getOrCreateColumnAsColumnItem(id);
    }

    private Column getOrCreateColumnAsColumnItem(String id)
    {
        boolean knownColumn = columns.containsKey(id);
        Column column = getOrCreateColumn(id);
        if (knownColumn == false)
        {
            columnItems.add(column);
        }
        return column;
    }

    private Column getOrCreateColumn(String id)
    {
        Column column = columns.get(id);
        if (column == null)
        {
            column = new Column(id, new IIndexProvider()
            {
                @Override
                public int getIndex()
                {
                    return rowObjects.size() - 1;
                }
            });
            columns.put(id, column);
        }
        return column;

    }

}
