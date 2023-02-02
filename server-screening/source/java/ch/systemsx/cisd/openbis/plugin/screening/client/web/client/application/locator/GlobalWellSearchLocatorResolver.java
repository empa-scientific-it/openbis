package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MaterialCodeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellSearchComponent;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;

/**
 * Locator resolver for plate metadata browser.
 * 
 * @author Tomasz Pylak
 */
public class GlobalWellSearchLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public GlobalWellSearchLocatorResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.GLOBAL_WELL_SEARCH_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        DispatcherHelper.dispatchNaviEvent(new AbstractTabItemFactory()
            {

                @Override
                public String getId()
                {
                    return ScreeningModule.ID + ScreeningLinkExtractor.GLOBAL_WELL_SEARCH_ACTION;
                }

                @Override
                public ITabItem create()
                {
                    String materialsList =
                            getOptionalParameter(locator,
                                    ScreeningLinkExtractor.WELL_SEARCH_MATERIAL_ITEMS_PARAMETER_KEY);
                    if (materialsList != null)
                    {
                        materialsList = MaterialCodeUtils.decode(materialsList);
                    }
                    Boolean exactMatchOnly =
                            getOptionalBooleanParameter(locator,
                                    ScreeningLinkExtractor.WELL_SEARCH_IS_EXACT_PARAMETER_KEY);
                    boolean showCombinedResults =
                            getOptionalBooleanParameter(
                                    locator,
                                    ScreeningLinkExtractor.WELL_SEARCH_SHOW_COMBINED_RESULTS_PARAMETER_KEY,
                                    ScreeningLinkExtractor.WELL_SEARCH_SHOW_COMBINED_RESULTS_DEFAULT);
                    TabContent wellSearchTab =
                            new WellSearchComponent(viewContext, materialsList, exactMatchOnly,
                                    showCombinedResults);
                    return DefaultTabItem.createUnaware(wellSearchTab, false, viewContext);
                }

                @Override
                public String tryGetLink()
                {
                    return locator.getHistoryToken();
                }

                @Override
                public String getTabTitle()
                {
                    return WellSearchComponent.getTabTitle(viewContext);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return null;
                }

            });
    }

    public static String createQueryBrowserLink()
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER,
                ScreeningLinkExtractor.GLOBAL_WELL_SEARCH_ACTION);
        return url.toStringWithoutDelimiterPrefix();
    }
}