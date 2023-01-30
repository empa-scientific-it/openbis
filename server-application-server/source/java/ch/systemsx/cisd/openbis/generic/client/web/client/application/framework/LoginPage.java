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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.Date;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action.AboutBoxAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginPanelAutofill;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * The login page.
 * 
 * @author Christian Ribeaud
 */
// WORKAROUND to layouting problems when using DockLayoutPanel
// NOTE: In the end we should rewrite LoginPage to use panels that work properly in Standards Mode
// (see http://code.google.com/intl/de-DE/webtoolkit/doc/latest/DevGuideUiPanels.html#Standards).
// If possible, we should use UiBinder.
final class LoginPage extends com.google.gwt.user.client.ui.VerticalPanel
{
    private static final int CELL_SPACING = 20;

    LoginPage(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        setSpacing(CELL_SPACING);
        setWidth("100%");
        this.setHeight("100%");
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        final Widget loginPanel = createLoginPanel(viewContext);
        final Widget logo = createLogo(viewContext);
        final Widget footerPanel = createFooter(viewContext);
        Element openbisInstanceElement = Document.get().getElementById(Dict.OPENBIS_INSTANCE);
        HTML welcomePanel;
        if (openbisInstanceElement != null)
        {
            welcomePanel = HTML.wrap(openbisInstanceElement);
        } else
        {
            welcomePanel = new HTML(viewContext.getMessage(Dict.OPENBIS_INSTANCE, new Date()));
        }
        welcomePanel.setStyleName("login-welcome-text");
        final CellPanel northPanel = createNorthPanel();

        final HorizontalPanel topPanel = new HorizontalPanel();
        northPanel.add(topPanel);
        northPanel.add(logo);
        northPanel.add(welcomePanel);
        add(getBannersPage());
        add(northPanel);

        viewContext.getCommonService().getDisabledText(
                new AbstractAsyncCallback<String>(viewContext)
                    {
                        @Override
                        protected void process(String noLogintext)
                        {
                            add(noLogintext == null ? loginPanel : new HTML(noLogintext));
                            add(footerPanel);
                        }
                    });

        this.setCellVerticalAlignment(footerPanel, HasVerticalAlignment.ALIGN_BOTTOM);

    }

    private Widget createLoginPanel(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        // Encapsulate loginWidget in a dummy panel.
        // Otherwise it will get the alignment of this panel.
        DockPanel loginPanel = new DockPanel();
        // WORKAROUND The mechanism behind the autofill support does not work in testing
        if (!GWTUtils.isTesting())
        {
            final LoginPanelAutofill loginWidget = LoginPanelAutofill.get(viewContext);
            loginPanel.add(loginWidget, DockPanel.CENTER);
        } else
        {
            final LoginWidget loginWidget = new LoginWidget(viewContext);
            loginPanel.add(loginWidget, DockPanel.CENTER);
        }
        return loginPanel;
    }

    private Widget createLogo(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        Image image = new Image(viewContext.getImageBundle().getOpenBISLogo());
        image.setTitle(viewContext.getMessage(Dict.OPENBIS_LOGO_TITLE));
        Anchor logo =
                new Anchor(image.getElement().getString(), true,
                        AboutBoxAction.OPENBIS_WEB_SITE, "_blank");
        return logo;
    }

    private final static CellPanel createNorthPanel()
    {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(20);
        return horizontalPanel;
    }

    private Widget createFooter(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        Element footerElement = Document.get().getElementById("footer");
        if (footerElement != null)
        {
            return HTML.wrap(footerElement);
        }
        HorizontalPanel footer = new HorizontalPanel();
        return footer;
    }

    private HTML getBannersPage()
    {
        Element headerElement = Document.get().getElementById("header");
        if (headerElement != null)
        {
            return HTML.wrap(headerElement);
        }
        HTML html = new HtmlPage("./custom/loginHeader");
        html.setStyleName("login-header");
        return html;
    }
}
