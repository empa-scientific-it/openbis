/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.evaluator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator.ReturnType;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluatorFactory;

/**
 * Tests of the {@link Evaluator}.
 * 
 * @author Bernd Rinn
 */
public abstract class EvaluatorTest extends AssertJUnit
{
    public abstract IJythonEvaluatorFactory getFactory();

    @BeforeTest
    public void init()
    {
        getFactory().initialize();
    }

    @Test
    public void testEvalWithNull()
    {
        final IJythonEvaluator eval = getFactory().create("None");
        assertEquals(null, eval.eval());
        assertEquals(null, eval.evalAsString());
    }

    @Test
    public void testSimpleExpression()
    {
        final IJythonEvaluator eval = getFactory().create("1+1");
        assertEquals(2, eval.evalToInt());
    }

    @Test
    public void testExpressionWithVariables()
    {
        final IJythonEvaluator eval = getFactory().create("a*b");
        eval.set("a", 2);
        eval.set("b", 3.1);
        assertEquals(6.2, eval.evalToDouble(), 1e-15);
    }

    @Test
    public void testExpressionWithVariablesMultipleEvaluations()
    {
        final IJythonEvaluator eval = getFactory().create("a*b");
        eval.set("a", 2);
        eval.set("b", 3.1);
        assertEquals(6.2, eval.evalToDouble(), 1e-15);
        eval.set("a", 0.5);
        eval.set("b", 2.0);
        assertEquals(1.0, eval.evalToDouble(), 1e-15);
    }

    @Test
    public void testVariables()
    {
        final IJythonEvaluator eval = getFactory().create("a");
        eval.set("a", 2);
        assertTrue(eval.has("a"));
        assertFalse(eval.has("b"));
        eval.delete("a");
        assertFalse(eval.has("a"));
    }

    @Test
    public void testBuiltInFunctionEval()
    {
        final IJythonEvaluator eval = getFactory().create("min(1,2)");
        assertEquals(1, eval.evalToInt());
        final IJythonEvaluator eval2 = getFactory().create("max(1,2)");
        assertEquals(2, eval2.evalToInt());
    }

    public static class Functions
    {
        public static double MinDbl(double a, double b)
        {
            return (a < b) ? a : b;
        }

        public static double MinDbl(double[] vals)
        {
            double result = Double.MAX_VALUE;
            for (double v : vals)
            {
                if (v < result)
                {
                    result = v;
                }
            }
            return result;
        }

        public static int MinInt(int a, int b)
        {
            return (a < b) ? a : b;
        }

    }

    @Test
    public void testFunctionEval()
    {
        IJythonEvaluator eval = getFactory().create("MinInt(1,2)", Functions.class, null);
        assertEquals(1, eval.evalToInt());
        eval = getFactory().create("MinDbl([1,2,0.1])", Functions.class, null);
        assertEquals(0.1, eval.evalToDouble(), 1e-15);
        eval = getFactory().create("MinDbl(v)", Functions.class, null);
        eval.set("v", new double[] { 1, 2, -99.9, 3 });
        assertEquals(-99.9, eval.evalToDouble(), 1e-15);
    }

    @Test
    public void testInitialScript()
    {
        final IJythonEvaluator eval = getFactory().create("hello()", null, "def hello():\n   return 'Hello World'");
        assertEquals("Hello World", eval.evalAsString());
    }

    @Test
    public void testGetTypeChanging()
    {
        final IJythonEvaluator eval = getFactory().create("a");
        eval.set("a", 2);
        assertEquals(ReturnType.INTEGER, eval.getType());
        eval.set("a", "2");
        assertEquals(ReturnType.STRING, eval.getType());
    }

    @Test(expectedExceptions = EvaluatorException.class)
    public void testInvalidExpression()
    {
        getFactory().create("pass");
    }

    @Test(expectedExceptions = EvaluatorException.class)
    public void testInvalidExpressionWithLineBreaks()
    {
        getFactory().create("1\nimport os\nos.remove('/etc/passwd')");
    }

    @Test(expectedExceptions = EvaluatorException.class)
    public void testMissingVariable()
    {
        final IJythonEvaluator eval = getFactory().create("a");
        eval.evalAsString();
    }

    @Test
    public void testInvalidReturnValue()
    {
        final IJythonEvaluator eval = getFactory().create("a");
        eval.set("a", true);
        assertEquals(ReturnType.BOOLEAN, eval.getType());
        assertEquals("true", eval.evalAsString());
        try
        {
            eval.evalToDouble();
            fail("Type mismatch not detected.");
        } catch (EvaluatorException ex)
        {
            assertEquals(ex.getMessage(),
                    "Expected a result of type DOUBLE, found BOOLEAN", ex.getMessage());
        }
    }

    @Test
    public void testWriteFails()
    {
        final File tagFile = new File("targets/newfile");
        tagFile.delete();
        final IJythonEvaluator eval = getFactory().create("open('targets/newfile', 'w').write('Should not work')");
        try
        {
            eval.evalAsString();
            fail("expected EvaluatorException");
        } catch (EvaluatorException ex)
        {
            // expected
        }
        assertFalse(tagFile.exists());
    }

    @Test
    public void testWriteSucceeds()
    {
        final File tagFile = new File("targets/newfile");
        tagFile.delete();
        final IJythonEvaluator eval = getFactory().create("open('targets/newfile', 'w').write('Should work')", null, null, null, null,
                false);
        eval.evalAsString();
        assertTrue(tagFile.exists());
    }

