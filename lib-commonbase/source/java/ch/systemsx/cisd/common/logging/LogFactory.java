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

import org.apache.log4j.Logger;

/**
 * This class is used to create loggers (using <code>log4j</code>).
 * 
 * @author Bernd Rinn
 */
public final class LogFactory
{

    private LogFactory()
    {
        // Can not be instantiated.
    }

    /**
     * @return The logger name for the given {@link LogCategory} and {@link Class}. It will contain the name of the <var>category</var>, followed by
     *         the canonical name of <var>clazz</var>.
     */
    public static String getLoggerName(LogCategory category, Class<?> clazz)
    {
        return category.name() + "." + clazz.getSimpleName();
    }

    /**
     * @return The logger name for the given {@link LogCategory}. Needs to be used for admin logs (i.e. {@link LogCategory#isAdminLog()} needs to
     *         return <code>true</code>). It will contain the name of the <var>category</var>.
     */
    public static String getLoggerName(LogCategory category)
    {
        if (category.isAdminLog() == false)
        {
            throw new IllegalArgumentException("Only admin logs are allowed here, but we got "
                    + category + ".");
        }
        return category.name();
    }

    /**
     * @return The logger for the given {@link LogCategory} and {@link Class}. The name of the logger will contain the name of the
     *         <var>category</var>, followed by the canonical name of <var>clazz</var>.
     */
    public static Logger getLogger(LogCategory category, Class<?> clazz)
    {
        return Logger.getLogger(getLoggerName(category, clazz));
    }

    /**
     * @return The logger for the given {@link LogCategory}. Needs to be an admin log (i.e. {@link LogCategory#isAdminLog()} needs to return
     *         <code>true</code>). The name of the logger will contain the name of the <var>category</var>.
     */
    public static Logger getLogger(LogCategory category)
    {
        return Logger.getLogger(getLoggerName(category));
    }

}
