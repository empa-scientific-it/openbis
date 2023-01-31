/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * The helper class to assert jython hooks. The jython hooks can log into the working directory, and the test classes can compare what the script has
 * logged with expectations
 * 
 * @author jakubs
 */
public class JythonHookTestTool
{
    private final File logFile;

    private String[] originalFileContents = new String[] {};

    private int lineNumber;

    private JythonHookTestTool(File workingDirectory)
    {
        this.logFile = new File(workingDirectory, "jython_hook_test");
        if (logFile.exists())
        {
            readContents();
        }
        lineNumber = 0;
    }

    private void readContents()
    {
        try
        {
            final Scanner s = new Scanner(logFile);

            List<String> list = new LinkedList<String>();

            while (s.hasNextLine())
            {
                final String line = s.nextLine();
                list.add(line);
            }
            originalFileContents = list.toArray(new String[0]);
        } catch (FileNotFoundException fnfe)
        {
            throw new RuntimeException(
                    "Impossible as we check if the file exists before creating scanner", fnfe);
        }
    }

    public void clear()
    {
        logFile.delete();
    }

    /**
     * Assert that the provided messages has been logged and nothign else. Clears the log file afterwards.
     */
    public static void assertMessagesInWorkingDirectory(File workingDirectory, String... messages)
    {
        JythonHookTestTool instance = createFromWorkingDirectory(workingDirectory);
        for (String message : messages)
        {
            instance.assertLogged(message);
        }
        instance.assertNoMoreMessages();
        instance.logFile.delete();
    }

    public static JythonHookTestTool createInTest()
    {
        File file = new File("targets/unit-test-wd").getAbsoluteFile();
        return new JythonHookTestTool(file);
    }

    /**
     * The factory method to create util
     * 
     * @param incoming - the logical or original incoming available from the dropbox
     */
    public static JythonHookTestTool createFromIncoming(File incoming)
    {
        File workingDirectory;
        if (incoming.getParentFile().getParentFile().getName().equals("pre-staging"))
        {
            // prestaging
            workingDirectory =
                    incoming.getParentFile().getParentFile().getParentFile().getParentFile();
        } else
        {
            // incoming
            workingDirectory = incoming.getParentFile().getParentFile();
        }
        return new JythonHookTestTool(workingDirectory);
    }

    /**
     * @param workingDirectory - the working directory of the test class
     */
    public static JythonHookTestTool createFromWorkingDirectory(File workingDirectory)
    {
        return new JythonHookTestTool(workingDirectory.getParentFile());
    }

    /**
     * Use this method in the dropbox to log some information during registration
     * 
     * @param message - message that will be logged with a \n at the end
     */
    public void log(String message) throws IOException
    {
        BufferedWriter output = null;
        try
        {
            output = new BufferedWriter(new FileWriter(logFile, true));
            output.write(message);
            output.newLine();
        } finally
        {
            if (output != null)
            {
                output.close();
            }
        }
    }

    /**
     * Method to use in the test to assert that the given messages were written to the file in the specified order
     */
    public void assertLogged(String message)
    {
        if (lineNumber >= originalFileContents.length)
        {
            throw new AssertionError("Missing message in jython hook test " + message);
        }
        String line = originalFileContents[lineNumber];
        if (false == line.equals(message))
        {
            throw new AssertionError("Mismatch in jython hook test. Expected " + message
                    + " but got " + line);
        }
        lineNumber++;
    }

    public void assertNoMoreMessages()
    {
        if (lineNumber < originalFileContents.length)
        {
            throw new AssertionError("Unexpected message in jython hook test."
                    + originalFileContents[lineNumber]);
        }
    }
}
