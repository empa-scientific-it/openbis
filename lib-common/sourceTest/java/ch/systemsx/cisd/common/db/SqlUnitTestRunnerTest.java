/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.internal.Cardinality;
import org.jmock.internal.InvocationExpectationBuilder;
import org.jmock.lib.action.ThrowAction;
import org.jmock.lib.action.VoidAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class SqlUnitTestRunnerTest
{
    private static final File TEST_SCRIPTS_FOLDER = new File("temporary_test_scripts_folder");

    private Mockery context;

    private ISqlScriptExecutor executor;

    private StringWriter monitor;

    private SqlUnitTestRunner testRunner;

    @BeforeMethod
    public void setup()
    {
        TEST_SCRIPTS_FOLDER.mkdir();
        context = new Mockery();
        executor = context.mock(ISqlScriptExecutor.class);
        monitor = new StringWriter();
        testRunner = new SqlUnitTestRunner(executor, new PrintWriter(monitor));
    }

    @AfterMethod
    public void teardown()
    {
        assert FileUtilities.deleteRecursively(TEST_SCRIPTS_FOLDER);
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testNullFolder()
    {
        testRunner.run(null);
        assertEquals("", monitor.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testNotExistingFolder()
    {
        testRunner.run(new File("blabla"));
        assertEquals("", monitor.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testEmptyFolder()
    {
        testRunner.run(TEST_SCRIPTS_FOLDER);
        assertEquals("", monitor.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testNonEmptyFolderButNoTestCases() throws IOException
    {
        assert new File(TEST_SCRIPTS_FOLDER, "some file").createNewFile();
        assert new File(TEST_SCRIPTS_FOLDER, ".folder").mkdir();

        testRunner.run(TEST_SCRIPTS_FOLDER);
        assertEquals("", monitor.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testEmptyTestCase()
    {
        assert new File(TEST_SCRIPTS_FOLDER, "my test case").mkdir();

        testRunner.run(TEST_SCRIPTS_FOLDER);
        assertEquals("====== Test case: my test case ======" + OSUtilities.LINE_SEPARATOR, monitor
                .toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testNonEmptyTestCaseButNoScripts() throws IOException
    {
        File testCaseFolder = new File(TEST_SCRIPTS_FOLDER, "my test case");
        assert testCaseFolder.mkdir();
        assert new File(testCaseFolder, "blabla.sql").createNewFile();
        assert new File(testCaseFolder, "folder").mkdir();

        testRunner.run(TEST_SCRIPTS_FOLDER);
        assertEquals("====== Test case: my test case ======" + OSUtilities.LINE_SEPARATOR, monitor
                .toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testTestCaseWithNoTestsButBuildupScript() throws IOException
    {
        File testCaseFolder = new File(TEST_SCRIPTS_FOLDER, "my test case");
        assert testCaseFolder.mkdir();
        createScriptPrepareExecutor(new File(testCaseFolder, "buildup.sql"), "-- build up\n", null);

        testRunner.run(TEST_SCRIPTS_FOLDER);
        assertEquals("====== Test case: my test case ======" + OSUtilities.LINE_SEPARATOR
                + "     execute script buildup.sql" + OSUtilities.LINE_SEPARATOR, monitor
                .toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testTestCaseWithFailingBuildupScript() throws IOException
    {
        File testCaseFolder = new File(TEST_SCRIPTS_FOLDER, "my test case");
        assert testCaseFolder.mkdir();
        RuntimeException runtimeException = new RuntimeException("42");
        createScriptPrepareExecutor(new File(testCaseFolder, "buildup.sql"), "-- build up\n",
                runtimeException);

        boolean exceptionThrown = false;
        try
        {
            testRunner.run(TEST_SCRIPTS_FOLDER);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
            final String message = StringUtils.split(e.getMessage(), "\n")[0];
            assertEquals("Script 'buildup.sql' of test case 'my test case' failed because of "
                    + runtimeException, message.trim());
        }
        assertTrue("AssertionError expected", exceptionThrown);
        assertEquals("====== Test case: my test case ======" + OSUtilities.LINE_SEPARATOR
                + "     execute script buildup.sql" + OSUtilities.LINE_SEPARATOR
                + "       script failed: skip test scripts and teardown script."
                + OSUtilities.LINE_SEPARATOR, monitor.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testOrderOfExecutingTestScripts() throws IOException
    {
        File testCaseFolder = new File(TEST_SCRIPTS_FOLDER, "my test case");
        assert testCaseFolder.mkdir();
        RuntimeException runtimeException = new RuntimeException("42");
        createScriptPrepareExecutor(new File(testCaseFolder, "9=b.sql"), "Select 9\n",
                runtimeException);
        FileUtils.writeStringToFile(new File(testCaseFolder, "abc=abc.sql"), "Select abc\n");
        createScriptPrepareExecutor(new File(testCaseFolder, "10=c.sql"), "Select 10\n", null);
        createScriptPrepareExecutor(new File(testCaseFolder, "1=a.sql"), "Select 1\n", null);
        boolean exceptionThrown = false;
        try
        {
            testRunner.run(TEST_SCRIPTS_FOLDER);

        } catch (AssertionError e)
        {
            exceptionThrown = true;
            // Strip away stack trace
            final String message = StringUtils.split(e.getMessage(), "\n")[0];
            assertEquals("Script '9=b.sql' of test case 'my test case' failed because of "
                    + runtimeException, message.trim());
        }
        assertTrue("AssertionError expected", exceptionThrown);
        assertEquals("====== Test case: my test case ======" + OSUtilities.LINE_SEPARATOR
                + "     execute script 1=a.sql" + OSUtilities.LINE_SEPARATOR
                + "     execute script 9=b.sql" + OSUtilities.LINE_SEPARATOR
                + "     execute script 10=c.sql" + OSUtilities.LINE_SEPARATOR, monitor.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testOrderOfExecutingTestCases() throws IOException
    {
        assert new File(TEST_SCRIPTS_FOLDER, "TC002").mkdir();
        File testCaseFolder1 = new File(TEST_SCRIPTS_FOLDER, "TC001");
        assert testCaseFolder1.mkdir();
        createScriptPrepareExecutor(new File(testCaseFolder1, "buildup.sql"), "create table\n",
                null);
        createScriptPrepareExecutor(new File(testCaseFolder1, "1=a.sql"), "Select 1\n", null);
        createScriptPrepareExecutor(new File(testCaseFolder1, "2=b.sql"), "Select 2\n", null);
        createScriptPrepareExecutor(new File(testCaseFolder1, "teardown.sql"), "drop table\n", null);

        testRunner.run(TEST_SCRIPTS_FOLDER);
        assertEquals("====== Test case: TC001 ======" + OSUtilities.LINE_SEPARATOR
                + "     execute script buildup.sql" + OSUtilities.LINE_SEPARATOR
                + "     execute script 1=a.sql" + OSUtilities.LINE_SEPARATOR
                + "     execute script 2=b.sql" + OSUtilities.LINE_SEPARATOR
                + "     execute script teardown.sql" + OSUtilities.LINE_SEPARATOR
                + "====== Test case: TC002 ======" + OSUtilities.LINE_SEPARATOR, monitor.toString());

        context.assertIsSatisfied();
    }

    private void createScriptPrepareExecutor(File scriptFile, String script, Throwable throwable)
            throws IOException
    {
        FileUtils.writeStringToFile(scriptFile, script);
        InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(new Cardinality(1, 1));
        builder.of(executor).execute(new Script(scriptFile.getName(), script), true, null);
        Action action = throwable == null ? new VoidAction() : new ThrowAction(throwable);
        context.addExpectation(builder.toExpectation(action));
    }

}
