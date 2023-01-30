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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddEntityTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Grid displaying data set types.
 * 
 * @author Piotr Buczek
 * @author Izabela Adamczyk
 */
public class DataSetTypeGrid extends AbstractEntityTypeGrid<DataSetType>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "data-set-type-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, ComponentProvider componentProvider)
    {
        final DataSetTypeGrid grid = new DataSetTypeGrid(viewContext, componentProvider);
        return grid.asDisposableWithoutToolbar();
    }

    private DataSetTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext, ComponentProvider componentProvider)
    {
        super(viewContext, componentProvider, BROWSER_ID, GRID_ID);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<DataSetType>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<DataSetType>> callback)
    {
        viewContext.getService().listDataSetTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<DataSetType>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetTypes(exportCriteria, callback);
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected void register(DataSetType dataSetType, AsyncCallback<Void> registrationCallback)
    {
        viewContext.getService().registerDataSetType(dataSetType, registrationCallback);
    }

    @Override
    protected DataSetType createNewEntityType()
    {
        return new DataSetType();
    }

    @Override
    protected Window createEditEntityTypeDialog(final EntityKind entityKind,
            final DataSetType dataSetType)
    {
        final String code = dataSetType.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE, 
                        EntityTypeUtils.translatedEntityKindForUI(viewContext, entityKind), code);
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                private final TextField<String> mainDataSetPatternField;

                private final TextField<String> mainDataSetPathField;

                private final CheckBoxField deletionDisallow;

                private final ScriptChooserField scriptChooser;

                {
                    descriptionField = createDescriptionField(viewContext);
                    FieldUtil
                            .setValueWithUnescaping(descriptionField, dataSetType.getDescription());
                    addField(descriptionField);

                    deletionDisallow = createDeletionDisallowField();
                    deletionDisallow.setValue(dataSetType.isDeletionDisallow());
                    addField(deletionDisallow);

                    mainDataSetPatternField = createMainDataSetPatternField();
                    FieldUtil.setValueWithUnescaping(mainDataSetPatternField,
                            dataSetType.getMainDataSetPattern());
                    addField(mainDataSetPatternField);

                    mainDataSetPathField = createMainDataSetPathField();
                    FieldUtil.setValueWithUnescaping(mainDataSetPathField,
                            dataSetType.getMainDataSetPath());
                    addField(mainDataSetPathField);

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());

                    Script script = dataSetType.getValidationScript();

                    scriptChooser =
                            createScriptChooserField(viewContext, script != null ? script.getName()
                                    : null, true, ScriptType.ENTITY_VALIDATION, EntityKind.DATA_SET);
                    addField(scriptChooser);

                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    dataSetType.setDescription(descriptionField.getValue());
                    dataSetType.setDeletionDisallow(deletionDisallow.getValue());
                    dataSetType.setMainDataSetPattern(mainDataSetPatternField.getValue());
                    dataSetType.setMainDataSetPath(mainDataSetPathField.getValue());

                    Script script = new Script();
                    script.setName(scriptChooser.getValue());
                    dataSetType.setValidationScript(script);

                    viewContext.getService().updateEntityType(entityKind, dataSetType,
                            registrationCallback);
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.DATA_SET_TYPE,
                            HelpPageIdentifier.HelpPageAction.EDIT);
                }

            };
    }

    @Override
    protected Window createRegisterEntityTypeDialog(String title, DataSetType newEntityType,
            EntityKind entityKind)
    {
        return new AddEntityTypeDialog<DataSetType>(viewContext, title, postRegistrationCallback,
                newEntityType, entityKind)
            {
                private TextField<String> mainDataSetPatternField;

                private TextField<String> mainDataSetPathField;

                private CheckBoxField deletionDisallow;

                {
                    deletionDisallow = createDeletionDisallowField();
                    addField(deletionDisallow);

                    mainDataSetPatternField = createMainDataSetPatternField();
                    addField(mainDataSetPatternField);

                    mainDataSetPathField = createMainDataSetPathField();
                    addField(mainDataSetPathField);

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());
                }

                @Override
                protected void register(DataSetType dataSetType,
                        AsyncCallback<Void> registrationCallback)
                {
                    dataSetType.setMainDataSetPath(mainDataSetPathField.getValue());
                    dataSetType.setMainDataSetPattern(mainDataSetPatternField.getValue());
                    dataSetType.setDeletionDisallow(deletionDisallow.getValue());

                    DataSetTypeGrid.this.register(dataSetType, registrationCallback);
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.DATA_SET_TYPE,
                            HelpPageIdentifier.HelpPageAction.REGISTER);
                }
            };
    }

    private TextField<String> createMainDataSetPatternField()
    {
        TextField<String> mainDataSetPatternField = new TextField<String>();
        mainDataSetPatternField.setFieldLabel(viewContext.getMessage(Dict.MAIN_DATA_SET_PATTERN));
        GWTUtils.setToolTip(mainDataSetPatternField,
                viewContext.getMessage(Dict.MAIN_DATA_SET_PATTERN_TOOLTIP));
        mainDataSetPatternField.setEmptyText(viewContext
                .getMessage(Dict.MAIN_DATA_SET_PATTERN_EXAMPLE));
        return mainDataSetPatternField;
    }

    private TextField<String> createMainDataSetPathField()
    {
        TextField<String> mainDataSetPathField = new TextField<String>();
        mainDataSetPathField.setFieldLabel(viewContext.getMessage(Dict.MAIN_DATA_SET_PATH));
        GWTUtils.setToolTip(mainDataSetPathField,
                viewContext.getMessage(Dict.MAIN_DATA_SET_PATH_TOOLTIP));
        mainDataSetPathField.setEmptyText(viewContext.getMessage(Dict.MAIN_DATA_SET_PATH_EXAMPLE));
        return mainDataSetPathField;
    }

    private CheckBoxField createDeletionDisallowField()
    {
        String label = viewContext.getMessage(Dict.DELETION_DISALLOW);
        CheckBoxField field = new CheckBoxField(label, false);
        GWTUtils.setToolTip(field, viewContext.getMessage(Dict.DELETION_DISALLOW_TOOLTIP));
        return field;
    }

    @Override
    public AddEntityTypeDialog<DataSetType> getNewDialog(DataSetType newType)
    {
        return (AddEntityTypeDialog<DataSetType>) createRegisterEntityTypeDialog("New DataSet", newType, newType.getEntityKind());
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] { createOrDelete(ObjectKind.DATASET_TYPE),
                edit(ObjectKind.DATASET_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE),
                edit(ObjectKind.PROPERTY_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
        };
    }
}
