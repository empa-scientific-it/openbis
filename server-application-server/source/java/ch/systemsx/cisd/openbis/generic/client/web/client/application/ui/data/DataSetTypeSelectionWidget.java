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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataSetTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of data set types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class DataSetTypeSelectionWidget extends DropDownList<DataSetTypeModel, DataSetType>
{
    public static final String SUFFIX = "data-set-type";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final boolean withTypesInFile;

    private final boolean withAll;

    private final String initialCodeOrNull;

    public DataSetTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix, final boolean withAll, final String initialCodeOrNull)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.DATA_SET_TYPE, ModelDataPropertyNames.CODE,
                "data set type", "data set types");
        this.viewContext = viewContext;
        this.withAll = withAll;
        this.initialCodeOrNull = initialCodeOrNull;
        this.withTypesInFile = false;// parameter not used yet outside this class
        setAutoSelectFirst(withAll && initialCodeOrNull == null);
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE,
                ModelDataPropertyNames.TOOLTIP));
    }

    public DataSetTypeSelectionWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            String idSuffix)
    {
        this(viewContext, idSuffix, false, null);
    }

    /**
     * Returns the {@link DataSetType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final DataSetType tryGetSelectedDataSetType()
    {
        return super.tryGetSelected();
    }

    @Override
    protected List<DataSetTypeModel> convertItems(List<DataSetType> result)
    {
        return DataSetTypeModel.convert(result, withAll, withTypesInFile);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<DataSetType>> callback)
    {
        viewContext.getService().listDataSetTypes(new ListDataSetTypesCallback(viewContext));
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { createOrDelete(ObjectKind.DATASET_TYPE), edit(ObjectKind.DATASET_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    //
    // initial value support
    //

    private void selectInitialValue()
    {
        if (initialCodeOrNull != null)
        {
            trySelectByPropertyValue(ModelDataPropertyNames.CODE, initialCodeOrNull,
                    "Data Set Type '" + initialCodeOrNull + "' doesn't exist.");
            updateOriginalValue();
        }
    }

    private class ListDataSetTypesCallback extends DataSetTypeSelectionWidget.ListItemsCallback
    {

        protected ListDataSetTypesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<DataSetType> result)
        {
            super.process(result);
            selectInitialValue();
        }
    }

}
