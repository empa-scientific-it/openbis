/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Runner of SQL Unit tests. Needs an implementation of {@link ISqlScriptExecutor} to do the actual tests. The runner executes all test scripts found
 * in the specified test scripts folder. The folder should have the following structure
 * 
 * <pre>
 *   &lt;&lt;i&gt;test script folder&lt;/i&gt;&gt;
 *      &lt;&lt;i&gt;1. test case&lt;/i&gt;&gt;
 *         buildup.sql
 *         1=&lt;&lt;i&gt;first test&lt;/i&gt;&gt;.sql
 *         2=&lt;&lt;i&gt;second test&lt;/i&gt;&gt;.sql
 *         ...
 *         teardown.sql
 *      &lt;&lt;i&gt;2. test case&lt;/i&gt;&gt;
 *         ...
 *      ...
 * </pre>
 * 
 * Folder starting with '.' or <code>migration</code> are ignored. The test cases are executed in lexicographical order of their name. For each test
 * case <code>buildup.sql</code> will be executed first. The test scripts follow the naming schema
 * 
 * <pre>
 *   &lt;&lt;i&gt;decimal number&lt;/i&gt;&gt;=&lt;&lt;i&gt;test name&lt;/i&gt;&gt;.sql
 * </pre>
 * 
 * They are executed in ascending order of their numbers. Finally <code>teardown.sql</code> is executed. If execution of <code>buildup.sql</code>
 * failed all test scripts and the tear down script are skipped. Note that <code>buildup.sql</code> and <code>teardown.sql</code> are optional.
 * <p>
 * A script fails if its execution throws an exception. Its innermost cause (usually a {@link SQLException}) will be recorded together with the name
 * of the test case and the script. All failed scripts will be recorded.
 * <p>
 * The runner throws an {@link AssertionError} if at least one script failed.
 * 
 * @author Franz-Josef Elmer
 */
public class SqlUnitTestRunner
{
    /** Name of ignored migration folder. */
    public static final String MIGRATION_FOLDER = "migration";

    private static final class TestResult
    {
        private final boolean ok;

        private final Throwable throwable;

        private final File testScript;

        public TestResult(File testScript)
        {
            this(testScript, true, null);
        }

        public TestResult(File testScript, Throwable throwable)
        {
            this(testScript, false, throwable);
        }

        public TestResult(File testScript, boolean ok, Throwable throwable)
        {
            this.testScript = testScript;
            this.ok = ok;
            this.throwable = throwable;
        }

        public final File getTestScript()
        {
            return testScript;
        }

        public final boolean isOK()
        {
            return ok;
        }

        public final Throwable getThrowable()
        {
            return throwable;
        }
    }

    private final ISqlScriptExecutor executor;

    private final PrintWriter writer;

    /**
     * Creates an instance for the specified SQL script executor and writer.
     * 
     * @param executor SQL script executor.
     * @param writer Writer used to monitor running progress by printing test and test case names.
     */
    public SqlUnitTestRunner(ISqlScriptExecutor executor, PrintWriter writer)
    {
        assert executor != null : "Undefined SQL script executor.";
        assert writer != null : "Undefined writer.";

        this.executor = executor;
        this.writer = writer;
    }

    /**
     * Executes all scripts in the specified folder. Does nothing if it does not exists or if it is empty.
     * 
     * @throws AssertionError if at least one script failed.
     */
    public void run(File testScriptsFolder) throws AssertionError
    {
        if (testScriptsFolder == null || testScriptsFolder.exists() == false)
        {
            return; // no tests
        }
        File[] testCases = testScriptsFolder.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    String name = pathname.getName();
                    return pathname.isDirectory() && name.startsWith(".") == false
                            && name.startsWith(MIGRATION_FOLDER) == false;
                }
            });
        Arrays.sort(testCases);
        List<TestResult> results = new ArrayList<TestResult>();
        for (File file : testCases)
        {
            runTestCase(file, results);
        }
        StringBuilder builder = new StringBuilder();
        for (TestResult result : results)
        {
            if (result.isOK() == false)
            {
                final File testScript = result.getTestScript();
                final Throwable throwable = result.getThrowable();
                builder.append("Script '").append(testScript.getName()).append("' of test case '");
                builder.append(testScript.getParentFile().getName()).append("' failed because of ");
                builder.append(throwable).append(OSUtilities.LINE_SEPARATOR);
                final StringWriter stacktraceWriter = new StringWriter();
                final PrintWriter pw = new PrintWriter(stacktraceWriter);
                throwable.printStackTrace(pw);
                builder.append(stacktraceWriter.toString()).append(OSUtilities.LINE_SEPARATOR);
            }
        }
        if (builder.length() > 0)
        {
            throw new AssertionError(builder.toString());
        }

    }

    private void runTestCase(File testCaseFolder, List<TestResult> results)
    {
        writer.println("====== Test case: " + testCaseFolder.getName() + " ======");
        File buildupFile = new File(testCaseFolder, "buildup.sql");
        if (buildupFile.exists())
        {
            TestResult result = runScript(buildupFile);
            results.add(result);
            if (result.isOK() == false)
            {
                writer.println("       script failed: skip test scripts and teardown script.");
                return;
            }
        }
        File[] testScripts = getTestScripts(testCaseFolder);
        for (File testScript : testScripts)
        {
            results.add(runScript(testScript));
        }
        File teardownFile = new File(testCaseFolder, "teardown.sql");
        if (teardownFile.exists())
        {
            results.add(runScript(teardownFile));
        }
    }

    private File[] getTestScripts(File testCaseFolder)
    {
        File[] testScripts = testCaseFolder.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return getNumber(name) >= 0;
                }
            });
        Arrays.sort(testScripts, new Comparator<File>()
            {
                @Override
                public int compare(File f1, File f2)
                {
                    return getNumber(f1.getName()) - getNumber(f2.getName());
                }
            });
        return testScripts;
    }

    private TestResult runScript(File scriptFile)
    {
        writer.println("     execute script " + scriptFile.getName());
        try
        {
            executor.execute(new Script(scriptFile.getName(), FileUtilities
                    .loadToString(scriptFile)), true, null);
            return new TestResult(scriptFile);
        } catch (Throwable t)
        {
            while (t.getCause() != null)
            {
                t = t.getCause();
            }
            return new TestResult(scriptFile, t);
        }
    }

    private int getNumber(String name)
    {
        int index = name.indexOf('=');
        if (index < 0)
        {
            return -1;
        }
        try
        {
            return Integer.parseInt(name.substring(0, index));
        } catch (NumberFormatException ex)
        {
            return -1;
        }
    }
}