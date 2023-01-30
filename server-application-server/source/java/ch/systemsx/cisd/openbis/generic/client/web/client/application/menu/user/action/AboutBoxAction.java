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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action;

import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class AboutBoxAction implements IDelegatedAction
{
    public static final String OPENBIS_WEB_SITE = "https://sis.id.ethz.ch/software/openbis.html";
    
    private final IViewContext<?> viewContext;

    public AboutBoxAction(IViewContext<?> viewContext)
    {
        super();
        this.viewContext = viewContext;
    }

    @Override
    public void execute()
    {
        MessageBox
                .info(viewContext.getMessage(Dict.ABOUT_BOX_DIALOG_TITLE),
                        viewContext.getMessage(Dict.FOOTER, viewContext.getModel()
                                .getApplicationInfo().getVersion())
                                + " <a href=\"" + OPENBIS_WEB_SITE + "\" target=\"_blank\">OpenBIS Home page</a>",
                        null);
    }

}
