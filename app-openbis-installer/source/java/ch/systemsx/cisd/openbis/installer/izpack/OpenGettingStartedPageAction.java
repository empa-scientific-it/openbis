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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * Opens a "getting started" page in the default operating system browser.
 * <p>
 * If no UI environment is available prints an appropriate message to the console.
 * 
 * @author Kaloyan Enimanev
 */
public class OpenGettingStartedPageAction implements PanelAction
{
    private final static String DOCS_DIR = "doc";

    private final static String INTRO_PAGE = "getting-started-with-openBIS.html";

    @Override
    public synchronized void executeAction(AutomatedInstallData data, AbstractUIHandler arg1)
    {
        if (GlobalInstallationContext.isUpdateInstallation || data.isInstallSuccess() == false
                || Utils.isASInstalled(GlobalInstallationContext.installDir) == false)
        {
            return;
        }

        boolean pageShownInBrowser = false;
        File dataDir = new File(GlobalInstallationContext.getDataDir(data));
        File introPage = new File(new File(dataDir, DOCS_DIR), INTRO_PAGE);

        if (Desktop.isDesktopSupported())
        {
            try
            {
                Desktop.getDesktop().browse(introPage.toURI());
                pageShownInBrowser = true;
            } catch (IOException ex)
            {
                // ignore
            }
        }

        if (false == pageShownInBrowser)
        {
            System.out
                    .println("For a short introduction to the most basic openBIS functionality please read "
                            + introPage.getAbsolutePath());
        }
    }

    @Override
    public void initialize(PanelActionConfiguration arg0)
    {
    }

}
