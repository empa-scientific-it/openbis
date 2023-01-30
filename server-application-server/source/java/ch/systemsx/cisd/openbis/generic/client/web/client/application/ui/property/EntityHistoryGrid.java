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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityHistoryCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid for historical entity values.
 * 
 * @author Franz-Josef Elmer
 */
public class EntityHistoryGrid extends TypedTableGrid<EntityHistory>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "entity_history_browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static DisposableTabContent createPropertiesHistorySection(
            final IViewContext<? extends IClientServiceAsync> viewContext,
            final EntityKind entityKind, final TechId entityID)
    {
        DisposableTabContent tabContent =
                new DisposableTabContent(viewContext.getMessage(Dict.ENTITY_HISTORY_TAB),
                        viewContext, null)
                    {
                        @Override
                        protected IDisposableComponent createDisposableContent()
                        {
                            EntityHistoryGrid grid =
                                    new EntityHistoryGrid(
                                            viewContext.getCommonViewContext(), entityKind,
                                            entityID);
                            return grid.asDisposableWithoutToolbar();
                        }
                    };
        tabContent.setIds(DisplayTypeIDGenerator.ENTITY_HISTORY_SECTION);
        return tabContent;
    }

    private EntityKind entityKind;

    private TechId entityID;

    private EntityHistoryGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKind, TechId entityID)
    {
        super(viewContext, BROWSER_ID, true,
                DisplayTypeIDGenerator.ENTITY_HISTORY_BROWSER_GRID);
        this.entityKind = entityKind;
        this.entityID = entityID;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<EntityHistory>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<EntityHistory>> callback)
    {
        ListEntityHistoryCriteria criteria = new ListEntityHistoryCriteria();
        criteria.copyPagingConfig(resultSetConfig);
        criteria.setEntityKind(entityKind);
        criteria.setEntityID(entityID);
        viewContext.getService().listEntityHistory(criteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<EntityHistory>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportEntityHistory(exportCriteria, callback);
    }

}
