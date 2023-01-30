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
