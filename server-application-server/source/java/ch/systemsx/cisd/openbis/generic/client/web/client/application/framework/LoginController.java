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

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * The {@link Controller} extension for logging events.
 * 
 * @author Christian Ribeaud
 */
public final class LoginController extends Controller
{
    private final LoginView loginView;

    public LoginController(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        registerEventTypes(AppEvents.LOGIN);
        loginView = new LoginView(this, viewContext);
    }

    //
    // Controller
    //

    @Override
    public final void handleEvent(final AppEvent event)
    {
        if (event.getType() == AppEvents.LOGIN)
        {
            forwardToView(loginView, event);
        } else
        {
            throw new IllegalArgumentException("Unknow event '" + event + "'.");
        }
    }
}
