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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.LoggedUserMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.user.ChangeUserSettingsDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * An {@link IDelegatedAction} that opens a {@link ChangeUserSettingsDialog}.
 * 
 * @author Piotr Buczek
 */
public class ChangeUserSettingsAction implements IDelegatedAction
{
    private final IViewContext<?> viewContext;

    private final LoggedUserMenu menu;

    private final IDelegatedAction onDisplaySettingsResetAction;

    public ChangeUserSettingsAction(final IViewContext<?> viewContext, LoggedUserMenu menu,
            IDelegatedAction onDisplaySettingsResetAction)
    {
        this.viewContext = viewContext;
        this.menu = menu;
        this.onDisplaySettingsResetAction = onDisplaySettingsResetAction;
    }

    @Override
    public void execute()
    {
        IDelegatedAction saveCallback = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    menu.refreshTitle();
                }
            };
        IDelegatedAction resetCallback = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    onDisplaySettingsResetAction.execute();
                }
            };

        ChangeUserSettingsDialog dialog =
                new ChangeUserSettingsDialog(viewContext, saveCallback, resetCallback);
        dialog.show();
    }
}
