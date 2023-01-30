/*
 * Copyright 2011 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * Abstract super class of
 * 
 * @author Franz-Josef Elmer
 */
public class BasicLoginCallback extends AbstractAsyncCallback<SessionContext>
{
    private static final int TIMER_PERIOD = 30 * 60 * 1000; // 30min

    private final String warningMessageKey;

    public static final String LOGIN_FAILED_DIALOG_ID = "login_failed_dialog";

    public BasicLoginCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
            String warningMessageKey)
    {
        super(viewContext);
        this.warningMessageKey = warningMessageKey;
    }

    @Override
    public final void process(final SessionContext sessionContext)
    {
        if (sessionContext == null)
        {
            handleMissingSession();
        } else
        {
            cleanup();
            viewContext.getService().setBaseURL(GWTUtils.getBaseIndexURL(),
                    new AbstractAsyncCallback<SessionContext>(viewContext)
                        {
                            @Override
                            protected void process(SessionContext result)
                            {
                            }
                        });
            viewContext.getPageController().reload(false);
            keepSessionAlive();
        }
    }

    protected void handleMissingSession()
    {

        MessageBox.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING), viewContext
                .getMessage(warningMessageKey), new Listener<MessageBoxEvent>()
            {

                //
                // Listener
                //

                @Override
                public void handleEvent(final MessageBoxEvent be)
                {
                    viewContext.getPageController().reload(false);
                }
            }).getDialog().setId(LOGIN_FAILED_DIALOG_ID);
    }

    /**
     * Does some clean up before finishing login.
     */
    protected void cleanup()
    {
    }

    /** tries to keep session alive until user logs out or closes browser */
    private void keepSessionAlive()
    {
        Timer t = new Timer()
            {
                @Override
                public void run()
                {
                    // callback will cancel keeping session alive if something went wrong
                    // or user logged out
                    AsyncCallback<String> callback = new AsyncCallback<String>()
                        {

                            @Override
                            public void onSuccess(String reasonOrNull)
                            {
                                if (reasonOrNull != null)
                                {
                                    cancel();
                                    GWTUtils.alert("Session Expiration", reasonOrNull);
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught)
                            {
                                cancel();
                                GWTUtils.alert("Server Connection", "Connection to the server is broken.");
                            }
                        };
                    viewContext.getCommonService().keepSessionAlive(callback);
                }
            };
        t.scheduleRepeating(TIMER_PERIOD);
    }

}
