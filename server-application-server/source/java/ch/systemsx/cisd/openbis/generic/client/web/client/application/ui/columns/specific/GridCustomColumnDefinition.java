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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * Column definition for a grid custom column.
 * 
 * @author Tomasz Pylak
 */
public class GridCustomColumnDefinition<T> implements IColumnDefinitionUI<T>
{
    private GridCustomColumnInfo columnMetadata;

    public GridCustomColumnDefinition(GridCustomColumnInfo columnMetadata)
    {
        this.columnMetadata = columnMetadata;
    }

    @Override
    public int getWidth()
    {
        return 150;
    }

    @Override
    public boolean isHidden()
    {
        return true;
    }

    @Override
    public boolean isNumeric()
    {
        DataTypeCode dataType = columnMetadata.getDataType();
        return dataType == DataTypeCode.INTEGER || dataType == DataTypeCode.REAL;
    }

    @Override
    public boolean isVocabulary()
    {
        return columnMetadata.getDataType() == DataTypeCode.CONTROLLEDVOCABULARY;
    }

    @Override
    public Vocabulary tryGetVocabulary()
    {
        return null;
    }

    @Override
    public boolean isEditable()
    {
        return false;
    }

    @Override
    public boolean isDynamicProperty()
    {
        return false;
    }

    public boolean isLink()
    {
        return false;
    }

    @Override
    public DataTypeCode tryToGetDataType()
    {
        return null;
    }

    @Override
    public String tryGetLink(T entity)
    {
        return null;
    }

    @Override
    public Comparable<?> tryGetComparableValue(GridRowModel<T> rowModel)
    {
        return getPrimitiveValue(rowModel);
    }

    private PrimitiveValue getPrimitiveValue(GridRowModel<T> rowModel)
    {
        String columnId = columnMetadata.getCode();
        PrimitiveValue value = rowModel.findColumnValue(columnId);
        return value;
    }

    @Override
    public String getValue(GridRowModel<T> rowModel)
    {
        String value = getPrimitiveValue(rowModel).toString();
        return value != null ? value : "";
    }

    @Override
    public String getHeader()
    {
        return columnMetadata.getLabel();
    }

    @Override
    public String getIdentifier()
    {
        return columnMetadata.getCode();
    }

    @Override
    public String tryToGetProperty(String key)
    {
        return null;
    }

    // GWT only
    @SuppressWarnings("unused")
    private GridCustomColumnDefinition()
    {
    }

    @Override
    public boolean isCustom()
    {
        return true;
    }
}