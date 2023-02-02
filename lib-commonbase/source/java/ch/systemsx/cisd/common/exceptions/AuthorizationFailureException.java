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

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A {@link UserFailureException} for authorization failure.
 * 
 * @author Christian Ribeaud
 */
public final class AuthorizationFailureException extends UserFailureException
{
    private static final String MESSAGE_FORMAT = "Authorization failure: %s.";

    private static final long serialVersionUID = 1L;

    public AuthorizationFailureException(final String detailMessage)
    {
        super(createMessage(detailMessage));
    }

    private final static String createMessage(final String detailMessage)
    {
        return String.format(MESSAGE_FORMAT, removeLastDotIfAny(detailMessage));
    }

    private final static String removeLastDotIfAny(final String detailMessage)
    {
        return detailMessage.endsWith(".") ? detailMessage.substring(0, detailMessage.length() - 1)
                : detailMessage;
    }
}
