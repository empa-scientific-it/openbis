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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDirectlyConnectedController;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
class SampleDataSetBrowser extends AbstractExternalDataGrid
{
    private static final String PREFIX = "sample-data-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IDirectlyConnectedController connectionTypeProvider;

    public static IDisposableComponent create(IViewContext<?> viewContext, TechId sampleId,
            final BasicEntityType sampleType,
            final IDirectlyConnectedController connectionTypeProvider)
    {
        IViewContext<ICommonClientServiceAsync> commonViewContext =
                viewContext.getCommonViewContext();

        SampleDataSetBrowser browser =
                new SampleDataSetBrowser(commonViewContext, sampleId, connectionTypeProvider)
                    {
                        @Override
                        public String getGridDisplayTypeID()
                        {
                            return super.getGridDisplayTypeID() + "-" + sampleType.getCode();
                        }

                    };
        return browser.asDisposableWithoutToolbar();
    }

    private final TechId sampleId;

    private SampleDataSetBrowser(IViewContext<ICommonClientServiceAsync> viewContext,
            TechId sampleId, IDirectlyConnectedController connectionTypeProvider)
    {
        super(viewContext, createBrowserId(sampleId), createGridId(sampleId),
                DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID);
        this.sampleId = sampleId;
        this.connectionTypeProvider = connectionTypeProvider;
        // refresh data when connection type provider value changes
        connectionTypeProvider.setOnChangeAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    refresh();
                }
            });
    }

    public static final String createGridId(TechId sampleId)
    {
        return createBrowserId(sampleId) + "-grid";
    }

    public static final String createBrowserId(TechId sampleId)
    {
        return ID_PREFIX + sampleId;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>> callback)
    {
        boolean onlyDirectlyConnected = connectionTypeProvider.isOnlyDirectlyConnected();
        viewContext.getService().listSampleDataSets(sampleId, resultSetConfig,
                onlyDirectlyConnected, callback);
    }
}
