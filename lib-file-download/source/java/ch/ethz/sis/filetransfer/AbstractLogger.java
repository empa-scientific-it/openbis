/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
public abstract class AbstractLogger implements ILogger
{
    private LogLevel level;

    public AbstractLogger(LogLevel level)
    {
        this.level = level;
    }

    @Override
    public boolean isEnabled(LogLevel level)
    {
        return level.ordinal() >= this.level.ordinal();
    }

    @Override
    public void log(Class<?> clazz, LogLevel level, String message)
    {
        if (isEnabled(level))
        {
            doLog(clazz, level, message);
        }
    }

    protected abstract void doLog(Class<?> clazz, LogLevel level, String message);

    @Override
    public void log(Class<?> clazz, LogLevel level, String message, Throwable throwable)
    {
        if (isEnabled(level))
        {
            doLog(clazz, level, message, throwable);
        }
    }

    protected abstract void doLog(Class<?> clazz, LogLevel level, String message, Throwable throwable);
}
