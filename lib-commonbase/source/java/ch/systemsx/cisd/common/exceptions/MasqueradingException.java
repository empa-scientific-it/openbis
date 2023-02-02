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

/**
 * A <code>RuntimeException</code> extension which almost clones given <var>rootException</var> without saving it as cause.
 * <p>
 * This means that it does not contain any third-party specific or proprietary <code>Exception</code> extension that the client does not know about
 * and does not understand.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class MasqueradingException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    /**
     * The class name of the root exception.
     * <p>
     * Can not be <code>null</code>.
     * </p>
     */
    private final String rootExceptionClassName;

    private final String prefix;

    public MasqueradingException(final Exception rootException, final String prefix)
    {
        super(rootException.getMessage());
        setStackTrace(rootException.getStackTrace());
        rootExceptionClassName = rootException.getClass().getName();
        this.prefix = prefix;
    }

    public MasqueradingException(final Exception rootException)
    {
        this(rootException, "Error occurred on server");
    }

    public final String getRootExceptionClassName()
    {
        return rootExceptionClassName;
    }

    //
    // RuntimeException
    //

    @Override
    public final String toString()
    {
        final String s = prefix + " [" + rootExceptionClassName + "]";
        final String message = getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}