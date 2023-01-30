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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * {@link AbstractExternalDataGrid} containing data sets directly connected with a specified data set being in a specified relationship role
 * (parent/child).
 * 
 * @author Piotr Buczek
 */
public class DataSetRelationshipBrowser extends AbstractExternalDataGrid
{
    private static final String PREFIX = "data-set-relationships-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static IDisposableComponent create(IViewContext<?> viewContext, TechId datasetId,
            final DataSetRelationshipRole role, final DataSetType datasetType)
    {
        IViewContext<ICommonClientServiceAsync> commonViewContext =
                viewContext.getCommonViewContext();
        DataSetRelationshipBrowser browser =
                new DataSetRelationshipBrowser(commonViewContext, datasetId, role)
                    {
                        @Override
                        public String getGridDisplayTypeID()
                        {
                            return super.getGridDisplayTypeID() + "-" + datasetType.getCode() + "-"
                                    + role;
                        }

                    };
        return browser.asDisposableWithoutToolbar();
    }

    private final TechId datasetId;

    private final DataSetRelationshipRole role;

    private DataSetRelationshipBrowser(IViewContext<ICommonClientServiceAsync> viewContext,
            TechId datasetId, DataSetRelationshipRole role)
    {
        super(viewContext, createBrowserId(datasetId, role), createGridId(datasetId, role),
                DisplayTypeIDGenerator.DATA_SET_DETAILS_GRID);
        this.datasetId = datasetId;
        this.role = role;
    }

    public static final String createGridId(TechId datasetId, DataSetRelationshipRole role)
    {
        return createBrowserId(datasetId, role) + "-grid";
    }

    public static final String createBrowserId(TechId datasetId, DataSetRelationshipRole role)
    {
        return ID_PREFIX + datasetId + "-" + role;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>> callback)
    {
        viewContext.getService().listDataSetRelationships(datasetId, role, resultSetConfig,
                callback);
    }
}
