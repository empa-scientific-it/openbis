/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.ScriptTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * {@link AbstractRegistrationForm} for registering and editing scripts.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractScriptEditRegisterForm extends AbstractRegistrationForm
{
    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    protected final ScriptTypeSelectionWidget scriptTypeChooserOrNull;

    protected final TextField<String> nameField;

    protected final DescriptionField descriptionField;

    protected final MultilineVarcharField scriptField;

    protected EntityKindSelectionWidget entityKindField;

    protected ScriptExecutionFramework scriptExecution;

    protected VarcharField entityKindListField;

    abstract protected void saveScript();

    abstract protected void setValues();

    abstract protected Script getScript();

    protected AbstractScriptEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext, EntityKind entityKindOrNull)
    {
        this(viewContext, null, entityKindOrNull);
    }

    public AbstractScriptEditRegisterForm(IViewContext<ICommonClientServiceAsync> viewContext,
            ScriptTypeSelectionWidget scriptTypeChooser, EntityKind entityKindOrNull)
    {
        this(viewContext, null, scriptTypeChooser, entityKindOrNull);
    }

    protected AbstractScriptEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext, TechId scriptIdOrNull,
            ScriptTypeSelectionWidget scriptTypeChooserOrNull, EntityKind entityKindOrNull)
    {
        super(viewContext, createId(scriptIdOrNull), DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;

        this.scriptTypeChooserOrNull = scriptTypeChooserOrNull;
        if (scriptTypeChooserOrNull != null)
        {
            ToolBar toolBar = null;
            toolBar = new ToolBar();
            setTopComponent(toolBar);

            scriptTypeChooserOrNull.setWidth(200);
            toolBar.add(new LabelToolItem(scriptTypeChooserOrNull.getFieldLabel()
                    + GenericConstants.LABEL_SEPARATOR));
            toolBar.add(scriptTypeChooserOrNull);
            scriptTypeChooserOrNull.addSelectionChangedListener(createScriptTypeChangedListener());
        }

        this.nameField = new VarcharField(viewContext.getMessage(Dict.NAME), true);
        this.nameField.setId(getId() + "-script-registration-name");
        this.scriptExecution =
                new ScriptExecutionFramework(viewContext, asValidable(formPanel), entityKindOrNull);
        entityKindListField = new VarcharField(viewContext.getMessage(Dict.ENTITY_KIND), false);
        entityKindListField.setVisible(false);
        this.entityKindField =
                new EntityKindSelectionWidget(viewContext, entityKindOrNull,
                        scriptIdOrNull == null, true);
        entityKindField
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<LabeledItem<EntityKind>>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<LabeledItem<EntityKind>>> se)
                        {
                            scriptExecution.updateEntityKind(entityKindField.tryGetEntityKind());
                        }
                    });
        entityKindField.setId(getId() + "-script-registration-entity-kind");
        this.descriptionField = AbstractRegistrationDialog.createDescriptionField(viewContext);
        this.descriptionField.setId(getId() + "-script-registration-description");
        this.scriptField = createScriptField(viewContext);
        this.scriptField.setId(getId() + "-script-registration-script-content");

        Listener<BaseEvent> scriptParametersListener = new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    PluginType pluginTypeOrNull = null;
                    scriptExecution.update(nameField.getValue(), scriptField.getValue(),
                            pluginTypeOrNull);
                }
            };

        scriptField.addListener(Events.Change, scriptParametersListener);
        nameField.addListener(Events.Change, scriptParametersListener);
    }

    private SelectionChangedListener<SimpleComboValue<ScriptType>> createScriptTypeChangedListener()
    {
        return new SelectionChangedListener<SimpleComboValue<ScriptType>>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<SimpleComboValue<ScriptType>> se)
                {
                    SimpleComboValue<ScriptType> selectedItem = se.getSelectedItem();
                    if (selectedItem != null)
                    {
                        onPluginOrScriptTypeChanged(PluginType.JYTHON, selectedItem.getValue());
                    }
                }
            };
    }

    protected void onPluginOrScriptTypeChanged(PluginType pluginType, ScriptType scriptType)
    {
        boolean pluginTypeIsPython = pluginType == PluginType.JYTHON;
        nameField.setVisible(pluginTypeIsPython);
        FieldUtil.setMandatoryFlag(nameField, pluginTypeIsPython);
        entityKindField.setVisible(pluginTypeIsPython);
        entityKindListField.setVisible(pluginTypeIsPython == false);
        scriptField.setVisible(pluginTypeIsPython);
        scriptField.setEnabled(pluginTypeIsPython);
        rightPanel.setVisible(scriptType == ScriptType.DYNAMIC_PROPERTY
                || scriptType == ScriptType.ENTITY_VALIDATION);
        this.scriptExecution.setScriptType(scriptType);
        if (pluginTypeIsPython)
        {
            scriptField.setValidator(validatorsByScriptType.get(scriptType));
        } else
        {
            scriptField.setValidator(null);
        }

        scriptExecution.update(nameField.getValue(), scriptField.getValue(), pluginType);
    }

    private IValidable asValidable(final FormPanel panel)
    {
        return new IValidable()
            {

                @Override
                public boolean isValid()
                {
                    return panel.isValid();
                }
            };
    }

    private static MultilineVarcharField createScriptField(
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final MultilineVarcharField field = new ScriptField(viewContext);
        field.setFireChangeEventOnSetValue(true);
        return field;
    }

    public static String createId(TechId id)
    {
        String editOrRegister = (id == null) ? "register" : ("edit_" + id);
        return GenericConstants.ID_PREFIX + "script-" + editOrRegister + "_form";
    }

    @Override
    protected void resetPanel()
    {
        super.resetPanel();
        nameField.reset();
    }

    private final void addFormFields()
    {
        formPanel.add(nameField);
        formPanel.add(entityKindListField);
        formPanel.add(entityKindField);
        formPanel.add(descriptionField);
        formPanel.add(scriptField);
        rightPanel.add(scriptExecution.getWidget());
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
                        saveScript();
                    }
                }
            });
    }

    @Override
    protected final void submitValidForm()
    {
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        setLoading(true);
        loadForm();
    }

    protected abstract void loadForm();

    protected void initGUI()
    {
        setValues();
        addFormFields();
        setLoading(false);
        layout();
    }

    //
    // script validators
    //

    private static Map<ScriptType, Validator> validatorsByScriptType =
            new HashMap<ScriptType, Validator>();

    static
    {
        validatorsByScriptType.put(ScriptType.DYNAMIC_PROPERTY,
                new DynamicPropertyScriptValidator());
        validatorsByScriptType.put(ScriptType.MANAGED_PROPERTY,
                new ManagedPropertyScriptValidator());
        validatorsByScriptType.put(ScriptType.ENTITY_VALIDATION,
                new EntityValidationScriptValidator());
    }

    private final static String NEWLINE = "\n";

    /** {@link Validator} for script of type {@link ScriptType#DYNAMIC_PROPERTY}. */
    private static class DynamicPropertyScriptValidator implements Validator
    {

        private final static String CALCULATE_DEFINITION = "def calculate():";

        private final static String CALCULATE_DEFINITION_NOT_FOUND_MSG =
                "Multiline script should contain definition of 'calculate()' function.";

        @Override
        public String validate(Field<?> field, final String fieldValue)
        {
            if (fieldValue.contains(NEWLINE))
            {
                final String[] lines = fieldValue.split(NEWLINE);
                for (String line : lines)
                {
                    if (line.startsWith(CALCULATE_DEFINITION))
                    {
                        // validated value is valid
                        return null;
                    }
                }
                return CALCULATE_DEFINITION_NOT_FOUND_MSG;
            }
            // validated value is valid
            return null;
        }
    }

    /** {@link Validator} for script of type {@link ScriptType#ENTITY_VALIDATION}. */
    private static class EntityValidationScriptValidator implements Validator
    {

        private final static String VALIDATE_DEFINITION = "def validate(";

        private final static String VALIDATE_DEFINITION_NOT_FOUND_MSG =
                "Script should contain definition of function 'validate(entity, is_new)'";

        @Override
        public String validate(Field<?> field, final String fieldValue)
        {
            final String[] lines = fieldValue.split(NEWLINE);
            for (String line : lines)
            {
                if (line.startsWith(VALIDATE_DEFINITION))
                {
                    return null;
                }
            }
            return VALIDATE_DEFINITION_NOT_FOUND_MSG;
        }
    }

    /** {@link Validator} for script of type {@link ScriptType#MANAGED_PROPERTY}. */
    private static class ManagedPropertyScriptValidator implements Validator
    {

        @Override
        public String validate(Field<?> field, String value)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

}
