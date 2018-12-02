/*
 * Copyright 2018 ETH Zuerich, CISD
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
 * @author pkupczyk
 */
public class ConsoleLogger implements ILogger
{

    private LogLevel level;

    public ConsoleLogger(LogLevel level)
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
        log(clazz, level, message, null);
    }

    @Override
    public void log(Class<?> clazz, LogLevel level, String message, Throwable throwable)
    {
        System.out.println("[" + Thread.currentThread().getName() + "] " + clazz.getName() + " " + level + " " + message + " "
                + (throwable != null ? throwable : ""));
    }

}
