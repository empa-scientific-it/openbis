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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.aggregation;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainPagePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.IViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * An {@link IViewLocatorResolver} that shows the results of an aggregation service as a table model.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AggregationServiceLocatorResolver extends AbstractViewLocatorResolver
{
    final static String ACTION = "AGGREGATION_SERVICE";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public AggregationServiceLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        DispatcherHelper.dispatchNaviEvent(new AbstractTabItemFactory()
            {

                private final static String ID = GenericConstants.ID_PREFIX + ACTION;

                @Override
                public ITabItem create()
                {
                    String tabTitle =
                            locator.getParameters().get(AggregationServicePanel.SERVICE_KEY_PARAM);
                    if (null == tabTitle)
                    {
                        tabTitle = getTabTitle();
                    }

                    return DefaultTabItem.createUnaware(tabTitle, new AggregationServicePanel(
                            viewContext, MainPagePanel.PREFIX, locator), false, viewContext);
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
                    // No need to go through the dictionary for this, since the panel is not a
                    // normal part of the openBIS app.
                    return "aggregation service";
                }

                @Override
                public String tryGetLink()
                {
                    return locator.getHistoryToken();
                }

            });
    }
}