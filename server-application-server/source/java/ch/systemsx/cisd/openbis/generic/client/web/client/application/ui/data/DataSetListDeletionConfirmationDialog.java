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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.Collections;

import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion.DeletionForceOptions;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

// TODO extend AbstractDataListTrashDeletionConfirmationDialog when trash is working properly
public final class DataSetListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<TableModelRowWithObject<AbstractExternalData>>
{
    private final SelectedAndDisplayedItems selectedAndDisplayedItemsOrNull;

    private final TableModelRowWithObject<AbstractExternalData> singleData;

    private DeletionForceOptions forceOptions;

    public DataSetListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, AsyncCallback<Void> callback,
            SelectedAndDisplayedItems selectedAndDisplayedItems)
    {
        super(viewContext, selectedAndDisplayedItems.getSelectedItems(), callback);
        this.withRadio();
        this.singleData = null;
        this.selectedAndDisplayedItemsOrNull = selectedAndDisplayedItems;
    }

    public DataSetListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            AsyncCallback<Void> deletionCallback, TableModelRowWithObject<AbstractExternalData> data)
    {
        super(viewContext, Collections.singletonList(data), deletionCallback);
        this.singleData = data;
        this.selectedAndDisplayedItemsOrNull = null;
    }

    @SuppressWarnings("unchecked")
    private IViewContext<ICommonClientServiceAsync> getViewContext()
    {
        return (IViewContext<ICommonClientServiceAsync>) viewContext;
    }

    @Override
    protected void executeDeletion(AsyncCallback<Void> deletionCallback)
    {
        final DeletionType deletionType = getDeletionType();

        if (selectedAndDisplayedItemsOrNull != null)
        {
            final DisplayedOrSelectedDatasetCriteria uploadCriteria =
                    selectedAndDisplayedItemsOrNull.createCriteria(isOnlySelected());
            getViewContext().getCommonService().deleteDataSets(uploadCriteria, reason.getValue(),
                    deletionType, getForceDisallowedTypesValue(), deletionCallback);
        } else
        {
            getViewContext().getCommonService().deleteDataSet(
                    singleData.getObjectOrNull().getCode(), reason.getValue(), deletionType,
                    getForceDisallowedTypesValue(), deletionCallback);
        }
    }

    @Override
    protected String getEntityName()
    {
        return EntityTypeUtils.translatedEntityKindForUI(viewContext, EntityKind.DATA_SET);
    }

    @Override
    protected final RadioGroup createRadio()
    {
        final IViewContext<ICommonClientServiceAsync> context = getViewContext();
        return WidgetUtils.createAllOrSelectedRadioGroup(
                onlySelectedRadioOrNull =
                        WidgetUtils.createRadio(context.getMessage(Dict.ONLY_SELECTED_RADIO,
                                data.size())), WidgetUtils.createRadio(context.getMessage(
                        Dict.ALL_RADIO, selectedAndDisplayedItemsOrNull.getDisplayedItemsCount())),
                context.getMessage(Dict.DATA_SETS_RADIO_GROUP_LABEL), data.size());
    }

    @Override
    protected void extendForm()
    {
        super.extendForm();

        if (false == isTrashEnabled())
        {
            forceOptions = new DeletionForceOptions(viewContext);
            formPanel.add(forceOptions);
        }
    }

    private boolean getForceDisallowedTypesValue()
    {
        return forceOptions != null ? forceOptions.getForceDisallowedTypesValue() : false;
    }

}
