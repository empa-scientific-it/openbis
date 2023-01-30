package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IComponentWithCloseConfirmation;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.BorderLayoutDataFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FormPanelWithSavePoint;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FormPanelWithSavePoint.DirtyChangeEvent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

public class NewEntityTypeForm extends ContentPanel implements IComponentWithCloseConfirmation
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "new-entity-type-form";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    //
    // Entity Type
    //
    private final EntityKind kind;

    private EntityType entityToEdit;

    private NewETNewPTAssigments newTypeWithAssigments;

    private final ComponentProvider componentProvider;

    //
    // Entity Form Related
    //
    private FormPanelWithSavePoint dialogForm;

    private PropertyTypeAssignmentGrid propertyGrid;

    private AbstractEntityTypeGrid<? extends EntityType> typeGrid;

    private Button saveButton;

    private Html unsavedChangesInfo;

    private boolean shouldAskForCloseConfirmation = true;

    //
    // Form Creation
    //
    private NewEntityTypeForm(EntityKind kind, EntityType entityToEdit, IViewContext<ICommonClientServiceAsync> viewContext,
            ComponentProvider componentProvider)
    {
        this.kind = kind;
        this.viewContext = viewContext;
        this.componentProvider = componentProvider;

        // Main panel
        this.setLayout(new BorderLayout());
        this.setHeaderVisible(false);
        this.setBorders(false);
        this.setBodyBorder(false);

        if (entityToEdit != null)
        {
            loadEntityTypeToEdit(this, entityToEdit); // Get a copy of the entity for edition to not affect other views using the same object
        } else
        {
            initForm();
        }
    }

    private class LoadCallBack<K extends EntityType> implements AsyncCallback<List<K>>
    {
        final NewEntityTypeForm newEntityTypeForm;

        final EntityType entityToEdit;

        public LoadCallBack(
                final NewEntityTypeForm newEntityTypeForm,
                final EntityType entityToEdit)
        {
            this.newEntityTypeForm = newEntityTypeForm;
            this.entityToEdit = entityToEdit;
        }

        @Override
        public void onSuccess(List<K> result)
        {
            for (EntityType entityType : result)
            {
                if (entityType.getCode().equals(entityToEdit.getCode()))
                {
                    newEntityTypeForm.entityToEdit = entityType;
                    break;
                }
            }
            initForm();
        }

        @Override
        public void onFailure(Throwable caught)
        {
            // TO-DO Should never happen
        }
    }

    private void loadEntityTypeToEdit(
            final NewEntityTypeForm newEntityTypeForm,
            final EntityType entityToEdit)
    {

        switch (kind)
        {
            case SAMPLE:
                this.viewContext.getCommonService().listSampleTypes(new LoadCallBack<SampleType>(newEntityTypeForm, entityToEdit));
                break;
            case DATA_SET:
                this.viewContext.getCommonService().listDataSetTypes(new LoadCallBack<DataSetType>(newEntityTypeForm, entityToEdit));
                break;
            case EXPERIMENT:
                this.viewContext.getCommonService().listExperimentTypes(new LoadCallBack<ExperimentType>(newEntityTypeForm, entityToEdit));
                break;
            case MATERIAL:
                this.viewContext.getCommonService().listMaterialTypes(new LoadCallBack<MaterialType>(newEntityTypeForm, entityToEdit));
                break;
        }
    }

    public static DatabaseModificationAwareComponent create(
            EntityKind kind,
            EntityType entityToEdit,
            final IViewContext<ICommonClientServiceAsync> viewContext,
            ComponentProvider componentProvider)
    {
        NewEntityTypeForm form = new NewEntityTypeForm(kind, entityToEdit, viewContext, componentProvider);
        return new DatabaseModificationAwareComponent(form, new CompositeDatabaseModificationObserver());
    }

    private void initForm()
    {
        // Cleanup for the form and for the in memory structure
        removeAll();
        newTypeWithAssigments = new NewETNewPTAssigments();
        newTypeWithAssigments.setAssigments(new ArrayList<NewPTNewAssigment>());

        // Top panel
        initEntityTypeForm();
        if (entityToEdit == null)
        {
            initCreateEntity();
        } else
        {
            initEditEntity();
        }
        dialogForm.setHeaderVisible(true);
        dialogForm.setHeading("Entity Type Information:");
        dialogForm.setBorders(false);
        dialogForm.setBodyBorder(false);
        dialogForm.setLabelWidth(180);

        add(dialogForm, BorderLayoutDataFactory.create(LayoutRegion.NORTH, 370));

        // Central panel
        propertyGrid = (PropertyTypeAssignmentGrid) PropertyTypeAssignmentGrid.create(
                viewContext,
                null,
                newTypeWithAssigments,
                entityToEdit != null
                ).getComponent();
        propertyGrid.setLayoutOnChange(true);

        ContentPanel gridPanel = new ContentPanel();
        gridPanel.setLayout(new BorderLayout());
        gridPanel.setLayoutOnChange(true);
        gridPanel.setHeaderVisible(true);
        gridPanel.setHeading("Assigned Property Types:");
        gridPanel.setBorders(false);
        gridPanel.setBodyBorder(false);
        gridPanel.add(propertyGrid, BorderLayoutDataFactory.create(LayoutRegion.CENTER, 170));

        add(gridPanel, BorderLayoutDataFactory.create(LayoutRegion.CENTER, 170));

        // Bottom panel
        ContentPanel bottomPanel = new ContentPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setLayoutOnChange(false);
        bottomPanel.setHeaderVisible(false);
        bottomPanel.setBorders(false);
        bottomPanel.setBodyBorder(false);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setMinButtonWidth(100);
        buttonBar.setAlignment(HorizontalAlignment.RIGHT);

        unsavedChangesInfo = createUnsavedChangesInfo();

        dialogForm.addDirtyChangeListener(new Listener<DirtyChangeEvent>()
            {
                @Override
                public void handleEvent(DirtyChangeEvent be)
                {
                    unsavedChangesInfo.setVisible(isDirty());
                }
            });
        propertyGrid.addDirtyChangeListener(new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent event)
                {
                    unsavedChangesInfo.setVisible(isDirty());
                }
            });

        buttonBar.add(unsavedChangesInfo);

        saveButton = createSaveButton();
        saveButton.setStyleAttribute("padding-right", "10px");
        buttonBar.add(saveButton);

        bottomPanel.add(buttonBar, BorderLayoutDataFactory.create(LayoutRegion.EAST, 300));

        add(bottomPanel, BorderLayoutDataFactory.create(LayoutRegion.SOUTH, 30));

        layout();
    }

    private void initEntityTypeForm()
    {
        AddEntityTypeDialog<? extends EntityType> dialog = null;

        switch (kind)
        {
            case SAMPLE:
                typeGrid = (SampleTypeGrid) SampleTypeGrid.create(viewContext, componentProvider).getComponent();
                dialog = ((SampleTypeGrid) typeGrid).getNewDialog((SampleType) new SampleType());
                break;
            case DATA_SET:
                typeGrid = (DataSetTypeGrid) DataSetTypeGrid.create(viewContext, componentProvider).getComponent();
                dialog = ((DataSetTypeGrid) typeGrid).getNewDialog((DataSetType) new DataSetType());
                break;
            case EXPERIMENT:
                typeGrid = (ExperimentTypeGrid) ExperimentTypeGrid.create(viewContext, componentProvider).getComponent();
                dialog = ((ExperimentTypeGrid) typeGrid).getNewDialog((ExperimentType) new ExperimentType());
                break;
            case MATERIAL:
                typeGrid = (MaterialTypeGrid) MaterialTypeGrid.create(viewContext, componentProvider).getComponent();
                dialog = (AddEntityTypeDialog<MaterialType>) ((MaterialTypeGrid) typeGrid)
                        .getNewDialog((MaterialType) new MaterialType());
                break;
        }

        // afterwards
        List<Component> dialogFormIntoList = dialog.getItems();
        dialogForm = (FormPanelWithSavePoint) dialogFormIntoList.get(0);

    }

    private void initCreateEntity()
    {
        switch (kind)
        {
            case SAMPLE:
                SampleType sampleType = new SampleType();
                sampleType.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());
                newTypeWithAssigments.setEntity(sampleType);
                break;
            case DATA_SET:
                DataSetType dataSetType = new DataSetType();
                dataSetType.setDataSetTypePropertyTypes(new ArrayList<DataSetTypePropertyType>());
                newTypeWithAssigments.setEntity(dataSetType);
                break;
            case EXPERIMENT:
                ExperimentType experimentType = new ExperimentType();
                experimentType.setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());
                newTypeWithAssigments.setEntity(experimentType);
                break;
            case MATERIAL:
                MaterialType materialType = new MaterialType();
                materialType.setMaterialTypePropertyTypes(new ArrayList<MaterialTypePropertyType>());
                newTypeWithAssigments.setEntity(materialType);
                break;
        }

        newTypeWithAssigments.getEntity().setCode("" + System.currentTimeMillis()); // Just needed so the grid don't break
    }

    private void initEditEntity()
    {
        List<Field<?>> formFields = dialogForm.getFields();

        ((CodeField) formFields.get(0)).setValue(entityToEdit.getCode());
        ((CodeField) formFields.get(0)).setEnabled(false);
        FieldUtil.setValueWithUnescaping(((DescriptionField) formFields.get(1)), entityToEdit.getDescription());
        if (entityToEdit.getValidationScript() != null)
        {
            ((ScriptChooserField) formFields.get(2)).setValue(entityToEdit.getValidationScript().getName());
        }
        switch (kind)
        {
            case SAMPLE:
                SampleType sampleToEdit = (SampleType) entityToEdit;
                ((CheckBoxField) formFields.get(3)).setValue(sampleToEdit.isListable());
                ((CheckBoxField) formFields.get(4)).setValue(sampleToEdit.isShowContainer());
                ((CheckBoxField) formFields.get(5)).setValue(sampleToEdit.isShowParents());
                ((CheckBoxField) formFields.get(6)).setValue(sampleToEdit.isSubcodeUnique());
                ((CheckBoxField) formFields.get(7)).setValue(sampleToEdit.isAutoGeneratedCode());
                ((CheckBoxField) formFields.get(8)).setValue(sampleToEdit.isShowParentMetadata());
                ((TextField<String>) formFields.get(9)).setValue(sampleToEdit.getGeneratedCodePrefix());
                break;
            case DATA_SET:
                DataSetType datasetToEdit = (DataSetType) entityToEdit;
                ((CheckBoxField) formFields.get(3)).setValue(datasetToEdit.isDeletionDisallow());
                FieldUtil.setValueWithUnescaping(((TextField<String>) formFields.get(4)), datasetToEdit.getMainDataSetPattern());
                FieldUtil.setValueWithUnescaping(((TextField<String>) formFields.get(5)), datasetToEdit.getMainDataSetPath());
                break;
        }

        newTypeWithAssigments.setEntity(entityToEdit);

        for (int i = 0; i < entityToEdit.getAssignedPropertyTypes().size(); i++)
        {
            NewPTNewAssigment assigment = new NewPTNewAssigment();
            assigment.setExistingPropertyType(true);
            assigment.setPropertyType(entityToEdit.getAssignedPropertyTypes().get(i).getPropertyType());

            NewETPTAssignment newETPTAssigment = new NewETPTAssignment();
            newETPTAssigment.setPropertyTypeCode(entityToEdit.getAssignedPropertyTypes().get(i).getPropertyType().getCode());
            newETPTAssigment.setEntityKind(entityToEdit.getEntityKind());
            newETPTAssigment.setOrdinal(entityToEdit.getAssignedPropertyTypes().get(i).getOrdinal());
            newETPTAssigment.setSection(entityToEdit.getAssignedPropertyTypes().get(i).getSection());
            newETPTAssigment.setMandatory(entityToEdit.getAssignedPropertyTypes().get(i).isMandatory());
            newETPTAssigment.setDynamic(entityToEdit.getAssignedPropertyTypes().get(i).isDynamic());
            newETPTAssigment.setManaged(entityToEdit.getAssignedPropertyTypes().get(i).isManaged());
            newETPTAssigment.setShownInEditView(entityToEdit.getAssignedPropertyTypes().get(i).isShownInEditView());
            newETPTAssigment.setShowRawValue(entityToEdit.getAssignedPropertyTypes().get(i).getShowRawValue());
            newETPTAssigment.setModificationDate(entityToEdit.getAssignedPropertyTypes().get(i).getModificationDate());

            assigment.setAssignment(newETPTAssigment);

            if (entityToEdit.getAssignedPropertyTypes().get(i).getScript() != null)
            {
                assigment.getAssignment().setScriptName(entityToEdit.getAssignedPropertyTypes().get(i).getScript().getName());
            }

            newTypeWithAssigments.getAssigments().add(assigment);
        }

        newTypeWithAssigments.updateOrdinalToGridOrder(); // Fixes gaps between positions for types created with the old UI.
    }

    private Button createSaveButton()
    {
        final Button save = new Button("Save", new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (dialogForm.isValid())
                    {
                        saveButton.setEnabled(false);

                        // Update Entity Type
                        setEntityFromForm();
                        // Update Entity Type Code at the Property Types
                        for (NewPTNewAssigment assigment : newTypeWithAssigments.getAssigments())
                        {
                            assigment.getAssignment().setEntityTypeCode(newTypeWithAssigments.getEntity().getCode());
                        }
                        // Register/Update Call

                        if (entityToEdit == null)
                        {
                            viewContext.getService().registerEntitytypeAndAssignPropertyTypes(newTypeWithAssigments, new AsyncCallbackEntityType());
                        } else
                        {
                            viewContext.getService().updateEntitytypeAndPropertyTypes(newTypeWithAssigments, new AsyncCallbackEntityType());
                        }
                    }
                }
            });
        return save;
    }

    //
    // Save Functionality
    //
    private void setEntityFromForm()
    {
        List<Field<?>> formFields = dialogForm.getFields();

        switch (kind)
        {
            case SAMPLE:
                SampleType toSaveSample = (SampleType) newTypeWithAssigments.getEntity();
                toSaveSample.setCode((String) formFields.get(0).getValue());
                toSaveSample.setDescription((String) formFields.get(1).getValue());
                if (formFields.get(2).getValue() != null)
                {
                    Script script = new Script();
                    script.setName((String) formFields.get(2).getValue());
                    toSaveSample.setValidationScript(script);
                }
                toSaveSample.setListable((Boolean) formFields.get(3).getValue());
                toSaveSample.setShowContainer((Boolean) formFields.get(4).getValue());
                toSaveSample.setShowParents((Boolean) formFields.get(5).getValue());
                toSaveSample.setSubcodeUnique((Boolean) formFields.get(6).getValue());
                toSaveSample.setAutoGeneratedCode((Boolean) formFields.get(7).getValue());
                toSaveSample.setShowParentMetadata((Boolean) formFields.get(8).getValue());
                toSaveSample.setGeneratedCodePrefix((String) formFields.get(9).getValue());
                newTypeWithAssigments.setEntity(toSaveSample);
                break;
            case DATA_SET:
                DataSetType toSaveDataSet = (DataSetType) newTypeWithAssigments.getEntity();
                toSaveDataSet.setCode((String) formFields.get(0).getValue());
                toSaveDataSet.setDescription((String) formFields.get(1).getValue());
                if (formFields.get(2).getValue() != null)
                {
                    Script script = new Script();
                    script.setName((String) formFields.get(2).getValue());
                    toSaveDataSet.setValidationScript(script);
                }
                toSaveDataSet.setDeletionDisallow((Boolean) formFields.get(3).getValue());
                toSaveDataSet.setMainDataSetPattern((String) formFields.get(4).getValue());
                toSaveDataSet.setMainDataSetPath((String) formFields.get(5).getValue());
                newTypeWithAssigments.setEntity(toSaveDataSet);
                break;
            case EXPERIMENT:
                ExperimentType toSaveExperiment = (ExperimentType) newTypeWithAssigments.getEntity();
                toSaveExperiment.setCode((String) formFields.get(0).getValue());
                toSaveExperiment.setDescription((String) formFields.get(1).getValue());
                if (formFields.get(2).getValue() != null)
                {
                    Script script = new Script();
                    script.setName((String) formFields.get(2).getValue());
                    toSaveExperiment.setValidationScript(script);
                }
                newTypeWithAssigments.setEntity(toSaveExperiment);
                break;
            case MATERIAL:
                MaterialType toSaveMaterial = (MaterialType) newTypeWithAssigments.getEntity();
                toSaveMaterial.setCode((String) formFields.get(0).getValue());
                toSaveMaterial.setDescription((String) formFields.get(1).getValue());
                if (formFields.get(2).getValue() != null)
                {
                    Script script = new Script();
                    script.setName((String) formFields.get(2).getValue());
                    toSaveMaterial.setValidationScript(script);
                }
                newTypeWithAssigments.setEntity(toSaveMaterial);
                break;
        }
    }

    public static String getTabId(EntityKind kind, EntityType type)
    {
        if (type == null) // Create new entity option
        {
            return NewEntityTypeForm.BROWSER_ID + "-" + kind.name() + "-New";
        } else
        // Edit existing entity option
        {
            return NewEntityTypeForm.BROWSER_ID + "-" + kind.name() + "-" + type.getCode();
        }
    }

    private class AsyncCallbackEntityType implements AsyncCallback<String>
    {
        @Override
        public void onFailure(Throwable throwable)
        {
            String message = "Error";
            if (throwable instanceof UserFailureException)
            {
                // Show error message
                UserFailureException userException = (UserFailureException) throwable;
                String details = GWTUtils.translateToHtmlLineBreaks(userException.getMessage());
                if (details != null)
                {
                    message = details;
                }
            }
            GWTUtils.alert("Error", message);
            saveButton.setEnabled(true);
        }

        @Override
        public void onSuccess(String result)
        {
            if (entityToEdit == null)
            {
                MessageBox.info("Success", "Registration Successful.", null);
            } else
            {
                MessageBox.info("Success", "Update Successful.", null);
            }

            shouldAskForCloseConfirmation = false;

            // Close Tab
            MainTabPanel tabPanel = (MainTabPanel) componentProvider.tryGetMainTabPanel();
            String getTabId = getTabId(kind, entityToEdit) + MainTabPanel.TAB_SUFFIX;
            TabItem item = tabPanel.getItemByItemId(getTabId);
            item.close();
        }
    }

    private Html createUnsavedChangesInfo()
    {
        Html result = new Html(viewContext.getMessage(Dict.UNSAVED_FORM_CHANGES_INFO));
        result.addStyleName("unsaved-changes-info");
        result.setVisible(false);
        return result;
    }

    private boolean isDirty()
    {
        return dialogForm.isDirtyForSavePoint() || propertyGrid.isDirty();
    }

    @Override
    public boolean shouldAskForCloseConfirmation()
    {
        return shouldAskForCloseConfirmation && isDirty();
    }

}
