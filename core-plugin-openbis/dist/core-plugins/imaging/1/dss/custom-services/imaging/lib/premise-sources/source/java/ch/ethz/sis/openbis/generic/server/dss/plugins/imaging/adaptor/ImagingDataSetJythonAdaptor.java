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

package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.ImagingServiceContext;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.helper.IDssServiceScriptRunner;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.helper.ScriptRunnerFactory;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

public class ImagingDataSetJythonAdaptor implements IImagingDataSetAdaptor
{
    private final String scriptPath;


    public ImagingDataSetJythonAdaptor(Properties properties) {
        this.scriptPath = properties.getProperty("script-path", "");
        if(scriptPath.isBlank()) {
            throw new UserFailureException("There is no script defined for this adaptor!");
        }
    }
    @Override
    public Serializable process(ImagingServiceContext context, File rootFile, Map<String, Serializable> previewConfig, Map<String, String> metaData)
    {
        CustomDSSServiceExecutionOptions options = new CustomDSSServiceExecutionOptions();
        options.withParameter("sessionToken", context.getSessionToken());
        options.withParameter("asApi", context.getAsApi());
        options.withParameter("dssApi", context.getDssApi());
        options.withParameter("file", rootFile);
        options.withParameter("config", previewConfig);
        options.withParameter("metaData", metaData);
            IDssServiceScriptRunner
                    runner = new ScriptRunnerFactory(scriptPath, context.getAsApi(),
                    context.getDssApi())
                    .createServiceRunner(context.getSessionToken());
        return runner.process(options);
    }
}
