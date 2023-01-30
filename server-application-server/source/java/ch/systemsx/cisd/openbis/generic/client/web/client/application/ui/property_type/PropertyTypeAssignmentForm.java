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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm.InfoBoxResetListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField.IScriptTypeProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Element;

/**
 * The property type assignment panel.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeAssignmentForm extends LayoutContainer implements
        IDatabaseModificationObserver
{
    private static final String UNSUPPORTED_ENTITY_KIND = "Unsupported entity kind";

    private static final int LABEL_WIDTH = 130;

    private static final int FIELD_WIDTH = 400;

    private static final String PREFIX = "property-type-assignment_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String PROPERTY_TYPE_ID_SUFFIX = "property_type";

    public static final String SAMPLE_TYPE_ID_SUFFIX = ID_PREFIX + "sample_type";

    public static final String EXPERIMENT_TYPE_ID_SUFFIX = ID_PREFIX + "experiment_type";

    public static final String MATERIAL_TYPE_ID_SUFFIX = ID_PREFIX + "material_type";

    public static final String DATA_SET_TYPE_ID_SUFFIX = ID_PREFIX + "data_set_type";

    public static final String MANDATORY_CHECKBOX_ID_SUFFIX = "mandatory_checkbox";

    public static final String SAVE_BUTTON_ID_SUFFIX = "save-button";

    protected static final String DEFAULT_VALUE_ID_PART = "default_value";

    protected static final String SECTION_VALUE_ID_PART = "section_value";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private ExperimentTypeSelectionWidget experimentTypeSelectionWidget;

    private MaterialTypeSelectionWidget materialTypeSelectionWidget;

    private DataSetTypeSelectionWidget dataSetTypeSelectionWidget;

    private PropertyTypeSelectionWidget propertyTypeSelectionWidget;

    private DatabaseModificationAwareField<?> defaultValueField;

    private CheckBox mandatoryCheckbox;

    private CheckBox scriptableCheckbox;

    private CheckBox shownInEditViewCheckBox;

    private CheckBox showRawValueCheckBox;

    private SectionSelectionWidget sectionSelectionWidget;

    private EntityTypePropertyTypeSelectionWidget etptSelectionWidget;

    private Button saveButton;

    private final InfoBox infoBox;

    private final FormPanel formPanel;

    private final CompositeDatabaseModificationObserver modificationManager;

    private final EntityKind entityKind;

    private final ScriptChooserField scriptChooser;

    private Radio scriptTypeManaged;

    private Radio scriptTypeDynamic;

    private final RadioGroup scriptTypeRadioGroup;

    // Track if the user has set a value
    private boolean userDidChangeShownInEditViewCheckBox = false;

    // Track if the user has set a value
    private boolean userDidChangeShowRawValueCheckBox = false;

    // Track the state of the code
    private boolean synchronizingGuiFields = false;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, EntityKind entityKind)
    {
        PropertyTypeAssignmentForm form = new PropertyTypeAssignmentForm(viewContext, entityKind);
        return new DatabaseModificationAwareComponent(form, form.modificationManager);
    }

    private PropertyTypeAssignmentForm(final IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKind)
    {
        this.entityKind = entityKind;
        this.modificationManager = new CompositeDatabaseModificationObserver();
        setLayout(new FlowLayout(5));
        setId(createId(entityKind));
        this.viewContext = viewContext;
        setScrollMode(Scroll.AUTO);
        add(infoBox = createInfoBox(viewContext));
        add(formPanel = createFormPanel());
        scriptTypeRadioGroup = createScriptTypeRadioGroup();
        scriptChooser =
                createScriptChooserField(viewContext, createScriptTypeProvider(), entityKind);
    }

    private IScriptTypeProvider createScriptTypeProvider()
    {
        return new IScriptTypeProvider()
            {
                @Override
                public ScriptType tryGetScriptType()
                {
                    return isManaged() ? ScriptType.MANAGED_PROPERTY : ScriptType.DYNAMIC_PROPERTY;
                }
            };
    }

    private static ScriptChooserField createScriptChooserField(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            IScriptTypeProvider scriptTypeProvider, EntityKind entityKindOrNull)
    {
        ScriptChooserField field =
                ScriptChooserField.create(viewContext.getMessage(Dict.PLUGIN_PLUGIN), true, null,
                        viewContext, scriptTypeProvider, entityKindOrNull);
        field.setId(ID_PREFIX + "script_chooser");
        FieldUtil.setVisibility(false, field);
        return field;
    }

    public static final String createId(EntityKind entityKind)
    {
        return ID_PREFIX + entityKind.name();
    }

    private String createChildId(String childSuffix)
    {
        return getId() + childSuffix;
    }

    private final static InfoBox createInfoBox(IMessageProvider messageProvider)
    {
        final InfoBox infoBox = new InfoBox(messageProvider);
        return infoBox;
    }

    private final RadioGroup createScriptTypeRadioGroup()
    {
        final RadioGroup result = new RadioGroup();
        result.setSelectionRequired(true);
        result.setVisible(false);
        result.setOrientation(Orientation.HORIZONTAL);
        scriptTypeManaged = createRadio(ScriptType.MANAGED_PROPERTY.getDescription());
        scriptTypeManaged.setId(ID_PREFIX + "managed_radio");
        scriptTypeDynamic = createRadio(ScriptType.DYNAMIC_PROPERTY.getDescription());
        scriptTypeDynamic.setId(ID_PREFIX + "dynamic_radio");
        result.add(scriptTypeManaged);
        result.add(scriptTypeDynamic);
        FieldUtil.setValueWithoutEvents(result, scriptTypeManaged);
        result.setLabelSeparator("");
        result.addListener(Events.Change, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    scriptChooser.setRawValue("");
                    updateVisibilityOfShownInEditViewField();
                    updateVisibilityOfShowRawValueField();
                }
            });
        return result;
    }

    private final Radio createRadio(final String label)
    {
        Radio result = new Radio();
        result.setBoxLabel(label);
        return result;
    }

    private PropertyTypeSelectionWidget getPropertyTypeWidget()
    {
        if (propertyTypeSelectionWidget == null)
        {
            propertyTypeSelectionWidget =
                    new PropertyTypeSelectionWidget(viewContext,
                            createChildId(PROPERTY_TYPE_ID_SUFFIX));
            propertyTypeSelectionWidget
                    .addListener(Events.Focus, new InfoBoxResetListener(infoBox));
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

    private DropDownList<?, ?> getTypeSelectionWidget()
    {
        DropDownList<?, ?> result = null;
        boolean created = false;
        switch (entityKind)
        {
            case EXPERIMENT:
                if (experimentTypeSelectionWidget == null)
                {
                    experimentTypeSelectionWidget =
                            new ExperimentTypeSelectionWidget(viewContext,
                                    EXPERIMENT_TYPE_ID_SUFFIX, null);
                    created = true;
                }
                result = experimentTypeSelectionWidget;
                break;
            case SAMPLE:
                if (sampleTypeSelectionWidget == null)
                {
                    sampleTypeSelectionWidget =
                            new SampleTypeSelectionWidget(viewContext, SAMPLE_TYPE_ID_SUFFIX,
                                    false, SampleTypeDisplayID.PROPERTY_ASSIGNMENT, null);
                    created = true;
                }
                result = sampleTypeSelectionWidget;
                break;
            case MATERIAL:
                if (materialTypeSelectionWidget == null)
                {
                    materialTypeSelectionWidget =
                            new MaterialTypeSelectionWidget(viewContext, null,
                                    MATERIAL_TYPE_ID_SUFFIX, false);
                    created = true;
                }
                result = materialTypeSelectionWidget;
                break;
            case DATA_SET:
                if (dataSetTypeSelectionWidget == null)
                {
                    dataSetTypeSelectionWidget =
                            new DataSetTypeSelectionWidget(viewContext, DATA_SET_TYPE_ID_SUFFIX);
                    created = true;
                }
                result = dataSetTypeSelectionWidget;
                break;
        }
        if (result == null)
        {
            throw new IllegalArgumentException(UNSUPPORTED_ENTITY_KIND);
        } else if (created)
        {
            FieldUtil.markAsMandatory(result);
            result.addListener(Events.Focus, new InfoBoxResetListener(infoBox));
            result.addListener(Events.SelectionChange, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        updateEntityTypePropertyTypeRelatedFields();
                    }
                });
        }
        return result;
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

    private CheckBox getMandatoryCheckbox()
    {
        if (mandatoryCheckbox == null)
        {
            mandatoryCheckbox = new CheckBoxField(viewContext.getMessage(Dict.MANDATORY), false);
            mandatoryCheckbox.setId(createChildId(MANDATORY_CHECKBOX_ID_SUFFIX));
            mandatoryCheckbox.setFireChangeEventOnSetValue(false);
            mandatoryCheckbox.setValue(false);
            mandatoryCheckbox.addListener(Events.Change, new InfoBoxResetListener(infoBox));
            FieldUtil.setVisibility(isScriptable() == false, mandatoryCheckbox);
        }
        return mandatoryCheckbox;
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
                    }
                });
            scriptableCheckbox.setId(ID_PREFIX + "scriptable_checkbox");
        }
        return scriptableCheckbox;
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

    private CheckBox getShownInEditViewCheckbox()
    {
        if (null == shownInEditViewCheckBox)
        {
            shownInEditViewCheckBox =
                    new CheckBoxField(viewContext.getMessage(Dict.IS_SHOWN_IN_EDIT_VIEW), false);
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
                    }
                });
        }

        return shownInEditViewCheckBox;
    }

    private void updateShowRawValue()
    {
        if (userDidChangeShowRawValueCheckBox)
        {
            // If the user has made a change, don't overwrite her changes.
            return;
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

    private boolean isScriptable()
    {
        return scriptableCheckbox.getValue();
    }

    private final FormPanel createFormPanel()
    {
        final FormPanel panel = new FormPanel();
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setWidth(LABEL_WIDTH + FIELD_WIDTH + 60);
        panel.setLabelWidth(LABEL_WIDTH);
        panel.setFieldWidth(FIELD_WIDTH);
        panel.setButtonAlign(HorizontalAlignment.RIGHT);
        saveButton = new Button(viewContext.getMessage(Dict.BUTTON_SAVE));
        saveButton.setStyleAttribute("marginRight", "20px");
        saveButton.setId(createChildId(SAVE_BUTTON_ID_SUFFIX));
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    submitForm();
                }
            });
        final Button resetButton = new Button(viewContext.getMessage(Dict.BUTTON_RESET));
        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    resetForm();
                }
            });
        panel.addButton(resetButton);
        panel.addButton(saveButton);
        return panel;
    }

    private String getSectionValue()
    {
        if (sectionSelectionWidget != null)
        {
            return sectionSelectionWidget.getSimpleValue();
        }
        return null;
    }

    private String getDefaultValue()
    {
        if (defaultValueField != null)
        {
            return PropertyFieldFactory.valueToString(defaultValueField.get().getValue());
        }
        return null;
    }

    private String getSelectedEntityCode()
    {
        return tryGetSelectedEntityType().getCode();
    }

    /**
     * extracts ordinal of an entity type property type after which new property will be added
     */
    private Long getPreviousETPTOrdinal()
    {
        return etptSelectionWidget.getSelectedEntityTypePropertyTypeOrdinal();
    }

    private EntityType tryGetSelectedEntityType()
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return experimentTypeSelectionWidget.tryGetSelectedExperimentType();
            case SAMPLE:
                return sampleTypeSelectionWidget.tryGetSelectedSampleType();
            case MATERIAL:
                return materialTypeSelectionWidget.tryGetSelectedMaterialType();
            case DATA_SET:
                return dataSetTypeSelectionWidget.tryGetSelectedDataSetType();
        }
        throw new IllegalArgumentException(UNSUPPORTED_ENTITY_KIND);
    }

    private final void addFormFields()
    {
        PropertyTypeSelectionWidget propertyTypeWidget = getPropertyTypeWidget();
        DropDownList<?, ?> typeSelectionWidget = getTypeSelectionWidget();
        formPanel.add(propertyTypeWidget);
        formPanel.add(typeSelectionWidget);
        formPanel.add(getScriptableCheckbox());
        formPanel.add(scriptTypeRadioGroup);
        formPanel.add(scriptChooser);
        formPanel.add(getMandatoryCheckbox());
        formPanel.add(getShownInEditViewCheckbox());
        formPanel.add(getShowRawValueCheckBox());
        updatePropertyTypeRelatedFields();

        modificationManager.addObserver(propertyTypeWidget);
        modificationManager.addObserver(typeSelectionWidget);
        if (defaultValueField != null)
        {
            modificationManager.addObserver(defaultValueField);
        }
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    private final class AssignPropertyTypeCallback extends AbstractAsyncCallback<String>
    {
        AssignPropertyTypeCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<String>(infoBox));
            saveButton.disable();
        }

        @Override
        protected final void process(final String result)
        {
            infoBox.displayInfo(result);
            resetForm();
            saveButton.enable();
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            super.finishOnFailure(caught);
            saveButton.enable();
        }
    }

    private void updatePropertyTypeRelatedFields()
    {
        hidePropertyTypeRelatedFields();
        final PropertyType propertyType = propertyTypeSelectionWidget.tryGetSelectedPropertyType();
        if (propertyType != null)
        {
            String fieldId = createChildId(DEFAULT_VALUE_ID_PART);
            DatabaseModificationAwareField<?> fieldHolder =
                    PropertyFieldFactory.createField(propertyType, false,
                            viewContext.getMessage(Dict.DEFAULT_VALUE), fieldId, null, viewContext);
            GWTUtils.setToolTip(fieldHolder.get(),
                    viewContext.getMessage(Dict.DEFAULT_VALUE_TOOLTIP));
            defaultValueField = fieldHolder;
            defaultValueField.get().show();
            FieldUtil.setVisibility(isScriptable() == false, defaultValueField.get());
            formPanel.add(defaultValueField.get());
        }
        updateEntityTypePropertyTypeRelatedFields();
    }

    private void hidePropertyTypeRelatedFields()
    {
        if (defaultValueField != null)
        {
            Field<?> field = defaultValueField.get();
            field.hide();
            formPanel.remove(field);
            defaultValueField = null;
        }
    }

    //

    private void updateEntityTypePropertyTypeRelatedFields()
    {
        hideEntityTypePropertyTypeRelatedFields();
        final PropertyType propertyType = propertyTypeSelectionWidget.tryGetSelectedPropertyType();
        final EntityType entityType = tryGetSelectedEntityType();
        if (propertyType != null && entityType != null)
        {
            final List<EntityTypePropertyType<?>> etpts =
                    new ArrayList<EntityTypePropertyType<?>>(entityType.getAssignedPropertyTypes());
            sectionSelectionWidget = createSectionSelectionWidget(etpts);
            formPanel.add(sectionSelectionWidget);
            etptSelectionWidget = createETPTSelectionWidget(etpts);
            formPanel.add(etptSelectionWidget);
        }
        layout();
    }

    private void hideEntityTypePropertyTypeRelatedFields()
    {
        if (sectionSelectionWidget != null && etptSelectionWidget != null)
        {
            sectionSelectionWidget.hide();
            etptSelectionWidget.hide();
            formPanel.remove(sectionSelectionWidget);
            formPanel.remove(etptSelectionWidget);
            sectionSelectionWidget = null;
            etptSelectionWidget = null;
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

    private SectionSelectionWidget createSectionSelectionWidget(
            List<EntityTypePropertyType<?>> etpts)
    {
        return SectionSelectionWidget.create(viewContext, etpts);
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

    private boolean isDynamic()
    {
        return isScriptable() && scriptTypeDynamic.getValue();
    }

    private boolean isManaged()
    {
        return isScriptable() && scriptTypeManaged.getValue();
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

    private boolean getShowRawValue()
    {
        if (false == (isManaged() && isShownInEditView()))
        {
            return false;
        }
        return showRawValueCheckBox.getValue();
    }

    private final void submitForm()
    {
        if (formPanel.isValid())
        {
            NewETPTAssignment newAssignment =
                    new NewETPTAssignment(entityKind,
                            propertyTypeSelectionWidget.tryGetSelectedPropertyTypeCode(),
                            getSelectedEntityCode(), getMandatoryCheckbox().getValue(),
                            getDefaultValue(), getSectionValue(), getPreviousETPTOrdinal(),
                            isDynamic(), isManaged(), tryGetScriptNameValue(), isShownInEditView(),
                            getShowRawValue());
            viewContext.getService().assignPropertyType(newAssignment,
                    new AssignPropertyTypeCallback(viewContext));
        }
    }

    private void resetForm()
    {
        formPanel.reset(); // updatePropertyTypeRelatedFields is invoked because selection changes
        // need to refresh list of assigned property types
        getTypeSelectionWidget().refreshStore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return modificationManager.getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        modificationManager.update(observedModifications);
    }

}
