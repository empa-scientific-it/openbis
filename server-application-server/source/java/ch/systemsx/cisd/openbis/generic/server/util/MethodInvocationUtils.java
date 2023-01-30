/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Helper methods for {@link MethodInvocation}.
 * 
 * @author Franz-Josef Elmer
 */
public class MethodInvocationUtils
{
    /**
     * see {@link #getMethod(MethodInvocation, List)}.
     */
    public static Method getMethod(final MethodInvocation methodInvocation,
            Class<? extends Annotation> annotationClass) throws NoSuchMethodException
    {
        final List<Class<? extends Annotation>> annotationList =
                Collections.<Class<? extends Annotation>> singletonList(annotationClass);
        return getMethod(methodInvocation, annotationList);

    }

    /**
     * Returns either the method of the specified {@link MethodInvocation} object or the method of the target object. The later is returned if
     * <code>methodInvocation.getMethod()</code> has none the specified annotations and the target object class isn't a synthetic proxy class.
     * <p>
     * This function should be used if it isn't known whether the interface of a proxy or the real target has the annotation.
     */
    public static Method getMethod(final MethodInvocation methodInvocation,
            List<Class<? extends Annotation>> annotationClasses) throws NoSuchMethodException
    {
        Method method = methodInvocation.getMethod();

        if (false == hasAnnotation(method, annotationClasses))
        {
            // If not authorization annotation found, try method of target class if it isn't a
            // proxy
            Object thisObject = methodInvocation.getThis();
            if (thisObject != null)
            {
                Class<? extends Object> targetClass = thisObject.getClass();
                if (targetClass.isSynthetic() == false)
                {
                    method = targetClass.getMethod(method.getName(), method.getParameterTypes());
                }
            }
        }
        return method;
    }

    /**
     * Return true if the method has at least one of the specified annotations, false otherwise.
     */
    private static boolean hasAnnotation(Method method,
            List<Class<? extends Annotation>> annotationClasses)
    {
        for (Class<? extends Annotation> annotationClass : annotationClasses)
        {
            if (method.getAnnotation(annotationClass) != null)
            {
                return true;
            }
        }
        return false;
    }

}
