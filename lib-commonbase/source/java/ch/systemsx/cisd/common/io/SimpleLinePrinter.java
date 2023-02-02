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

package ch.systemsx.cisd.common.io;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Line printer based on a {@link Writer} instance.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleLinePrinter implements ILinePrinter
{
    private final PrintWriter printWriter;

    /**
     * Creates an instance for the specified writer.
     */
    public SimpleLinePrinter(Writer writer)
    {
        assert writer != null : "Unspecified writer.";

        printWriter = new PrintWriter(writer, true);
    }

    /**
     * Prints the text with a new line on the wrapped writer.
     */
    @Override
    public void println(String text)
    {
        printWriter.println(text);
    }

}
