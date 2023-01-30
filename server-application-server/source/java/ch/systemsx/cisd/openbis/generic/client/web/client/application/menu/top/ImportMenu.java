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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.top;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * The Import menu of the top menu bar.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ImportMenu extends TopMenuItem
{
    public ImportMenu(final IViewContext<ICommonClientServiceAsync> viewContext,
            ComponentProvider componentProvider)
    {
        super(viewContext.getMessage(Dict.MENU_IMPORT));
        setId("import_menu");

        IMessageProvider messageProvider = viewContext;
        ApplicationInfo applicationInfo = viewContext.getModel().getApplicationInfo();

        Menu submenu = new Menu();
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.EXPERIMENT_MENU_IMPORT, messageProvider,
                componentProvider.getExperimentBatchRegistration()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.EXPERIMENT_MENU_MASS_UPDATE,
                messageProvider, componentProvider.getExperimentBatchUpdate()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_IMPORT, messageProvider,
                componentProvider.getSampleBatchRegistration()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_MASS_UPDATE, messageProvider,
                componentProvider.getSampleBatchUpdate()));

        SelectionListener<? extends MenuEvent> listener = new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent ce)
                {
                    final URLMethodWithParameters urlParams =
                            new URLMethodWithParameters("/openbis/openbis/"
                                    + BasicConstant.DATA_SET_UPLOAD_CLIENT_PATH);
                    String sessionToken = viewContext.getModel().getSessionContext().getSessionID();
                    urlParams.addParameter("session", sessionToken);
                    urlParams.addParameter(BasicConstant.SERVER_URL_PARAMETER,
                            GWT.getHostPageBaseURL());
                    Window.open(urlParams.toString(), "_blank",
                            "resizable=yes,scrollbars=yes,dependent=yes");
                }
            };

        boolean cifexConfigured =
                StringUtils.isNotBlank(applicationInfo.getCifexRecipient())
                        && StringUtils.isNotBlank(applicationInfo.getCifexURL());
        if (cifexConfigured)
        {
            submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_UPLOAD,
                    messageProvider, componentProvider.getDataSetUploadTab(null)));
        }

        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_MASS_UPDATE,
                messageProvider, componentProvider.getDataSetBatchUpdate()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.MATERIAL_MENU_IMPORT, messageProvider,
                componentProvider.getMaterialBatchRegistration()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.MATERIAL_MENU_MASS_UPDATE,
                messageProvider, componentProvider.getMaterialBatchUpdate()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.GENERAL_IMPORT_MENU, messageProvider,
                componentProvider.createGeneralImport()));

        if (applicationInfo.getCustomImports() != null
                && applicationInfo.getCustomImports().size() > 0)
        {
            submenu.add(new ActionMenu(TopMenu.ActionMenuKind.CUSTOM_IMPORT_MENU, messageProvider,
                    componentProvider.getCustomImport()));
        }

        setMenu(submenu);
    }

}
