/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * {@link ComboBox} containing list of property type codes loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeSelectionWidget extends
        DropDownList<PropertyTypeSelectionWidget.PropertyTypeComboModel, PropertyType>
{
    private static final String EMPTY_RESULT_SUFFIX = "property types";

    private static final String CHOOSE_SUFFIX = "property type";

    static class PropertyTypeComboModel extends SimplifiedBaseModelData
    {
        private static final long serialVersionUID = 1L;

        public PropertyTypeComboModel(PropertyType entity, List<PropertyType> types)
        {
            set(ModelDataPropertyNames.CODE, PropertyTypeRenderer.getDisplayName(entity, types));
            set(ModelDataPropertyNames.TOOLTIP, PropertyTypeRenderer.renderAsTooltip(entity));
            set(ModelDataPropertyNames.OBJECT, entity);
        }
    }

    static final String SUFFIX = "property-type";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public PropertyTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.PROPERTY_TYPE, ModelDataPropertyNames.CODE,
                CHOOSE_SUFFIX, EMPTY_RESULT_SUFFIX);
        this.viewContext = viewContext;
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE,
                ModelDataPropertyNames.TOOLTIP));
    }

    public final String tryGetSelectedPropertyTypeCode()
    {
        final PropertyType propertyType = tryGetSelectedPropertyType();
        return propertyType == null ? null : propertyType.getCode();
    }

    /**
     * Returns the property type currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final PropertyType tryGetSelectedPropertyType()
    {
        return super.tryGetSelected();
    }

    private final class ListPropertyTypesCallback extends
            AbstractAsyncCallback<TypedTableResultSet<PropertyType>>
    {
        ListPropertyTypesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final TypedTableResultSet<PropertyType> result)
        {
            ResultSet<TableModelRowWithObject<PropertyType>> resultSet = result.getResultSet();
            resultSetKey = resultSet.getResultSetKey();
            final ListStore<PropertyTypeComboModel> propertyTypeStore = getStore();
            propertyTypeStore.removeAll();
            List<TableModelRowWithObject<PropertyType>> rows =
                    resultSet.getList().extractOriginalObjects();
            ArrayList<PropertyType> types = new ArrayList<PropertyType>();
            for (TableModelRowWithObject<PropertyType> row : rows)
            {
                types.add(row.getObjectOrNull());
            }
            propertyTypeStore.add(convertItems(types));
            if (propertyTypeStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, CHOOSE_SUFFIX));
                setReadOnly(false);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, EMPTY_RESULT_SUFFIX));
                setReadOnly(true);
            }
            applyEmptyText();
            removeResultSetFromCache();
        }
    }

    @Override
    protected List<PropertyTypeComboModel> convertItems(List<PropertyType> types)
    {
        final List<PropertyTypeComboModel> result = new ArrayList<PropertyTypeComboModel>();
        for (final PropertyType st : types)
        {
            result.add(new PropertyTypeComboModel(st, types));
        }
        return result;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<PropertyType>> callback)
    {
        DefaultResultSetConfig<String, TableModelRowWithObject<PropertyType>> config =
                DefaultResultSetConfig.createFetchAll();
        viewContext.getService().listPropertyTypes(config,
                new ListPropertyTypesCallback(viewContext));
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROPERTY_TYPE);
    }
}
