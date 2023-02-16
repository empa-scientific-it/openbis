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
 * A logger interface. Depending on a use case an actual implementation may log messages using a standard logging library (e.g. log4j) or may use some
 * other logging solution.
 * 
 * @author pkupczyk
 */
public interface ILogger
{

    /**
     * Returns true if a given log level is enabled. Returns false otherwise. This method can be used to improve performance and avoid creation of
     * complex log messages in case a given log level is disabled and the message won't be used anyway.
     */
    public boolean isEnabled(LogLevel level);

    /**
     * Logs a message in a context of a given class using the specified log level.
     */
    public void log(Class<?> clazz, LogLevel level, String message);

    /**
     * Logs a message together with an exception in a context of a given class using the specified log level.
     */
    public void log(Class<?> clazz, LogLevel level, String message, Throwable throwable);

}
