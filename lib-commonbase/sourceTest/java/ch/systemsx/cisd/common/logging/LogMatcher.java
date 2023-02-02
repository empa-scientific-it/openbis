/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class LogMatcher
{
    private final BufferedAppender logRecorder;

    private final String[] expectedLines;

    public LogMatcher(BufferedAppender logRecorder, String... expectedLines)
    {
        this.logRecorder = logRecorder;
        this.expectedLines = expectedLines;
    }

    public void assertMatches()
    {
        try
        {
            BufferedReader reader =
                    new BufferedReader(new StringReader(logRecorder.getLogContent()));

            int lineNumber = 0;
            for (String expectedLine : expectedLines)
            {
                String actualLine;
                actualLine = reader.readLine();
                if (actualLine == null)
                {
                    throw new AssertionError(expectedLines.length + " lines expected instead of "
                            + lineNumber + ":\n" + logRecorder.getLogContent());
                }
                if (false == actualLine.matches(expectedLine))
                {
                    throw new AssertionError(format(lineNumber, expectedLine, actualLine));
                }
                ++lineNumber;
            }
        } catch (IOException ex)
        {
            // Ignore, we are reading from a string
        }
    }

    private String format(int lineNumber, Object expected, Object actual)
    {
        return "Line " + lineNumber + " expected:<" + expected + "> but was:<" + actual + ">\n["
                + logRecorder.getLogContent() + "]";
    }
}