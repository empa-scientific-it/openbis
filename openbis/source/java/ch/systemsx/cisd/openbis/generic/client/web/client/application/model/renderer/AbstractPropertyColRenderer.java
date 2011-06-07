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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer;

import com.extjs.gxt.ui.client.widget.form.Field;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.DefaultCellEditorField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;

/**
 * An abstract decorator for {@link EntityPropertyColDef} to be used in grids for rendering values
 * in a different way in grids than in export. <br>
 * It changes {@link EntityPropertyColDef#getValue(GridRowModel)} behavior distinguishing certain
 * property types so in grid there can be e.g. a link displayed for hyperlink property. Other
 * methods are delegated without any change.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractPropertyColRenderer<T extends IEntityPropertiesHolder> implements
        IColumnDefinitionUI<T>
{

    /**
     * @return property renderer for given column of given entity
     */
    public static <S extends IEntityPropertiesHolder> AbstractPropertyColRenderer<S> getPropertyColRenderer(
            final IViewContext<?> viewContext, EntityPropertyColDef<S> colDef,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        // NOTE: keep in sync with TypedTableGrid.tryGetSpecificRenderer
        switch (colDef.getDataTypeCode())
        {
            case REAL:
                return new RealPropertyColRenderer<S>(colDef);
            case HYPERLINK:
                return new HyperlinkPropertyColRenderer<S>(colDef);
            case MULTILINE_VARCHAR:
                return new MultilineVarcharPropertyColRenderer<S>(colDef);
            case XML:
                return new MultilineVarcharPropertyColRenderer<S>(colDef);
            case CONTROLLEDVOCABULARY:
                return new VocabularyPropertyColRenderer<S>(colDef);
            case TIMESTAMP:
                return new TimestampPropertyColRenderer<S>(colDef);
            case MATERIAL:
                return new MaterialPropertyColRenderer<S>(viewContext, colDef);
            default:
                return new DefaultPropertyColRenderer<S>(colDef);
        }
    }

    protected final EntityPropertyColDef<T> colDef;

    public AbstractPropertyColRenderer(EntityPropertyColDef<T> colDef)
    {
        super();
        this.colDef = colDef;
    }

    public String getValue(GridRowModel<T> rowModel)
    {
        IEntityProperty property = colDef.tryGetProperty(rowModel.getOriginalObject());
        String valueAsString = property == null ? null : property.tryGetAsString();

        // error message should always display as multiline HTML
        if (StringUtils.isBlank(valueAsString) == false
                && StringEscapeUtils.unescapeHtml(valueAsString).startsWith(
                        BasicConstant.ERROR_PROPERTY_PREFIX))
        {
            valueAsString =
                    StringEscapeUtils.unescapeHtml(valueAsString).substring(
                            BasicConstant.ERROR_PROPERTY_PREFIX.length());
            return (new MultilineHTML(valueAsString)).toString();
        } else
        {
            return renderValue(rowModel);
        }
    }

    public Comparable<?> tryGetComparableValue(GridRowModel<T> rowModel)
    {
        return colDef.tryGetComparableValue(rowModel);
    }

    public boolean isLink()
    {
        return false;
    }

    public boolean isEditable()
    {
        return false;
    }

    public Field<? extends Object> createEditorField()
    {
        return new DefaultCellEditorField();
    }

    public String tryGetLink(T entity)
    {
        return null;
    }

    /**
     * @return given <var>value</var> rendered depending on property type
     */
    protected abstract String renderValue(GridRowModel<T> entity);

    // default delegate methods

    public String getHeader()
    {
        return colDef.getHeader();
    }

    public String getIdentifier()
    {
        return colDef.getIdentifier();
    }

    public int getWidth()
    {
        return colDef.getWidth();
    }

    public boolean isHidden()
    {
        return colDef.isHidden();
    }

    public boolean isNumeric()
    {
        return colDef.isNumeric();
    }

    public DataTypeCode getDataTypeCode()
    {
        return colDef.getDataTypeCode();
    }

    public String tryToGetProperty(String key)
    {
        return null;
    }

}
