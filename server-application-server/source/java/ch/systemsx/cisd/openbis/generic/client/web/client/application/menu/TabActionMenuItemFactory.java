/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * @author Franz-Josef Elmer
 */
public class TabActionMenuItemFactory
{
    public static <S extends IClientServiceAsync> ActionMenu createActionMenu(
            final IViewContext<S> viewContext, final String widgetIDPrefix,
            final ITabActionMenuItemDefinition<S> definition)
    {
        IActionMenuItem menuItem = new IActionMenuItem()
            {
                @Override
                public String getMenuId()
                {
                    return widgetIDPrefix + "_" + definition.getName();
                }

                @Override
                public String getMenuText(IMessageProvider messageProvider)
                {
                    return messageProvider.getMessage(definition.getName() + "_menu_item");
                }
            };
        final String tabLabelKey = definition.getName() + "_tab_label";
        return new ActionMenu(menuItem, viewContext, new AbstractTabItemFactory()
            {
                @Override
                public String getId()
                {
                    return widgetIDPrefix + "_" + tabLabelKey;
                }

                @Override
                public ITabItem create()
                {
                    return DefaultTabItem.create(getTabTitle(),
                            definition.createComponent(viewContext), viewContext, false);
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
                    return definition.tryGetLink();
                }
            });

    }
}
