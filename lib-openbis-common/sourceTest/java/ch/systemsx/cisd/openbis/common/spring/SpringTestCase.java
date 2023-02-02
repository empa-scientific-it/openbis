/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.spring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.testng.AssertJUnit;

import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * @author Franz-Josef Elmer
 */
public class SpringTestCase extends AssertJUnit
{
    protected static final int DEFAULT_TIMEOUT = 10000;

    protected static final Action WAIT_ACTION = new WaitAction(Integer.MAX_VALUE);

    protected void assertLogContent(BufferedAppender logRecorder, String... expectedLinesOfContent)
    {
        BufferedReader reader = new BufferedReader(new StringReader(logRecorder.getLogContent()));
        StringBuilder builder = new StringBuilder();
        for (String line : expectedLinesOfContent)
        {
            builder.append(line).append('\n');
        }
        String expectedContent = builder.toString();
        builder.setLength(0);
        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("\tat") == false && line.startsWith("\t... ") == false)
                {
                    builder.append(line).append('\n');
                }
            }
        } catch (IOException ex)
        {
            // ignored, because we are reading from a string
        }
        assertEquals(expectedContent, builder.toString());
    }

    /**
     * Returns an action which invokes the wrapped action after specified delay (in milliseconds).
     */
    protected Action delay(final Action action, final long delay)
    {
        return new Action()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    Thread.sleep(delay);
                    return action.invoke(invocation);
                }

                @Override
                public void describeTo(Description description)
                {
                    action.describeTo(description);
                    description.appendText(" delayed by " + delay + " milliseconds");
                }
            };
    }
}
