package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators.uglify;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class UglifyJS
{

    public static void main(String[] args)
    {

        new UglifyJS().exec(args);
    }

    public Reader getResourceReader(String url)
    {

        Reader reader = null;

        try
        {
            reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(url));

            /*
             * if (reader == null) { reader = new InputStreamReader(getClass().getResourceAsStream("../"+url)); } if (reader == null) { reader = new
             * InputStreamReader(getClass().getResourceAsStream("/"+url)); }
             */
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return reader;
    }

    public void exec(String[] args)
    {

        ScriptEngine engine = ECMAScriptEngineFactory.getECMAScriptEngine();
        engine.put("uglify_args", args);
        engine.put("uglify_no_output", false);
        run(engine);

        try
        {
            engine.eval("uglify();");
        } catch (ScriptException e)
        {
            e.printStackTrace();
        }
    }

    private void run(ScriptEngine engine)
    {
        try
        {

            Reader parsejsReader = getResourceReader("ch/ethz/sis/openbis/generic/shared/api/v3/dto/generators/uglify/javascript/parse-js.js");
            Reader processjsReader = getResourceReader("ch/ethz/sis/openbis/generic/shared/api/v3/dto/generators/uglify/javascript/process.js");
            Reader sysjsReader = getResourceReader("ch/ethz/sis/openbis/generic/shared/api/v3/dto/generators/uglify/javascript/adapter/sys.js");
            Reader jsonjsReader = getResourceReader("ch/ethz/sis/openbis/generic/shared/api/v3/dto/generators/uglify/javascript/adapter/JSON.js");
            Reader arrayjsReader = getResourceReader("ch/ethz/sis/openbis/generic/shared/api/v3/dto/generators/uglify/javascript/adapter/Array.js");
            Reader uglifyjsReader = getResourceReader("ch/ethz/sis/openbis/generic/shared/api/v3/dto/generators/uglify/javascript/uglifyjs.js");

            engine.eval(arrayjsReader);
            engine.eval(sysjsReader);
            engine.eval(parsejsReader);
            engine.eval(processjsReader);
            engine.eval(jsonjsReader);
            engine.eval(uglifyjsReader);

        } catch (ScriptException e)
        {
            e.printStackTrace();
        }

    }

    public String uglify(String[] args)
    {
        ScriptEngine engine = ECMAScriptEngineFactory.getECMAScriptEngine();
        engine.put("uglify_args", args);
        engine.put("uglify_no_output", true);
        run(engine);

        String result = null;

        try
        {
            result = (String) engine.eval("uglify();");
        } catch (ScriptException e)
        {
            e.printStackTrace();
        }

        return result;
    }

}
