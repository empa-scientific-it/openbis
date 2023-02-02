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

package ch.systemsx.cisd.common.jython.v27;

import org.python27.core.CompileMode;
import org.python27.core.CompilerFlags;
import org.python27.core.Py;
import org.python27.core.PyBaseCode;
import org.python27.core.PyException;
import org.python27.core.PyFunction;
import org.python27.core.PyInteger;
import org.python27.core.PyObject;

import ch.systemsx.cisd.common.jython.IJythonFunction;
import ch.systemsx.cisd.common.jython.IJythonInterpreter;
import ch.systemsx.cisd.common.jython.IJythonInterpreterFactory;
import ch.systemsx.cisd.common.jython.IJythonObject;

public class Jython27InterpreterFactory implements IJythonInterpreterFactory
{

    @Override
    public IJythonInterpreter createInterpreter()
    {
        return new Jython27Interpreter();
    }

    static class Jython27Function implements IJythonFunction
    {
        PyFunction function;

        Jython27Function(PyFunction function)
        {
            this.function = function;
        }

        @Override
        public IJythonObject invoke(Object... arguments)
        {
            PyObject result = JythonUtils.invokeFunction(function, arguments);
            if (result == null)
            {
                return null;
            }
            return new Jython27Object(result);
        }

        @SuppressWarnings("deprecation")
        @Override
        public int getArgumentCount()
        {
            if (this.function.getFuncCode() instanceof PyBaseCode)
            {
                return ((PyBaseCode) this.function.getFuncCode()).co_argcount;
                // return ((PyBaseCode) this.function.func_code).co_argcount;
            }
            return -1;
        }
    }

    static class Jython27Object implements IJythonObject
    {
        PyObject pyObject;

        public Jython27Object(PyObject object)
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

    static class Jython27Interpreter implements IJythonInterpreter
    {
        private PythonInterpreter27 interpreter;

        public Jython27Interpreter()
        {
            interpreter = PythonInterpreter27.createIsolatedPythonInterpreter();
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
            PyFunction function = JythonUtils.tryJythonFunction(interpreter, name);
            if (function == null)
            {
                return null;
            }
            else
            {
                return new Jython27Function(function);
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