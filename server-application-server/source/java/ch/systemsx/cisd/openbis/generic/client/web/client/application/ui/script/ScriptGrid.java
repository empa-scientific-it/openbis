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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListPermanentDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListScriptsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Grid displaying scripts.
 * 
 * @author Izabela Adamczyk
 */
public class ScriptGrid extends TypedTableGrid<Script>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "script-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static final String ADD_BUTTON_ID = BROWSER_ID + "_add-button";

    public static final String DELETE_BUTTON_ID = BROWSER_ID + "_delete-button";

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "-edit-button";

    private final EntityKind entityKindOrNull;

    private final ScriptType scriptTypeOrNull;

    public static DisposableEntityChooser<TableModelRowWithObject<Script>> create(
            final IViewContext<ICommonClientServiceAsync> viewContext, ScriptType scriptTypeOrNull,
            EntityKind entityKindOrNull)
    {
        final ScriptGrid grid = new ScriptGrid(viewContext, scriptTypeOrNull, entityKindOrNull);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private ScriptGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            ScriptType scriptTypeOrNull, EntityKind entityKindOrNull)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.SCRIPTS_BROWSER_GRID);
        this.entityKindOrNull = entityKindOrNull;
        this.scriptTypeOrNull = scriptTypeOrNull;
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addScriptButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD,
                        viewContext.getMessage(Dict.PLUGIN_PLUGIN)),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    DispatcherHelper.dispatchNaviEvent(new ComponentProvider(
                                            viewContext).getScriptRegistration(entityKindOrNull));
                                }
                            });
        addScriptButton.setId(ADD_BUTTON_ID);
        addButton(addScriptButton);

        Button editButton =
                createSelectedItemButton(
                        viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<Script>>>()
                            {
                                @Override
                                public void invoke(
                                        BaseEntityModel<TableModelRowWithObject<Script>> selectedItem,
                                        boolean keyPressed)
                                {
                                    openEditor(selectedItem, keyPressed);
                                }
                            });
        editButton.setId(EDIT_BUTTON_ID);
        addButton(editButton);

        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<Script>> scripts,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new ScriptListDeletionConfirmationDialog(viewContext,
                                            scripts, createRefreshCallback(invoker));
                                }
                            });
        addButton(deleteButton);
        allowMultipleSelection();

        addEntityOperationsSeparator();
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Script>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Script>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(ScriptGridColumnIDs.DESCRIPTION,
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(ScriptGridColumnIDs.SCRIPT,
                createMultilineStringCellRenderer());
        return schema;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Script>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Script>> callback)
    {
        ListScriptsCriteria criteria = new ListScriptsCriteria();
        criteria.copyPagingConfig(resultSetConfig);
        criteria.setScriptType(scriptTypeOrNull);
        criteria.setEntityKind(entityKindOrNull);
        viewContext.getService().listScripts(criteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Script>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportScripts(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ScriptGridColumnIDs.NAME);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { DatabaseModificationKind.createOrDelete(ObjectKind.SCRIPT),
                DatabaseModificationKind.edit(ObjectKind.SCRIPT) };
    }

    private void openEditor(BaseEntityModel<TableModelRowWithObject<Script>> selectedItem,
            boolean keyPressed)
    {
        final Script script = selectedItem.getBaseObject().getObjectOrNull();
        final TechId scriptId = TechId.create(script);
        AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    Component component = ScriptEditForm.create(viewContext, scriptId);
                    return DefaultTabItem
                            .createUnaware(getTabTitle(), component, true, viewContext);
                }

                @Override
                public String getId()
                {
                    return AbstractScriptEditRegisterForm.createId(scriptId);
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.EDIT_TITLE,
                            viewContext.getMessage(Dict.PLUGIN_PLUGIN), "");
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SCRIPT, HelpPageAction.EDIT);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
        tabFactory.setInBackground(keyPressed);
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    private static final class ScriptListDeletionConfirmationDialog extends
            AbstractDataListPermanentDeletionConfirmationDialog<TableModelRowWithObject<Script>>
    {

        public ScriptListDeletionConfirmationDialog(
                IViewContext<ICommonClientServiceAsync> viewContext,
                List<TableModelRowWithObject<Script>> data, AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data, callback);
        }

        @Override
        protected void executeDeletion(AsyncCallback<Void> deletionCallback)
        {
            viewContext.getCommonService().deleteScripts(TechId.createList(data), deletionCallback);
        }

        @Override
        protected String getEntityName()
        {
            return messageProvider.getMessage(Dict.PLUGIN_PLUGIN);
        }

    }
}
