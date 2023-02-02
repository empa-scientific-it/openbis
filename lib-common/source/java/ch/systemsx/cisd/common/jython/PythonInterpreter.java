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

package ch.systemsx.cisd.common.jython;

import org.apache.log4j.Logger;
import org.python.core.CompileMode;
import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.resource.Resources;

/**
 * @author pkupczyk
 */
public class PythonInterpreter extends org.python.util.PythonInterpreter
{

    private Logger log = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private Resources resources = new Resources();

    protected PythonInterpreter()
    {
    }

    private PythonInterpreter(PyObject dict, PySystemState systemState)
    {
        super(dict, systemState);
    }

    @Override
    public void set(String name, Object value)
    {
        super.set(name, value);
        resources.add(value);
    }

    @Override
    public void set(String name, PyObject value)
    {
        super.set(name, value);
        resources.add(value);
    }

    public void addToPath(String... pathElements)
    {
        if (pathElements != null)
        {
            PyList pyPath = getSystemState().path;

            for (String pathElement : pathElements)
            {
                PyString pyPathElement = new PyString(pathElement);

                if (pyPath.contains(pyPathElement) == false)
                {
                    pyPath.add(pyPathElement);
                }
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("Python path: " + getSystemState().path);
        }
    }

    public void removeFromPath(String... pathElements)
    {
        if (pathElements != null)
        {
            PyList pyPath = getSystemState().path;

            for (String pathElement : pathElements)
            {
                PyString pyPathElement = new PyString(pathElement);

                if (pyPath.contains(pyPathElement))
                {
                    pyPath.remove(new PyString(pathElement));
                }
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("Python path: " + getSystemState().path);
        }
    }

    public void releaseResources()
    {
        resources.release();
    }

    /**
     * Creates a new Jython interpreter with a fully isolated system state (i.e. interpreters in different threads don't influence each other).
     */
    public static PythonInterpreter createIsolatedPythonInterpreter()
    {
        return new PythonInterpreter(null, new PySystemState());
    }

    /**
     * Creates a new Jython interpreter with a non-isolated system state (i.e. interpreters in different threads influence each other and see each
     * others variables).
     * <p>
     * Use this if you don't need thread isolation as the isolated interpreter has some gotchas.
     */
    public static PythonInterpreter createNonIsolatedPythonInterpreter()
    {
        return new PythonInterpreter();
    }

    public void exec(String data, String filename)
    {
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(filename);

        try
        {
            // Add the script directory to the path. Without it importing
            // other files from the same directory does not work.
            addToPath(pythonPath);

            setSystemState();

            Py.exec(Py.compile_flags(data, filename, CompileMode.exec, cflags), getLocals(), null);
            Py.flushLine();
        } finally
        {
            // Remove the script directory from the path. We do not want to pollute
            // the interpreter as it might be reused for executing other scripts.
            removeFromPath(pythonPath);
        }
    }

}
