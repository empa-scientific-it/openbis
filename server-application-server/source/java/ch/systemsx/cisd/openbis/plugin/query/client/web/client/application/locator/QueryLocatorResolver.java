package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.locator;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.IViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ITabActionMenuItemDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.QueryParameterValue;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.QueryModuleDatabaseMenuItem;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.QueryModuleDatabaseMenuItem.ActionMenuDefinition;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.QueryViewer;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.RunCannedQueryToolbar;

/**
 * {@link IViewLocatorResolver} for Query locators.
 * 
 * @author Piotr Buczek
 */
public class QueryLocatorResolver extends AbstractViewLocatorResolver
{
    private static final String QUERY_ACTION = "QUERY";

    private static final String QUERY_NAME_PARAMETER_KEY = "name";

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    public QueryLocatorResolver(IViewContext<IQueryClientServiceAsync> viewContext)
    {
        super(QUERY_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        // opens a predefined query results viewer with optional:
        // - query selection using query name
        // - filling of parameter values using parameter names
        Map<String, String> originalParameters = locator.getParameters();
        Map<String, QueryParameterValue> parameters = new HashMap<String, QueryParameterValue>();
        for (String key : originalParameters.keySet())
        {
            parameters.put(key, new QueryParameterValue(originalParameters.get(key), false));
        }
        final String queryNameOrNull = locator.getParameters().get(QUERY_NAME_PARAMETER_KEY);

        final DatabaseModificationAwareComponent component =
                QueryViewer.create(viewContext, RunCannedQueryToolbar.createGeneric(viewContext,
                        queryNameOrNull, parameters));

        final ITabActionMenuItemDefinition<IQueryClientServiceAsync> definition =
                ActionMenuDefinition.RUN_CANNED_QUERY;
        final String tabLabelKey = definition.getName() + "_tab_label";
        final AbstractTabItemFactory tabItemFactory = new AbstractTabItemFactory()
            {
                @Override
                public String getId()
                {
                    return QueryModuleDatabaseMenuItem.ID + "_" + tabLabelKey;
                }

                @Override
                public ITabItem create()
                {
                    return DefaultTabItem.create(getTabTitle(), component, viewContext, false);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific(definition.getHelpPageTitle());
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(tabLabelKey);
                }

                @Override
                public String tryGetLink()
                {
                    return locator.getHistoryToken();
                }
            };
        DispatcherHelper.dispatchNaviEvent(tabItemFactory);
    }

    public static String createQueryBrowserLink()
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, QUERY_ACTION);
        return url.toString().substring(1);
    }

}