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

package ch.systemsx.cisd.openbis.installer.izpack;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * Action which extracts the passwords from DSS service.properties to fill the passwords in page for key store.
 * 
 * @author Franz-Josef Elmer
 */
public class KeystoreAction implements PanelAction
{

    private static final String DEFAULT_PASSWORD = "changeit";

    private static final String DEFAULT_KEY_PASSWORD = "changeit";

    @Override
    public void initialize(PanelActionConfiguration configuration)
    {
    }

    @Override
    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        data.setVariable(GlobalInstallationContext.KEY_STORE_PASSWORD_VARNAME,
                getDssServiceProperty(Utils.DSS_KEYSTORE_PASSWORD_KEY, DEFAULT_PASSWORD));
        data.setVariable(GlobalInstallationContext.KEY_PASSWORD_VARNAME,
                getDssServiceProperty(Utils.DSS_KEYSTORE_KEY_PASSWORD_KEY, DEFAULT_KEY_PASSWORD));
        String keyStoreFilePath =
                data.getVariable(GlobalInstallationContext.KEY_STORE_FILE_VARNAME);
        GlobalInstallationContext.presentKeyStoreFile =
                keyStoreFilePath != null && keyStoreFilePath.length() > 0;
    }

    public String getDssServiceProperty(String propertyKey, String defaultValue)
    {
        String property =
                Utils.tryToGetServicePropertyOfDSS(GlobalInstallationContext.installDir,
                        propertyKey);
        return property == null ? defaultValue : property;
    }

}
