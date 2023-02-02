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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * General utility methods for working with annotations.
 * 
 * @author Christian Ribeaud
 */
public final class AnnotationUtils
{

    private AnnotationUtils()
    {
        // Can not be instantiated.
    }

    /**
     * For given <code>Class</code> returns a list of methods that are annotated with given <var>annotationClass</var>.
     */
    public final static List<Method> getAnnotatedMethodList(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass)
    {
        return AnnotationUtils.getAnnotatedMethodList(clazz, annotationClass, null);
    }

    /**
     * For given <code>Class</code> returns a list of methods that are annotated with given <var>annotationClass</var>.
     * 
     * @param methods if <code>null</code>, then a new <code>List</code> is created.
     */
    private final static List<Method> getAnnotatedMethodList(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass, final List<Method> methods)
    {
        assert clazz != null : "Unspecified class.";
        assert annotationClass != null : "Unspecified annotation class.";
        List<Method> list = methods;
        if (list == null)
        {
            list = new ArrayList<Method>();
        }
        for (final Method method : clazz.getDeclaredMethods())
        {
            if (method.getAnnotation(annotationClass) != null)
            {
                list.add(method);
            }
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null)
        {
            return getAnnotatedMethodList(superclass, annotationClass, list);
        }
        return list;
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with given <var>annotationClass</var>.
     */
    public final static List<Field> getAnnotatedFieldList(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass)
    {
        return AnnotationUtils.getAnnotatedFieldList(clazz, annotationClass, null);
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with given <var>annotationClass</var>.
     * 
     * @param fields if <code>null</code>, then a new <code>List</code> is created.
     */
    private final static List<Field> getAnnotatedFieldList(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass, final List<Field> fields)
    {
        assert clazz != null : "Unspecified class.";
        assert annotationClass != null : "Unspecified annotation class.";
        List<Field> list = fields;
        if (list == null)
        {
            list = new ArrayList<Field>();
        }
        for (final Field field : clazz.getDeclaredFields())
        {
            if (field.getAnnotation(annotationClass) != null)
            {
                list.add(field);
            }
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null)
        {
            return getAnnotatedFieldList(superclass, annotationClass, list);
        }
        return list;
    }

    /**
     * From the list of given annotations tries to return the one of given type.
     * 
     * @return <code>null</code> if not found.
     */
    public final static <A extends Annotation> A tryGetAnnotation(final Annotation[] annotations,
            final Class<A> annotationType)
    {
        assert annotations != null : "Unspecified annotations";
        assert annotationType != null : "Unspecified annotation type";
        for (final Annotation annotation : annotations)
        {
            if (annotation.annotationType().equals(annotationType))
            {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    /**
     * Returns a list of method parameters where given <var>annotation</var> could be found.
     * 
     * @return never <code>null</code> but could return an empty list.
     */
    public final static <A extends Annotation> List<Parameter<A>> getAnnotatedParameters(
            final Method method, final Class<A> annotationType)
    {
        assert method != null : "Unspecified method";
        assert annotationType != null : "Unspecified annotation type";
        final Annotation[][] annotations = method.getParameterAnnotations();
        final Class<?>[] types = method.getParameterTypes();
        final List<Parameter<A>> list = new ArrayList<Parameter<A>>();
        for (int i = 0; i < types.length; i++)
        {
            final Class<?> type = types[i];
            final A annotationOrNull = tryGetAnnotation(annotations[i], annotationType);
            if (annotationOrNull != null)
            {
                list.add(new Parameter<A>(i, type, annotationOrNull));
            }
        }
        return list;
    }

    //
    // Helper classes
    //

    public final static class Parameter<A extends Annotation>
    {
        /**
         * This parameter index in the list of method arguments.
         */
        private final int index;

        private final Class<?> type;

        private final A annotation;

        Parameter(final int index, final Class<?> type, final A annotation)
        {
            this.index = index;
            this.type = type;
            this.annotation = annotation;
        }

        public final int getIndex()
        {
            return index;
        }

        public final Class<?> getType()
        {
            return type;
        }

        public final A getAnnotation()
        {
            return annotation;
        }
    }
}
