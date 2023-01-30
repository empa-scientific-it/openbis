/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.grid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMetaprojectsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MetaprojectGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying metaprojects.
 * 
 * @author pkupczyk
 */
public class MetaprojectGrid extends TypedTableGrid<Metaproject>
{

    private IChosenEntitiesProvider<String> chosenProvider;

    public static final String METAPROJECT_CHOOSER_GRID_ID = GenericConstants.ID_PREFIX
            + "metaproject-chooser" + TypedTableGrid.GRID_POSTFIX;

    public static DisposableEntityChooser<TableModelRowWithObject<Metaproject>> createChooser(
            final IViewContext<?> viewContext, IChosenEntitiesProvider<String> chosenProvider)
    {
        final MetaprojectGrid grid =
                new MetaprojectGrid(viewContext, METAPROJECT_CHOOSER_GRID_ID,
                        DisplayTypeIDGenerator.METAPROJECT_CHOOSER_GRID, chosenProvider);
        grid.allowMultipleSelection();
        return grid.asDisposableWithoutToolbar();
    }

    private MetaprojectGrid(IViewContext<?> viewContext, String gridId,
            DisplayTypeIDGenerator gridDisplayTypeId, IChosenEntitiesProvider<String> chosenProvider)
    {
        super(viewContext.getCommonViewContext(), gridId, true, gridDisplayTypeId);
        this.chosenProvider = chosenProvider;
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Metaproject>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Metaproject>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(MetaprojectGridColumnIDs.DESCRIPTION,
                createMultilineStringCellRenderer());
        return schema;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Metaproject>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Metaproject>> callback)
    {
        ListMetaprojectsCriteria listCriteria = new ListMetaprojectsCriteria();
        List<String> chosenMetaprojects = chosenProvider.getEntities();

        if (chosenMetaprojects != null && chosenProvider.isBlackList())
        {
            listCriteria.setBlacklist(new HashSet<String>(chosenMetaprojects));
        } else if (chosenMetaprojects != null && chosenProvider.isBlackList() == false)
        {
            listCriteria.setWhitelist(new HashSet<String>(chosenMetaprojects));
        }

        listCriteria.copyPagingConfig(resultSetConfig);
        viewContext.getService().listMetaprojects(listCriteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Metaproject>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMetaprojects(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(MetaprojectGridColumnIDs.NAME);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

}
