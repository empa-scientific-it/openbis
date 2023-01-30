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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ICallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IComponentWithCloseConfirmation;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithConfirmations;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithConfirmations.IConfirmation;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithConfirmations.IConfirmationChain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FormPanelWithSavePoint;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FormPanelWithSavePoint.DirtyChangeEvent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;

/**
 * An <i>abstract</i> registration form.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractRegistrationForm extends ContentPanel implements
        IComponentWithCloseConfirmation
{

    public static final int PANEL_MARGIN = 100;

    private static final String SESSION_KEY_PREFIX = "sessionKey_";

    public static final String SESSION_KEYS_NUMBER = "sessionKeysNumber";

    public static final String SAVE_BUTTON = "save-button";

    public static final int DEFAULT_LABEL_WIDTH = 100;

    public static final int DEFAULT_FIELD_WIDTH = 500;

    public static final int SECTION_LABEL_WIDTH = DEFAULT_LABEL_WIDTH + 9;

    public static final int SECTION_FIELD_WIDTH = DEFAULT_FIELD_WIDTH;

    public static final int SECTION_DEFAULT_FIELD_WIDTH = DEFAULT_FIELD_WIDTH;// -15

    public static final int SECTION_WIDTH = SECTION_FIELD_WIDTH + SECTION_LABEL_WIDTH + 80;// +16

    protected IMessageProvider messageProvider;

    protected InfoBox infoBox;

    protected InfoBoxResetListener infoBoxResetListener;

    protected FormPanelWithSavePoint formPanel;

    protected final int labelWidth;

    protected final int fieldWidth;

    protected ButtonWithConfirmations saveButton;

    private boolean sessionKeysInitiated = false;

    private Html loadingInfo;

    protected Html unsavedChangesInfo;

    protected LayoutContainer rightPanel;

    private Button resetButton;

    private Button revertButton;

    private boolean dirtyCheckEnabled = true;

    protected final boolean isPopUp;

    protected AbstractRegistrationForm(final IMessageProvider messageProvider, final String id)
    {
        this(messageProvider, id, DEFAULT_LABEL_WIDTH, DEFAULT_FIELD_WIDTH, false);
    }

    protected AbstractRegistrationForm(final IMessageProvider messageProvider, final String id,
            final int labelWidth, final int fieldWidth)
    {
        this(messageProvider, id, labelWidth, fieldWidth, false);
    }

    protected AbstractRegistrationForm(final IMessageProvider messageProvider, final String id, final boolean isPopUp)
    {
        this(messageProvider, id, DEFAULT_LABEL_WIDTH, DEFAULT_FIELD_WIDTH, isPopUp);
    }

    protected AbstractRegistrationForm(final IMessageProvider messageProvider, final String id,
            final int labelWidth, final int fieldWidth, final boolean isPopUp)
    {
        this.messageProvider = messageProvider;
        this.labelWidth = labelWidth;
        this.fieldWidth = fieldWidth;
        this.isPopUp = isPopUp;
        setHeaderVisible(false);
        setLayout(new FlowLayout(5));
        setBodyBorder(false);
        setBorders(false);
        setScrollMode(Scroll.AUTO);
        setId(id);
        add(infoBox = createInfoBox(messageProvider));
        if (isPopUp)
        {
            infoBox.setVisible(false);
        }
        add(loadingInfo = createLoadingInfo());
        add(WidgetUtils.inRow(formPanel = createFormPanel(), rightPanel = createAdditionalPanel()));
        if (isPopUp)
        {
            rightPanel.setVisible(false);
        }
        formPanel.setId("registration-panel-" + id);
        add(unsavedChangesInfo = createUnsavedChangesInfo());
    }

    public FormPanel getFormPanel()
    {
        return formPanel;
    }

    private LayoutContainer createAdditionalPanel()
    {
        LayoutContainer c = new LayoutContainer();
        c.setLayout(new FlowLayout(5));
        c.setBorders(false);
        c.setScrollMode(Scroll.AUTO);
        return c;
    }

    private Html createLoadingInfo()
    {
        Html result = new Html("Loading...");
        result.setVisible(false);
        return result;
    }

    private Html createUnsavedChangesInfo()
    {
        Html result = new Html(messageProvider.getMessage(Dict.UNSAVED_FORM_CHANGES_INFO));
        result.addStyleName("unsaved-changes-info");
        result.setWidth(labelWidth + fieldWidth + PANEL_MARGIN);
        result.setVisible(false);
        return result;
    }

    private final static InfoBox createInfoBox(IMessageProvider messageProvider)
    {
        final InfoBox infoBox = new InfoBox(messageProvider);
        return infoBox;
    }

    protected void setLoading(boolean loading)
    {
        formPanel.setVisible(loading == false);
        loadingInfo.setVisible(loading);
        adjustFieldsSizes();
    }

    protected void updateDirtyCheck()
    {
        updateDirtyCheckAfterChange(formPanel.isDirtyForSavePoint());
    }

    protected void updateDirtyCheckAfterChange(boolean isDirty)
    {
        if (isDirtyCheckEnabled() && isDirty)
        {
            unsavedChangesInfo.setVisible(true);
        } else
        {
            unsavedChangesInfo.setVisible(false);
        }
    }

    protected void resetFieldsAfterSave()
    {
        resetPanel();
        updateDirtyCheckAfterSave();
    }

    protected void updateDirtyCheckAfterSave()
    {
        formPanel.setSavePoint();
        updateDirtyCheckAfterChange(false);
    }

    protected void resetPanel()
    {
        formPanel.reset();
        updateDirtyCheckAfterReset();
    }

    protected void updateDirtyCheckAfterReset()
    {
        updateDirtyCheckAfterChange(false);
    }

    protected void revertPanel()
    {
        updateDirtyCheckAfterRevert();
    }

    protected void updateDirtyCheckAfterRevert()
    {
        formPanel.resetToSavePoint();
        updateDirtyCheckAfterChange(false);
    }

    public void adjustFieldsSizes()
    {
        for (Field<?> field : formPanel.getFields())
        {
            field.syncSize();
        }
    }

    private FormPanelWithSavePoint createFormPanel()
    {
        final FormPanelWithSavePoint panel = new FormPanelWithSavePoint();
        infoBoxResetListener = new InfoBoxResetListener(infoBox);
        panel.addClickListener(infoBoxResetListener);
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setWidth(labelWidth + fieldWidth + PANEL_MARGIN);
        panel.setLabelWidth(labelWidth);
        panel.setFieldWidth(fieldWidth);
        panel.setButtonAlign(HorizontalAlignment.RIGHT);
        panel.addDirtyChangeListener(new Listener<DirtyChangeEvent>()
            {
                @Override
                public void handleEvent(DirtyChangeEvent e)
                {
                    updateDirtyCheckAfterChange(e.isDirtyForSavePoint());
                }
            });

        saveButton = new ButtonWithConfirmations();
        saveButton.setText(messageProvider.getMessage(Dict.BUTTON_SAVE));
        saveButton.setStyleAttribute("marginRight", "20px");
        saveButton.setId(getId() + SAVE_BUTTON);
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (panel.isValid())
                    {
                        submitValidForm();
                    }
                }
            });
        addSaveButtonConfirmationListener();
        resetButton = new Button(messageProvider.getMessage(Dict.BUTTON_RESET));
        resetButton.setVisible(false);
        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (isDirtyCheckEnabled() && panel.isDirtyForSavePoint())
                    {
                        ce.setCancelled(true);
                        new ConfirmationDialog(
                                messageProvider
                                        .getMessage(Dict.RESET_UNSAVED_FORM_CHANGES_CONFIRMATION_TITLE),
                                messageProvider
                                        .getMessage(Dict.LOSE_UNSAVED_FORM_CHANGES_CONFIRMATION_MSG))
                            {
                                @Override
                                protected void onYes()
                                {
                                    resetPanel();
                                }
                            }.show();
                    } else
                    {
                        resetPanel();
                    }
                }
            });

        revertButton = new Button(messageProvider.getMessage(Dict.BUTTON_REVERT));
        revertButton.setVisible(false);
        revertButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (isDirtyCheckEnabled() && panel.isDirtyForSavePoint())
                    {
                        ce.setCancelled(true);
                        new ConfirmationDialog(
                                messageProvider
                                        .getMessage(Dict.REVERT_UNSAVED_FORM_CHANGES_CONFIRMATION_TITLE),
                                messageProvider
                                        .getMessage(Dict.LOSE_UNSAVED_FORM_CHANGES_CONFIRMATION_MSG))
                            {
                                @Override
                                protected void onYes()
                                {
                                    revertPanel();
                                }
                            }.show();
                    } else
                    {
                        revertPanel();
                    }
                }
            });

        if (false == isPopUp)
        {
            panel.addButton(resetButton);
            panel.addButton(revertButton);
            panel.addButton(saveButton);
        }

        return panel;
    }

    protected void setUploadEnabled(boolean enabled)
    {
        saveButton.setEnabled(enabled);
    }

    protected void setResetButtonVisible(boolean visible)
    {
        resetButton.setVisible(visible);
    }

    protected void setRevertButtonVisible(boolean visible)
    {
        revertButton.setVisible(visible);
    }

    protected void addSaveButtonConfirmationListener()
    {
        addSaveButtonConfirmationListener(saveButton);
    }

    protected void addSaveButtonConfirmationListener(final ButtonWithConfirmations button)
    {
        button.clearConfirmations();

        button.addConfirmation(new IConfirmation()
            {
                @Override
                public void confirm(final IConfirmationChain chain)
                {
                    if (formPanel.isValid() && isDirtyCheckEnabled()
                            && formPanel.isDirtyForSavePoint() == false)
                    {
                        new ConfirmationDialog(messageProvider
                                .getMessage(Dict.SAVE_UNCHANGED_FORM_CONFIRMATION_TITLE),
                                messageProvider
                                        .getMessage(Dict.SAVE_UNCHANGED_FORM_CONFIRMATION_MSG))
                            {
                                @Override
                                protected void onYes()
                                {
                                    chain.next();
                                }
                            }.show();
                    } else
                    {
                        chain.next();
                    }

                }
            });
    }

    /**
     * Submits a valid form.
     * <p>
     * This method only gets executed when the form has been successfully validated.
     * </p>
     */
    protected abstract void submitValidForm();

    //
    // Helper classes
    //

    protected abstract class AbstractRegistrationCallback<T> extends AbstractAsyncCallback<T>
    {
        protected AbstractRegistrationCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<T>(infoBox));
            setUploadEnabled(false);
        }

        protected AbstractRegistrationCallback(final IViewContext<?> viewContext, final ICallbackListener<T> listener)
        {
            super(viewContext, listener);
            setUploadEnabled(false);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected void process(final T result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo(result));
            try
            {
                resetFieldsAfterSave();
            } catch (JavaScriptException e)
            {
                // ignored because it might be thrown for file upload field in system tests on CI
                // server
            }
            setUploadEnabled(true);
        }

        protected abstract List<? extends IMessageElement> createSuccessfullRegistrationInfo(T result);

        @Override
        public void finishOnFailure(final Throwable caught)
        {
            setUploadEnabled(true);
        }

    }

    public final static class InfoBoxResetListener implements Listener<FieldEvent>, ClickHandler
    {
        private final InfoBox infoBox;

        private boolean enabled = true;

        public InfoBoxResetListener(final InfoBox infoBox)
        {
            assert infoBox != null : "Unspecified info box.";
            this.infoBox = infoBox;
        }

        private void resetInfoBox()
        {
            if (enabled)
            {
                infoBox.reset();
            }
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        //
        // Listener
        //

        @Override
        public final void handleEvent(final FieldEvent be)
        {
            resetInfoBox();
        }

        @Override
        public final void onClick(ClickEvent sender)
        {
            resetInfoBox();
        }
    }

    /**
     * Field specifying the session key needed by the file uploader on the server side.
     * <p>
     * This key is the session attribute key where you can access the uploaded files.
     * </p>
     */
    public final static HiddenField<String> createHiddenSessionField(String value, int counter)
    {
        String name = SESSION_KEY_PREFIX + counter;
        return createHiddenField(name, value);
    }

    public static HiddenField<String> createHiddenField(String name, String value)
    {
        final HiddenField<String> hiddenField = new HiddenField<String>();
        hiddenField.setName(name);
        hiddenField.setValue(value);
        return hiddenField;
    }

    protected final void addUploadFeatures(String sessionKey)
    {
        ArrayList<String> sessionKeys = new ArrayList<String>();
        sessionKeys.add(sessionKey);
        addUploadFeatures(sessionKeys);
    }

    protected final void addUploadFeatures(List<String> sessionKeys)
    {
        assert sessionKeysInitiated == false : "This method should be called only once.";
        addFileUploadFeature(formPanel, sessionKeys);
        sessionKeysInitiated = true;
    }

    public static void addFileUploadFeature(FormPanel formPanel, List<String> sessionKeys)
    {
        formPanel.setAction(GenericConstants.createServicePath("upload"));
        formPanel.setEncoding(Encoding.MULTIPART);
        formPanel.setMethod(Method.POST);
        formPanel.add(createHiddenField(SESSION_KEYS_NUMBER, sessionKeys.size() + ""));
        for (int i = 0; i < sessionKeys.size(); i++)
        {
            formPanel.add(AbstractRegistrationForm.createHiddenSessionField(sessionKeys.get(i), i));
        }
    }

    public static String getEditTitle(final IMessageProvider messageProvider,
            final String entityKindDictKey, final IIdAndCodeHolder identifiable)
    {
        return messageProvider.getMessage(Dict.EDIT_TITLE,
                messageProvider.getMessage(entityKindDictKey), identifiable.getCode());
    }

    @Override
    public boolean shouldAskForCloseConfirmation()
    {
        return isDirtyCheckEnabled() && formPanel.isDirtyForSavePoint();
    }

    public void setDirtyCheckEnabled(boolean dirtyCheckEnabled)
    {
        this.dirtyCheckEnabled = dirtyCheckEnabled;
    }

    public boolean isDirtyCheckEnabled()
    {
        return dirtyCheckEnabled;
    }

}
