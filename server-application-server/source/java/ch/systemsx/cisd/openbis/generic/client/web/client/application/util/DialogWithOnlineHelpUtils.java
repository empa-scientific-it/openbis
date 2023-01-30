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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.http.client.URL;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * An utility class used for enhancing {@link Window} with online help functionality.
 * <p>
 * NOTE: Adding abstract superclass to our dialog hierarchy doesn't work well in our case as we already have complex dialog hierarchy subclassing
 * either {@link Window} and {@link Dialog} and we don't want all our dialogs to have help.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public class DialogWithOnlineHelpUtils
{
    /**
     * Adds a help button to the header.
     */
    public static void addHelpButton(final IViewContext<ICommonClientServiceAsync> viewContext,
            final Window window, final HelpPageIdentifier helpPageIdentifier)
    {
        ToolButton toolButton =
                new ToolButton("x-tool-help", new SelectionListener<IconButtonEvent>()
                    {
                        @Override
                        public void componentSelected(IconButtonEvent ce)
                        {
                            onInvokeHelp(viewContext, helpPageIdentifier);
                        }
                    });

        window.getHeader().addTool(toolButton);
    }

    /**
     * Called when the user presses the help button.
     */
    private static void onInvokeHelp(final IViewContext<ICommonClientServiceAsync> viewContext,
            final HelpPageIdentifier helpPageIdentifier)
    {
        URLMethodWithParameters url =
                new URLMethodWithParameters(GenericConstants.HELP_REDIRECT_SERVLET_NAME);
        url.addParameter(GenericConstants.HELP_REDIRECT_PAGE_TITLE_KEY, helpPageIdentifier
                .getHelpPageTitle(viewContext));
        WindowUtils.openWindow(URL.encode(url.toString()));
    }
}
