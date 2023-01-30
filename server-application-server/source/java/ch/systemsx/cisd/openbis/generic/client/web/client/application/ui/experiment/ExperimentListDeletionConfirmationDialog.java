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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.Collections;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowserGrid.DisplayedAndSelectedExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

// TODO extend AbstractDataListTrashDeletionConfirmationDialog when trash is working properly
public final class ExperimentListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<Experiment>
{
    private final DisplayedAndSelectedExperiments selectedAndDisplayedItemsOrNull;

    private final Experiment singleDataOrNull;

    public ExperimentListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, AsyncCallback<Void> callback,
            DisplayedAndSelectedExperiments selectedAndDisplayedItems)
    {
        super(viewContext, selectedAndDisplayedItems.getExperiments(), callback);
        this.withRadio();
        this.singleDataOrNull = null;
        this.selectedAndDisplayedItemsOrNull = selectedAndDisplayedItems;
        this.setId("deletion-confirmation-dialog");
    }

    public ExperimentListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            AsyncCallback<Void> deletionCallback, Experiment experiment)
    {
        super(viewContext, Collections.singletonList(experiment), deletionCallback);
        this.singleDataOrNull = experiment;
        this.selectedAndDisplayedItemsOrNull = null;
        this.setId("deletion-confirmation-dialog");
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
            final DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Experiment>> uploadCriteria =
                    selectedAndDisplayedItemsOrNull.createCriteria(isOnlySelected());
            getViewContext().getCommonService().deleteExperiments(uploadCriteria,
                    reason.getValue(), deletionType, deletionCallback);
        } else
        {
            getViewContext().getCommonService().deleteExperiment(TechId.create(singleDataOrNull),
                    reason.getValue(), deletionType, deletionCallback);
        }
    }

    @Override
    protected String getEntityName()
    {
        return EntityTypeUtils.translatedEntityKindForUI(viewContext, EntityKind.EXPERIMENT);
    }

    @Override
    protected final RadioGroup createRadio()
    {
        final IViewContext<ICommonClientServiceAsync> context = getViewContext();
        RadioGroup onlySelectedOrAll = WidgetUtils.createAllOrSelectedRadioGroup(
                onlySelectedRadioOrNull =
                        WidgetUtils.createRadio(context.getMessage(Dict.ONLY_SELECTED_RADIO,
                                data.size())), WidgetUtils.createRadio(context.getMessage(
                        Dict.ALL_RADIO, selectedAndDisplayedItemsOrNull.getDisplayedItemsCount())),
                context.getMessage(Dict.EXPERIMENTS_RADIO_GROUP_LABEL), data.size());
        onlySelectedOrAll.setStyleName("gray-delete-radios");
        return onlySelectedOrAll;
    }

}
