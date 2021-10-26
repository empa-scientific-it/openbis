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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion.DeletionGrid.DisplayedAndSelectedDeletions;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

public final class PermanentDeletionConfirmationDialog extends
        AbstractDataConfirmationDialog<List<Deletion>>
{
    private static final int LABEL_WIDTH = 60;

    private static final int FIELD_WIDTH = 200;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AsyncCallback<Void> callback;

    private final DeletionForceOptions forceOptions;

    private final DisplayedAndSelectedDeletions selectedAndDisplayedItems;

    private Radio onlySelectedRadio;

    private Radio allRadio;

    private DeletionForceCheckBox forceToDeleteDependentDeletionSetsCheckBox;

    public PermanentDeletionConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            DisplayedAndSelectedDeletions selectedAndDisplayedItems, AsyncCallback<Void> callback)
    {
        super(viewContext, null, viewContext
                .getMessage(Dict.PERMANENT_DELETIONS_CONFIRMATION_TITLE));
        setStyleName("permanentDeletionConfirmationDialog");
        this.viewContext = viewContext;
        this.callback = callback;
        this.forceToDeleteDependentDeletionSetsCheckBox = new DeletionForceCheckBox();
        this.forceToDeleteDependentDeletionSetsCheckBox.setText("Force dependent deletions: ");
        this.forceToDeleteDependentDeletionSetsCheckBox.setTooltip("Dependent deletions have entities "
                + "which have to be permanently deleted together or before the entities of the selected deletions "
                + "can be permanently deleted, too.");
        this.forceOptions = new DeletionForceOptions(viewContext);
        this.selectedAndDisplayedItems = selectedAndDisplayedItems;
        this.setId("deletion-confirmation-dialog");
    }

    @Override
    protected void executeConfirmedAction()
    {
        DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Deletion>> criteria =
                selectedAndDisplayedItems.createCriteria(WidgetUtils.isSelected(onlySelectedRadio));

        viewContext.getCommonService().deletePermanently(criteria,
                forceToDeleteDependentDeletionSetsCheckBox.getValue(),
                forceOptions.getForceDisallowedTypesValue(),
                AsyncCallbackWithProgressBar.decorate(callback, viewContext.getMessage(Dict.PERMANENT_DELETIONS_PROGRESS)));
    }

    @Override
    protected String createMessage()
    {
        if (WidgetUtils.isSelected(onlySelectedRadio))
        {
            return viewContext.getMessage(Dict.PERMANENT_DELETIONS_SELECTED_CONFIRMATION_MSG,
                    selectedAndDisplayedItems.getSelectedDeletions().size());
        } else
        {
            return viewContext.getMessage(Dict.PERMANENT_DELETIONS_ALL_CONFIRMATION_MSG);
        }
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);

        onlySelectedRadio = WidgetUtils
                .createRadio(viewContext.getMessage(Dict.ONLY_SELECTED_RADIO, selectedAndDisplayedItems.getSelectedDeletions().size()));
        allRadio = WidgetUtils.createRadio(viewContext.getMessage(Dict.ALL_RADIO, selectedAndDisplayedItems.getDisplayedItemsCount()));

        RadioGroup radioGroup = WidgetUtils.createAllOrSelectedRadioGroup(onlySelectedRadio, allRadio,
                viewContext.getMessage(Dict.DELETION_RADIO_GROUP_LABEL), selectedAndDisplayedItems.getSelectedDeletions().size(),
                createRefreshMessageAction());
        radioGroup.setStyleName("gray-delete-radios");

        formPanel.add(radioGroup);
        Panel panel = new VerticalPanel();
        panel.addStyleName("deletionForceOptions");
        panel.add(forceToDeleteDependentDeletionSetsCheckBox);
        formPanel.add(panel);
        formPanel.add(forceOptions);
    }

}
