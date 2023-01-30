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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ReasonField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;

/**
 * {@link AbstractDataConfirmationDialog} abstract implementation for deleting given list of data on confirm.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractDataListDeletionConfirmationDialog<T> extends
        AbstractDataConfirmationDialog<List<T>>
{
    private static final int LABEL_WIDTH = 70;

    private static final int FIELD_WIDTH = 180;

    private static final String ALL_EMPHASIZED = "<b>ALL</b> displayed ";

    private static final String SELECTED = " selected ";

    protected final IViewContext<?> viewContext;

    private final AsyncCallback<Void> deletionCallback;

    private boolean withRadio = false;

    protected Radio onlySelectedRadioOrNull;

    protected ReasonField reason;

    public AbstractDataListDeletionConfirmationDialog(IViewContext<?> viewContext, List<T> data,
            AsyncCallback<Void> deletionCallback)
    {
        super(viewContext, data, viewContext.getMessage(Dict.DELETE_CONFIRMATION_TITLE));
        this.viewContext = viewContext;
        this.deletionCallback = deletionCallback;
    }

    // optional initialization

    /** adds radio group for selecting between deletion of all or only selected data */
    protected void withRadio()
    {
        this.withRadio = true;
    }

    //

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);

        reason = new ReasonField(messageProvider, true);
        reason.setId("deletion-reason");
        reason.focus();
        reason.addKeyListener(keyListener);
        if (withRadio)
        {
            formPanel.add(createRadio());
        }

        formPanel.add(reason);
    }

    @Override
    protected String createMessage()
    {
        String deletedObjects;
        if (withRadio == false)
        {
            deletedObjects = data.size() + " " + getEntityName();
        } else
        {
            deletedObjects =
                    (isOnlySelected() ? data.size() + SELECTED : ALL_EMPHASIZED) + getEntityName();
        }
        return messageProvider.getMessage(Dict.DELETE_CONFIRMATION_MESSAGE_WITH_REASON_TEMPLATE,
                getOperationName(), deletedObjects, getAdditionalMessage());
    }

    protected String getAdditionalMessage()
    {
        return null;
    }

    protected abstract String getEntityName();

    protected abstract void executeDeletion(AsyncCallback<Void> callback);

    // property based enabling of trash is only a temporary solution

    protected DeletionType getDeletionType()
    {
        return isTrashEnabled() ? DeletionType.TRASH : DeletionType.PERMANENT;
    }

    String getOperationName()
    {
        final String dictKey = isTrashEnabled() ? Dict.DELETING : Dict.DELETING_PERMANENTLY;
        return viewContext.getMessage(dictKey);
    }

    String getProgressMessage()
    {
        final String dictKey =
                isTrashEnabled() ? Dict.DELETE_PROGRESS_MESSAGE
                        : Dict.DELETE_PERMANENTLY_PROGRESS_MESSAGE;
        return viewContext.getMessage(dictKey, getEntityName());
    }

    protected final boolean isTrashEnabled()
    {
        return viewContext.getModel().getApplicationInfo().getWebClientConfiguration()
                .getEnableTrash();
    }

    @Override
    protected final void executeConfirmedAction()
    {
        executeDeletion(getCallbackWithProgressBar());
    }

    /**
     * This method should be overriden in subclasses if dialog is supposed to use a radio and set {@link #onlySelectedRadioOrNull}.
     */
    protected RadioGroup createRadio()
    {
        return null;
    }

    protected final boolean isOnlySelected()
    {
        return WidgetUtils.isSelected(onlySelectedRadioOrNull);
    }

    /**
     * Returns deletion callback and shows a progress bar that will be hidden when the callback is finished.
     */
    private AsyncCallback<Void> getCallbackWithProgressBar()
    {
        return AsyncCallbackWithProgressBar.decorate(deletionCallback, getProgressMessage());
    }
}
