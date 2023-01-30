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
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user;

import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IMainPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action.AboutBoxAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action.ChangeUserSettingsAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action.LoginAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action.LogoutAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;

/**
 * User top menu.
 * 
 * @author Piotr Buczek
 */
public final class LoggedUserMenu extends TopMenuItem
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public LoggedUserMenu(final IViewContext<ICommonClientServiceAsync> viewContext,
            ComponentProvider componentProvider)
    {
        super(null); // menu title is set later
        setId("user_menu");
        this.viewContext = viewContext;

        Menu submenu = new Menu();
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.USER_MENU_ABOUT_BOX, viewContext,
                new AboutBoxAction(viewContext)));

        if (viewContext.getModel().isAnonymousLogin())
        {
            submenu.add(new ActionMenu(TopMenu.ActionMenuKind.USER_MENU_LOGIN, viewContext,
                    new LoginAction(viewContext)));
        } else
        {
            submenu.add(new ActionMenu(TopMenu.ActionMenuKind.USER_MENU_CHANGE_SETTINGS,
                    viewContext, new ChangeUserSettingsAction(viewContext, this,
                            createOnDisplaySettingsResetAction(componentProvider))));
            submenu.add(new ActionMenu(TopMenu.ActionMenuKind.USER_MENU_LOGOUT, viewContext,
                    new LogoutAction(viewContext)));
        }

        setMenu(submenu);
        refreshTitle();
    }

    public void refreshTitle()
    {
        setText(getUserInfo());
    }

    private final String getUserInfo()
    {
        final SessionContext sessionContext = viewContext.getModel().getSessionContext();
        final User user = sessionContext.getUser();
        final String userName = user.getUserName();
        final String homeGroup = user.getHomeGroupCode();
        final String info;
        if (homeGroup == null)
        {
            info = viewContext.getMessage(Dict.HEADER_USER_WITHOUT_HOMEGROUP, userName);
        } else
        {
            info = viewContext.getMessage(Dict.HEADER_USER_WITH_HOMEGROUP, userName, homeGroup);
        }
        return info;
    }

    public IDelegatedAction createOnDisplaySettingsResetAction(
            final ComponentProvider componentProvider)
    {
        // all tabs will be closed after reset of display settings
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    final IMainPanel tabPanelOrNull = componentProvider.tryGetMainTabPanel();
                    if (tabPanelOrNull == null)
                    {
                        // ignore
                    } else
                    {
                        tabPanelOrNull.reset();
                    }
                }
            };
    }
}
