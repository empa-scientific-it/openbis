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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import org.apache.commons.text.StringEscapeUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Translator of server side {@link ch.systemsx.cisd.common.exceptions.UserFailureException} into GWT compatible {@link UserFailureException}.
 * 
 * @author Franz-Josef Elmer
 */
public class UserFailureExceptionTranslator
{
    private static final String WEB_CLIENT_EXCEPTIONS_PACKAGE =
            getPackageName(UserFailureException.class);

    private static String getPackageName(Class<?> clazz)
    {
        String fullName = clazz.getName();
        return fullName.substring(0, fullName.length() - clazz.getSimpleName().length() - 1);
    }

    private UserFailureExceptionTranslator()
    {
    }

    /**
     * Converts any {@link ch.systemsx.cisd.common.exceptions.UserFailureException} or subclass of it to a <i>GWT</i> {@link UserFailureException} (or
     * subclass of it if this one could be found in the same package).
     */
    public static UserFailureException translate(
            ch.systemsx.cisd.common.exceptions.UserFailureException exception)
    {
        return translate(exception, null);
    }

    /**
     * Converts any {@link ch.systemsx.cisd.common.exceptions.UserFailureException} or subclass of it to a <i>GWT</i> {@link UserFailureException} (or
     * subclass of it if this one could be found in the same package) using specified message as the main message and message in the original
     * exception as the detail message.
     */
    public static UserFailureException translate(
            ch.systemsx.cisd.common.exceptions.UserFailureException exception,
            String simpleMessageOrNull)
    {
        final String className =
                WEB_CLIENT_EXCEPTIONS_PACKAGE + "." + exception.getClass().getSimpleName();
        final String fullMessage = StringEscapeUtils.escapeHtml4(exception.getMessage());

        try
        {
            if (simpleMessageOrNull != null)
            {
                return ClassUtils.create(UserFailureException.class, className,
                        simpleMessageOrNull, fullMessage);
            } else
            {
                return ClassUtils.create(UserFailureException.class, className, fullMessage);
            }
        } catch (final CheckedExceptionTunnel e)
        {
            if (simpleMessageOrNull != null)
            {
                return new UserFailureException(simpleMessageOrNull, fullMessage);
            } else
            {
                return new UserFailureException(fullMessage);
            }
        }

    }

}
