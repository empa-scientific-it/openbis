package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataSetTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField.IScriptTypeProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.XmlField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.DataTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.EntityTypePropertyTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentGrid.InMemoryGridAddCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.SectionSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularySelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * {@link Window} Property Type registration form.
 * 
 * @author Juan Fuentes
 */
public class AddPropertyTypeDialog extends AbstractRegistrationDialog
{
    //
    // Cosmetics
    //
    private static final int FORM_WIDTH = 600;

    private static final int LABEL_WIDTH = 150;

    private static final int FIELD_WIDTH = 400;

    private Label loading;

    //
    // Select/New Property Selector
    //
    private RadioGroup selectAssignGroup;

    private boolean isSelect = true;

    public boolean isSelect()
    {
        return isSelect;
    }

    public void setSelect(boolean isSelect)
    {
        this.isSelect = isSelect;
    }

    //
    // Create Property Type Form attributes
    //
    public static final String ID = GenericConstants.ID_PREFIX + "property-type-registration_form";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private CodeField propertyTypeCodeField;

    private VarcharField propertyTypeLabelField;

    private MultilineVarcharField propertyTypeDescriptionField;

    private DataTypeSelectionWidget dataTypeSelectionWidget;

    private VocabularySelectionWidget vocabularySelectionWidget;

    private MaterialTypeSelectionWidget materialTypeSelectionWidget;

    private SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private XmlField xmlSchemaField;

    private XmlField xslTransformationsField;

    //
    // Assign Property Type Form attributes
    //
    private EntityType entity;

    private EntityKind entityKind;

    private boolean isExitingEntity;

    private static final String PREFIX = "property-type-assignment_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String PROPERTY_TYPE_ID_SUFFIX = "property_type";

    public static final String SAMPLE_TYPE_ID_SUFFIX = ID_PREFIX + "sample_type";

    public static final String EXPERIMENT_TYPE_ID_SUFFIX = ID_PREFIX + "experiment_type";

    public static final String MATERIAL_TYPE_ID_SUFFIX = ID_PREFIX + "material_type";

    public static final String DATA_SET_TYPE_ID_SUFFIX = ID_PREFIX + "data_set_type";

    public static final String MANDATORY_CHECKBOX_ID_SUFFIX = "mandatory_checkbox";

    private static final String UNSUPPORTED_ENTITY_KIND = "Unsupported entity kind";

    protected static final String DEFAULT_VALUE_ID_PART = "default_value";

    private DropDownList<? extends SimplifiedBaseModelData, ? extends EntityType> entityTypeSelectionWidget;

    private PropertyTypeSelectionWidget propertyTypeSelectionWidget;

    private DatabaseModificationAwareField<?> defaultValueField;

    private CheckBox mandatoryCheckbox;

    private CheckBox scriptableCheckbox;

    private CheckBox shownInEditViewCheckBox;

    private CheckBox showRawValueCheckBox;

    private SectionSelectionWidget sectionSelectionWidget;

    private EntityTypePropertyTypeSelectionWidget etptSelectionWidget;

    private ScriptChooserField scriptChooser;

    private Radio scriptTypeManaged;

    private Radio scriptTypeDynamic;

    private RadioGroup scriptTypeRadioGroup;

    private boolean userDidChangeShownInEditViewCheckBox = false; // Track if the user has set a value

    private boolean userDidChangeShowRawValueCheckBox = false; // Track if the user has set a value

    private boolean synchronizingGuiFields = false; // Track the state of the code

    //
    // Save Property Type on memory
    //
    InMemoryGridAddCallback inMemoryGridCallback;

