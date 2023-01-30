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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Abstract {@link Dialog} that executes an action with specified data on confirm.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractDataConfirmationDialog<T> extends Dialog
{
    protected final IMessageProvider messageProvider;

    protected final T data;

    protected final FormPanel formPanel;

    protected final LabelField messageField;

    protected final KeyListener keyListener;

    protected AbstractDataConfirmationDialog(IMessageProvider messageProvider, T data, String title)
    {
        this.messageProvider = messageProvider;
        this.data = data;
        initializeData();
        setHeading(title);
        setButtons(Dialog.OKCANCEL);
        setHideOnButtonClick(true);
        setModal(true);
        this.messageField = new LabelField();
        this.messageField.setStyleName("message");
        this.formPanel = createForm();
        this.keyListener = new KeyListener()
            {
                @Override
                public void handleEvent(ComponentEvent ce)
                {
                    updateOkButtonState();
                }

            };
    }

    protected abstract String createMessage();

    protected abstract void extendForm();

    protected abstract void executeConfirmedAction();

    /** Additional initialization of data that will be performed before dialog form is created. */
    protected void initializeData()
    {
        // by default nothing to do
    }

    /** Sets OK button state to disabled if validation fails. */
    protected final void updateOkButtonState()
    {
        Component okButtonOrNull = getItemByItemId(OK);
        if (okButtonOrNull != null)
        {
            okButtonOrNull.setEnabled(validate());
        }
    }

    /**
     * Validates data provided in the dialog. By default validates form values.
     * 
     * @return <code>true</code> if valid, otherwise <code>false</code>
     */
    protected boolean validate()
    {
        return formPanel.isValid();
    }

    private FormPanel createForm()
    {
        FormPanel form = new FormPanel();
        form.setBodyBorder(false);
        form.setHeaderVisible(false);
        return form;
    }

    @Override
    protected final void onButtonPressed(final Button button)
    {
        if (button.getItemId().equals(Dialog.OK))
        {
            button.disable();
            if (validate())
            {
                executeConfirmedAction();
                super.onButtonPressed(button);
            } else
            {
                button.enable();
            }
        } else
        {
            super.onButtonPressed(button);
        }
    }

    @Override
    protected void onRender(Element parent, int pos)
    {
        super.onRender(parent, pos);
        add(messageField);
        extendForm();
        refreshMessage();
        add(formPanel);
        updateOkButtonState();
    }

    protected final void refreshMessage()
    {
        messageField.setText(createMessage());
    }

    protected final IDelegatedAction createRefreshMessageAction()
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    refreshMessage();
                }
            };
    }
}
