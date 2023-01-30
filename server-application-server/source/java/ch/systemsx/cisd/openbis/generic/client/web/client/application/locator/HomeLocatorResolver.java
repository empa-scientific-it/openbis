package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainPagePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.WelcomePanelHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * {@link IViewLocatorResolver} that always goes to the home page.
 * 
 * @author Kaloyan Enimanev
 */
public class HomeLocatorResolver extends AbstractViewLocatorResolver
{
    public final static String HOME_ACTION = "HOME";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public HomeLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(HOME_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        DispatcherHelper.dispatchNaviEvent(new AbstractTabItemFactory()
            {

                private final static String ID = GenericConstants.ID_PREFIX + HOME_ACTION;

                @Override
                public ITabItem create()
                {
                    return DefaultTabItem.createUnaware(getTabTitle(), WelcomePanelHelper
                            .createWelcomePanel(viewContext, MainPagePanel.PREFIX), false,
                            viewContext);
                }

                @Override
                public String getId()
                {
                    return ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return null;
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.APPLICATION_NAME);
                }

                @Override
                public String tryGetLink()
                {
                    return locator.getHistoryToken();
                }

            });
    }
}