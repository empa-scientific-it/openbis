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
