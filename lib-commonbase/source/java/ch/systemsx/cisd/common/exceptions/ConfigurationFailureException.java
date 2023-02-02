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

package ch.systemsx.cisd.common.exceptions;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;

/**
 * The <code>ConfigurationFailureException</code> is the super class of all exceptions that have their cause in an inappropriate configuration of the
 * system. This implies that an application administrator can fix the problem.
 * 
 * @author Bernd Rinn
 */
public class ConfigurationFailureException extends HighLevelException
{

    private static final long serialVersionUID = 1L;

    public ConfigurationFailureException(String message)
    {
        super(message);
    }

    public ConfigurationFailureException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates an {@link ConfigurationFailureException} using a {@link java.util.Formatter}.
     */
    public static ConfigurationFailureException fromTemplate(String messageTemplate, Object... args)
    {
        return new ConfigurationFailureException(String.format(messageTemplate, args));
    }

    /**
     * Creates an {@link ConfigurationFailureException} using a {@link java.util.Formatter}.
     */
    public static ConfigurationFailureException fromTemplate(Throwable cause,
            String messageTemplate, Object... args)
    {
        return new ConfigurationFailureException(String.format(messageTemplate, args), cause);
    }

}
