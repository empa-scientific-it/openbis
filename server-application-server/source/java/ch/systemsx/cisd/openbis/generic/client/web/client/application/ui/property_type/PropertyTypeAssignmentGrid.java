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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddPropertyTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SemanticAnnotationGridColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid with 'entity type' - 'property type' assignments.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeAssignmentGrid extends TypedTableGrid<EntityTypePropertyType<?>>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "property-type-assignment-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    private interface UnassigmentExecution
    {
        void executeUnassignment();
    }

    private static final class UnassignmentPreparationCallback extends
            AbstractAsyncCallback<Integer>
    {
        private final IViewContext<ICommonClientServiceAsync> commonViewContext;

        private final EntityTypePropertyType<?> etpt;

        private final IBrowserGridActionInvoker invoker;

        private final UnassigmentExecution execution;

        private UnassignmentPreparationCallback(
                IViewContext<ICommonClientServiceAsync> viewContext,
                EntityTypePropertyType<?> etpt, IBrowserGridActionInvoker invoker, UnassigmentExecution execution)
        {
            super(viewContext);
            commonViewContext = viewContext;
            this.etpt = etpt;
            this.invoker = invoker;
            this.execution = execution;
        }

        @Override
        protected void process(Integer result)
        {
            Dialog dialog =
                    new UnassignmentConfirmationDialog(commonViewContext, etpt, result, invoker, execution);
            dialog.show();
        }
    }

    private static final class UnassignmentConfirmationDialog extends Dialog
    {
        private final EntityKind entityKind;

        private final String entityTypeCode;

        private final String propertyTypeCode;

        private final UnassigmentExecution execution;

        UnassignmentConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
                EntityTypePropertyType<?> etpt, int numberOfProperties,
                IBrowserGridActionInvoker invoker, UnassigmentExecution execution)
        {
            setHeading(viewContext.getMessage(Dict.UNASSIGNMENT_CONFIRMATION_DIALOG_TITLE));
            setButtons(Dialog.YESNO);
            setHideOnButtonClick(true);
            setModal(true);
            entityKind = etpt.getEntityKind();
            entityTypeCode = etpt.getEntityType().getCode();
            propertyTypeCode = etpt.getPropertyType().getCode();
            String entityKindCode = entityKind.toString().toLowerCase();
            if (numberOfProperties == 0)
            {
                addText(viewContext.getMessage(
                        Dict.UNASSIGNMENT_CONFIRMATION_TEMPLATE_WITHOUT_PROPERTIES, entityKindCode,
                        entityTypeCode, propertyTypeCode));
            } else
            {
                addText(viewContext.getMessage(
                        Dict.UNASSIGNMENT_CONFIRMATION_TEMPLATE_WITH_PROPERTIES, entityKindCode,
                        entityTypeCode, propertyTypeCode, numberOfProperties));
            }
            this.execution = execution;
            setWidth(400);
        }

        @Override
        protected void onButtonPressed(Button button)
        {
            super.onButtonPressed(button);
            if (button.getItemId().equals(Dialog.YES))
            {
                execution.executeUnassignment();
            }
        }
    }

    private static final class RefreshCallback extends AbstractAsyncCallback<Void>
    {
        private final IBrowserGridActionInvoker invoker;

        private RefreshCallback(IViewContext<?> viewContext, IBrowserGridActionInvoker invoker)
        {
            super(viewContext);
            this.invoker = invoker;
        }

        @Override
        protected void process(Void result)
        {
            invoker.refresh();
        }
    }

    public static IDisposableComponent create(final IViewContext<ICommonClientServiceAsync> viewContext, EntityType entity,
            NewETNewPTAssigments newTypeWithAssigments, boolean isEntityTypeEdit)
    {
        return new PropertyTypeAssignmentGrid(viewContext, entity, newTypeWithAssigments, isEntityTypeEdit).asDisposableWithoutToolbar();
    }

    private final IDelegatedAction postRegistrationCallback;

    private final EntityType entity;

    private final PropertyTypeAssignmentGridAssignmentsHolder assignmentsHolder;

    private final boolean isEntityTypeEdit;

    private List<Listener<BaseEvent>> dirtyChangeListeners =
            new ArrayList<Listener<BaseEvent>>();

    private PropertyTypeAssignmentGrid(final IViewContext<ICommonClientServiceAsync> viewContext, EntityType entity,
            NewETNewPTAssigments newTypeWithAssigments, boolean isEntityTypeEdit)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.PROPERTY_TYPE_ASSIGNMENT_BROWSER_GRID);
        this.entity = entity;
        this.assignmentsHolder = new PropertyTypeAssignmentGridAssignmentsHolder(newTypeWithAssigments);
        this.isEntityTypeEdit = isEntityTypeEdit;
        extendBottomToolbar();
        postRegistrationCallback = createRefreshGridAction(new IDataRefreshCallback()
            {
                @Override
                public void postRefresh(boolean wasSuccessful)
                {
                    notifyDirtyChangeListeners();
                }
            });
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        //
        // Buttons used by the in memory grid form to create new entity types
        //
        if (assignmentsHolder.getAssignments() != null)
        {
            final Button addButton =
                    new Button(viewContext.getMessage(Dict.BUTTON_ADD, ""),
                            new SelectionListener<ButtonEvent>()
                                {
                                    @Override
                                    public void componentSelected(ButtonEvent ce)
                                    {
                                        AddPropertyTypeDialog dialog = new AddPropertyTypeDialog(
                                                viewContext,
                                                createRefreshGridAction(),
                                                assignmentsHolder.getAssignments().getEntity().getEntityKind(),
                                                null,
                                                new InMemoryGridAddCallback(),
                                                assignmentsHolder.getAssignments().getEntity(),
                                                isEntityTypeEdit);
                                        dialog.show();
                                    }
                                });
            addButton.setId(GRID_ID + "-add");
            addButton(addButton);

            Button editButton = createSelectedItemButton(
                    viewContext.getMessage(Dict.BUTTON_EDIT),
                    new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<EntityTypePropertyType<?>>>>()
                        {

                            @Override
                            public void invoke(
                                    BaseEntityModel<TableModelRowWithObject<EntityTypePropertyType<?>>> selectedItem,
                                    boolean keyPressed)
                            {
                                final EntityTypePropertyType<?> etpt = selectedItem.getBaseObject().getObjectOrNull();
                                if (etpt.isManagedInternally())
                                {
                                    final String errorMsg = "Assignments of internally managed property types cannot be edited.";
                                    GWTUtils.alert("Error", errorMsg);
                                } else
                                {
                                    createEditDialog(etpt, assignmentsHolder.getAssignments()).show();
                                }
                            }
                        });
            editButton.setId(GRID_ID + "-edit");
            addButton(editButton);

            Button removeButton =
                    createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                            new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<EntityTypePropertyType<?>>>>()
                                {
                                    @Override
                                    public void invoke(BaseEntityModel<TableModelRowWithObject<EntityTypePropertyType<?>>> selectedItem,
                                            boolean keyPressed)
                                    {
                                        final IBrowserGridActionInvoker invoker = asActionInvoker();
                                        final EntityTypePropertyType<?> etpt = selectedItem.getBaseObject().getObjectOrNull();
                                        final EntityKind entityKind = etpt.getEntityKind();
                                        final String entityTypeCode = etpt.getEntityType().getCode();
                                        final String propertyTypeCode = etpt.getPropertyType().getCode();

                                        final AsyncCallback<Integer> callback = new UnassignmentPreparationCallback(viewContext, etpt, invoker,
                                                new UnassigmentExecution()
                                                    {
                                                        @Override
                                                        public void executeUnassignment()
                                                        {
                                                            (new InMemoryGridRemoveCallback()).callback(etpt);
                                                        }
                                                    });
                                        viewContext.getService().countPropertyTypedEntities(entityKind, propertyTypeCode, entityTypeCode, callback);

                                    }

                                });
            removeButton.setId(GRID_ID + "-remove");
            addButton(removeButton);
        }

        //
        // Button used by the entity types grids
        //
        if (entity != null)
        { // View showing only property types for one type allow to add new properties
            final EntityType addEntity = this.entity;
            final Button addButton =
                    new Button(viewContext.getMessage(Dict.BUTTON_ADD, ""),
                            new SelectionListener<ButtonEvent>()
                                {
                                    @Override
                                    public void componentSelected(ButtonEvent ce)
                                    {
                                        AddPropertyTypeDialog dialog =
                                                new AddPropertyTypeDialog(viewContext, createRefreshGridAction(), addEntity.getEntityKind(),
                                                        addEntity.getCode(), null, null, true);
                                        dialog.show();
                                    }
                                });
            addButton.setId(GRID_ID + "-add");
            addButton(addButton);
        }

        //
        // Buttons used by the entity types grids and propertyes browser
        //
        if (assignmentsHolder.getAssignments() == null)
        {
            Button editButton = createSelectedItemButton(
                    viewContext.getMessage(Dict.BUTTON_EDIT),
                    new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<EntityTypePropertyType<?>>>>()
                        {

                            @Override
                            public void invoke(
                                    BaseEntityModel<TableModelRowWithObject<EntityTypePropertyType<?>>> selectedItem,
                                    boolean keyPressed)
                            {
                                final EntityTypePropertyType<?> etpt =
                                        selectedItem.getBaseObject().getObjectOrNull();
                                if (etpt.isManagedInternally())
                                {
                                    final String errorMsg =
                                            "Assignments of internally managed property types cannot be edited.";
                                    GWTUtils.alert("Error", errorMsg);
                                } else
                                {
                                    createEditDialog(etpt, null).show();
                                }
                            }
                        });
            editButton.setId(GRID_ID + "-edit");
            addButton(editButton);

            Button releaseButton = createSelectedItemButton(
                    viewContext.getMessage(Dict.UNASSIGN_BUTTON_LABEL),
                    new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<EntityTypePropertyType<?>>>>()
                        {
                            @Override
                            public void invoke(
                                    BaseEntityModel<TableModelRowWithObject<EntityTypePropertyType<?>>> selectedItem,
                                    boolean keyPressed)
                            {
                                final EntityTypePropertyType<?> etpt =
                                        selectedItem.getBaseObject().getObjectOrNull();
                                unassignPropertyType(etpt);
                            }

                        });
            releaseButton.setId(GRID_ID + "-release");
            addButton(releaseButton);
        }

        addEntityOperationsSeparator();
    }

    public class InMemoryGridRemoveCallback
    {
        public void callback(final EntityTypePropertyType<?> etpt)
        {
            String codeToDelete = etpt.getPropertyType().getCode();
            assignmentsHolder.getAssignments().refreshOrderDelete(codeToDelete);
            refresh(new IDataRefreshCallback()
                {
                    @Override
                    public void postRefresh(boolean wasSuccessful)
                    {
                        notifyDirtyChangeListeners();
                    }
                });
        }
    }

    public class InMemoryGridAddCallback
    {
        @SuppressWarnings("deprecation")
        public void callback(boolean isExixtingPropertyType, PropertyType propertyType, NewETPTAssignment assignment, AddPropertyTypeDialog dialog)
        {
            NewPTNewAssigment newPTNewAssigment = new NewPTNewAssigment();
            newPTNewAssigment.setExistingPropertyType(isExixtingPropertyType);
            newPTNewAssigment.setPropertyType(propertyType);
            newPTNewAssigment.setAssignment(assignment);
            try
            {
                assignmentsHolder.getAssignments().refreshOrderAdd(newPTNewAssigment);
                dialog.close();
                refresh(new IDataRefreshCallback()
                    {
                        @Override
                        public void postRefresh(boolean wasSuccessful)
                        {
                            notifyDirtyChangeListeners();
                        }
                    });
            } catch (Exception ex)
            {
                GWTUtils.alert("Error", ex.getMessage());
            }
        }
    }

    private static ScriptChooserField createScriptChooserField(
            final IViewContext<ICommonClientServiceAsync> viewContext, String initialValue,
            boolean visible, ScriptType scriptTypeOrNull, EntityKind entityKindOrNull)
    {
        ScriptChooserField field =
                ScriptChooserField.create(viewContext.getMessage(Dict.PLUGIN_PLUGIN), true,
                        initialValue, viewContext, scriptTypeOrNull, entityKindOrNull);
        FieldUtil.setVisibility(visible, field);
        return field;
    }

    private Window createEditDialog(final EntityTypePropertyType<?> etpt, final NewETNewPTAssigments newETNewPTAssigments)
    {
        final EntityKind entityKind = etpt.getEntityKind();
        final String entityTypeCode = etpt.getEntityType().getCode();
        final String propertyTypeCode = etpt.getPropertyType().getCode();
        final String title = viewContext.getMessage(
                Dict.EDIT_PROPERTY_TYPE_ASSIGNMENT_TITLE,
                EntityTypeUtils.translatedEntityKindForUI(viewContext, entityKind),
                entityTypeCode,
                propertyTypeCode);

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                {
                    setScrollMode(Scroll.NONE);
                }

                Script script = etpt.getScript();

                private CodeField codeField;

                private boolean originalIsMandatory;

                private SectionSelectionWidget sectionSelectionWidget;

                private EntityTypePropertyTypeSelectionWidget etptSelectionWidget;

                private CheckBox mandatoryCheckbox;

                private Field<?> defaultValueField;

                private ScriptChooserField scriptChooser;

                private CheckBox shownInEditViewCheckBox;

                private CheckBox showRawValuesCheckBox;

                private Label loading;

                private boolean isLoaded = false;

                {
                    loading = new Label(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
                    addField(loading);

                    viewContext.getCommonService().listPropertyTypeAssignments(etpt.getEntityType(),
                            new AbstractAsyncCallback<List<EntityTypePropertyType<?>>>(viewContext)
                                {
                                    @Override
                                    protected void process(List<EntityTypePropertyType<?>> etpts)
                                    {
                                        form.remove(loading);
                                        if (assignmentsHolder.getAssignments() == null)
                                        {
                                            initFields(etpts);
                                        } else
                                        {
                                            initFields(assignmentsHolder.getAssignments().getEntity().getAssignedPropertyTypes());
                                        }
                                        isLoaded = true;
                                    }
                                });
                }

                private void initFields(List<? extends EntityTypePropertyType<?>> etpts)
                {
                    // Code Field
                    if (newETNewPTAssigments != null && newETNewPTAssigments.isNewPropertyType(etpt.getPropertyType().getCode()))
                    {
                        codeField = new CodeField(viewContext, viewContext.getMessage(Dict.CODE));
                        codeField.setId(getId() + "_code");
                        codeField.setValue(etpt.getPropertyType().getCode());
                        addField(codeField);
                    }

                    // Mandatory Field
                    originalIsMandatory = etpt.isMandatory();
                    mandatoryCheckbox = new CheckBoxField(viewContext.getMessage(Dict.MANDATORY), false);
                    mandatoryCheckbox.setValue(originalIsMandatory);
                    if (script != null)
                    {
                        mandatoryCheckbox.setVisible(false);
                    }
                    addField(mandatoryCheckbox);

                    // Script Field
                    scriptChooser = createScriptChooserField(viewContext, script != null ? script.getName()
                            : null, script != null, script != null ? script.getScriptType()
                                    : null,
                            entityKind);
                    addField(scriptChooser);

                    // Show in edit views Field
                    shownInEditViewCheckBox = new CheckBoxField(viewContext.getMessage(Dict.SHOWN_IN_EDIT_VIEW), false);
                    shownInEditViewCheckBox.setValue(etpt.isShownInEditView());
                    shownInEditViewCheckBox.setVisible(!etpt.isDynamic()); // This option is shown for all non system generated properties
                    shownInEditViewCheckBox.addListener(Events.Change, new Listener<FieldEvent>()
                        {
                            @Override
                            public void handleEvent(FieldEvent be)
                            {
                                if (!shownInEditViewCheckBox.getValue())
                                {
                                    mandatoryCheckbox.setValue(Boolean.FALSE);
                                }
                            }
                        });

                    addField(shownInEditViewCheckBox);

                    // Show raw values Field
                    showRawValuesCheckBox = new CheckBoxField(viewContext.getMessage(Dict.SHOW_RAW_VALUE), false);
                    showRawValuesCheckBox.setValue(etpt.getShowRawValue());
                    if (false == etpt.isManaged())
                    {
                        // This option is currently only available for managed properties.
                        showRawValuesCheckBox.setVisible(false);
                    }
                    addField(showRawValuesCheckBox);

                    // default value needs to be specified only if currently property is optional
                    if (originalIsMandatory == false)
                    {
                        String originalRawValue = null;
                        if (newETNewPTAssigments != null)
                        {
                            for (NewPTNewAssigment assigment : newETNewPTAssigments.getAssigments())
                            {
                                if (assigment.getAssignment().getPropertyTypeCode().equals(propertyTypeCode))
                                {
                                    originalRawValue = assigment.getAssignment().getDefaultValue();
                                }
                            }
                        }

                        defaultValueField = PropertyFieldFactory.createField(
                                etpt.getPropertyType(),
                                false,
                                viewContext.getMessage(Dict.DEFAULT_UPDATE_VALUE),
                                "default_value_field",
                                originalRawValue,
                                viewContext).get();

                        defaultValueField.setToolTip(viewContext.getMessage(Dict.DEFAULT_UPDATE_VALUE_TOOLTIP));
                        addField(defaultValueField);

                        mandatoryCheckbox.addListener(Events.Change, new Listener<FieldEvent>()
                            {
                                @Override
                                public void handleEvent(FieldEvent be)
                                {
                                    defaultValueField.setVisible(getMandatoryValue()
                                            && etpt.isDynamic() == false);
                                }
                            });
                        mandatoryCheckbox.fireEvent(Events.Change);
                    } else
                    {
                        defaultValueField = null;
                    }

                    sectionSelectionWidget = createSectionSelectionWidget(etpts);
                    sectionSelectionWidget.setSimpleValue(etpt.getSection());
                    addField(sectionSelectionWidget);

                    etptSelectionWidget = createETPTSelectionWidget(etpts);
                    addField(etptSelectionWidget);

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());

                    layout();
                    WindowUtils.resize(this, form.getElement());
                }

                private SectionSelectionWidget createSectionSelectionWidget(
                        List<? extends EntityTypePropertyType<?>> etpts)
                {
                    return SectionSelectionWidget.create(viewContext, etpts);
                }

                private EntityTypePropertyTypeSelectionWidget createETPTSelectionWidget(
                        List<? extends EntityTypePropertyType<?>> allETPTs)
                {
                    // create a new list of items from all etpts assigned to entity type
                    final List<EntityTypePropertyType<?>> etpts = new ArrayList<EntityTypePropertyType<?>>();
                    etpts.add(null); // null will be transformed into '(top)'
                    String initialPropertyTypeCodeOrNull = null;
                    String previousPropertyTypeCodeOrNull = EntityTypePropertyTypeSelectionWidget.TOP_ITEM_CODE;
                    for (EntityTypePropertyType<?> currentETPT : allETPTs)
                    {
                        final String currentPropertyTypeCode = currentETPT.getPropertyType().getCode();
                        if (propertyTypeCode.equals(currentPropertyTypeCode) == false)
                        {
                            etpts.add(currentETPT);
                            previousPropertyTypeCodeOrNull = currentPropertyTypeCode;
                        } else
                        {
                            initialPropertyTypeCodeOrNull = previousPropertyTypeCodeOrNull;
                        }
                    }
                    final EntityTypePropertyTypeSelectionWidget result =
                            new EntityTypePropertyTypeSelectionWidget(viewContext, getId(), etpts, initialPropertyTypeCodeOrNull);
                    FieldUtil.setMandatoryFlag(result, true);
                    return result;
                }

                private String getSectionValue()
                {
                    return sectionSelectionWidget.getRawValue();
                }

                /**
                 * extracts ordinal of an entity type property type after which edited property should be put
                 */
                private Long getPreviousETPTOrdinal()
                {
                    return etptSelectionWidget.getSelectedEntityTypePropertyTypeOrdinal();
                }

                private String tryGetScriptNameValue()
                {
                    if (scriptChooser == null)
                    {
                        return null;
                    } else
                    {
                        return scriptChooser.getValue();
                    }
                }

                private String getDefaultValue()
                {
                    if (defaultValueField != null)
                    {
                        return PropertyFieldFactory.valueToString(defaultValueField.getValue());
                    }
                    return null;
                }

                private boolean getMandatoryValue()
                {
                    return mandatoryCheckbox.getValue();
                }

                private boolean isShownInEditView()
                {
                    if (etpt.isDynamic())
                    {
                        return false;
                    } else
                    {
                        return shownInEditViewCheckBox.getValue();
                    }
                }

                private boolean getShowRawValue()
                {
                    // The logic for defaulting the value of the showRawValue check box is
                    // duplicated here to enforce the current semantics that this value is only
                    // considered by managed properties
                    if (false == (etpt.isManaged() && isShownInEditView()))
                    {
                        return false;
                    }

                    return showRawValuesCheckBox.getValue();
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    if (isLoaded)
                    {
                        String propertyTypeCodeToUse = null;
                        if (codeField != null)
                        {
                            propertyTypeCodeToUse = codeField.getValue();
                        } else
                        {
                            propertyTypeCodeToUse = propertyTypeCode;
                        }

                        NewETPTAssignment toRegister = new NewETPTAssignment(entityKind, propertyTypeCodeToUse, entityTypeCode,
                                getMandatoryValue(), getDefaultValue(), getSectionValue(),
                                getPreviousETPTOrdinal(), etpt.isDynamic(),
                                etpt.isManaged(), etpt.getModificationDate(),
                                tryGetScriptNameValue(), isShownInEditView(),
                                getShowRawValue());

                        if (assignmentsHolder.getAssignments() == null)
                        {
                            viewContext.getService().updatePropertyTypeAssignment(toRegister, registrationCallback);
                        } else
                        {
                            try
                            {
                                if (codeField != null)
                                {
                                    assignmentsHolder.getAssignments().updateCodeFromNewPropertyType(etpt.getPropertyType().getCode(),
                                            codeField.getValue());
                                }
                                assignmentsHolder.getAssignments().refreshOrderUpdate(toRegister);
                                registrationCallback.onSuccess(null);
                            } catch (Exception ex)
                            {
                                registrationCallback.onFailure(ex);
                            }
                        }
                    }
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.ASSIGNMENT, HelpPageIdentifier.HelpPageAction.EDIT);
                }
            };
    }

    private void unassignPropertyType(final EntityTypePropertyType<?> etpt)
    {
        final EntityKind entityKind = etpt.getEntityKind();
        final String entityTypeCode = etpt.getEntityType().getCode();
        final String propertyTypeCode = etpt.getPropertyType().getCode();
        final IBrowserGridActionInvoker invoker = asActionInvoker();
        final AsyncCallback<Integer> callback = new UnassignmentPreparationCallback(viewContext, etpt, invoker,
                new ServerUnassingmentExecutor(etpt, invoker));
        viewContext.getService().countPropertyTypedEntities(entityKind, propertyTypeCode, entityTypeCode, callback);
    }

    private class ServerUnassingmentExecutor implements UnassigmentExecution
    {
        private final EntityKind entityKind;

        private final String entityTypeCode;

        private final String propertyTypeCode;

        private final IBrowserGridActionInvoker invoker;

        public ServerUnassingmentExecutor(final EntityTypePropertyType<?> etpt, IBrowserGridActionInvoker invoker)
        {
            this.entityKind = etpt.getEntityKind();
            this.entityTypeCode = etpt.getEntityType().getCode();
            this.propertyTypeCode = etpt.getPropertyType().getCode();
            this.invoker = invoker;
        }

        @Override
        public void executeUnassignment()
        {
            viewContext.getService().unassignPropertyType(
                    entityKind,
                    propertyTypeCode,
                    entityTypeCode,
                    AsyncCallbackWithProgressBar.decorate(new RefreshCallback(viewContext,
                            invoker), "Releasing assignment..."));
        }
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<EntityTypePropertyType<?>>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<EntityTypePropertyType<?>>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(PropertyTypeAssignmentGridColumnIDs.DESCRIPTION,
                createMultilineStringCellRenderer());

        new SemanticAnnotationGridColumns().setRenderers(schema);

        return schema;
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(PropertyTypeAssignmentGridColumnIDs.PROPERTY_TYPE_CODE,
                PropertyTypeAssignmentGridColumnIDs.ASSIGNED_TO,
                PropertyTypeAssignmentGridColumnIDs.TYPE_OF);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<EntityTypePropertyType<?>>> resultSetConfig,
            final AbstractAsyncCallback<TypedTableResultSet<EntityTypePropertyType<?>>> callback)
    {

        AbstractAsyncCallback<TypedTableResultSet<EntityTypePropertyType<?>>> extendedCallback =
                new AbstractAsyncCallback<TypedTableResultSet<EntityTypePropertyType<?>>>(
                        viewContext)
                    {
                        @Override
                        protected void process(TypedTableResultSet<EntityTypePropertyType<?>> result)
                        {
                            callback.onSuccess(result);
                        }

                        @Override
                        public void finishOnFailure(Throwable caught)
                        {
                            callback.finishOnFailure(caught);
                        }

                    };
        if (assignmentsHolder.getAssignments() == null)
        {
            viewContext.getService().listPropertyTypeAssignments(resultSetConfig, entity, extendedCallback);
        } else
        {
            viewContext.getService().listPropertyTypeAssignmentsFromBrowser(resultSetConfig, assignmentsHolder.getAssignments().getEntity(),
                    assignmentsHolder.getAssignments().getAssigments(), extendedCallback);
        }
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<EntityTypePropertyType<?>>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPropertyTypeAssignments(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROPERTY_TYPE_ASSIGNMENT);
    }

    public void addDirtyChangeListener(Listener<BaseEvent> listener)
    {
        dirtyChangeListeners.add(listener);
    }

    private void notifyDirtyChangeListeners()
    {
        BaseEvent event = new BaseEvent(this);
        for (Listener<BaseEvent> dirtyChangeListener : dirtyChangeListeners)
        {
            dirtyChangeListener.handleEvent(event);
        }
    }

    public boolean isDirty()
    {
        return assignmentsHolder.isDirty();
    }

}
