/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.jython.v25;

import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyBaseCode;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import ch.systemsx.cisd.common.jython.IJythonFunction;
import ch.systemsx.cisd.common.jython.IJythonInterpreter;
import ch.systemsx.cisd.common.jython.IJythonInterpreterFactory;
import ch.systemsx.cisd.common.jython.IJythonObject;
import ch.systemsx.cisd.common.jython.PythonInterpreter;

public class Jython25InterpreterFactory implements IJythonInterpreterFactory
{
    @Override
    public IJythonInterpreter createInterpreter()
    {
        return new Jython25Interpreter();
    }

    static class Jython25Function implements IJythonFunction
    {
        PyFunction function;

        Jython25Function(PyFunction function)
        {
            this.function = function;
        }

        @Override
        public IJythonObject invoke(Object... arguments)
        {
            PyObject[] pyArgs = new PyObject[arguments.length];
            for (int i = 0; i < arguments.length; i++)
            {
                pyArgs[i] = Py.java2py(arguments[i]);
            }
            PyObject result = function.__call__(pyArgs);
            if (result == null)
            {
                return null;
            }
            return new Jython25Object(result);
        }

        @Override
        public int getArgumentCount()
        {
            if (this.function.func_code instanceof PyBaseCode)
            {
                return ((PyBaseCode) this.function.func_code).co_argcount;
            }
            return -1;
        }
    }

    static class Jython25Object implements IJythonObject
    {
        PyObject pyObject;

        public Jython25Object(PyObject object)
        {
            this.pyObject = object;
        }

        @Override
        public String getJythonType()
        {
            return pyObject.getClass().toString();
        }

        @Override
        public boolean isInteger()
        {
            return pyObject instanceof PyInteger;
        }

        @Override
        public int asInteger()
        {
            if (false == isInteger())
            {
                throw new IllegalStateException("Object is not integer");
            }
            return ((PyInteger) pyObject).asInt();
        }

    }

    static class Jython25Interpreter implements IJythonInterpreter
    {
        private PythonInterpreter interpreter;

        public Jython25Interpreter()
        {
            interpreter = PythonInterpreter.createIsolatedPythonInterpreter();
        }

        @Override
        public void exec(String scriptString, String scriptFile)
        {
            interpreter.exec(scriptString, scriptFile);
        }

        @Override
        public void exec(String scriptString)
        {
            interpreter.exec(scriptString);
        }
        
        @Override
        public void set(String variableName, Object object)
        {
            interpreter.set(variableName, object);
        }

        @Override
        public void addToPath(String... pythonPaths)
        {
            interpreter.addToPath(pythonPaths);
        }

        @Override
        public void releaseResources()
        {
            interpreter.releaseResources();
        }

        @Override
        public IJythonFunction tryJythonFunction(String name)
        {
            try
            {
                PyFunction function = interpreter.get(name, PyFunction.class);
                return function == null ? null : new Jython25Function(function);
            } catch (Exception e)
            {
                return null;
            }
        }

        @Override
        public boolean isNextCommand(String lines)
        {
            try
            {
                PyObject object =
                        Py.compile_command_flags(lines, "<input>", CompileMode.single,
                                new CompilerFlags(), true);
                return object != Py.None;
            } catch (PyException e)
            {
                return false;
            }
        }

    }
}
