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

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * A {@link ComboBox} extension for selecting a {@link DataType}.
 * 
 * @author Christian Ribeaud
 */
public final class DataTypeSelectionWidget extends DropDownList<DataTypeModel, DataType>
{
    private static final String EMPTY_RESULT = "data types";

    private static final String CHOOSE = "data type";

    public static final String SUFFIX = "data-type";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public DataTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final boolean mandatory)
    {
        super(viewContext, SUFFIX, Dict.DATA_TYPE, ModelDataPropertyNames.CODE, CHOOSE,
                EMPTY_RESULT);
        this.viewContext = viewContext;
        FieldUtil.setMandatoryFlag(this, mandatory);
    }

    /**
     * Returns the {@link DataType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final DataType tryGetSelectedDataType()
    {
        return super.tryGetSelected();
    }

    private final class ListDataTypesCallback extends AbstractAsyncCallback<List<DataType>>
    {
        ListDataTypesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final List<DataType> result)
        {
            final ListStore<DataTypeModel> dataTypeStore = getStore();
            dataTypeStore.removeAll();
            dataTypeStore.add(convertItems(result));
            if (dataTypeStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, CHOOSE));
                setReadOnly(false);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, EMPTY_RESULT));
                setReadOnly(true);
            }
            applyEmptyText();
        }
    }

    @Override
    protected List<DataTypeModel> convertItems(List<DataType> result)
    {
        return DataTypeModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<DataType>> callback)
    {
        viewContext.getService().listDataTypes(new ListDataTypesCallback(viewContext));
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.EMPTY_ARRAY;
    }
}
