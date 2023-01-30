package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;

public abstract class AbstractBatchRegistrationForm extends AbstractRegistrationForm
{
    public final class BatchRegistrationCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<List<BatchRegistrationResult>>
    {
        public BatchRegistrationCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(
                final List<BatchRegistrationResult> result)
        {
            final StringBuilder builder = new StringBuilder();
            for (final BatchRegistrationResult batchRegistrationResult : result)
            {
                builder.append("<b>" + batchRegistrationResult.getFileName() + "</b>: ");
                builder.append(batchRegistrationResult.getMessage());
                builder.append("<br />");
            }
            return Arrays.asList(new HtmlMessageElement(builder.toString()));
        }
    }

    private static final String FIELD_LABEL_TEMPLATE = "File";

    private static final int NUMBER_OF_FIELDS = 1;

    protected final String sessionKey;

    protected Field<?> templateField;

    protected final BasicFileFieldManager fileFieldsManager;

    protected final TextField<String> emailField;

    protected final RadioGroup asynchronous;

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    public AbstractBatchRegistrationForm(IViewContext<ICommonClientServiceAsync> viewContext,
            String id, String sessionKey)
    {
        super(viewContext, id);
        setResetButtonVisible(true);
        this.sessionKey = sessionKey;
        this.viewContext = viewContext;
        setScrollMode(Scroll.AUTO);
        asynchronous = createAsyncRadio();
        emailField =
                createEmailField(viewContext.getModel().getSessionContext().getUser()
                        .getUserEmail());
        templateField = createTemplateField();
        fileFieldsManager =
                new BasicFileFieldManager(sessionKey, NUMBER_OF_FIELDS, FIELD_LABEL_TEMPLATE);
        fileFieldsManager.setMandatory();
        addUploadFeatures(sessionKey);
    }

    /**
     * Perform registration on the service
     */
    protected abstract void save();

    /**
     * Adds additional fields to the form panel. File upload field will be added automatically after specific fields.
     */
    protected void addSpecificFormFields(FormPanel form)
    {

    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    private static class RadioAsync extends Radio
    {
        private boolean async;

        private RadioAsync(boolean async)
        {
            this.async = async;
        }

        public boolean isAsync()
        {
            return async;
        }
    }

    protected boolean isAsync()
    {
        return isAsync(asynchronous);
    }

    private boolean isAsync(RadioGroup group)
    {
        Radio radio = group.getValue();
        if (radio == null)
        {
            return false;
        }
        if (radio instanceof RadioAsync)
        {
            return ((RadioAsync) radio).isAsync();
        }
        return false;
    }

    private RadioGroup createAsyncRadio()
    {
        final Radio radioSync = new RadioAsync(false);
        radioSync.setBoxLabel("Wait for result");
        radioSync.setValue(false);

        final Radio radioAsync = new RadioAsync(true);
        radioAsync.setBoxLabel("Run in background (and confirm)");
        radioAsync.setValue(true);

        final RadioGroup radioGroup = new RadioGroup();
        radioGroup.setFieldLabel("Wait for result");
        radioGroup.add(radioSync);
        radioGroup.add(radioAsync);

        radioGroup.addListener(Events.Change, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    if (isAsync(radioGroup))
                    {
                        formPanel.remove(asynchronous);
                        for (FileUploadField attachmentField : fileFieldsManager.getFields())
                        {
                            formPanel.remove(wrapUnaware((Field<?>) attachmentField).get());
                        }
                        addOnlyFormFields(true);
                    } else
                    {
                        formPanel.remove(emailField);
                    }
                    formPanel.layout();
                }
            });
        return radioGroup;
    }

    private TextField<String> createEmailField(String userEmail)
    {
        TextField<String> field = new TextField<String>();
        field.setAllowBlank(false);
        field.setFieldLabel("Send confirmation email to");
        FieldUtil.markAsMandatory(field);
        field.setValue(userEmail);
        field.setValidateOnBlur(true);
        field.setRegex(GenericConstants.EMAIL_REGEX);
        field.getMessages().setRegexText("Expected email address format: user@domain.com");
        AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
        FieldUtil.addInfoIcon(field,
                "All relevant notifications will be send to this email address",
                infoIcon.createImage());
        return field;
    }

    protected Field<?> createTemplateField()
    {
        LabelField result =
                new LabelField(LinkRenderer.renderAsLink(viewContext
                        .getMessage(Dict.FILE_TEMPLATE_LABEL)));
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {

                    WindowUtils.openWindow(createTemplateUrl());
                }
            });
        return result;
    }

    protected String createTemplateUrl()
    {
        return null;
    }

    @Override
    protected void submitValidForm()
    {
    }

    @Override
    protected void resetFieldsAfterSave()
    {
        for (FileUploadField attachmentField : fileFieldsManager.getFields())
        {
            attachmentField.reset();
        }
        updateDirtyCheckAfterSave();
    }

    protected void addOnlyFormFields(boolean forceAddEmailField)
    {
        formPanel.add(asynchronous);
        if (forceAddEmailField || isAsync())
        {
            formPanel.add(emailField);
        }
        if (templateField != null)
        {
            formPanel.add(templateField);
        }
        for (FileUploadField attachmentField : fileFieldsManager.getFields())
        {
            formPanel.add(wrapUnaware((Field<?>) attachmentField).get());
        }
    }

    private final void addFormFields()
    {
        addSpecificFormFields(formPanel);

        addOnlyFormFields(false);

        formPanel.addListener(Events.BeforeSubmit, new Listener<FormEvent>()
            {
                @Override
                public void handleEvent(FormEvent be)
                {
                    infoBox.displayProgress(messageProvider.getMessage(Dict.PROGRESS_UPLOADING));
                }
            });

        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    infoBox.displayProgress(messageProvider.getMessage(Dict.PROGRESS_PROCESSING));
                    save();
                }

                @Override
                protected void setUploadEnabled()
                {
                    AbstractBatchRegistrationForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        addSaveButtonConfirmationListener();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (fileFieldsManager.filesDefined() > 0)
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            save();
                        }
                    }
                }
            });
    }

    @Override
    protected void setUploadEnabled(boolean enabled)
    {
        super.setUploadEnabled(enabled);
        infoBoxResetListener.setEnabled(enabled);
    }
}
