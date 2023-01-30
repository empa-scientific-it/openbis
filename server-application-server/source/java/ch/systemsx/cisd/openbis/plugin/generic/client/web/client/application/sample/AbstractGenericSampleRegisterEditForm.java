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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IOnSuccessAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SpaceModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserButton;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserButton.SampleChooserButtonAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.AttachmentsFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithConfirmations;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * A {@link AbstractGenericEntityRegistrationForm} extension for registering and editing samples.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractGenericSampleRegisterEditForm extends
        AbstractGenericEntityRegistrationForm<SampleType, SampleTypePropertyType>
{
    public static final String ID_SUFFIX_CONTAINER = "container";

    public static final String ID_SUFFIX_PARENT = "parent";

    public static final String ID_SUFFIX_EXPERIMENT = "experiment";

    protected SampleType sampleType; // used for code prefix and container/parent field visibility

    protected AttachmentsFileFieldManager attachmentsManager;

    protected String attachmentsSessionKey;

    protected String simpleId;

    private String initialProjectIdentifierOrNull;

    protected ProjectSelectionWidget projectChooser;

    protected ExperimentChooserFieldAdaptor experimentField;

    private ExperimentIdentifier initialExperimentIdentifierOrNull;

    protected SpaceSelectionWidget groupSelectionWidget;

    private String initialSpaceCodeOrNull;

    protected SampleChooserFieldAdaptor container;

    protected SampleChooserButtonAdaptor parentButton;

    protected ParentSamplesArea parentsArea;

    private String sampleIdentifierForUploadOrNull = null;

    private Button saveUploadButton;

    private Sample parentOrNull;

    protected boolean projectSamplesEnabled;

    protected AbstractGenericSampleRegisterEditForm(
            IViewContext<IGenericClientServiceAsync> viewContext,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            ActionContext actionContext)
    {
        this(viewContext, inputWidgetDescriptions, actionContext, null);
    }

    protected AbstractGenericSampleRegisterEditForm(
            IViewContext<IGenericClientServiceAsync> viewContext,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            ActionContext actionContext, IIdAndCodeHolder identifiable)
    {
        super(viewContext, inputWidgetDescriptions, identifiable, EntityKind.SAMPLE);
        this.simpleId = createSimpleId(identifiable, EntityKind.SAMPLE);
        this.attachmentsSessionKey = simpleId + "_attachments";
        List<String> sessionKeys = new ArrayList<String>();
        sessionKeys.add(attachmentsSessionKey);
        this.projectSamplesEnabled = viewContext.getModel().getApplicationInfo().isProjectSamplesEnabled();
        addUploadFeatures(sessionKeys);
        extractInitialValues(actionContext);
        saveUploadButton = createSaveAndUploadButton();
        ApplicationInfo applicationInfo = viewContext.getModel().getApplicationInfo();
        boolean cifexConfigured =
                StringUtils.isNotBlank(applicationInfo.getCifexRecipient())
                        && StringUtils.isNotBlank(applicationInfo.getCifexURL());
        if (cifexConfigured)
        {
            formPanel.addButton(saveUploadButton);
        }
    }

    private void extractInitialValues(ActionContext context)
    {
        this.initialExperimentIdentifierOrNull = tryGetExperimentIdentifier(context);
        this.initialSpaceCodeOrNull = tryGetSpaceCode(context);
        parentOrNull = context.getParent();
    }

    private ExperimentIdentifier tryGetExperimentIdentifier(ActionContext context)
    {
        return (context.tryGetExperiment() == null) ? null : ExperimentIdentifier
                .createIdentifier(context.tryGetExperiment());
    }

    private String tryGetSpaceCode(ActionContext context)
    {
        return (context.tryGetSpaceCode() == null) ? null : context.tryGetSpaceCode();
    }

    private ExperimentChooserFieldAdaptor createExperimentField()
    {
        String label = viewContext.getMessage(Dict.EXPERIMENT);
        ExperimentChooserFieldAdaptor experimentField = ExperimentChooserField.create(label, false, initialExperimentIdentifierOrNull,
                viewContext.getCommonViewContext());

        experimentField.getChooserField().setId(getId() + ID_SUFFIX_EXPERIMENT);
        experimentField.getChooserField().addChosenEntityListener(
                new IChosenEntitiesListener<TableModelRowWithObject<Experiment>>()
                    {
                        @Override
                        public void entitiesChosen(
                                List<TableModelRowWithObject<Experiment>> entities)
                        {
                            if (entities.isEmpty() == false)
                            {
                                groupSelectionWidget.setValue(new SpaceModel(entities.get(0)
                                        .getObjectOrNull().getProject().getSpace()));
                                if (projectChooser != null)
                                {
                                    projectChooser.setValue(new ProjectSelectionWidget.ProjectComboModel(entities.get(0)
                                            .getObjectOrNull().getProject(), true));
                                }
                            }
                        }
                    });
        return experimentField;
    }

    @Override
    protected void setUploadEnabled(boolean enabled)
    {
        super.setUploadEnabled(enabled);
        saveUploadButton.setEnabled(enabled);
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
                        if (attachmentsManager.filesDefined() > 0)
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

    private Button createSaveAndUploadButton()
    {
        ButtonWithConfirmations button = new ButtonWithConfirmations();
        button.setText(viewContext.getMessage(Dict.BUTTON_SAVE_AND_UPLOAD));
        addSaveButtonConfirmationListener(button);
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        sampleIdentifierForUploadOrNull = createSampleIdentifier();
                        if (attachmentsManager.filesDefined() > 0)
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
        return button;
    }

    protected <R, T extends AbstractRegistrationForm.AbstractRegistrationCallback<R>> T enrichWithPostRegistration(
            T callback)
    {
        callback.addOnFailureAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sampleIdentifierForUploadOrNull = null;
                }
            });
        callback.addOnSuccessAction(new IOnSuccessAction<R>()
            {
                @Override
                public void execute(R result)
                {
                    if (StringUtils.isBlank(sampleIdentifierForUploadOrNull) == false)
                    {
                        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext
                                .getCommonViewContext())
                                        .getDataSetUploadTab(sampleIdentifierForUploadOrNull));
                    }
                    sampleIdentifierForUploadOrNull = null;
                }
            });
        return callback;
    }

    protected abstract void save();

    @Override
    public final void submitValidForm()
    {
    }

    @Override
    protected PropertiesEditor<SampleType, SampleTypePropertyType> createPropertiesEditor(
            String id, Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            IViewContext<ICommonClientServiceAsync> context)
    {
        SamplePropertyEditor editor =
                new SamplePropertyEditor(id, inputWidgetDescriptions, context);
        return editor;
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(wrapUnaware(experimentField.getField()));
        if (projectSamplesEnabled)
        {
            fields.add(projectChooser.asDatabaseModificationAware());
        }
        fields.add(groupSelectionWidget.asDatabaseModificationAware());
        fields.add(wrapUnaware(parentsArea));
        fields.add(wrapUnaware(parentButton.getField()));
        fields.add(wrapUnaware(container.getField()));
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

    @Override
    protected String getGeneratedCodePrefix()
    {
        // for samples prefix is taken from sample type property
        return sampleType.getGeneratedCodePrefix();
    }

    @Override
    protected void postRenderingTask()
    {
        if (parentOrNull != null)
        {
            parentsArea.appendItem(parentOrNull.getIdentifier());
        }
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        groupSelectionWidget =
                new SpaceSelectionWidget(viewContext, getId(), true, false, initialSpaceCodeOrNull);
        groupSelectionWidget.setId("register-sample-space-selection");
        FieldUtil.markAsMandatory(groupSelectionWidget);

        if (projectSamplesEnabled)
        {
            projectChooser =
                    new ProjectSelectionWidget(viewContext, simpleId, initialProjectIdentifierOrNull);
            projectChooser.setFieldLabel(viewContext.getMessage(Dict.PROJECT));
            projectChooser.addSelectionChangedListener(new SelectionChangedListener<ProjectSelectionWidget.ProjectComboModel>()
                {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<ProjectSelectionWidget.ProjectComboModel> se_)
                    {
                        final SelectionChangedEvent<ProjectSelectionWidget.ProjectComboModel> se = se_;
                        Scheduler.get().scheduleDeferred(new ScheduledCommand()
                            {
                                @Override
                                public void execute()
                                {

                                    if (se.getSelectedItem() == null)
                                    {
                                        return;
                                    }

                                    Project project = se.getSelectedItem().get(ModelDataPropertyNames.OBJECT);
                                    ExperimentIdentifier currentExperiment = experimentField.tryToGetValue();
                                    if (currentExperiment != null
                                            && currentExperiment.getIdentifier().startsWith(project.getIdentifier() + "/") == false)
                                    {
                                        experimentField.getChooserField().setValue(null);
                                    }

                                    SpaceModel currentSpace = groupSelectionWidget.getValue();
                                    String currentSpaceCode = null;
                                    if (currentSpace != null)
                                    {
                                        currentSpaceCode = currentSpace.getBaseObject().getCode();
                                    }

                                    String newSpaceCode = project.getSpace().getCode();

                                    if (currentSpace == null || currentSpaceCode.equals(newSpaceCode) == false)
                                    {
                                        groupSelectionWidget.setValue(new SpaceModel(project.getSpace()));
                                    }
                                }
                            });
                    }
                });
            FieldUtil.setMandatoryFlag(projectChooser, false);
        }

        groupSelectionWidget.setFieldLabel(viewContext.getMessage(Dict.GROUP));
        groupSelectionWidget.addSelectionChangedListener(new SelectionChangedListener<SpaceModel>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<SpaceModel> se_)
                {
                    final SelectionChangedEvent<SpaceModel> se = se_;

                    Scheduler.get().scheduleDeferred(new ScheduledCommand()
                        {
                            @Override
                            public void execute()
                            {
                                ExperimentIdentifier currentExperiment = experimentField.tryToGetValue();
                                if (currentExperiment != null
                                        && currentExperiment.getIdentifier()
                                                .startsWith("/" + se.getSelectedItem().getBaseObject().getCode() + "/") == false)
                                {
                                    experimentField.getChooserField().setValue(null);
                                }

                                if (projectChooser != null && projectChooser.getValue() != null &&
                                        ((Project) projectChooser.getValue().get(ModelDataPropertyNames.OBJECT)).getSpace().getCode() != se
                                                .getSelectedItem().getBaseObject().getCode())
                                {
                                    String currentProjectSpace =
                                            ((Project) projectChooser.getValue().get(ModelDataPropertyNames.OBJECT)).getSpace().getCode();
                                    String newSpace = se.getSelectedItem().getBaseObject().getCode();
                                    if (currentProjectSpace.equals(newSpace) == false)
                                    {
                                        projectChooser.setValue(null);
                                    }
                                }
                            }
                        });
                }
            });
        parentButton =
                SampleChooserButton.create(null, viewContext.getMessage(Dict.ADD_PARENT), true,
                        false, false, viewContext.getCommonViewContext(), getId()
                                + ID_SUFFIX_PARENT,
                        SampleTypeDisplayID.SAMPLE_REGISTRATION_PARENT_CHOOSER
                                .withSuffix(getSampleTypeCode()),
                        true);
        parentsArea = new ParentSamplesArea(viewContext, getId());
        SampleChooserButton parentChooserButton = parentButton.getChooserButton();
        parentChooserButton
                .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Sample>>()
                    {
                        @Override
                        public void entitiesChosen(List<TableModelRowWithObject<Sample>> entities)
                        {
                            for (TableModelRowWithObject<Sample> row : entities)
                            {
                                parentsArea.appendItem(row.getObjectOrNull().getIdentifier());
                            }
                        }
                    });
        container =
                SampleChooserField.create(viewContext.getMessage(Dict.PART_OF_SAMPLE), false, null,
                        true, false, false, viewContext.getCommonViewContext(), getId()
                                + ID_SUFFIX_CONTAINER,
                        SampleTypeDisplayID.SAMPLE_REGISTRATION_CONTAINER_CHOOSER
                                .withSuffix(getSampleTypeCode()),
                        false);
        experimentField = createExperimentField();
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
                    AbstractGenericSampleRegisterEditForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
        setContainerAndParentVisibility(sampleType);
    }

    protected final String createSampleIdentifier()
    {
        return createSampleIdentifier(groupSelectionWidget, codeField);
    }

    public static final String createSampleIdentifier(SpaceSelectionWidget spaceSelectionWidget,
            Field<String> codeField)
    {
        final Space space = spaceSelectionWidget.tryGetSelectedSpace();
        final boolean shared = SpaceSelectionWidget.isSharedSpace(space);
        final String code = codeField.getValue();
        final StringBuilder builder = new StringBuilder("/");
        if (shared == false)
        {
            builder.append(space.getCode() + "/");
        }
        builder.append(code);
        return builder.toString().toUpperCase();
    }

    /** sets visibility of container and parent fields dependent on sample type */
    private final void setContainerAndParentVisibility(final SampleType sampleType)
    {
        boolean showContainer = sampleType.isShowContainer();
        boolean showParents = sampleType.isShowParents();
        FieldUtil.setVisibility(showContainer, container.getField());
        FieldUtil.setVisibility(showParents, parentButton.getField(), parentsArea);
    }

    private String getSampleTypeCode()
    {
        return sampleType.getCode();
    }

    protected String[] getParents()
    {
        return parentsArea.tryGetSamples();
    }

}
