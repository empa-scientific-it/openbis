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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SearchWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.HomeLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action.LoginAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action.LogoutAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * Adds the header section in simple view mode.
 * 
 * @author Kaloyan Enimanev
 */
public class SimpleModeHeader extends LayoutContainer
{
    public static final String ID = GenericConstants.ID_PREFIX + "simple-mode-header";

    private final ToolBar toolBar;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public SimpleModeHeader(final IViewContext<ICommonClientServiceAsync> viewContext,
            ComponentProvider componentProvider)
    {
        this.viewContext = viewContext;
        setId(ID);
        setLayout(new FlowLayout());
        setBorders(true);
        toolBar = new ToolBar();
        add(toolBar);
    }

    final void refresh()
    {
        toolBar.removeAll();
        toolBar.add(createHomeButton());
        toolBar.add(new FillToolItem());
        toolBar.add(new SearchWidget(viewContext));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(viewContext.getModel().isAnonymousLogin() ? createLoginButton()
                : createLogoutButton());
    }

    private Component createHomeButton()
    {
        String homeLabel = viewContext.getMessage(Dict.BUTTON_HOME_LABEL);
        return new Button(homeLabel, new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    URLMethodWithParameters url = new URLMethodWithParameters("");
                    url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER,
                            HomeLocatorResolver.HOME_ACTION);
                    String urlParameters = url.toString().substring(1);
                    History.newItem(urlParameters);
                }

            });
    }

    private Button createLoginButton()
    {
        String logoutLabel = viewContext.getMessage(Dict.BUTTON_LOGIN_LABEL);
        return new Button(logoutLabel, new SelectionListener<ButtonEvent>()
            {
                private LoginAction loginAction = new LoginAction(viewContext);

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    loginAction.execute();
                }

            });
    }

    private Button createLogoutButton()
    {
        String logoutLabel = viewContext.getMessage(Dict.BUTTON_LOGOUT_LABEL);
        return new Button(logoutLabel, new SelectionListener<ButtonEvent>()
            {
                private LogoutAction logoutAction = new LogoutAction(viewContext);

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    logoutAction.execute();
                }

            });
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

}
