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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * A simple dialog with cancel and accept (e.g. Save or Ok) buttons.
 * 
 * @author Tomasz Pylak
 */
public class SimpleDialog extends Dialog
{
    public static final String ACCEPT_BUTTON_ID =
            GenericConstants.ID_PREFIX + "dialog-accept-button";

    private final IMessageProvider messageProvider;

    private final Button acceptButton;

    private IDelegatedAction acceptActionOrNull;

    private IDelegatedAction cancelActionOrNull;

    public SimpleDialog(final Widget widget, final String heading, String acceptButtonLabel,
            IMessageProvider messageProvider, Button... buttons)
    {
        this.messageProvider = messageProvider;

        setHeading(heading);
        setButtons(""); // no default buttons
        setScrollMode(Scroll.AUTO);
        setHideOnButtonClick(true);
        setModal(true);

        setLayout(new FitLayout());
        add(widget);

        for (Button button : buttons)
        {
            super.addButton(button);
        }

        acceptButton = createAcceptButton(acceptButtonLabel);
        addButton(acceptButton);
        addButton(createCancelButton());

        addWindowListener(new WindowListener()
            {
                @Override
                public void windowHide(WindowEvent we)
                {
                    fireCancel();
                }
            });
    }

    public void setEnableOfAcceptButton(boolean enable)
    {
        acceptButton.setEnabled(enable);
    }

    private Button createCancelButton()
    {
        final Button button =
                new Button(messageProvider.getMessage(Dict.BUTTON_CANCEL),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public final void componentSelected(ButtonEvent ce)
                                {
                                    fireCancel();
                                }
                            });
        return button;
    }

    private Button createAcceptButton(String label)
    {
        final Button button = new Button(label, new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    fireAccept();
                }
            });
        button.setId(ACCEPT_BUTTON_ID);
        return button;
    }

    public void setAcceptAction(IDelegatedAction acceptActionOrNull)
    {
        this.acceptActionOrNull = acceptActionOrNull;
    }

    public void setCancelAction(IDelegatedAction cancelActionOrNull)
    {
        this.cancelActionOrNull = cancelActionOrNull;
    }

    private void fireAccept()
    {
        if (acceptActionOrNull != null)
        {
            acceptActionOrNull.execute();
        }
    }

    private void fireCancel()
    {
        if (cancelActionOrNull != null)
        {
            cancelActionOrNull.execute();
        } else
        {
            hide();
        }
    }

    /**
     * Extends the functionality of auto closing dialog to all the buttons.
     */
    @Override
    public void addButton(Button button)
    {
        if (isHideOnButtonClick())
        {
            button.addSelectionListener(new SelectionListener<ButtonEvent>()
                {

                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        hide();
                    }
                });
        }
        super.addButton(button);
    }
}
