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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action;

import com.google.gwt.user.client.History;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

final class LogoutCallback extends AbstractAsyncCallback<Void>
{
    LogoutCallback(IViewContext<?> viewContext)
    {
        super(viewContext);
        System.out.println("CREATE " + this);
    }

    @Override
    protected void finalize() throws Throwable
    {
        System.out.println("FINALIZE " + this);
    }

    @Override
    public final void process(final Void result)
    {
        History.newItem(""); // clears history token in URL
        GWTUtils.removeConfirmExitMessage();
        viewContext.getPageController().reload(true);
    }
}