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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.MasqueradingException;
import ch.systemsx.cisd.common.reflection.ClassUtils;

/**
 * Provides utilities for manipulating and examining <code>Throwable</code> objects.
 * 
 * @author Christian Ribeaud
 */
public final class ExceptionUtils
{
    /**
     * Default packages (and subpackages) for not been masqueraded.
     */
    private final static String[] ACCEPTED_PACKAGE_NAME_DEPENDENCIES =
            { "java.lang", "ch.systemsx.cisd.base", "ch.systemsx.cisd.common.exceptions" };

    ExceptionUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Creates a new {@link MasqueradingException} from given <var>exception</var> and collection of packages which are not masqueraded (in addition
     * to the default packages) only if it is needed ({@link #isCandidateForMasquerading(Exception, Collection)} returns <code>true</code>). Otherwise
     * returns given <var>exception</var>.
     */
    private final static Exception createMasqueradingException(final Exception exception,
            final Collection<String> acceptedPackages)
    {
        final Exception rootException = CheckedExceptionTunnel.unwrapIfNecessary(exception);
        if (isCandidateForMasquerading(rootException, acceptedPackages))
        {
            return new MasqueradingException(rootException);
        } else
        {
            try
            {
                Constructor<? extends Exception> constructor = rootException.getClass().getConstructor(String.class);
                Exception clonedException = constructor.newInstance(rootException.getMessage());
                clonedException.setStackTrace(rootException.getStackTrace());
                return clonedException;
            } catch (Exception e)
            {
                return new MasqueradingException(rootException);
            }
        }
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
                    createMasqueradingException(fromCauseException, acceptedPackages);
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

    /**
     * Whether given <var>exception</var> is a candidate for masquerading or not.
     * 
     * @return <code>true</code> if the fully qualified class name of <code>exception</code> doesn't start with a package name from
     *         <code>acceptedPackages</code> or <code>java.lang, ch.systemsx.cisd.common</code>.
     */
    private final static boolean isCandidateForMasquerading(final Exception exception,
            final Collection<String> acceptedPackages)
    {
        assert exception != null : "Unspecified exception.";
        final String className = exception.getClass().getName();
        for (final String packageName : createSetOfAcceptedPackages(acceptedPackages))
        {
            if (className.startsWith(packageName))
            {
                return false;
            }
        }
        return true;
    }

    private static Set<String> createSetOfAcceptedPackages(Collection<String> acceptedPackages)
    {
        final LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.addAll(Arrays.asList(ACCEPTED_PACKAGE_NAME_DEPENDENCIES));
        set.addAll(acceptedPackages);
        return set;
    }

    /**
     * Analyzes given <var>exception</var> and makes it independent to packages outside the specified collection or
     * <code>java.lang, ch.systemsx.cisd.common</code>.
     */
    public final static Exception createMasqueradingExceptionIfNeeded(final Exception exception,
            final Collection<String> acceptedPackages)
    {
        assert exception != null : "Unspecified SQL Exception.";
        final Exception clientSafeException =
                createMasqueradingException(exception, acceptedPackages);
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