    //
    // Constructor and Init Methods
    //
    public AddPropertyTypeDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
            final IDelegatedAction postRegistrationCallback, EntityKind entityKind,
            String entityCode, InMemoryGridAddCallback inMemoryGridCallback, EntityType inMemoryEntityType, boolean isExitingEntity)
    {
        super(viewContext, viewContext.getMessage(Dict.PROPERTY_TYPE_REGISTRATION), postRegistrationCallback);
        this.viewContext = viewContext;
        this.inMemoryGridCallback = inMemoryGridCallback;
        this.entityKind = entityKind;
        this.isExitingEntity = isExitingEntity;

        setWidth(FORM_WIDTH);
        getFormPanel().setFieldWidth(FIELD_WIDTH);
        getFormPanel().setLabelWidth(LABEL_WIDTH);
        loading = new Label(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        addField(loading);

        this.entity = inMemoryEntityType;
        loadEntityDialog(entityKind, entityCode);
    }

    private void loadEntityDialog(EntityKind kind, String code)
    {
        switch (kind)
        {
            case MATERIAL:
                viewContext.getService().listMaterialTypes(
                        new AsyncCallbackEntityTypeForDialog<List<MaterialType>>(code));
                break;
            case EXPERIMENT:
                viewContext.getService().listExperimentTypes(
                        new AsyncCallbackEntityTypeForDialog<List<ExperimentType>>(code));
                break;
            case SAMPLE:
                viewContext.getService().listSampleTypes(
                        new AsyncCallbackEntityTypeForDialog<List<SampleType>>(code));
                break;
            case DATA_SET:
                viewContext.getService().listDataSetTypes(
                        new AsyncCallbackEntityTypeForDialog<List<DataSetType>>(code));
                break;
        }
    }

    private class AsyncCallbackEntityTypeForDialog<T extends List<? extends EntityType>> implements
            AsyncCallback<T>
    {

        private String code;

        AsyncCallbackEntityTypeForDialog(String code)
        {
            this.code = code;
        }

        @Override
        public void onFailure(Throwable caught)
        {

        }

        @Override
        public void onSuccess(T result)
        {
            EntityType finalType = null;
            for (EntityType type : result)
            {
                if (type.getCode().equals(code))
                {
                    finalType = type;
                }
            }
            entityLoaded(finalType);
        }
    }

    private void entityLoaded(EntityType entity)
    {
        if (inMemoryGridCallback == null)
        { // If is not an in memory grid
            this.entity = entity;
        }

        removeField(loading);
        // Enable Layout Changes
        getFormPanel().setLayoutOnChange(true);
        setLayoutOnChange(true);

        // Create Select/Create Property Selector
        addField(getSelector());

        // Create Form widgets
        initSelectPropertyForm();
        fixLayout();
    }

    private final void fixLayout()
    {
        this.getFormPanel().layout();
        this.layout();

        this.setSize(FORM_WIDTH, 650);
        this.center();
    }

    //
    // Existing/New Property Selector and Forms layout methods
    //
    public RadioGroup getSelector()
    {
        if (selectAssignGroup == null)
        {
            final Radio selectProperty = new Radio();
            selectProperty.setBoxLabel(viewContext.getMessage(Dict.ASSIGN_GROUP_EXISTING)); // "Existing"
            selectProperty.setValue(true);

            final Radio newPropertie = new Radio();
            newPropertie.setBoxLabel(viewContext.getMessage(Dict.ASSIGN_GROUP_NEW)); // "New"
            newPropertie.setValue(false);

            selectAssignGroup = new RadioGroup();
            selectAssignGroup.setFieldLabel(viewContext.getMessage(Dict.ASSIGN_GROUP_PROPERTY)); // "Property"
            selectAssignGroup.add(selectProperty);
            selectAssignGroup.add(newPropertie);

            // TO-DO Property

            selectAssignGroup.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        setSelect(false == isSelect());
                        hidePropertyTypeRelatedFields();
                        hideEntityTypePropertyTypeRelatedFields();
                        getFormPanel().removeAll();
                        if (isSelect())
                        {
                            initSelectPropertyForm();
                        } else
                        {
                            initNewPropertyForm();
                        }

                        fixLayout();
                    }
                });
        }
        return selectAssignGroup;
    }

    private final void initSelectPropertyForm()
    {
        addField(getSelector());
        // Add Assign Property Form
        addField(getPropertyTypeSelectionWidget());
        getPropertyTypeSelectionWidget().clear();
        getPropertyTypeSelectionWidget().enable();
        getPropertyTypeSelectionWidget().setVisible(true);

        if (inMemoryGridCallback == null)
        {
            addField(getEntityTypeSelectionWidget());
            getEntityTypeSelectionWidget().disable();
            getEntityTypeSelectionWidget().setVisible(false);
        }

        addField(getScriptableCheckbox());
        getScriptableCheckbox().clear();

        getScriptTypeRadioGroup().setVisible(false);
        addField(getScriptTypeRadioGroup());

        addField(getScriptChooserField());
        getScriptChooserField().clear();
        getScriptChooserField().setVisible(false);
        addField(getMandatoryCheckbox());
        getMandatoryCheckbox().clear();

        addField(getShownInEditViewCheckbox());
        getShownInEditViewCheckbox().clear();
        getShownInEditViewCheckbox().setVisible(false);

        addField(getShowRawValueCheckBox());
        getShowRawValueCheckBox().clear();
        getShowRawValueCheckBox().setVisible(false);
    }

    private final void initNewPropertyForm()
    {
        addField(getSelector());
        // Add New Property Form
        addField(getPropertyTypeCodeField());
        getPropertyTypeCodeField().clear();
        addField(getPropertyTypeLabelField());
        getPropertyTypeLabelField().clear();
        addField(getPropertyTypeDescriptionField());
        getPropertyTypeDescriptionField().clear();
        addField(getDataTypeSelectionWidget());
        getDataTypeSelectionWidget().clear();
        addField(getVocabularySelectionWidget());
        getVocabularySelectionWidget().clear();
        getVocabularySelectionWidget().setVisible(false);

        addField(getMaterialTypeSelectionWidget());
        getMaterialTypeSelectionWidget().clear();
        getMaterialTypeSelectionWidget().setVisible(false);
        addField(getSampleTypeSelectionWidget());
        getSampleTypeSelectionWidget().clear();
        getSampleTypeSelectionWidget().setVisible(false);
        addField(getXmlSchemaField());
        getXmlSchemaField().clear();
        getXmlSchemaField().setVisible(false);
        addField(getXslTransformationsField());
        getXslTransformationsField().clear();
        getXslTransformationsField().setVisible(false);

        // Add Assign Property Form
        getPropertyTypeSelectionWidget().clear();
        addField(getPropertyTypeSelectionWidget());
        getPropertyTypeSelectionWidget().disable();
        getPropertyTypeSelectionWidget().setVisible(false);

        if (inMemoryGridCallback == null)
        {
            addField(getEntityTypeSelectionWidget());
            getEntityTypeSelectionWidget().disable();
            getEntityTypeSelectionWidget().setVisible(false);
        }

        addField(getScriptableCheckbox());
        getScriptableCheckbox().clear();

        getScriptTypeRadioGroup().setVisible(false);
        addField(getScriptTypeRadioGroup());

        addField(getScriptChooserField());
        getScriptChooserField().clear();
        getScriptChooserField().setVisible(false);
        addField(getMandatoryCheckbox());
        getMandatoryCheckbox().clear();

        addField(getShownInEditViewCheckbox());
        getShownInEditViewCheckbox().clear();
        getShownInEditViewCheckbox().setVisible(false);

        addField(getShowRawValueCheckBox());
        getShowRawValueCheckBox().clear();
        getShowRawValueCheckBox().setVisible(false);
    }

    //
    // Register Change Methods
    //
    @Override
    protected void register(final AsyncCallback<Void> registrationCallback)
    {
        if (inMemoryGridCallback == null)
        {
            if (false == isSelect())
            {
                final PropertyType propertyType = createPropertyType();
                final NewETPTAssignment assignment = createAssignment();
                viewContext.getService().registerAndAssignPropertyType(propertyType, assignment,
                        new AssignPropertyTypeCallback(viewContext, registrationCallback));
            } else
            {
                final NewETPTAssignment assignment = createAssignment();
                viewContext.getService().assignPropertyType(assignment,
                        new AssignPropertyTypeCallback(viewContext, registrationCallback));
            }
        } else
        {
            boolean isExixtingPropertyType = isSelect();
            PropertyType propertyType = null;
            NewETPTAssignment assignment = createAssignment();

            if (false == isSelect())
            {
                propertyType = createPropertyType();
            } else
            {
                propertyType = propertyTypeSelectionWidget.tryGetSelectedPropertyType();
            }

            inMemoryGridCallback.callback(isExixtingPropertyType, propertyType, assignment, this);
        }
    }

    private final class AssignPropertyTypeCallback extends AbstractAsyncCallback<String>
    {
        final AsyncCallback<Void> registrationCallback;

        AssignPropertyTypeCallback(final IViewContext<?> viewContext,
                final AsyncCallback<Void> registrationCallback)
        {
            super(viewContext, null);
            this.registrationCallback = registrationCallback;
        }

        @Override
        protected final void process(final String result)
        {
            registrationCallback.onSuccess(null);
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            // One Pop Up with the error is shown automatically
        }
    }

    private final PropertyType createPropertyType()
    {
        final PropertyType propertyType = new PropertyType();
        if (getPropertyTypeCodeField().getValue() != null)
        {
            propertyType.setCode(getPropertyTypeCodeField().getValue().toUpperCase());
        }
        propertyType.setLabel(getPropertyTypeLabelField().getValue());
        propertyType.setDescription(getPropertyTypeDescriptionField().getValue());
        propertyType.setDataType(getDataTypeSelectionWidget().tryGetSelectedDataType());
        if (propertyType.getDataType() != null)
        {
            switch (propertyType.getDataType().getCode())
            {
                case MATERIAL:
                    propertyType.setMaterialType(getMaterialTypeSelectionWidget().tryGetSelected());
                    break;
                case SAMPLE:
                    propertyType.setSampleType(getSampleTypeSelectionWidget().tryGetSelected());
                    break;
                case CONTROLLEDVOCABULARY:
                    propertyType.setVocabulary((Vocabulary) GWTUtils.tryGetSingleSelected(getVocabularySelectionWidget()));
                    break;
                case XML:
                    propertyType.setSchema(getXmlSchemaField().getValue());
                    propertyType.setTransformation(getXslTransformationsField().getValue());
                    break;
                default:
                    break;
            }
        }

        return propertyType;
    }

    private final NewETPTAssignment createAssignment()
    {
        // This code field can only be assigend here if the entity type exists.
        String entityTypeCode = null;
        if (inMemoryGridCallback == null)
        {
            entityTypeCode = ((EntityType) getEntityTypeSelectionWidget().tryGetSelected()).getCode();
        }

        // This code field comes from a different component depending if the property is being created or just assigned.
        String propertyTypeCode = null;

        if (isSelect())
        {
            propertyTypeCode = propertyTypeSelectionWidget.tryGetSelectedPropertyTypeCode();
        } else
        {
            propertyTypeCode = getPropertyTypeCodeField().getValue().toUpperCase();
        }

        NewETPTAssignment newAssignment = new NewETPTAssignment(
                entityKind,
                propertyTypeCode,
                entityTypeCode,
                getMandatoryCheckbox().getValue(),
                getDefaultValue(),
                getSectionValue(),
                getPreviousETPTOrdinal(),
                isDynamic(),
                isManaged(),
                tryGetScriptNameValue(),
                isShownInEditView(),
                getShowRawValue());
        return newAssignment;
    }

    //
    // Assign Property Type Form Widgets
    //
    private final RadioGroup getScriptTypeRadioGroup()
    {
        if (scriptTypeRadioGroup == null)
        {
            scriptTypeRadioGroup = new RadioGroup();
            scriptTypeRadioGroup.setSelectionRequired(true);
            scriptTypeRadioGroup.setVisible(false);
            scriptTypeRadioGroup.setOrientation(Orientation.HORIZONTAL);

            scriptTypeManaged = new Radio();
            scriptTypeManaged.setBoxLabel(ScriptType.MANAGED_PROPERTY.getDescription());
            scriptTypeManaged.setId(ID_PREFIX + "managed_radio");
            scriptTypeRadioGroup.add(scriptTypeManaged);

            scriptTypeDynamic = new Radio();
            scriptTypeDynamic.setBoxLabel(ScriptType.DYNAMIC_PROPERTY.getDescription());
            scriptTypeDynamic.setId(ID_PREFIX + "dynamic_radio");
            scriptTypeRadioGroup.add(scriptTypeDynamic);
            scriptTypeRadioGroup.setLabelSeparator("");
            scriptTypeRadioGroup.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        scriptChooser.setRawValue("");
                        updateVisibilityOfShownInEditViewField();
                        updateVisibilityOfShowRawValueField();
                    }
                });
        }
        FieldUtil.setValueWithoutEvents(scriptTypeRadioGroup, scriptTypeManaged);
        return scriptTypeRadioGroup;
    }

    private ScriptChooserField getScriptChooserField()
    {
        if (scriptChooser == null)
        {
            IScriptTypeProvider scriptTypeProvider = new IScriptTypeProvider()
                {
                    @Override
                    public ScriptType tryGetScriptType()
                    {
                        return isManaged() ? ScriptType.MANAGED_PROPERTY
                                : ScriptType.DYNAMIC_PROPERTY;
                    }
                };

            scriptChooser = ScriptChooserField.create(
                    viewContext.getMessage(Dict.PLUGIN_PLUGIN),
                    true, null,
                    viewContext, scriptTypeProvider, entityKind);
            scriptChooser.setId(ID_PREFIX + "script_chooser");
            FieldUtil.setVisibility(false, scriptChooser);
        }
        return scriptChooser;
    }

    public PropertyTypeSelectionWidget getPropertyTypeSelectionWidget()
    {
        if (propertyTypeSelectionWidget == null)
        {
            propertyTypeSelectionWidget =
                    new PropertyTypeSelectionWidget(viewContext,
                            createChildId(PROPERTY_TYPE_ID_SUFFIX));
            FieldUtil.markAsMandatory(propertyTypeSelectionWidget);
            propertyTypeSelectionWidget.addListener(Events.SelectionChange,
                    new Listener<BaseEvent>()
                        {
                            @Override
                            public void handleEvent(BaseEvent be)
                            {
                                updatePropertyTypeRelatedFields();
                            }
                        });
        }
        return propertyTypeSelectionWidget;
    }

    private DropDownList<?, ?> getEntityTypeSelectionWidget()
    {
        if (entityTypeSelectionWidget == null)
        {
            switch (entityKind)
            {
                case EXPERIMENT:
                    ExperimentTypeSelectionWidget ew =
                            new ExperimentTypeSelectionWidget(viewContext,
                                    EXPERIMENT_TYPE_ID_SUFFIX, null);
                    ew.setValue(new ExperimentTypeModel((ExperimentType) entity));
                    entityTypeSelectionWidget = ew;
                    break;
                case SAMPLE:
                    SampleTypeSelectionWidget sw =
                            new SampleTypeSelectionWidget(viewContext, SAMPLE_TYPE_ID_SUFFIX,
                                    false, SampleTypeDisplayID.PROPERTY_ASSIGNMENT, null);
                    sw.setValue(new SampleTypeModel((SampleType) entity));
                    entityTypeSelectionWidget = sw;
                    break;
                case MATERIAL:
                    MaterialTypeSelectionWidget mw =
                            new MaterialTypeSelectionWidget(viewContext, null,
                                    MATERIAL_TYPE_ID_SUFFIX, false);
                    mw.setValue(new MaterialTypeModel((MaterialType) entity));
                    entityTypeSelectionWidget = mw;
                    break;
                case DATA_SET:
                    DataSetTypeSelectionWidget dw =
                            new DataSetTypeSelectionWidget(viewContext, DATA_SET_TYPE_ID_SUFFIX);
                    dw.setValue(new DataSetTypeModel((DataSetType) entity));
                    entityTypeSelectionWidget = dw;
                    break;
            }

            if (entityTypeSelectionWidget == null)
            {
                throw new IllegalArgumentException(UNSUPPORTED_ENTITY_KIND);
            }

            FieldUtil.markAsMandatory(entityTypeSelectionWidget);
            entityTypeSelectionWidget.addListener(Events.SelectionChange, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        updateEntityTypePropertyTypeRelatedFields();
                    }
                });
            updateEntityTypePropertyTypeRelatedFields();
        }

        return entityTypeSelectionWidget;
    }

    private CheckBox getScriptableCheckbox()
    {
        if (scriptableCheckbox == null)
        {
            scriptableCheckbox = new CheckBoxField(viewContext.getMessage(Dict.SCRIPTABLE), false);
            scriptableCheckbox.setValue(false);
            scriptableCheckbox.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        updateShownInEditView();
                        updateShowRawValue();
                        updateVisibilityOfScriptRelatedFields();
                        fixLayout();
                    }
                });
            scriptableCheckbox.setId(ID_PREFIX + "scriptable_checkbox");
        }
        return scriptableCheckbox;
    }

    private CheckBox getMandatoryCheckbox()
    {
        if (mandatoryCheckbox == null)
        {
            mandatoryCheckbox = new CheckBoxField(viewContext.getMessage(Dict.MANDATORY), false);
            mandatoryCheckbox.setId(createChildId(MANDATORY_CHECKBOX_ID_SUFFIX));
            mandatoryCheckbox.setValue(false);
            mandatoryCheckbox.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        // Show init value if necessary
                        updatePropertyTypeRelatedFields();
                    }
                });

            FieldUtil.setVisibility(isScriptable() == false, mandatoryCheckbox);
        }
        return mandatoryCheckbox;
    }

    private CheckBox getShownInEditViewCheckbox()
    {
        if (null == shownInEditViewCheckBox)
        {
            shownInEditViewCheckBox = new CheckBoxField(viewContext.getMessage(Dict.IS_SHOWN_IN_EDIT_VIEW), false);
            shownInEditViewCheckBox.setValue(true);
            shownInEditViewCheckBox.setVisible(false);
            shownInEditViewCheckBox.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        // Make sure the User triggered the change
                        if (false == synchronizingGuiFields)
                        {
                            userDidChangeShownInEditViewCheckBox = true;
                        }
                        updateVisibilityOfShowRawValueField();
                        fixLayout();
                    }
                });
        }

        return shownInEditViewCheckBox;
    }

    private CheckBox getShowRawValueCheckBox()
    {
        if (null == showRawValueCheckBox)
        {
            showRawValueCheckBox =
                    new CheckBoxField(viewContext.getMessage(Dict.SHOW_RAW_VALUE_IN_FORMS), false);
            showRawValueCheckBox.setValue(true);
            showRawValueCheckBox.setVisible(false);
            showRawValueCheckBox.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        // Make sure the User triggered the change
                        if (false == synchronizingGuiFields)
                        {
                            userDidChangeShowRawValueCheckBox = true;
                        }
                    }
                });
        }

        return showRawValueCheckBox;
    }

    private void updatePropertyTypeRelatedFields()
    {
        hidePropertyTypeRelatedFields();

        PropertyType propertyType = null;

        if (isSelect())
        {
            propertyType = getPropertyTypeSelectionWidget().tryGetSelectedPropertyType();
        } else
        {
            propertyType = this.createPropertyType();
        }

        // Is necessary to manage the case where the vocabulary is not set to avoid a null pointer
        if (propertyType != null && propertyType.getDataType() != null
                && propertyType.getDataType().getCode() != DataTypeCode.CONTROLLEDVOCABULARY
                ||
                propertyType != null && propertyType.getDataType() != null
                        && propertyType.getDataType().getCode() == DataTypeCode.CONTROLLEDVOCABULARY
                        && propertyType.getVocabulary() != null)
        {
            if (isExitingEntity && getMandatoryCheckbox().getValue())
            {
                String fieldId = createChildId(DEFAULT_VALUE_ID_PART);
                DatabaseModificationAwareField<?> fieldHolder =
                        PropertyFieldFactory.createField(propertyType,
                                false,
                                viewContext.getMessage(Dict.DEFAULT_VALUE),
                                fieldId,
                                null,
                                viewContext);
                GWTUtils.setToolTip(fieldHolder.get(), viewContext
                        .getMessage(Dict.DEFAULT_VALUE_TOOLTIP));
                defaultValueField = fieldHolder;
                defaultValueField.get().show();
                FieldUtil.setVisibility(isScriptable() == false, defaultValueField.get());
                this.addField(defaultValueField.get());
            }
        }
        updateEntityTypePropertyTypeRelatedFields();
    }

    private String createChildId(String childSuffix)
    {
        return getId() + childSuffix;
    }

    private void updateEntityTypePropertyTypeRelatedFields()
    {
        hideEntityTypePropertyTypeRelatedFields();

        PropertyType propertyType = null;
        if (isSelect())
        {
            propertyType = getPropertyTypeSelectionWidget().tryGetSelectedPropertyType();
        } else
        {
            propertyType = this.createPropertyType();
        }

        EntityType entityType = null;

        if (inMemoryGridCallback == null)
        {
            entityType = (EntityType) getEntityTypeSelectionWidget().tryGetSelected();
        } else
        {
            entityType = entity;
        }

        if (propertyType != null && entityType != null && propertyType.getDataType() != null)
        {
            final List<EntityTypePropertyType<?>> etpts = new ArrayList<EntityTypePropertyType<?>>(entityType.getAssignedPropertyTypes());
            this.addField(getSectionSelectionWidget(etpts));
            this.addField(getETPTSelectionWidget(etpts));
        }

        fixLayout();
    }

    private EntityTypePropertyTypeSelectionWidget getETPTSelectionWidget(final List<EntityTypePropertyType<?>> etpts)
    {
        if (etptSelectionWidget == null)
        {
            etptSelectionWidget = createETPTSelectionWidget(etpts);
        }
        return etptSelectionWidget;
    }

    private SectionSelectionWidget getSectionSelectionWidget(final List<EntityTypePropertyType<?>> etpts)
    {
        if (sectionSelectionWidget == null)
        {
            sectionSelectionWidget = SectionSelectionWidget.create(viewContext, etpts);
        }
        return sectionSelectionWidget;
    }

    private void hideEntityTypePropertyTypeRelatedFields()
    {
        if (sectionSelectionWidget != null && this.getFormPanel().getFields().contains(sectionSelectionWidget))
        {
            this.removeField(sectionSelectionWidget);
        }

        if (etptSelectionWidget != null && this.getFormPanel().getFields().contains(etptSelectionWidget))
        {
            this.removeField(etptSelectionWidget);
        }
    }

    private void updateShownInEditView()
    {
        if (userDidChangeShownInEditViewCheckBox)
        {
            // If the user has made a change, don't overwrite her changes.
            return;
        }

        synchronizingGuiFields = true;
        try
        {
            if (false == isScriptable())
            {
                shownInEditViewCheckBox.setValue(true);
                return;
            }

            if (isDynamic())
            {
                shownInEditViewCheckBox.setValue(false);
                return;
            }

            if (isManaged())
            {
                shownInEditViewCheckBox.setValue(false);
                return;
            }
        } finally
        {
            synchronizingGuiFields = false;
        }
    }

    private void updateShowRawValue()
    {
        if (userDidChangeShowRawValueCheckBox)
        {
            return; // If the user has made a change, don't overwrite her changes.
        }

        synchronizingGuiFields = true;
        try
        {
            showRawValueCheckBox.setValue(true);
            return;
        } finally
        {
            synchronizingGuiFields = false;
        }
    }

    private void updateVisibilityOfScriptRelatedFields()
    {
        boolean scriptable = isScriptable();
        FieldUtil.setVisibility(scriptable, scriptTypeRadioGroup, scriptChooser);
        if (defaultValueField != null)
        {
            FieldUtil.setVisibility(scriptable == false, defaultValueField.get());
        }
        if (mandatoryCheckbox != null)
        {
            FieldUtil.setVisibility(scriptable == false, mandatoryCheckbox);
        }
        updateVisibilityOfShownInEditViewField();
        updateVisibilityOfShowRawValueField();
    }

    private void hidePropertyTypeRelatedFields()
    {
        if (defaultValueField != null)
        {
            Field<?> field = defaultValueField.get();
            field.hide();
            if (this.getFormPanel().getFields().contains(field))
            {
                this.removeField(field);
            }
            defaultValueField = null;
        }
    }

    private EntityTypePropertyTypeSelectionWidget createETPTSelectionWidget(
            List<EntityTypePropertyType<?>> etpts)
    {
        // by default - append
        etpts.add(0, null); // null will be transformed into '(top)'
        final String lastCode =
                (etpts.size() > 1) ? etpts.get(etpts.size() - 1).getPropertyType().getCode()
                        : EntityTypePropertyTypeSelectionWidget.TOP_ITEM_CODE;
        final EntityTypePropertyTypeSelectionWidget result =
                new EntityTypePropertyTypeSelectionWidget(viewContext, getId(), etpts, lastCode);
        FieldUtil.setMandatoryFlag(result, true);
        return result;
    }

    private String getDefaultValue()
    {
        if (defaultValueField != null)
        {
            return PropertyFieldFactory.valueToString(defaultValueField.get().getValue());
        }
        return null;
    }

    private boolean getShowRawValue()
    {
        if (false == (isManaged() && isShownInEditView()))
        {
            return false;
        }
        return showRawValueCheckBox.getValue();
    }

    String tryGetScriptNameValue()
    {
        if (scriptChooser == null)
        {
            return null;
        } else
        {
            return scriptChooser.getValue();
        }
    }

    private Long getPreviousETPTOrdinal()
    {
        Long value = 0L;
        if (etptSelectionWidget != null)
        {
            value = etptSelectionWidget.getSelectedEntityTypePropertyTypeOrdinal();
        }
        return value;
    }

    private String getSectionValue()
    {
        if (sectionSelectionWidget != null)
        {
            return sectionSelectionWidget.getSimpleValue();
        }
        return null;
    }

    private void updateVisibilityOfShownInEditViewField()
    {
        if (shownInEditViewCheckBox != null)
        {
            FieldUtil.setVisibility(isManaged(), shownInEditViewCheckBox);
        }
    }

    private void updateVisibilityOfShowRawValueField()
    {
        if (showRawValueCheckBox != null)
        {
            FieldUtil.setVisibility(isManaged() && isShownInEditView(), showRawValueCheckBox);
        }
    }

    private boolean isManaged()
    {
        return isScriptable() && scriptTypeManaged.getValue();
    }

    private boolean isDynamic()
    {
        return isScriptable() && scriptTypeDynamic.getValue();
    }

    private boolean isScriptable()
    {
        return scriptableCheckbox.getValue();
    }

    private boolean isShownInEditView()
    {
        // The logic for defaulting the value of the shownInEditView check box is duplicated here to
        // enforce the current semantics that this value is only considered by managed properties
        if (false == isScriptable())
        {
            return true;
        }
        if (isDynamic())
        {
            return false;
        }

        return shownInEditViewCheckBox.getValue();
    }

    //
    // Create Property Type Form widgets
    //
    private final CodeField getPropertyTypeCodeField()
    {
        if (propertyTypeCodeField == null)
        {
            propertyTypeCodeField = new CodeField(viewContext, viewContext.getMessage(Dict.CODE));
            propertyTypeCodeField.setId(getId() + "_code");
        }
        return propertyTypeCodeField;
    }

    private final VarcharField getPropertyTypeLabelField()
    {
        if (propertyTypeLabelField == null)
        {
            propertyTypeLabelField = new VarcharField(viewContext.getMessage(Dict.LABEL), true);
            propertyTypeLabelField.setId(getId() + "_label");
            propertyTypeLabelField.setMaxLength(GenericConstants.COLUMN_LABEL);
        }
        return propertyTypeLabelField;
    }

    private final MultilineVarcharField getPropertyTypeDescriptionField()
    {
        if (propertyTypeDescriptionField == null)
        {
            propertyTypeDescriptionField = new DescriptionField(viewContext, true, getId());
        }
        return propertyTypeDescriptionField;
    }

    private final DataTypeSelectionWidget getDataTypeSelectionWidget()
    {
        if (dataTypeSelectionWidget == null)
        {
            dataTypeSelectionWidget = new DataTypeSelectionWidget(viewContext, true);

            SelectionChangedListener<DataTypeModel> dataTypeSelectionChangedListener =
                    new SelectionChangedListener<DataTypeModel>()
                        {
                            @Override
                            public final void selectionChanged(
                                    final SelectionChangedEvent<DataTypeModel> se)
                            {
                                hideDataTypeRelatedFields();

                                DataTypeModel selectedItem = se.getSelectedItem();
                                if (selectedItem != null)
                                {
                                    DataTypeCode dataTypeCode =
                                            selectedItem.getDataType().getCode();
                                    switch (dataTypeCode)
                                    {
                                        case CONTROLLEDVOCABULARY:
                                            showFields(vocabularySelectionWidget);
                                            break;
                                        case MATERIAL:
                                            showFields(getMaterialTypeSelectionWidget());
                                            break;
                                        case SAMPLE:
                                            showFields(getSampleTypeSelectionWidget());
                                            break;
                                        case XML:
                                            showFields(xmlSchemaField, xslTransformationsField);
                                            break;
                                        default:
                                            break;
                                    }

                                    updatePropertyTypeRelatedFields();
                                }

                            }

                            private void showFields(Field<?>... fields)
                            {
                                FieldUtil.setVisibility(true, fields);
                            }

                            private void hideDataTypeRelatedFields()
                            {
                                FieldUtil.setVisibility(false, vocabularySelectionWidget,
                                        getMaterialTypeSelectionWidget(), getSampleTypeSelectionWidget(),
                                        xmlSchemaField, xslTransformationsField);
                            }
                        };

            dataTypeSelectionWidget.addSelectionChangedListener(dataTypeSelectionChangedListener);
        }
        return dataTypeSelectionWidget;
    }

    private final VocabularySelectionWidget getVocabularySelectionWidget()
    {
        if (vocabularySelectionWidget == null)
        {
            vocabularySelectionWidget = new VocabularySelectionWidget(viewContext);
            Listener<BaseEvent> vocabularyListener = new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        updatePropertyTypeRelatedFields();
                    }
                };
            vocabularySelectionWidget.addListener(Events.SelectionChange, vocabularyListener);
            FieldUtil.markAsMandatory(vocabularySelectionWidget);
        }
        return vocabularySelectionWidget;
    }

    private MaterialTypeSelectionWidget getMaterialTypeSelectionWidget()
    {
        if (materialTypeSelectionWidget == null)
        {
            materialTypeSelectionWidget =
                    MaterialTypeSelectionWidget.createWithAdditionalOption(viewContext, viewContext.getMessage(Dict.ALLOW_ANY_TYPE), null, ID);
            Listener<BaseEvent> materialListener = new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        // TO-DO The material type selector widget don't supports to change the selected type after creation.
                        // Because of that is necessary to force the reload of this fields.
                        updatePropertyTypeRelatedFields();
                    }
                };
            materialTypeSelectionWidget.addListener(Events.SelectionChange, materialListener);
            FieldUtil.markAsMandatory(materialTypeSelectionWidget);
        }
        return materialTypeSelectionWidget;
    }

    private SampleTypeSelectionWidget getSampleTypeSelectionWidget()
    {
        if (sampleTypeSelectionWidget == null)
        {
            sampleTypeSelectionWidget = new SampleTypeSelectionWidget(viewContext, ID, false, true, false, null,
                    SampleTypeDisplayID.SAMPLE_QUERY);
            sampleTypeSelectionWidget.addListener(Events.SelectionChange, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        updatePropertyTypeRelatedFields();
                    }
                });
        }
        return sampleTypeSelectionWidget;
    }

    private final XmlField getXmlSchemaField()
    {
        if (xmlSchemaField == null)
        {
            final String label = viewContext.getMessage(Dict.XML_SCHEMA);
            final String description = viewContext.getMessage(Dict.XML_SCHEMA_INFO);
            final AbstractImagePrototype infoIcon =
                    AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
            xmlSchemaField = new XmlField(label, false);
            FieldUtil.addInfoIcon(xmlSchemaField, description, infoIcon.createImage());
        }
        return xmlSchemaField;
    }

    private final XmlField getXslTransformationsField()
    {
        if (xslTransformationsField == null)
        {
            final String label = viewContext.getMessage(Dict.XSLT);
            final String description = viewContext.getMessage(Dict.XSLT_INFO);
            final AbstractImagePrototype infoIcon =
                    AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
            xslTransformationsField = new XmlField(label, false);
            FieldUtil.addInfoIcon(xslTransformationsField, description, infoIcon.createImage());
        }
        return xslTransformationsField;
    }

    @Override
    protected String createSaveButtonText()
    {
        return getViewContext().getMessage(Dict.ADD_PROPERTY_TYPE_BUTTON);
    }

}
