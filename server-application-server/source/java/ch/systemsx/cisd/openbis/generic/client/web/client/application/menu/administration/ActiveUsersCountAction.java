/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.administration;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * @author Pawel Glyzewski
 */
public class ActiveUsersCountAction implements IDelegatedAction
{
    private static class MessageBoxCallback extends AbstractAsyncCallback<Integer>
    {
        private MessageBoxCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Integer result)
        {
            MessageBox mb = new MessageBox();
            mb.setTitle(viewContext.getMessage(Dict.MESSAGEBOX_WARNING));
            mb.setMessage(viewContext
                    .getMessage(Dict.ACTIVE_USERS_DIALOG, result,
                            "<a href=\"mailto:cisd.helpdesk@bsse.ethz.ch\" target=\"_blank\">CISD Helpdesk</a>"));
            mb.setIcon(MessageBox.WARNING);
            mb.setButtons(MessageBox.OKCANCEL);
            mb.addCallback(new Listener<MessageBoxEvent>()
                {
                    @Override
                    public void handleEvent(MessageBoxEvent be)
                    {
                        String value = be.getButtonClicked().getItemId();
                        if (MessageBox.OK.equals(value))
                        {
                            viewContext.getCommonService().sendCountActiveUsersEmail(
                                    new ConfirmationDialogCallback(viewContext));
                        }
                    }
                });
            mb.show();
        }
    }

    private static class ConfirmationDialogCallback extends AbstractAsyncCallback<Void>
    {
        private ConfirmationDialogCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Void r)
        {
            MessageBox.info(viewContext.getMessage(Dict.MESSAGEBOX_INFO),
                    viewContext.getMessage(Dict.ACTIVE_USERS_EMAIL_SENT_CONFIRMATION), null);
        }
    }

    private final IViewContext<?> viewContext;

    public ActiveUsersCountAction(IViewContext<?> viewContext)
    {
        super();
        this.viewContext = viewContext;
    }

    @Override
    public void execute()
    {
        viewContext.getCommonService().countActiveUsers(new MessageBoxCallback(viewContext));
    }
}
