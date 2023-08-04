/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PySequenceList;

import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.PythonInterpreter;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ValidationScriptRunner
{
    private final static String FILE_VALIDATION_FUNCTION_NAME = "validate_data_set_file";

    private final static String EXTRACT_METADATA_FUNCTION_NAME = "extract_metadata";

    // Factory methods

    /**
     * Factory method for creating a ValidationScriptRunner given a path to a script.
     * <p>
     * Use this on the server-side.
     */
    public static ValidationScriptRunner createValidatorFromScriptPaths(String[] scriptPaths)
    {
        return createValidatorFromScriptPaths(scriptPaths, true);
    }

    /**
     * Factory method for creating a ValidationScriptRunner given a path to a script.
     * 
     * @param isolateJythonSystemState If <code>true</code>, create a jython interpreter with an isolated system state. Use this on the server side
     *            where multiple Jython interpreters may run in different threads. Note, however, that the re module has some restrictions in this
     *            mode.
     */
    public static ValidationScriptRunner createValidatorFromScriptPaths(String[] scriptPaths,
            boolean isolateJythonSystemState)
    {
        String scriptStringOrNull = ValidationScriptReader.tryReadValidationScript(scriptPaths);

        if (StringUtils.isBlank(scriptStringOrNull))
        {
            return new NullValidationScriptRunner();
        }

        String[] jythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPaths);

        return new ValidationScriptRunner(scriptStringOrNull, jythonPath, isolateJythonSystemState);
    }

    /**
     * Factory method for creating a ValidationScriptRunner given the script as a string.
     * <p>
     * Use this on the server-side.
     */
    public static ValidationScriptRunner createValidatorFromScriptString(String scriptString)
    {
        return createValidatorFromScriptString(scriptString, true);
    }

    /**
     * Factory method for creating a ValidationScriptRunner given the script as a string.
     * 
     * @param isolateJythonSystemState If <code>true</code>, create a jython interpreter with an isolated system state. Use this on the server side
     *            where multiple Jython interpreters may run in different threads. Note, however, that the re module has some restrictions in this
     *            mode.
     */
    public static ValidationScriptRunner createValidatorFromScriptString(String scriptString,
            boolean isolateJythonSystemState)
    {
        if (scriptString == null)
        {
            return new NullValidationScriptRunner();
        }
        return new ValidationScriptRunner(scriptString, null, isolateJythonSystemState);
    }

    private final PythonInterpreter interpreter;

    private final String scriptString;

    private ValidationScriptRunner(String scriptString, String[] pythonPath, boolean isolateJythonSystemState)
    {
        this.interpreter =
                isolateJythonSystemState ? PythonInterpreter.createIsolatedPythonInterpreter()
                        : PythonInterpreter.createNonIsolatedPythonInterpreter();
        this.interpreter.addToPath(pythonPath);
        // Load the script
        this.scriptString = scriptString;

        interpreter.exec(this.scriptString);
    }

    /**
     * Protected constructor for the null script runner.
     * 
     * @param scriptIsNull
     */
    protected ValidationScriptRunner(boolean scriptIsNull)
    {
        this.interpreter = null;
        // Load the script
        this.scriptString = null;
    }

    @SuppressWarnings("unchecked")
    public List<ValidationError> validate(File dataSetFile)
    {
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        PyFunction function = tryJythonFunction(FILE_VALIDATION_FUNCTION_NAME);
        PyObject result = function.__call__(Py.java2py(dataSetFile));
        if (null != result)
        {
            errors.addAll((Collection<? extends ValidationError>) result);
        }

        return errors;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Serializable> extractMetadata(File dataSetFile)
    {
        Map<String, Serializable> metadata = new HashMap<String, Serializable>();
        PyFunction function = tryJythonFunction(EXTRACT_METADATA_FUNCTION_NAME);
        if (function == null)
        {
            return Collections.emptyMap();
        }

        PyObject result = function.__call__(Py.java2py(dataSetFile));
        if (null != result)
        {
            Map<String, String> javaResult = null;
            if (result instanceof PyDictionary)
            {
                javaResult = convertPyDictToMap((PyDictionary) result);
            } else
            {
                javaResult = (Map<String, String>) result;
            }
            metadata.putAll(javaResult);
        }

        return metadata;
    }
    
    private static Map<String, String> convertPyDictToMap(PyDictionary result)
    {
        Map<String, String> javaMap = new HashMap<String, String>();
        for (Object item : result.items())
        {
            PySequenceList tuple = (PySequenceList) item;
            javaMap.put(tuple.get(0).toString(), tuple.get(1).toString());
        }
        return javaMap;
    }

    public String getScriptString()
    {
        return scriptString;
    }

    private PyFunction tryJythonFunction(String functionName)
    {
        try
        {
            PyFunction function = interpreter.get(functionName, PyFunction.class);
            return function;
        } catch (Exception e)
        {
            return null;
        }
    }

    public static class NullValidationScriptRunner extends ValidationScriptRunner
    {
        public NullValidationScriptRunner()
        {
            super(true);
        }

        @Override
        public List<ValidationError> validate(File dataSetFile)
        {
            return Collections.emptyList();
        }

        @Override
        public Map<String, Serializable> extractMetadata(File dataSetFile)
        {
            return Collections.emptyMap();
        }

    }

}
