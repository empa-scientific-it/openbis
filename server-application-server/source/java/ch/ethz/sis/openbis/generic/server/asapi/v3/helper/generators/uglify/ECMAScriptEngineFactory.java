/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators.uglify;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ECMAScriptEngineFactory
{

    public static ScriptEngine getECMAScriptEngine()
    {
        ScriptEngine engine = null;
        ScriptEngineManager manager = new ScriptEngineManager();

        engine = manager.getEngineByExtension("js");

        if (engine == null)
        {
            throw new RuntimeException("the java version do not install ECMAScipt engine, must be above java 1.6");
        }

        return engine;
    }

}
