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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.common.utilities.JythonUtils;
import ch.systemsx.cisd.common.utilities.PythonUtils;

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
     */
    public static ValidationScriptRunner createValidatorFromScriptPaths(String[] scriptPaths)
    {
        String scriptStringOrNull = ValidationScriptReader.tryReadValidationScript(scriptPaths);
        if (StringUtils.isBlank(scriptStringOrNull))
        {
            return new NullValidationScriptRunner();
        }

        return new ValidationScriptRunner(scriptStringOrNull);
    }

    /**
     * Factory method for creating a ValidationScriptRunner given the script as a string.
     */
    public static ValidationScriptRunner createValidatorFromScriptString(String scriptString)
    {
        if (scriptString == null)
        {
            return new NullValidationScriptRunner();
        }
        return new ValidationScriptRunner(scriptString);
    }

    private final PythonInterpreter interpreter;

    private final String scriptString;

    private ValidationScriptRunner(String scriptString)
    {
        this.interpreter = PythonUtils.createIsolatedPythonInterpreter();
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
    public Map<String, String> extractMetadata(File dataSetFile)
    {
        Map<String, String> metadata = new HashMap<String, String>();
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
                javaResult = JythonUtils.convertPyDictToMap((PyDictionary) result);
            } else
            {
                javaResult = (Map<String, String>) result;
            }
            metadata.putAll(javaResult);
        }

        return metadata;
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
        public Map<String, String> extractMetadata(File dataSetFile)
        {
            return Collections.emptyMap();
        }

    }

}

