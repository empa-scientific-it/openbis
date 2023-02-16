/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.fs.plugins;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;

/**
 * @author Jakub Straszewski
 */
public class JythonResolver implements IResolverPlugin
{

    private static Map<String, IJythonEvaluator> interpreters = new HashMap<>();

    private static final String RESOLVE_FUNCTION_NAME = "resolve";

    private static final String SCRIPT_FILENAME_PROPERTY = "script-file";
    private static final String DEV_MODE_PROPERTY = "ftp.resolver-dev-mode";

    private IJythonEvaluator interpreter;

    @Override
    public IFileSystemViewResponse resolve(String[] pathItems, IResolverContext resolverContext)
    {
        Object result = interpreter.evalFunction(RESOLVE_FUNCTION_NAME, pathItems, resolverContext);
        return (IFileSystemViewResponse) result;
    }

    @Override
    public void initialize(String pluginName, String code)
    {
        IJythonEvaluator interpreter = interpreters.get(pluginName);
        if (interpreter == null)
        {
            String scriptPath = PropertyUtils.getMandatoryProperty(DssPropertyParametersUtil.loadServiceProperties(),
                    pluginName + "." + SCRIPT_FILENAME_PROPERTY);
            String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
            String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);
            interpreter = Evaluator.getFactory().create("", pythonPath, scriptPath, null, scriptString, false);
            if (PropertyUtils.getBoolean(DssPropertyParametersUtil.loadServiceProperties(), DEV_MODE_PROPERTY, false) == false)
            {
                interpreters.put(pluginName, interpreter);
            }
        }
        this.interpreter = interpreter;
    }

}
