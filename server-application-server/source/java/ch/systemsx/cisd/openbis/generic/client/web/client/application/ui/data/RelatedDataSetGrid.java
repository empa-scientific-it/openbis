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
 * 
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid with data sets related with specified entities.
 * 
 * @author Piotr Buczek
 */
public class RelatedDataSetGrid extends AbstractExternalDataGrid
{

    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "related-data-set-browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final RelatedDataSetCriteria<? extends IEntityInformationHolder> relatedCriteria)
    {
        RelatedDataSetGrid grid = new RelatedDataSetGrid(viewContext, relatedCriteria);
        return grid.asDisposableWithoutToolbar();
    }

    private RelatedDataSetCriteria<? extends IEntityInformationHolder> relatedCriteria;

    private RelatedDataSetGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            final RelatedDataSetCriteria<? extends IEntityInformationHolder> relatedCriteria)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.RELATED_DATA_SET_GRID);
        this.relatedCriteria = relatedCriteria;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>> callback)
    {
        viewContext.getService().searchForDataSets(relatedCriteria, resultSetConfig, callback);
    }

    @Override
    protected void refresh()
    {
        if (relatedCriteria == null)
        {
            return;
        }
        super.refresh();
    }

}
