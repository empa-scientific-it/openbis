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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.DataSetProteinColDefKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentAndReferenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;

/**
 * @author Franz-Josef Elmer
 */
class DataSetProteinGrid extends AbstractSimpleBrowserGrid<DataSetProtein>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "data-set-protein-browser";

    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    static IDisposableComponent create(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            Experiment experimentOrNull, TechId proteinReferenceID)
    {
        return new DataSetProteinGrid(viewContext, experimentOrNull, proteinReferenceID)
                .asDisposableWithoutToolbar();
    }

    private static String createWidgetID(Experiment experimentOrNull, TechId proteinReferenceID)
    {
        return "-" + (experimentOrNull == null ? "" : experimentOrNull.getIdentifier() + "-")
                + proteinReferenceID;
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private ListProteinByExperimentAndReferenceCriteria criteria;

    private DataSetProteinGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            Experiment experimentOrNull, TechId proteinReferenceID)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID
                + createWidgetID(experimentOrNull, proteinReferenceID), GRID_ID
                + createWidgetID(experimentOrNull, proteinReferenceID), true);
        specificViewContext = viewContext;
        criteria = new ListProteinByExperimentAndReferenceCriteria();
        if (experimentOrNull != null)
        {
            criteria.setExperimentID(new TechId(experimentOrNull.getId()));
        }
        criteria.setProteinReferenceID(proteinReferenceID);
        setDisplayTypeIDGenerator(PhosphoNetXDisplayTypeIDGenerator.DATA_SET_PROTEIN_BROWSER_GRID);
    }

    @Override
    protected IColumnDefinitionKind<DataSetProtein>[] getStaticColumnsDefinition()
    {
        return DataSetProteinColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<DataSetProtein>> getInitialFilters()
    {
        return asColumnFilters(new DataSetProteinColDefKind[] {});
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, DataSetProtein> resultSetConfig,
            AbstractAsyncCallback<ResultSet<DataSetProtein>> callback)
    {
        criteria.copyPagingConfig(resultSetConfig);
        specificViewContext.getService().listProteinsByExperimentAndReference(criteria, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<DataSetProtein> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportDataSetProteins(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

}
