/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.execute.ExecuteCustomDSSServiceOperationResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.dssapi.v3.plugin.service.ICustomDSSServiceExecutor;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.helper.IDssServiceScriptRunner;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.helper.ScriptRunnerFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

import java.io.Serializable;
import java.util.Properties;

public class JythonBasedCustomDSSServiceExecutor implements ICustomDSSServiceExecutor
{
    private static final String SCRIPT_PATH = "script-path";

    private final String scriptPath;
    public JythonBasedCustomDSSServiceExecutor(Properties properties)
    {
        scriptPath = PropertyUtils.getMandatoryProperty(properties, SCRIPT_PATH);
    }
    @Override
    public Serializable executeService(String sessionToken, ICustomDSSServiceId serviceId,
            CustomDSSServiceExecutionOptions options)
    {
        IDssServiceScriptRunner runner = new ScriptRunnerFactory(scriptPath, getApplicationServerApi(), getDataStoreServerApi())
                .createServiceRunner(sessionToken);
        return new ExecuteCustomDSSServiceOperationResult(runner.process(options));
    }

    private IApplicationServerApi getApplicationServerApi()
    {
        return ServiceProvider.getV3ApplicationService();
    }

    private IDataStoreServerApi getDataStoreServerApi() {
        return ServiceProvider.getV3DataStoreService();
    }
}

