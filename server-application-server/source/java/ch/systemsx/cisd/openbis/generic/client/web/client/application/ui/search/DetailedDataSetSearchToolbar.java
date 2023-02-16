/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.List;

import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetGridUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetProcessingMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ReportingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetGridUtils.IAddProcessingPluginsMenuAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Extension of {@link DetailedSearchWindow} adding widgets for reporting and processing plugins.
 * 
 * @author Piotr Buczek
 */
public class DetailedDataSetSearchToolbar extends DetailedSearchToolbar
{
    private final ReportingPluginSelectionWidget reportSelectionWidget;

    public DetailedDataSetSearchToolbar(final IViewContext<?> viewContext,
            final DataSetSearchHitGrid grid, String buttonName,
            final DetailedSearchWindow searchWindow,
            final ReportingPluginSelectionWidget reportSelectionWidget)
    {
        this(viewContext, grid, buttonName, searchWindow, reportSelectionWidget, false);
    }

    public DetailedDataSetSearchToolbar(final IViewContext<?> viewContext,
            final DataSetSearchHitGrid grid, String buttonName,
            final DetailedSearchWindow searchWindow,
            final ReportingPluginSelectionWidget reportSelectionWidget,
            boolean initializeDescriptionFromSearchWindow)
    {
        super(grid, buttonName, searchWindow, initializeDescriptionFromSearchWindow);
        this.reportSelectionWidget = reportSelectionWidget;
        add(reportSelectionWidget);
        if (viewContext.isSimpleOrEmbeddedMode() == false)
        {
            // processing plugins should be hidden in simple view mode
            IAddProcessingPluginsMenuAction addPluginsAction =
                    new IAddProcessingPluginsMenuAction()
                        {
                            @Override
                            public void addProcessingPlugins(DataSetProcessingMenu menu)
                            {
                                add(new SeparatorToolItem());
                                add(menu);
                            }
                        };
            viewContext.getCommonService().listDataStoreServices(
                    DataStoreServiceKind.PROCESSING,
                    new DataSetGridUtils.LoadProcessingPluginsCallback(viewContext, grid,
                            addPluginsAction));
        }
    }

    @Override
    public void updateSearchResults(DetailedSearchCriteria searchCriteria,
            String searchDescription, List<PropertyType> availablePropertyTypes)
    {
        reportSelectionWidget.selectMetadataPlugin();
        super.updateSearchResults(searchCriteria, searchDescription, availablePropertyTypes);
    }

}
