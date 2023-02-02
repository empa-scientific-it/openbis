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

package ch.systemsx.cisd.common.logging;

import java.io.PrintStream;

/**
 * A {@link ISimpleLogger} that logs to {@link System#out} (for debugging purposes).
 * 
 * @author Bernd Rinn
 */
public class ConsoleLogger implements ISimpleLogger
{

    private final PrintStream pstream;

    public ConsoleLogger()
    {
        this(System.out);
    }

    public ConsoleLogger(PrintStream pstream)
    {
        this.pstream = pstream;
    }

    @Override
    public void log(LogLevel level, String message)
    {
        pstream.println(level.toString() + ": " + message);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwableOrNull)
    {
        log(level, message);
        if (throwableOrNull != null)
        {
            throwableOrNull.printStackTrace(pstream);
        }
    }

}
