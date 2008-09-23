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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginPage;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class Client implements EntryPoint
{
    private GenericViewContext viewContext;

    public void onModuleLoad()
    {
        if (viewContext == null)
        {
            viewContext = createViewContext();
        }
        final IGenericClientServiceAsync service = viewContext.getService();
        service.getApplicationInfo(new AbstractAsyncCallback<ApplicationInfo>(viewContext)
            {
                public void onSuccess(ApplicationInfo info)
                {
                    viewContext.getModel().setApplicationInfo(info);
                    service
                            .tryToGetCurrentSessionContext(new AbstractAsyncCallback<SessionContext>(
                                    viewContext)
                                {
                                    public void onSuccess(SessionContext sessionContext)
                                    {
                                        RootPanel rootPanel = RootPanel.get();
                                        rootPanel.clear();
                                        Component widget;
                                        if (sessionContext == null)
                                        {
                                            widget = new LoginPage(viewContext);
                                        } else
                                        {
                                            viewContext.getModel()
                                                    .setSessionContext(sessionContext);
                                            widget = new Application(viewContext);
                                        }
                                        rootPanel.add(widget);
                                    }
                                });

                }
            });
    }

    private GenericViewContext createViewContext()
    {
        IGenericClientServiceAsync service = GWT.create(IGenericClientService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(GenericConstants.SERVER_NAME);
        IGenericImageBundle imageBundle =
                GWT.<IGenericImageBundle> create(IGenericImageBundle.class);
        IMessageProvider messageProvider = new DictonaryBasedMessageProvider("generic");
        IPageController pageController = new IPageController()
            {
                public void reload()
                {
                    onModuleLoad();
                }
            };
        return new GenericViewContext(service, messageProvider, imageBundle, pageController);
    }

}