    @Test
    void testImportExternalPlugin() throws IOException
    {
        final File dir = new File("targets/testModule");
        dir.mkdirs();
        final File importedScriptFile = new File("targets/test/testmodule.py");
        FileUtils.writeStringToFile(importedScriptFile, "def test_hello_world(): return \"hello world\"");

        String script =
                "import os.path\n" +
                        "import sys\n" +
                        "print __file__\n" +
                        "def package_folder(module):\n" +
                        "    return os.path.dirname(os.path.abspath(module))\n" +
                        "\n" +
                        "\n" +
                        "def inject_local_package(name):\n" +
                        "    \"\"\"patches sys.path to prefer / find packages provided next to this script\"\"\"\n" +
                        "    here = package_folder(__file__)\n" +
                        "    sys.path.insert(0, os.path.join(here, name))\n" +
                        "\n" +
                        "\n" +
                        "def import_and_check_location(package_name):\n" +
                        "    \"\"\"checks if given package was imported from right place\"\"\"\n" +
                        "\n" +
                        "    print \"import %s: \" % package_name,\n" +
                        "    package = __import__(package_name)\n" +
                        "    print \"succeeded\" \n" +
                        "\n" +
                        "    print \"check location: \",\n" +
                        "    found_location = package_folder(package.__file__)\n" +
                        "    here = package_folder(__file__)\n" +
                        "    if found_location.startswith(here):\n" +
                        "        print \"ok\"\n" +
                        "    else:\n" +
                        "        print \"failed: package is imported from %s\" % found_location\n" +
                        "\n" +
                        "\n" +
                        "inject_local_package(\"testmodule\")\n" +
                        "import_and_check_location(\"testmodule\")\n" +
                        "import testmodule\n" +
                        "def test_from_module():\n" +
                        "  return testmodule.test_hello_world()\n" +
                        "";

        File scriptFile = new File("targets/test/file.py");
        String scriptPath = scriptFile.getAbsolutePath();
        FileUtils.writeStringToFile(scriptFile, script);

        final IJythonEvaluator eval =
                getFactory().create("test_from_module()", JythonUtils.getScriptDirectoryPythonPath(scriptPath), scriptPath, null, script, false);

        assertEquals("hello world", eval.evalFunction("test_from_module").toString());
    }

    @Test
    public void testEvalFunctionWithStringArgument()
    {
        IJythonEvaluator eval = getFactory().create("", null, "def hello(name):\n  return 'hello ' + name");
        assertEquals("hello world", eval.evalFunction("hello", "world").toString());
    }

    @Test
    public void testEvalFunctionWithTwoArguments()
    {
        IJythonEvaluator evaluator = getFactory().create("", null, "def get(map, key):\n  return map.get(key)\n");
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("physicists", Arrays.asList("Newton", "Einstein"));
        Object result = evaluator.evalFunction("get", map, "physicists");
        assertEquals("Result " + result.getClass(), true, result instanceof List);
        assertEquals("[Newton, Einstein]", result.toString());
    }

    @Test
    public void testEvalFunctionWhichReturnsAList()
    {
        IJythonEvaluator eval = getFactory().create("", null, "def get():\n  return ['a','b']");
        Object result = eval.evalFunction("get");
        assertEquals("Result " + result.getClass(), true, result instanceof List);
        assertEquals("['a', 'b']", result.toString());
    }

    @Test
    public void testEvalFunctionWithScriptError()
    {
        IJythonEvaluator eval = getFactory().create("", null, "def hello(name):\n  return unknown\n");
        try
        {
            eval.evalFunction("hello", "world");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error occurred in line 2 of the script when evaluating 'hello(world)': "
                    + "NameError: global name 'unknown' is not defined", ex.getMessage());
        }
    }

    @Test
    public void testHasFunction()
    {
        IJythonEvaluator evaluator = getFactory().create("", null, "text = 'hi'\n"
                + "def hello(name):\n  return 'hello ' + name");

        assertEquals(true, evaluator.hasFunction("hello"));
        assertEquals(false, evaluator.hasFunction("text"));
        assertEquals(false, evaluator.hasFunction("blabla"));
    }

    @Test
    public void testEvalUnkownFunction()
    {
        IJythonEvaluator evaluator = getFactory().create("", null, "def hello(name):\n  return 'hello ' + name");
        try
        {
            evaluator.evalFunction("func", "world");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'func(world)': Unknown function: func", ex.getMessage());
        }
    }

    @Test
    public void testEvalFunctionFunctionNameIsVariableName()
    {
        IJythonEvaluator eval = getFactory().create("", null, "text = 'universe'\n"
                + "def hello(name):\n  return 'hello ' + name");
        try
        {
            eval.evalFunction("text", "world");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'text(world)': Not a function: 'text' is of type str.",
                    ex.getMessage());
        }
    }

    @Test
    public void testEvalFunctionWithNotEnoughArguments()
    {
        IJythonEvaluator evaluator = getFactory().create("", null, "def hello(name):\n  return 'hello ' + name");
        try
        {
            evaluator.evalFunction("hello");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'hello()': TypeError: "
                    + "hello() takes exactly 1 argument (0 given)", ex.getMessage());
        }
    }

    @Test
    public void testEvalFunctionWithToManyArguments()
    {
        IJythonEvaluator evaluator = getFactory().create("", null, "def hello(name):\n  return 'hello ' + name");
        try
        {
            evaluator.evalFunction("hello", "world", "universe");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'hello(world, universe)': TypeError: "
                    + "hello() takes exactly 1 argument (2 given)", ex.getMessage());
        }
    }

    @Test
    public void testEvalFunctionWithWrongArgument()
    {
        IJythonEvaluator evaluator = getFactory().create("", null, "def get(map, key):\n  return map.get(key)\n");
        try
        {
            evaluator.evalFunction("get", "world", "universe");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error occurred in line 2 of the script when evaluating "
                    + "'get(world, universe)': AttributeError: "
                    + "'str' object has no attribute 'get'", ex.getMessage());
        }
    }

}
