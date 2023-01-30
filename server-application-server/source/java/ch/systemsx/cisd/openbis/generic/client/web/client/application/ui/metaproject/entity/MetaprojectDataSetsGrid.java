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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.entity;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author pkupczyk
 */
public class MetaprojectDataSetsGrid extends AbstractExternalDataGrid
{

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "metaproject-data-set-browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    private TechId metaprojectId;

    public static MetaprojectDataSetsGrid create(final IViewContext<?> viewContext,
            TechId metaprojectId)
    {
        MetaprojectDataSetsGrid grid = new MetaprojectDataSetsGrid(viewContext, metaprojectId);
        grid.addEntityOperationsLabel();
        grid.addTaggingButtons(false);
        grid.addEntityOperationsSeparator();
        grid.allowMultipleSelection();
        return grid;
    }

    private MetaprojectDataSetsGrid(final IViewContext<?> viewContext, TechId metaprojectId)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID,
                DisplayTypeIDGenerator.RELATED_DATA_SET_GRID);
        this.metaprojectId = metaprojectId;
    }

    @Override
    protected void extendBottomToolbar()
    {
        // do nothing
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>> callback)
    {
        viewContext.getService().listMetaprojectDataSets(metaprojectId, resultSetConfig, callback);
    }
}
