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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IReportInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableGridColumnDefinition;

/**
 * @author Franz-Josef Elmer
 */
public class ReportGrid extends TypedTableGrid<ReportRowModel>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "DataSetReporterGrid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            TableModelReference tableModelReference, IReportInformationProvider infoProvider,
            String displaySettingsId)
    {
        final ReportGrid grid =
                new ReportGrid(viewContext, tableModelReference, displaySettingsId,
                        infoProvider.getDownloadURL());
        return grid.asDisposableWithoutToolbar();
    }

    public static String createId(String idSuffix)
    {
        return BROWSER_ID + "_" + idSuffix;
    }

    private final String resultSetKey;

    private final String displaySettingsId;

    private final TableModelReference tableModelReference;

    private ReportGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            TableModelReference tableModelReference, String displaySettingsId, String downloadURL)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.DATA_SET_REPORTING_GRID);
        setDownloadURL(downloadURL);
        setId(BROWSER_ID);
        this.tableModelReference = tableModelReference;
        this.resultSetKey = tableModelReference.getResultSetKey();
        this.displaySettingsId = displaySettingsId;
        updateDefaultRefreshButton();
    }

    @Override
    public String getGridDisplayTypeID()
    {
        return createGridDisplayTypeID(displaySettingsId);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<ReportRowModel>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<ReportRowModel>> callback)
    {
        // In all cases the data should be taken from the cache, and we know the key already.
        // The custom columns should be recomputed.
        resultSetConfig.setCacheConfig(ResultSetFetchConfig
                .createFetchFromCacheAndRecompute(resultSetKey));

        Set<IColumnDefinition<TableModelRowWithObject<ReportRowModel>>> availableColumns =
                new HashSet<IColumnDefinition<TableModelRowWithObject<ReportRowModel>>>();

        if (tableModelReference.getHeader() != null)
        {
            for (TableModelColumnHeader header : tableModelReference.getHeader())
            {
                availableColumns.add(new TypedTableGridColumnDefinition<ReportRowModel>(header,
                        null, null, null));
            }
        }

        resultSetConfig.setAvailableColumns(availableColumns);

        viewContext.getService().listReport(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<ReportRowModel>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportReport(exportCriteria, callback);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        // do nothing
    }

}
