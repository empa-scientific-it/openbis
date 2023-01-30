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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.AttachmentsFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;

/**
 * A {@link AbstractGenericEntityRegistrationForm} extension for registering and editing experiments.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractGenericExperimentRegisterEditForm extends
        AbstractGenericEntityRegistrationForm<ExperimentType, ExperimentTypePropertyType>
{
    protected AttachmentsFileFieldManager attachmentsManager;

    protected String attachmentsSessionKey;

    protected ProjectSelectionWidget projectChooser;

    private String initialProjectIdentifierOrNull;

    protected ExperimentSamplesArea samplesArea;

    protected final String samplesSessionKey;

    protected BasicFileFieldManager importSamplesFileManager;

    private SampleTypeSelectionWidget importSampleTypeSelection;

    protected Radio existingSamplesRadio;

    private Radio importSamplesRadio;

    protected CheckBoxField autoGenerateCodes;

    protected String simpleId;

    private LabelField templateField;

    protected AbstractGenericExperimentRegisterEditForm(
            IViewContext<IGenericClientServiceAsync> viewContext,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            ActionContext context)
    {
        this(viewContext, inputWidgetDescriptions, context, null);
    }

    protected AbstractGenericExperimentRegisterEditForm(
            IViewContext<IGenericClientServiceAsync> viewContext,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            ActionContext actionContext, IIdAndCodeHolder identifiable)
    {
        super(viewContext, inputWidgetDescriptions, identifiable, EntityKind.EXPERIMENT);
        simpleId = createSimpleId(identifiable, EntityKind.EXPERIMENT);
        attachmentsSessionKey = simpleId + "_attachments";
        samplesSessionKey = simpleId + "_samples";
        List<String> sesionKeys = new ArrayList<String>();
        sesionKeys.add(attachmentsSessionKey);
        sesionKeys.add(samplesSessionKey);
        addUploadFeatures(sesionKeys);
        extractInitialValues(actionContext);
    }

    private void extractInitialValues(ActionContext context)
    {
        this.initialProjectIdentifierOrNull = context.tryGetProjectIdentifier();
    }

    protected void updateSamples()
    {
        Boolean useExistingSamples = existingSamplesRadio.getValue();
        FieldUtil.setVisibility(useExistingSamples, samplesArea);
        FieldUtil.setVisibility(useExistingSamples == false, importSampleTypeSelection,
                autoGenerateCodes, importSamplesFileManager.getFields().get(0), templateField);
    }

    private void redefineSaveListeners()
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
                        if (attachmentsManager.filesDefined() > 0
                                || importSamplesFileManager.filesDefined() > 0)
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

    protected abstract void save();

    protected String extractProjectIdentifier()
    {
        return projectChooser.tryGetSelectedProject().getIdentifier();
    }

    @Override
    public final void submitValidForm()
    {
    }

    @Override
    protected PropertiesEditor<ExperimentType, ExperimentTypePropertyType> createPropertiesEditor(
            String id, Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            IViewContext<ICommonClientServiceAsync> context)
    {
        ExperimentPropertyEditor editor =
                new ExperimentPropertyEditor(id, inputWidgetDescriptions, context);
        return editor;
    }

    RadioGroup createSamplesSourceRadio(Radio existing, Radio importFromFile)
    {
        RadioGroup result = new RadioGroup();
        result.setSelectionRequired(true);
        result.setFieldLabel(viewContext.getMessage(Dict.SAMPLES));
        result.setOrientation(Orientation.HORIZONTAL);
        result.add(existing);
        result.add(importFromFile);
        result.addListener(Events.Change, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    updateSamples();
                }
            });
        return result;
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(projectChooser.asDatabaseModificationAware());
        fields.add(wrapUnaware(createSamplesSourceRadio(existingSamplesRadio, importSamplesRadio)));
        fields.add(wrapUnaware(samplesArea));
        fields.add(importSampleTypeSelection.asDatabaseModificationAware());
        fields.add(wrapUnaware(autoGenerateCodes));
        for (FileUploadField samplesFileField : importSamplesFileManager.getFields())
        {
            fields.add(wrapUnaware((Field<?>) samplesFileField));
        }
        fields.add(wrapUnaware(templateField));
        return fields;
    }

    @Override
    protected void addFormFieldsToPanel(FormPanel panel)
    {
        super.addFormFieldsToPanel(panel);
        attachmentsManager.addAttachmentFieldSetsToPanel(panel);
    }

    @Override
    protected void resetPanel()
    {
        super.resetPanel();
        attachmentsManager.resetAttachmentFieldSetsInPanel(formPanel);
    }

    @Override
    protected void revertPanel()
    {
        super.revertPanel();
        attachmentsManager.resetAttachmentFieldSetsInPanel(formPanel);
    }

    private static LabelField createTemplateField(String label, final String sampleTypeName, 
            final SampleTypeSelectionWidget typeSelection, final CheckBox autoGenerate)
    {
        LabelField result = new LabelField(LinkRenderer.renderAsLink(label));
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    if (typeSelection.tryGetSelectedSampleType() != null)
                    {
                        WindowUtils.openWindow(UrlParamsHelper.createTemplateURL(EntityKind.SAMPLE,
                                typeSelection.tryGetSelected(), autoGenerate.getValue(), false,
                                false, BatchOperationKind.REGISTRATION));
                    } else
                    {
                        
                        GWTUtils.alert(sampleTypeName + " not selected.",
                                sampleTypeName + " must be selected before downloading file template.");
                    }
                }
            });
        return result;
    }

    protected String[] getSamples()
    {
        if (existingSamplesRadio.getValue())
            return samplesArea.tryGetModifiedSampleCodes();
        else
            return null;
    }

    protected SampleType getSampleType()
    {
        if (existingSamplesRadio.getValue() == false)
            return importSampleTypeSelection.tryGetSelectedSampleType();
        else
            return null;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        projectChooser =
                new ProjectSelectionWidget(viewContext, simpleId, initialProjectIdentifierOrNull);
        projectChooser.setFieldLabel(viewContext.getMessage(Dict.PROJECT));
        FieldUtil.markAsMandatory(projectChooser);

        samplesArea = new ExperimentSamplesArea(viewContext, simpleId);

        importSamplesFileManager = new BasicFileFieldManager(samplesSessionKey, 1, "File");
        importSamplesFileManager.setMandatory();
        importSampleTypeSelection =
                new SampleTypeSelectionWidget(viewContext, simpleId, false, false, true, null,
                        SampleTypeDisplayID.EXPERIMENT_REGISTRATION);
        FieldUtil.markAsMandatory(importSampleTypeSelection);

        existingSamplesRadio = cerateExistingSamplesRadio();
        importSamplesRadio = createImportRadio();
        autoGenerateCodes = createAutoGenerateCheckbox();
        templateField =
                createTemplateField(viewContext.getMessage(Dict.FILE_TEMPLATE_LABEL),
                        viewContext.getMessage(Dict.SAMPLE_TYPE),
                        importSampleTypeSelection, autoGenerateCodes);

        attachmentsManager = new AttachmentsFileFieldManager(attachmentsSessionKey, viewContext);
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    save();
                }

                @Override
                protected void setUploadEnabled()
                {
                    AbstractGenericExperimentRegisterEditForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    protected static Radio createImportRadio()
    {
        Radio importRadio = new Radio();
        importRadio.setBoxLabel("register from a file and attach");
        return importRadio;
    }

    protected static CheckBoxField createAutoGenerateCheckbox()
    {
        return new CheckBoxField("Generate codes automatically", false);
    }

    private Radio cerateExistingSamplesRadio()
    {
        Radio existingRadio = new Radio();
        existingRadio.setBoxLabel("specify the list of existing " + viewContext.getMessage(Dict.SAMPLES).toLowerCase());
        existingRadio.setValue(true);
        return existingRadio;
    }

}
