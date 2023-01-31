/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.jython.IJythonInterpreterFactory;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluatorFactory;
import ch.systemsx.cisd.common.jython.v27.Jython27EvaluatorFactory;
import ch.systemsx.cisd.common.jython.v27.Jython27InterpreterFactory;

/**
 * Provider of a single {@link Jython27EvaluatorFactory} instance and a single {@link Jython27InterpreterFactory}
 * instance. Both lazily loaded by {@link Jython27ClassLoader}.
 *
 * @author Franz-Josef Elmer
 */
public class Jython27FactoriesProvider
{
    private static final ClassLoader JYTHON_CLASS_LOADER = createJythonClassLoader();
    private static IJythonEvaluatorFactory jythonEvaluatorFactory;
    private static IJythonInterpreterFactory jythonInterpreterFactory;
    
    private static ClassLoader createJythonClassLoader()
    {
        File[] files = new File("lib").listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.getName().startsWith("jython27"))
                {
                    return new Jython27ClassLoader(file);
                }
            }
        }
        return Jython27ClassLoader.class.getClassLoader();
    }
    
    public synchronized static IJythonEvaluatorFactory getEvaluatorFactory()
    {
        if (jythonEvaluatorFactory == null)
        {
            try
            {
                jythonEvaluatorFactory = (IJythonEvaluatorFactory) JYTHON_CLASS_LOADER.loadClass(
                        "ch.systemsx.cisd.common.jython.v27.Jython27EvaluatorFactory").newInstance();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        return jythonEvaluatorFactory;
    }
    
    public synchronized static IJythonInterpreterFactory getInterpreterFactory()
    {
        if (jythonInterpreterFactory == null)
        {
            try
            {
                jythonInterpreterFactory = (IJythonInterpreterFactory) JYTHON_CLASS_LOADER.loadClass(
                            "ch.systemsx.cisd.common.jython.v27.Jython27InterpreterFactory").newInstance();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        return jythonInterpreterFactory;
    }
}
