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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddPropertyTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.rpc.AsyncCallback;

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

    private static final class UnassignmentPreparationCallback extends
            AbstractAsyncCallback<Integer>
    {
        private final IViewContext<ICommonClientServiceAsync> commonViewContext;

        private final EntityTypePropertyType<?> etpt;

        private final IBrowserGridActionInvoker invoker;

        private UnassignmentPreparationCallback(
                IViewContext<ICommonClientServiceAsync> viewContext,
                EntityTypePropertyType<?> etpt, IBrowserGridActionInvoker invoker)
        {
            super(viewContext);
            commonViewContext = viewContext;
            this.etpt = etpt;
            this.invoker = invoker;
        }

        @Override
        protected void process(Integer result)
        {
            Dialog dialog =
                    new UnassignmentConfirmationDialog(commonViewContext, etpt, result, invoker);
            dialog.show();
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

    private static final class UnassignmentConfirmationDialog extends Dialog
    {
        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final IBrowserGridActionInvoker invoker;

        private final EntityKind entityKind;

        private final String entityTypeCode;

        private final String propertyTypeCode;

        UnassignmentConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
                EntityTypePropertyType<?> etpt, int numberOfProperties,
                IBrowserGridActionInvoker invoker)
        {
            this.viewContext = viewContext;
            this.invoker = invoker;
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
            setWidth(400);
        }

        @Override
        protected void onButtonPressed(Button button)
        {
            super.onButtonPressed(button);
            if (button.getItemId().equals(Dialog.YES))
            {
                viewContext.getService().unassignPropertyType(
                        entityKind,
                        propertyTypeCode,
                        entityTypeCode,
                        AsyncCallbackWithProgressBar.decorate(new RefreshCallback(viewContext,
                                invoker), "Releasing assignment..."));
            }
        }
    }

    public static IDisposableComponent create(final IViewContext<ICommonClientServiceAsync> viewContext, EntityType entity)
    {
        return new PropertyTypeAssignmentGrid(viewContext, entity).asDisposableWithoutToolbar();
    }

    private final IDelegatedAction postRegistrationCallback;
    private final EntityType entity;
    
    private PropertyTypeAssignmentGrid(final IViewContext<ICommonClientServiceAsync> viewContext, EntityType entity)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.PROPERTY_TYPE_ASSIGNMENT_BROWSER_GRID);
        this.entity = entity;
        extendBottomToolbar();
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        if(entity == null) { //Generic View showing all assignments allow to edit
            Button editButton =
                    createSelectedItemButton(
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
                                            MessageBox.alert("Error", errorMsg, null);
                                        } else
                                        {
                                            createEditDialog(etpt).show();
                                        }
                                    }
                                });
            editButton.setId(GRID_ID + "-edit");
            addButton(editButton);
        } else { //View showing only property types for one type allow to add new properties  
            final EntityType addEntity = this.entity;
            final Button addButton =
                    new Button(viewContext.getMessage(Dict.BUTTON_ADD, ""),
                            new SelectionListener<ButtonEvent>()
                                {
                                    @Override
                                    public void componentSelected(ButtonEvent ce)
                                    {
                                        AddPropertyTypeDialog dialog = new AddPropertyTypeDialog(viewContext, createRefreshGridAction(), addEntity.getEntityKind(), addEntity.getCode());
                                        dialog.show();
                                    }
                                });
            addButton.setId(GRID_ID + "-add");
            addButton(addButton);
        }
        

        Button releaseButton =
                createSelectedItemButton(
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

        addEntityOperationsSeparator();
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

    private Window createEditDialog(final EntityTypePropertyType<?> etpt)
    {
        final EntityKind entityKind = etpt.getEntityKind();
        final String entityTypeCode = etpt.getEntityType().getCode();
        final String propertyTypeCode = etpt.getPropertyType().getCode();

        final String title =
                viewContext.getMessage(Dict.EDIT_PROPERTY_TYPE_ASSIGNMENT_TITLE,
                        entityKind.getDescription(), entityTypeCode, propertyTypeCode);

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                {
                    setScrollMode(Scroll.NONE);
                }

                Script script = etpt.getScript();

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

                    viewContext.getCommonService().listPropertyTypeAssignments(
                            etpt.getEntityType(),
                            new AbstractAsyncCallback<List<EntityTypePropertyType<?>>>(viewContext)
                                {
                                    @Override
                                    protected void process(List<EntityTypePropertyType<?>> etpts)
                                    {
                                        form.remove(loading);
                                        initFields(etpts);
                                        isLoaded = true;
                                    }
                                });
                }

                private void initFields(List<EntityTypePropertyType<?>> etpts)
                {
                    originalIsMandatory = etpt.isMandatory();

                    mandatoryCheckbox =
                            new CheckBoxField(viewContext.getMessage(Dict.MANDATORY), false);
                    mandatoryCheckbox.setValue(originalIsMandatory);

                    if (script != null)
                    {
                        mandatoryCheckbox.setVisible(false);
                    }
                    addField(mandatoryCheckbox);

                    scriptChooser =
                            createScriptChooserField(viewContext, script != null ? script.getName()
                                    : null, script != null, script != null ? script.getScriptType()
                                    : null, entityKind);
                    addField(scriptChooser);

                    shownInEditViewCheckBox =
                            new CheckBoxField(viewContext.getMessage(Dict.SHOWN_IN_EDIT_VIEW),
                                    false);
                    shownInEditViewCheckBox.setValue(etpt.isShownInEditView());
                    if (false == etpt.isManaged())
                    {
                        // This option is currently only available for managed properties.
                        shownInEditViewCheckBox.setVisible(false);
                    }
                    addField(shownInEditViewCheckBox);

                    showRawValuesCheckBox =
                            new CheckBoxField(viewContext.getMessage(Dict.SHOW_RAW_VALUE), false);
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
                        defaultValueField =
                                PropertyFieldFactory.createField(etpt.getPropertyType(), false,
                                        viewContext.getMessage(Dict.DEFAULT_UPDATE_VALUE),
                                        "default_value_field", null, viewContext).get();
                        defaultValueField.setToolTip(viewContext
                                .getMessage(Dict.DEFAULT_UPDATE_VALUE_TOOLTIP));
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
                        List<EntityTypePropertyType<?>> etpts)
                {
                    return SectionSelectionWidget.create(viewContext, etpts);
                }

                private EntityTypePropertyTypeSelectionWidget createETPTSelectionWidget(
                        List<EntityTypePropertyType<?>> allETPTs)
                {
                    // create a new list of items from all etpts assigned to entity type
                    final List<EntityTypePropertyType<?>> etpts =
                            new ArrayList<EntityTypePropertyType<?>>();
                    etpts.add(null); // null will be transformed into '(top)'
                    String initialPropertyTypeCodeOrNull = null;
                    String previousPropertyTypeCodeOrNull =
                            EntityTypePropertyTypeSelectionWidget.TOP_ITEM_CODE;
                    for (EntityTypePropertyType<?> currentETPT : allETPTs)
                    {
                        final String currentPropertyTypeCode =
                                currentETPT.getPropertyType().getCode();
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
                            new EntityTypePropertyTypeSelectionWidget(viewContext, getId(), etpts,
                                    initialPropertyTypeCodeOrNull);
                    FieldUtil.setMandatoryFlag(result, true);
                    return result;
                }

                private String getSectionValue()
                {
                    return sectionSelectionWidget.getSimpleValue();
                }

                /**
                 * extracts ordinal of an entity type property type after which edited property
                 * should be put
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
                    // The logic for defaulting the value of the shownInEditView check box is
                    // duplicated here to enforce the current semantics that this value is only
                    // considered by managed properties
                    if (false == etpt.isManaged())
                    {
                        if (etpt.isDynamic())
                        {
                            return false;
                        } else
                        {
                            return true;
                        }
                    }
                    return shownInEditViewCheckBox.getValue();
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
                        viewContext.getService().updatePropertyTypeAssignment(
                                new NewETPTAssignment(entityKind, propertyTypeCode, entityTypeCode,
                                        getMandatoryValue(), getDefaultValue(), getSectionValue(),
                                        getPreviousETPTOrdinal(), etpt.isDynamic(),
                                        etpt.isManaged(), etpt.getModificationDate(),
                                        tryGetScriptNameValue(), isShownInEditView(),
                                        getShowRawValue()), registrationCallback);
                    }
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.ASSIGNMENT,
                            HelpPageIdentifier.HelpPageAction.EDIT);
                }
            };
    }

    private void unassignPropertyType(final EntityTypePropertyType<?> etpt)
    {
        final EntityKind entityKind = etpt.getEntityKind();
        final String entityTypeCode = etpt.getEntityType().getCode();
        final String propertyTypeCode = etpt.getPropertyType().getCode();
        final IBrowserGridActionInvoker invoker = asActionInvoker();
        final AsyncCallback<Integer> callback =
                new UnassignmentPreparationCallback(viewContext, etpt, invoker);
        viewContext.getService().countPropertyTypedEntities(entityKind, propertyTypeCode,
                entityTypeCode, callback);
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
                    
        viewContext.getService().listPropertyTypeAssignments(resultSetConfig, entity, extendedCallback);
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

}
