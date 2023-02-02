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

package ch.systemsx.cisd.common.reflection;

import java.lang.reflect.Method;
import java.util.Arrays;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Functions around {@link Method}.
 * 
 * @author Franz-Josef Elmer
 */
public final class MethodUtils
{
    private MethodUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Returns the specified method as string which shows method name and parameter types in a way which more compact than {@link Method#toString()}.
     */
    public final static String toString(final Method method)
    {
        final StringBuilder builder = new StringBuilder(method.getName()).append('(');
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            builder.append(parameterTypes[i].getSimpleName());
            if (i < parameterTypes.length - 1)
            {
                builder.append(", ");
            }
        }
        return builder.append(')').toString();
    }

    /**
     * Returns the currently called <code>Method</code>.
     * <p>
     * Returns <code>null</code> if none could be found.
     * </p>
     */
    public final static Method getCurrentMethod()
    {
        return getMethodOnStack(2);
    }

    /**
     * Returns the <code>Method</code> on the stack of <var>level</var>.
     * <p>
     * <code>level=0</code> is this method itself, <code>level=1</code> is the method that called it and so forth. This method internally uses
     * {@link Class#getDeclaredMethods()} to retrieve the <code>Method</code>.
     * </p>
     * <p>
     * IMPORTANT NOTE: You should carefully use this method in a class having more than one method with the same name. The internal idea used here (
     * <code>new Throwable().getStackTrace()</code>) only returns a method name and does not make any other consideration.
     * </p>
     * 
     * @see StackTraceElement#getMethodName()
     * @throw {@link IndexOutOfBoundsException} if given <var>level</var> smaller or equal to number of stack trace elements.
     */
    public final static Method getMethodOnStack(final int level)
    {
        assert level > -1 : "Level must be positive.";

        final StackTraceElement[] elements = new Throwable().getStackTrace();
        if (elements.length <= level)
        {
            throw new IndexOutOfBoundsException(String.format("Not enough elements: %d <= %d",
                    elements.length, level));
        }
        final StackTraceElement element = elements[level];
        final String methodName = element.getMethodName();
        Method[] methods = null;
        try
        {
            methods = Class.forName(element.getClassName()).getDeclaredMethods();
            for (final Method method : methods)
            {
                if (method.getName().equals(methodName))
                {
                    return method;
                }
            }
            // SecurityException, ClassNotFoundException
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        throw new IllegalArgumentException(String.format(
                "Method name '%s' could not be found in methods '%s'.", methodName, Arrays
                        .toString(methods)));
    }

    /**
     * Describes given <var>method</var> in following format: <code>&lt;class-name&gt;.&lt;method-name&gt;</code>, for instance
     * <code>Object.hashCode</code>.
     */
    public final static String describeMethod(final Method method)
    {
        assert method != null : "Unspecified method";
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
}
