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

import java.net.InetAddress;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * Sets the "hostname" variable dependeing on the type of installation chosen (server or local installation).
 * 
 * @author Kaloyan Enimanev
 */
public class SetHostnameVariableAction implements PanelAction
{
    private static final String INSTALLATION_TYPE_VARNAME = "INSTALLATION_TYPE";

    private static final String HOSTNAME_VARNAME = "HOSTNAME";

    private static final String SERVER_INSTALLATION_TYPE = "server";

    @Override
    public synchronized void executeAction(AutomatedInstallData data, AbstractUIHandler arg1)
    {
        String installationType = data.getVariable(INSTALLATION_TYPE_VARNAME);
        if (SERVER_INSTALLATION_TYPE.equalsIgnoreCase(installationType))
        {
            data.setVariable(HOSTNAME_VARNAME, tryGetCannonicalHostName());
        } else
        {
            data.setVariable(HOSTNAME_VARNAME, tryGetHostName());
        }
    }

    @Override
    public void initialize(PanelActionConfiguration arg0)
    {
    }

    /**
     * Return the cannonical host name for the localhost machine i.e. the best guess this machine can make about its
     */
    private static String tryGetCannonicalHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Exception ex)
        {
            return tryGetHostName();
        }
    }

    /**
     */
    private static String tryGetHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ex)
        {
            return "localhost";
        }
    }
}
