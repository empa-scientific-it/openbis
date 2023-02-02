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

package ch.systemsx.cisd.common.jython.v27;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.python27.core.Py;
import org.python27.core.PyDictionary;
import org.python27.core.PyFunction;
import org.python27.core.PyObject;
import org.python27.core.PySequenceList;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Jython utility methods.
 * 
 * @author Kaloyan Enimanev
 */
class JythonUtils
{

    /**
     * Converts a {@link PyDictionary} to a Java map.
     * 
     * @return a map equivalent to the given Jython dictionary.
     */
    static Map<String, String> convertPyDictToMap(PyDictionary result)
    {
        Map<String, String> javaMap = new HashMap<String, String>();
        for (Object item : result.items())
        {
            PySequenceList tuple = (PySequenceList) item;
            javaMap.put(tuple.get(0).toString(), tuple.get(1).toString());
        }
        return javaMap;
    }

    /**
     * Tries to get a function defined in jython script
     * 
     * @return a Jython function object, or <code>null</code> if function doesn't exist.
     */
    static PyFunction tryJythonFunction(PythonInterpreter27 interpreter, String functionName)
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

    /**
     * Turn all arguments into a python objects, and calls the specified function.
     */
    static PyObject invokeFunction(PyFunction function, Object... args)
    {
        PyObject[] pyArgs = new PyObject[args.length];
        for (int i = 0; i < args.length; i++)
        {
            pyArgs[i] = Py.java2py(args[i]);
        }
        return invokeFunction(function, pyArgs);
    }

    static PyObject invokeFunction(final PyFunction function, final PyObject[] pyArgs)
    {
        return executeWithContextClassLoader(function, new Callable<PyObject>()
            {
                @Override
                public PyObject call() throws Exception
                {
                    return function.__call__(pyArgs);
                }
            });
    }
    
    static final <V> V executeWithContextClassLoader(Object object, Callable<V> action)
    {
        Thread thread = Thread.currentThread();
        ClassLoader originalContextClassLoader = thread.getContextClassLoader();
        try
        {
            thread.setContextClassLoader(object.getClass().getClassLoader());
            try
            {
                return action.call();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        } finally
        {
            thread.setContextClassLoader(originalContextClassLoader);
        }
    }

}
