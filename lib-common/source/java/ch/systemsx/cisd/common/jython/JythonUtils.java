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

package ch.systemsx.cisd.common.jython;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * Jython utility methods.
 * 
 * @author Kaloyan Enimanev
 */
public class JythonUtils
{
    /**
     * @return script string from file with given path
     * @throws EvaluatorException if the file doesn't exist or is empty
     */
    public static String extractScriptFromPath(String scriptPath) throws EvaluatorException
    {
        File scriptFile = new File(scriptPath);
        if (false == scriptFile.exists())
        {
            throw new EvaluatorException("Plugin script [" + scriptPath
                    + "] specified in the configuration doesn't exist.");
        } else
        {
            String scriptString = FileUtilities.loadToString(scriptFile);
            if (StringUtils.isBlank(scriptString))
            {
                throw new EvaluatorException("Plugin script [" + scriptPath
                        + "] specified in the configuration is empty.");
            } else
            {
                try
                {
                    return scriptString + "\n";
                } catch (EvaluatorException ex)
                {
                    throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
                }
            }
        }
    }

    /**
     * Returns a python path that contains folders the specified scripts are located in. Returns null if no scripts have been specified.
     */
    public static String[] getScriptDirectoryPythonPath(String... scriptPaths)
    {
        if (scriptPaths == null || scriptPaths.length == 0)
        {
            return null;
        }

        List<String> pythonPath = new ArrayList<String>();

        for (String scriptPath : scriptPaths)
        {
            if (scriptPath != null)
            {
                File scriptFile = new File(scriptPath);
                File scriptDirectory = scriptFile.getParentFile();

                if (scriptDirectory != null)
                {
                    pythonPath.add(scriptDirectory.getAbsolutePath());
                }
            }
        }

        if (pythonPath.isEmpty())
        {
            return null;
        } else
        {
            return pythonPath.toArray(new String[pythonPath.size()]);
        }
    }
}
