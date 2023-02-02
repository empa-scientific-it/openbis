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

/**
 * Interface of factories creating a logger which implements the same methods of a class or interface <code>T</code> which should be logged. Such
 * loggers are used by {@link LogInterceptor}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IInvocationLoggerFactory<T>
{
    /**
     * Creates an instance of the logger with specified flag which tells whether invocation was successful or not and specified elapsed time.
     */
    public T createLogger(IInvocationLoggerContext context);
}
