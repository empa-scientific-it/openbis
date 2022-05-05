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

import java.util.Collection;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.reflection.ClassUtils;

/**
 * Provides utilities for manipulating and examining <code>Throwable</code> objects.
 * 
 * @author Christian Ribeaud
 */
public final class ExceptionUtils
{
    ExceptionUtils()
    {
        // Can not be instantiated.
    }

    /** Recursively copies cause exception from <var>fromException</var> to <var>toException</var>. */
    private final static void copyCauseException(final Exception fromException,
            final Exception toException, final Collection<String> acceptedPackages)
    {
        assert fromException != null : "Unspecified 'from' Exception.";
        assert toException != null : "Unspecified 'to' Exception.";
        final Exception fromCauseException = (Exception) fromException.getCause();
        if (fromCauseException != null && fromCauseException != fromException)
        {
            final Exception toCauseException =
                    new MasqueradingException(CheckedExceptionTunnel.unwrapIfNecessary(fromCauseException));
            if (toException.getCause() != toCauseException)
            {
                if (ClassUtils.setFieldValue(toException, "cause", toCauseException) == false)
                {
                    toException.initCause(toCauseException);
                }
            }
            copyCauseException(fromCauseException, toCauseException, acceptedPackages);
        }
    }

    public final static Exception createMasqueradingException(final Exception exception,
            final Collection<String> acceptedPackages)
    {
        assert exception != null : "Unspecified Exception.";
        final Exception clientSafeException =
                new MasqueradingException(CheckedExceptionTunnel.unwrapIfNecessary(exception));
        copyCauseException(exception, clientSafeException, acceptedPackages);
        return clientSafeException;
    }

    /**
     * Returns the first found <code>Throwable</code> of given <var>clazz</var> from the exception chain of given <var>throwable</var>.
     */
    public final static <T extends Throwable> T tryGetThrowableOfClass(final Throwable throwable,
            final Class<T> clazz)
    {
        assert throwable != null : "Unspecified throwable";
        assert clazz != null : "Unspecified class";
        if (clazz.isAssignableFrom(throwable.getClass()))
        {
            return clazz.cast(throwable);
        }
        final Throwable cause = throwable.getCause();
        if (cause != null)
        {
            return tryGetThrowableOfClass(cause, clazz);
        }
        return null;
    }

    /**
     * Returns the last {@link Throwable} of a chain of throwables.
     */
    public static Throwable getEndOfChain(Throwable throwable)
    {
        Throwable cause = throwable.getCause();
        return cause == null ? throwable : getEndOfChain(cause);
    }
}