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

import java.io.Serializable;

/**
 * This is an extension to the <code>RuntimeException</code> for unimplemented methods. The empty constructor tries to get the method resp. the class
 * name the exception has been thrown.
 * <p>
 * This class should be used everywhere where the <i>Apache</i> commons one can not (because the library is not imported, for instance).
 * </p>
 * 
 * @author Chritian Ribeaud
 * @see org.apache.commons.lang3.NotImplementedException
 */
public final class NotImplementedException extends RuntimeException
{

    static final String MESSAGE_TEMPLATE = "'%s' method not implemented in '%s'.";

    private static final long serialVersionUID = 1L;

    /** The error message for this exception. */
    private final String message;

    public NotImplementedException()
    {
        super();
        final StackTraceExtractor extractor = new StackTraceExtractor(this);
        this.message =
                String.format(MESSAGE_TEMPLATE, extractor.getMethodName(), extractor
                        .getSimpleClassName());
    }

    public NotImplementedException(final String message)
    {
        super(message);
        this.message = null;
    }

    public NotImplementedException(final Throwable cause)
    {
        super(cause);
        this.message = null;
    }

    public NotImplementedException(String message, Throwable cause)
    {
        super(message, cause);
        this.message = null;
    }

    //
    // RuntimeException
    //

    /**
     * Returns the error message for this exception. If the error message has not been defined in this class, returns the error message defined in the
     * super class.
     * 
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public final String getMessage()
    {
        if (message == null)
        {
            return super.getMessage();
        }
        return message;
    }

    /**
     * An automatic stack trace extractor.
     * 
     * @author Christian Ribeaud
     */
    public static class StackTraceExtractor implements Serializable
    {

        private static final long serialVersionUID = 1L;

        private final Throwable throwable;

        private final int index;

        public StackTraceExtractor(final Throwable throwable)
        {
            this(throwable, 0);
        }

        public StackTraceExtractor(final Throwable throwable, final int index)
        {
            this.throwable = throwable;
            this.index = index;
        }

        public final String getClassName()
        {
            return throwable.getStackTrace()[index].getClassName();
        }

        public final String getMethodName()
        {
            return throwable.getStackTrace()[index].getMethodName();
        }

        public final String getSimpleClassName()
        {
            String fullClassName = getClassName();
            if (null == fullClassName || "".equals(fullClassName))
            {
                return "";
            }
            // The simple class name is everything after the last dot.
            // If there's no dot then the whole thing is the class name.
            int lastDot = fullClassName.lastIndexOf('.');
            if (0 > lastDot)
            {
                return fullClassName;
            }
            // Otherwise, extract the class name.
            return fullClassName.substring(++lastDot);
        }
    }
}
