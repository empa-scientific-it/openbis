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

package ch.systemsx.cisd.common.exceptions;

import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.InvalidExternalDataException;

/**
 * Data provided to the system from another system is invalid. This class is supposed to be used in cases where the data cannot be attributed to any
 * user, but rather another system.
 * 
 * @author Bernd Rinn
 */
public class InvalidExternalDataException extends HighLevelException
{

    private static final long serialVersionUID = 1L;

    public InvalidExternalDataException(String message)
    {
        super(message);
    }

    public InvalidExternalDataException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates an {@link InvalidExternalDataException} using a {@link java.util.Formatter}.
     */
    public static InvalidExternalDataException fromTemplate(String messageTemplate, Object... args)
    {
        return new InvalidExternalDataException(String.format(messageTemplate, args));
    }

    /**
     * Creates an {@link InvalidExternalDataException} using a {@link java.util.Formatter}.
     */
    public static InvalidExternalDataException fromTemplate(Throwable cause, String messageTemplate,
            Object... args)
    {
        return new InvalidExternalDataException(String.format(messageTemplate, args), cause);
    }

}
