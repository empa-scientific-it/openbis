/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.filetransfer;

/**
 * @author Franz-Josef Elmer
 */
public class RecordingLogger extends AbstractLogger
{
    private StringBuilder builder = new StringBuilder();

    public RecordingLogger()
    {
        super(LogLevel.TRACE);
    }

    @Override
    public void doLog(Class<?> clazz, LogLevel level, String message)
    {
        doLog(clazz, level, message, null);
    }

    @Override
    public void doLog(Class<?> clazz, LogLevel level, String message, Throwable throwable)
    {
        builder.append(clazz.getSimpleName() + ": [" + level + "] " + message);
        if (throwable != null)
        {
            builder.append(" {" + throwable + "}");
        }
        builder.append('\n');
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

}
