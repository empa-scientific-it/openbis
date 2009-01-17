/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.List;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * A {@link LayoutContainer} which contains the grid where the samples are displayed.
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
public final class SampleBrowserGrid extends AbstractBrowserGrid<Sample, SampleModel>
{
    private static final String PREFIX = "sample-browser-grid_";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX + "sample_browser";

    public static final String GRID_ID = GenericConstants.ID_PREFIX + PREFIX + "grid";

    private ListSampleCriteria criteria;

    SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, GRID_ID);
        setId(BROWSER_ID);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Sample>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listSamples(criteria, callback);
    }

    private void copyPagingConfig(DefaultResultSetConfig<String> resultSetConfig)
    {
        criteria.setLimit(resultSetConfig.getLimit());
        criteria.setOffset(resultSetConfig.getOffset());
        criteria.setSortInfo(resultSetConfig.getSortInfo());
        criteria.setResultSetKey(resultSetConfig.getResultSetKey());
    }

    @Override
    protected List<SampleModel> createModels(List<Sample> entities)
    {
        return SampleModel.asSampleModels(entities);
    }

    @Override
    protected void showEntityViewer(SampleModel sampleModel)
    {
        final Sample sample = sampleModel.getBaseObject();
        final EntityKind entityKind = EntityKind.SAMPLE;
        final ITabItemFactory tabView =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        sample.getSampleType()).createClientPlugin(entityKind).createEntityViewer(
                        sample);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    private static final String createHeader(ListSampleCriteria criteria)
    {
        final StringBuilder builder = new StringBuilder("Samples");
        builder.append(" of type ");
        builder.append(criteria.getSampleType().getCode());
        if (criteria.isIncludeGroup())
        {
            builder.append(" belonging to the group ");
            builder.append(criteria.getGroupCode());
        }
        if (criteria.isIncludeInstance())
        {
            if (criteria.isIncludeGroup())
            {
                builder.append(" or shared");
            } else
            {
                builder.append(" which are shared among all the groups");
            }
        }
        return builder.toString();
    }

    /**
     * Refreshes the sample browser grid up to given parameters.
     * <p>
     * Note that, doing so, the result set associated on the server side with this
     * <code>resultSetKey</code> will be removed.
     * </p>
     */
    public final void refresh(ListSampleCriteria newCriteria,
            final IDataRefreshCallback newRefreshCallback)
    {
        boolean refreshColumnsDefinition = hasColumnsDefinitionChanged(newCriteria);
        this.criteria = newCriteria;
        String newHeader = createHeader(newCriteria);

        super.refresh(newRefreshCallback, newHeader, refreshColumnsDefinition);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Sample> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSamples(exportCriteria, callback);
    }

    @Override
    protected ColumnDefsAndConfigs<Sample> createColumnsDefinition()
    {
        return SampleModel.createColumnsSchema(viewContext, criteria.getSampleType());
    }

    private boolean hasColumnsDefinitionChanged(ListSampleCriteria newCriteria)
    {
        SampleType sampleType = newCriteria.getSampleType();
        return (criteria == null || sampleType.equals(criteria.getSampleType()) == false);
    }
}
