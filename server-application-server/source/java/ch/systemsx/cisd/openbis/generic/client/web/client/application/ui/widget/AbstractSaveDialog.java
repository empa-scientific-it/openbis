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

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * Abstract {@link Window} with Save and Cancel buttons, useful when save operation is needed.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
abstract public class AbstractSaveDialog extends Window
{
    private static final int FIELD_WIDTH = 450;

    private static final int SAVE_DIALOG_WIDTH = 600;

    protected abstract void save(AsyncCallback<Void> registrationCallback);

    // public only for tests
    public final class SaveDialogCallback extends AbstractAsyncCallback<Void>
    {
        private SaveDialogCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final Void result)
        {
            hide();
            postSaveCallback.execute();
        }
    }

    public static final String SAVE_BUTTON_ID = GenericConstants.ID_PREFIX + "dialog-save-button";

    public static final String CANCEL_BUTTON_ID = GenericConstants.ID_PREFIX
            + "dialog-cancel-button";

    private final IDelegatedAction postSaveCallback;

    private final IViewContext<?> viewContext;

    protected final FormPanelWithSavePoint form;

    protected Button saveButton;

    public AbstractSaveDialog(final IViewContext<?> viewContext, String title,
            final IDelegatedAction postSaveCallback)
    {
        this.postSaveCallback = postSaveCallback;
        this.viewContext = viewContext;
        setHeading(title);
        setModal(true);
        setWidth(SAVE_DIALOG_WIDTH);
        this.form = new FormPanelWithSavePoint();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        form.setFieldWidth(FIELD_WIDTH);
        add(form);

        addButton(saveButton = createSaveButton());
        addButton(createCancelButton());
    }

    protected final FormPanel getFormPanel()
    {
        return form;
    }

    public final void addField(Widget widget)
    {
        form.add(widget);
    }

    public final void removeField(Widget widget)
    {
        form.remove(widget);
    }

    public final void insertField(Widget widget, int index)
    {
        form.insert(widget, index);
    }

    public final int indexOfField(Component component)
    {
        return form.indexOf(component);
    }

    private Button createCancelButton()
    {
        final Button button =
                new Button(viewContext.getMessage(Dict.BUTTON_CANCEL),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public final void componentSelected(ButtonEvent ce)
                                {
                                    hide();
                                }
                            });
        button.setId(CANCEL_BUTTON_ID);
        return button;
    }

    private Button createSaveButton()
    {
        final Button button =
                new Button(createSaveButtonText(),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public final void componentSelected(final ButtonEvent ce)
                                {
                                    if (form.isValid())
                                    {
                                        save(new SaveDialogCallback(viewContext));
                                    }
                                }
                            });
        button.setId(SAVE_BUTTON_ID);
        return button;
    }

    protected String createSaveButtonText()
    {
        return getViewContext().getMessage(Dict.BUTTON_SAVE);
    }

    @Override
    protected void onRender(Element parent, int pos)
    {
        super.onRender(parent, pos);
        setFocusToFirstField(form.getFields());
    }

    public static void setFocusToFirstField(List<Field<?>> fields)
    {
        int fieldNumber = 0;
        while (fieldNumber < fields.size())
        {
            Field<?> field = fields.get(fieldNumber);
            if (field.isEnabled())
            {
                field.focus();
                return;
            }
        }
    }

    protected IViewContext<?> getViewContext()
    {
        return viewContext;
    }

}
