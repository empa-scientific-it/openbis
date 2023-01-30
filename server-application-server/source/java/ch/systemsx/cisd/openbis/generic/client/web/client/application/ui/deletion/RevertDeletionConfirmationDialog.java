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

import java.util.Collections;

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion.DeletionGrid.DisplayedAndSelectedDeletions;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityWithDeletionInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

public final class RevertDeletionConfirmationDialog extends AbstractDataConfirmationDialog<Void>
{
    private static final int LABEL_WIDTH = 60;

    private static final int FIELD_WIDTH = 180;

    private IViewContext<ICommonClientServiceAsync> viewContext;

    private AsyncCallback<Void> callback;

    private IEntityWithDeletionInformation deletedEntityOrNull;

    private DisplayedAndSelectedDeletions selectedAndDisplayedItemsOrNull;

    private Radio onlySelectedRadio;

    private Radio allRadio;

    public RevertDeletionConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            DisplayedAndSelectedDeletions selectedAndDisplayedItems, AsyncCallback<Void> callback)
    {
        this(viewContext, callback);
        this.selectedAndDisplayedItemsOrNull = selectedAndDisplayedItems;
    }

    public RevertDeletionConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            IEntityWithDeletionInformation deletedEntity, AsyncCallback<Void> callback)
    {
        this(viewContext, callback);
        this.deletedEntityOrNull = deletedEntity;
    }

    private RevertDeletionConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext, AsyncCallback<Void> callback)
    {
        super(viewContext, null, viewContext.getMessage(Dict.REVERT_DELETIONS_CONFIRMATION_TITLE));
        addStyleName("revertDeletionConfirmationDialog");
        this.viewContext = viewContext;
        this.callback = callback;
    }

    @Override
    protected void executeConfirmedAction()
    {
        AsyncCallbackWithProgressBar<Void> callbackWithProgress = AsyncCallbackWithProgressBar.decorate(callback,
                viewContext.getMessage(Dict.REVERT_DELETIONS_PROGRESS));

        if (selectedAndDisplayedItemsOrNull != null)
        {
            DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Deletion>> criteria =
                    selectedAndDisplayedItemsOrNull.createCriteria(WidgetUtils.isSelected(onlySelectedRadio));
            viewContext.getCommonService().revertDeletions(criteria, callbackWithProgress);
        } else if (deletedEntityOrNull != null)
        {
            TechId techId = TechId.create(deletedEntityOrNull.getDeletion());
            viewContext.getCommonService().revertDeletions(Collections.singletonList(techId), callbackWithProgress);
        }
    }

    @Override
    protected String createMessage()
    {
        if (selectedAndDisplayedItemsOrNull != null)
        {
            if (WidgetUtils.isSelected(onlySelectedRadio))
            {
                return viewContext.getMessage(Dict.REVERT_DELETIONS_SELECTED_CONFIRMATION_MSG,
                        selectedAndDisplayedItemsOrNull.getSelectedDeletions().size());
            } else
            {
                return viewContext.getMessage(Dict.REVERT_DELETIONS_ALL_CONFIRMATION_MSG);
            }
        } else if (deletedEntityOrNull != null)
        {
            String deletedEntity =
                    EntityTypeUtils.translatedEntityKindForUI(viewContext, deletedEntityOrNull.getEntityKind()) + " '"
                            + deletedEntityOrNull.getCode() + "'";
            Deletion deletion = deletedEntityOrNull.getDeletion();
            String deletedBy = deletion.getRegistrator().toString();
            String deletionDate =
                    DateRenderer.renderDate(deletion.getRegistrationDate(),
                            BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN);
            String deletionReason = StringEscapeUtils.unescapeHtml(deletion.getReason());
            return viewContext.getMessage(Dict.REVERT_ENTITY_DELETION_CONFIRMATION_MSG,
                    deletedEntity, deletedBy, deletionDate, deletionReason);
        } else
        {
            return null;
        }
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);

        if (selectedAndDisplayedItemsOrNull != null)
        {
            onlySelectedRadio = WidgetUtils
                    .createRadio(viewContext.getMessage(Dict.ONLY_SELECTED_RADIO, selectedAndDisplayedItemsOrNull.getSelectedDeletions().size()));
            allRadio = WidgetUtils.createRadio(viewContext.getMessage(Dict.ALL_RADIO, selectedAndDisplayedItemsOrNull.getDisplayedItemsCount()));

            RadioGroup radioGroup = WidgetUtils.createAllOrSelectedRadioGroup(onlySelectedRadio, allRadio,
                    viewContext.getMessage(Dict.DELETION_RADIO_GROUP_LABEL), selectedAndDisplayedItemsOrNull.getSelectedDeletions().size(),
                    createRefreshMessageAction());
            radioGroup.setStyleName("gray-delete-radios");
            formPanel.add(radioGroup);
        }
    }

}
