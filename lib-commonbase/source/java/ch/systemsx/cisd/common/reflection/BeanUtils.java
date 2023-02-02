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

package ch.systemsx.cisd.common.reflection;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Some utilities around <i>Java Bean</i>s.
 * 
 * @author Christian Ribeaud
 * @author Bernd Rinn
 */
public final class BeanUtils
{

    /** A generally accepted variation for <code>Boolean</code> bean property. */
    private static final String BOOLEAN_GETTER_PREFIX = "is";

    /** The typical <i>getter</i> prefix for <i>bean</i> property. */
    private static final String GETTER_PREFIX = "get";

    /** The typical <i>setter</i> prefix for <i>bean</i> property. */
    private static final String SETTER_PREFIX = "set";

    private BeanUtils()
    {
        // Can not be instantiated.
    }

    /**
     * A map that provides annotations for given annotation classes.
     */
    public interface AnnotationMap
    {
        /**
         * Returns the name of the entity that this annotation stems from.
         */
        public String getAnnotatedEntity();

        /**
         * Returns the annotation for <var>annotationClazz</var>.
         */
        public <T extends Annotation> T getAnnotation(Class<T> annotationClazz);
    }

    /**
     * An empty {@link AnnotationMap} (to be used when there are no annotations).
     */
    private static class EmptyAnnotationMap implements AnnotationMap
    {

        @Override
        public String getAnnotatedEntity()
        {
            return "GENERIC";
        }

        @Override
        public <T extends Annotation> T getAnnotation(final Class<T> annotationClazz)
        {
            return null;
        }

    }

    /**
     * An {@link AnnotationMap} that gets its annotation from a (setter) method.
     */
    private static class SetterAnnotationMap implements AnnotationMap
    {

        private final Method setterMethod;

        SetterAnnotationMap(final Method setterMethod)
        {
            assert setterMethod != null;

            this.setterMethod = setterMethod;
        }

        @Override
        public String getAnnotatedEntity()
        {
            return setterMethod.toGenericString();
        }

        @Override
        public <T extends Annotation> T getAnnotation(final Class<T> annotationClazz)
        {
            return setterMethod.getAnnotation(annotationClazz);
        }

    }

    /**
     * Marker interface for converter classes. The real method are determined via reflection. A converter needs to match both the source and the
     * destination bean. If the destination bean has a setter <code>setFoo(FooClass foo)</code> and the converter has a method
     * <code>FooClass convertToFoo(SourceBeanClass sourceBean)</code>, then this will be called instead of any getter of the <var>sourceBean</var>.
     * <p>
     * Note:
     * <ul>
     * <li>The declared <code>SourceBeanClass</code> in the converter method can also be a superclass of <code>sourceBean</code>or an interface
     * implemented by <code>sourceBean</code>
     * <li>If there is a matching method <code>convertToFoo()</code>, it needs to have an appropriate return type or else an
     * {@link IllegalArgumentException} will be thrown.
     * </ul>
     */
    public interface Converter
    {
    }

    private static final AnnotationMap EMPTY_ANNOTATION_MAP = new EmptyAnnotationMap();

    private static final Converter NULL_CONVERTER = new Converter()
        {
        };

    @SuppressWarnings({ "rawtypes" })
    static final Set<Class> immutableTypes = new LinkedHashSet<Class>(Arrays.asList(boolean.class,
            Boolean.class, byte.class, Byte.class, short.class, Short.class, int.class,
            Integer.class, long.class, Long.class, float.class, Float.class, double.class,
            Double.class, String.class, Date.class));

    /**
     * Creates a new list of Beans of type <var>clazz</var>.
     * 
     * @param clazz element type of the new list.
     * @param source The iterable to fill the new bean list from. Can be <code>null</code>, in which case the method returns <code>null</code>.
     * @return The new list filled from <var>sourceList</var> or <code>null</code>, if <var>sourceList</var> is <code>null</code>.
     */
    public final static <T, S> List<T> createBeanList(final Class<T> clazz, final Iterable<S> source)
    {
        return createBeanList(clazz, source, null);
    }

    /**
     * Creates a new array of Beans of type <var>clazz</var>. See <code>createBeanList()</code> for parameter specification.
     */
    public final static <T, S> T[] createBeanArray(final Class<T> clazz, final Collection<S> source)
    {
        return createBeanArray(clazz, source, null);
    }

    /**
     * Creates a new array of Beans of type <var>clazz</var>. See <code>createBeanList()</code> for parameter specification.
     */
    public final static <T, S> T[] createBeanArray(final Class<T> clazz, final S[] source)
    {
        return createBeanArray(clazz, Arrays.asList(source), null);
    }

    /**
     * Creates a new array of Beans of type <var>clazz</var>. See <code>createBeanList()</code> for parameter specification.
     */
    public final static <T, S> T[] createBeanArray(final Class<T> clazz, final S[] source,
            final Converter converter)
    {
        return createBeanArray(clazz, Arrays.asList(source), converter);
    }

    /**
     * Creates a new array of Beans of type <var>clazz</var>. See <code>createBeanList()</code> for parameter specification.
     */
    public final static <T, S> T[] createBeanArray(final Class<T> clazz,
            final Collection<S> source, final Converter converter)
    {
        assert clazz != null : "Unspecified class";

        if (source == null)
        {
            return null;
        }

        final T result[] = createArrayOfType(clazz, source.size());
        int i = 0;
        for (final S element : source)
        {
            result[i] = BeanUtils.createBean(clazz, element, converter);
            i++;
        }
        return result;
    }

    /**
     * Creates a new array of Beans of type <var>clazz</var>. See <code>createBeanList()</code> for parameter specification.
     */
    public static <T, S> T[] createBeanArray(final Class<T> clazz, final Iterable<S> source,
            final Converter converter)
    {
        final List<T> list = createBeanList(clazz, source, converter);
        final T result[] = createArrayOfType(clazz, list.size());
        return list.toArray(result);
    }

    /**
     * Creates a new list of Beans of type <var>clazz</var>.
     * 
     * @param clazz element type of the new list.
     * @param source The iterable to fill the new bean list from. Can be <code>null</code>, in which case the method returns <code>null</code>.
     * @param converter The {@link Converter} to use to perform non-standard conversions when filling the bean. Can be <code>null</code>, in which
     *            case only standard conversions are allowed.
     * @return The new list filled from <var>sourceList</var> or <code>null</code>, if <var>sourceList</var> is <code>null</code>.
     */
    public final static <T, S> List<T> createBeanList(final Class<T> clazz,
            final Iterable<S> source, final Converter converter)
    {
        assert clazz != null;

        if (source == null)
        {
            return null;
        }

        final List<T> resultList = new ArrayList<T>();
        for (final S element : source)
        {
            resultList.add(BeanUtils.createBean(clazz, element, converter));
        }
        return resultList;
    }

    /**
     * Convenience method for {@link #createBean(Class, Object, ch.systemsx.cisd.common.reflection.BeanUtils.Converter)} where <var>converter</var> is
     * <code>NULL_CONVERTER</code>.
     */
    public static <T> T fillBean(final Class<T> beanClass, final T beanInstance,
            final Object sourceBean)
    {
        return fillBean(beanClass, beanInstance, new IdentityHashMap<Object, Object>(), sourceBean,
                EMPTY_ANNOTATION_MAP, NULL_CONVERTER);
    }

    /**
     * Convenience method for {@link #createBean(Class, Object, ch.systemsx.cisd.common.reflection.BeanUtils.Converter)} where <var>converter</var> is
     * <code>NULL_CONVERTER</code>.
     */
    public static <T> T createBean(final Class<T> beanClass, final Object sourceBean)
    {
        return fillBean(beanClass, null, new IdentityHashMap<Object, Object>(), sourceBean,
                EMPTY_ANNOTATION_MAP, NULL_CONVERTER);
    }

    /**
     * Fills a new bean <var>beanInstance</var> of type <var>beanClass</var> with values from <var>sourceBean</var>.
     * 
     * @param beanClass The class to create a new instance from.
     * @param beanInstance Instance of the bean to be filled. If <code>null</code> a new instance will be created.
     * @param sourceBean The bean to get the values from. Can be <code>null</code>, in which case the method returns <code>null</code>.
     * @param converter The {@link Converter} to use to perform non-standard conversions when filling the bean. Can be <code>null</code>, in which
     *            case only standard conversions are allowed.
     * @return The new bean or <code>null</code> if <var>sourceBean</var> is <code>null</code>.
     */
    public static <T> T fillBean(final Class<T> beanClass, final T beanInstance,
            final Object sourceBean, final Converter converter)
    {
        Converter c = converter;
        if (c == null)
        {
            c = NULL_CONVERTER;
        }
        return fillBean(beanClass, beanInstance, new IdentityHashMap<Object, Object>(), sourceBean,
                EMPTY_ANNOTATION_MAP, c);
    }

    /**
     * Creates a new bean of type <var>beanClass</var> and fills it with values from <var>sourceBean</var> (which can be a bean or a
     * {@link Properties} object.
     * 
     * @param beanClass The class to create a new instance from.
     * @param sourceBean The bean to get the values from. Can be <code>null</code>, in which case the method returns <code>null</code>. If this is a
     *            {@link Properties} object, the bean will be created from the property values. In this case only primitive values and Strings are
     *            supported.
     * @param converter The {@link Converter} to use to perform non-standard conversions when filling the bean. Can be <code>null</code>, in which
     *            case only standard conversions are allowed.
     * @return The new bean or <code>null</code> if <var>sourceBean</var> is <code>null</code>.
     */
    public static <T> T createBean(final Class<T> beanClass, final Object sourceBean,
            final Converter converter)
    {
        return createBean(beanClass, new IdentityHashMap<Object, Object>(), sourceBean, converter);
    }

    private static <T> T createBean(final Class<T> beanClass, final Map<Object, Object> repository,
            final Object sourceBean, final Converter converter)
    {
        Converter c = converter;
        if (c == null)
        {
            c = NULL_CONVERTER;
        }
        return fillBean(beanClass, null, repository, sourceBean, EMPTY_ANNOTATION_MAP, c);
    }

    /**
     * Fills the specified bean instance with values from <var>sourceBean</var>.
     * 
     * @param beanClass The class to create a new instance from.
     * @param beanInstance Instance of the bean to be filled. If <code>null</code> a new instance will be created.
     * @param sourceBean The bean to get the values from. Can be <code>null</code>, in which case the method returns <code>null</code>.
     * @param setterAnnotations The annotations attached to the setter that can be used to determine how the result should be created.
     * @param converter The {@link Converter} to use to perform non-standard conversions when filling the bean. Can be <code>null</code>, in which
     *            case only standard conversions are allowed.
     * @param repository a {@link Map} composed of source beans as keys and destination beans as values. This avoids cycling dependencies during the
     *            conversion.
     * @return The new bean or <code>null</code> if <var>sourceBean</var> is <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    private static <T> T fillBean(final Class<T> beanClass, final T beanInstance,
            final Map<Object, Object> repository, final Object sourceBean,
            final AnnotationMap setterAnnotations, final Converter converter)
    {
        assert beanClass != null : "undefined bean class";
        assert setterAnnotations != null : "undefined setter annotations for " + beanClass;
        assert converter != null : "undefined converter for " + beanClass;

        if (sourceBean == null)
        {
            return null;
        }
        Object convertedBean = repository.get(sourceBean);
        if (convertedBean != null)
        {
            return (T) convertedBean;
        }

        try
        {
            T destinationBean =
                    beanInstance != null ? beanInstance : instantiateBean(beanClass, sourceBean,
                            setterAnnotations);
            if (isArray(destinationBean))
            {
                if (isArray(sourceBean))
                {
                    destinationBean =
                            copyArrayToArray(destinationBean, repository, sourceBean, converter);
                } else if (isCollection(sourceBean))
                {
                    destinationBean =
                            (T) copyCollectionToArray(destinationBean, repository,
                                    (Collection<?>) sourceBean, converter);
                }
            } else if (isCollection(destinationBean))
            {
                if (isArray(sourceBean))
                {
                    copyArrayToCollection((Collection<?>) destinationBean, repository, sourceBean,
                            setterAnnotations, converter);
                } else if (isCollection(sourceBean))
                {
                    copyCollectionToCollection((Collection<?>) destinationBean, repository,
                            (Collection<?>) sourceBean, setterAnnotations, converter);
                }
            } else
            {
                repository.put(sourceBean, destinationBean);
                if (sourceBean instanceof Properties)
                {
                    copyBeanFromProperties(destinationBean, (Properties) sourceBean, converter);
                } else
                {
                    copyBean(destinationBean, repository, sourceBean, converter);
                }
            }
            return destinationBean;
        } catch (final InvocationTargetException ex)
        {
            final Throwable cause = ex.getCause();
            if (cause instanceof Error)
            {
                throw (Error) cause;
            }
            throw CheckedExceptionTunnel.wrapIfNecessary((Exception) cause);
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private static boolean isCollection(final Object o)
    {
        return o instanceof Collection<?>;
    }

    private static boolean isCollection(final Class<?> clazz)
    {
        return Collection.class.isAssignableFrom(clazz);
    }

    private static boolean isArray(final Object o)
    {
        return o != null && o.getClass().isArray();
    }

    private static boolean isArray(final Class<?> clazz)
    {
        return clazz.isArray();
    }

    private static <T> T instantiateBean(final Class<T> beanClass, final Object sourceBean,
            final AnnotationMap setterAnnotations) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        if (sourceBean == null)
        {
            return null;
        }
        final boolean beanIsArray = isArray(beanClass);
        final boolean beanIsCollection = isCollection(beanClass);
        if (beanIsArray || beanIsCollection)
        {
            final Integer size = getSize(sourceBean);
            if (size == null)
            {
                return null;
            }
            if (beanIsArray)
            {
                return createArray(beanClass, size);
            }
            if (beanIsCollection)
            {
                return createCollection(size, setterAnnotations);
            }
        }
        return beanClass.newInstance();
    }

    private static Integer getSize(final Object o)
    {
        if (isArray(o))
        {
            return Array.getLength(o);
        } else if (isCollection(o))
        {
            return ((Collection<?>) o).size();
        } else
        { // Don't know how to get the size of o.
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createArray(final Class<T> beanClass, final int length)
            throws NegativeArraySizeException
    {
        return (T) Array.newInstance(beanClass.getComponentType(), length);
    }

    @SuppressWarnings("unchecked")
    private static <E> E[] createArrayOfType(final Class<E> elemClass, final int length)
            throws NegativeArraySizeException
    {
        return (E[]) Array.newInstance(elemClass, length);
    }

    // No way to avoid the warning since the compiler doesn't accept something like
    // ArrayList<String>.class
    @SuppressWarnings("rawtypes")
    private final static <T> T createCollection(final int size,
            final AnnotationMap setterAnnotations) throws InstantiationException,
            IllegalAccessException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, InvocationTargetException
    {
        final CollectionMapping mapping = getCollectionMapping(setterAnnotations);
        try
        {
            final Constructor<? extends Collection> constructorWithSize =
                    mapping.collectionClass().getConstructor(new Class[]
                    { int.class });
            return constructCollection(constructorWithSize, size);
        } catch (final NoSuchMethodException ex)
        { // Happens e.g. for a LinkedList
            return constructCollection(mapping.collectionClass());
        }
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private static <T> T constructCollection(
            final Constructor<? extends Collection> constructorWithSize, final int size)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException
    {
        // This conversion _can_ go wrong if the concrete collection class doesn't implement the
        // right sub-interface of
        // collection, e.g. when using a HashSet as concrete collection class where a List is
        // required.
        return (T) constructorWithSize.newInstance(new Object[]
        { size });
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private final static <T> T constructCollection(final Class<? extends Collection> collectionClazz)
            throws InstantiationException, IllegalAccessException
    {
        // This conversion _can_ go wrong if the concrete collection class doesn't implement the
        // right sub-interface of
        // collection, e.g. when using a HashSet as concrete collection class where a List is
        // required.
        return (T) collectionClazz.newInstance();
    }

    @SuppressWarnings("unchecked")
    private final static <T> T copyArrayToArray(final T destination,
            final Map<Object, Object> repository, final Object source, final Converter converter)
            throws IllegalAccessException, InvocationTargetException
    {
        if (destination == null)
        {
            return null;
        }
        final Class<?> componentType = destination.getClass().getComponentType();
        final int length = Array.getLength(source);
        final T returned;
        if (Array.getLength(destination) < length)
        {
            returned = (T) Array.newInstance(componentType, length);
        } else
        {
            returned = destination;
        }
        if (immutableTypes.contains(componentType))
        {
            if (componentType == source.getClass().getComponentType())
            {
                System.arraycopy(source, 0, returned, 0, length);
            } else
            {
                for (int index = 0; index < length; ++index)
                {
                    final Object sourceElement = Array.get(source, index);
                    Array.set(returned, index, sourceElement);
                }
            }
        } else
        {
            for (int index = 0; index < length; ++index)
            {
                final Object sourceElement = Array.get(source, index);
                final Object destinationElement =
                        createBean(componentType, repository, sourceElement, converter);
                Array.set(returned, index, destinationElement);
            }
        }
        return returned;
    }

    @SuppressWarnings("unchecked")
    private final static <T> T[] copyCollectionToArray(final Object destination,
            final Map<Object, Object> repository, final Collection<T> source,
            final Converter converter) throws IllegalAccessException, InvocationTargetException
    {
        if (destination == null)
        {
            return null;
        }
        final Class<?> componentType = destination.getClass().getComponentType();
        final int size = source.size();
        final T[] returned;
        if (Array.getLength(destination) < size)
        {
            returned = (T[]) Array.newInstance(componentType, size);
        } else
        {
            returned = (T[]) destination;
        }
        if (immutableTypes.contains(componentType))
        {
            int index = 0;
            for (final Object sourceElement : source)
            {
                Array.set(returned, index++, sourceElement);
            }
        } else
        {
            int index = 0;
            for (final Object sourceElement : source)
            {
                final Object destinationElement =
                        createBean(componentType, repository, sourceElement, converter);
                Array.set(returned, index++, destinationElement);
            }
        }
        return returned;
    }

    private static void copyArrayToCollection(final Collection<?> destination,
            final Map<Object, Object> repository, final Object source,
            final AnnotationMap setterAnnotations, final Converter converter)
    {
        if (destination == null)
        {
            return;
        }
        final Class<?> componentType = getCollectionComponentType(setterAnnotations);
        final int length = getSize(source);
        if (immutableTypes.contains(componentType))
        {
            for (int index = 0; index < length; ++index)
            {
                final Object sourceElement = Array.get(source, index);
                addToUntypedCollection(destination, sourceElement);
            }
        } else
        {
            for (int index = 0; index < length; ++index)
            {
                final Object sourceElement = Array.get(source, index);
                final Object destinationElement =
                        createBean(componentType, repository, sourceElement, converter);
                addToUntypedCollection(destination, destinationElement);
            }
        }
    }

    private static void copyCollectionToCollection(final Collection<?> destination,
            final Map<Object, Object> repository, final Collection<?> source,
            final AnnotationMap setterAnnotations, final Converter converter)
    {
        if (destination == null)
        {
            return;
        }
        final Class<?> componentType = getCollectionComponentType(setterAnnotations);
        if (immutableTypes.contains(componentType))
        {
            for (final Object sourceElement : source)
            {
                addToUntypedCollection(destination, sourceElement);
            }
        } else
        {
            for (final Object sourceElement : source)
            {
                final Object destinationElement =
                        createBean(componentType, repository, sourceElement, converter);
                addToUntypedCollection(destination, destinationElement);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void addToUntypedCollection(final Collection destination, final Object element)
    {
        destination.add(element);
    }

    private static Class<?> getCollectionComponentType(final AnnotationMap setterAnnotations)
    {
        return getCollectionMapping(setterAnnotations).elementClass();
    }

    private static CollectionMapping getCollectionMapping(final AnnotationMap setterAnnotations)
    {
        final CollectionMapping mapping = setterAnnotations.getAnnotation(CollectionMapping.class);
        if (mapping == null)
        {
            throw new IllegalArgumentException("No collection mapping specified for '"
                    + setterAnnotations.getAnnotatedEntity() + "'.");
        }
        return mapping;
    }

    private static <T> void copyBeanFromProperties(final T destination, final Properties source,
            final Converter converter) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        if (destination == null)
        {
            return;
        }
        final Collection<Method> destinationSetters =
                scanForPublicMethods(destination, SETTER_PREFIX, 1).values();
        for (final Method setter : destinationSetters)
        {
            Object valueOrNull = null;
            final Method converterMethod = getConverterMethod(setter, source, converter);
            if (converterMethod != null)
            {
                valueOrNull = converterMethod.invoke(converter, new Object[]
                { source });
            } else
            {
                final String propertyKey =
                        Character.toLowerCase(setter.getName().charAt(3))
                                + setter.getName().substring("setX".length());
                final String propertyValueOrNull = (String) source.get(propertyKey);
                if (propertyValueOrNull != null)
                {
                    final Class<?> resultType = setter.getParameterTypes()[0];
                    if (resultType == String.class)
                    {
                        valueOrNull = propertyValueOrNull;
                    } else if (resultType == boolean.class || resultType == Boolean.class)
                    {
                        valueOrNull = Boolean.parseBoolean(propertyValueOrNull);
                    } else if (resultType == int.class || resultType == Integer.class)
                    {
                        valueOrNull = Integer.parseInt(propertyValueOrNull);
                    } else if (resultType == float.class || resultType == Float.class)
                    {
                        valueOrNull = Float.parseFloat(propertyValueOrNull);
                    } else if (resultType == long.class || resultType == Long.class)
                    {
                        valueOrNull = Long.parseLong(propertyValueOrNull);
                    } else if (resultType == double.class || resultType == Double.class)
                    {
                        valueOrNull = Double.parseDouble(propertyValueOrNull);
                    } else if (resultType == short.class || resultType == Short.class)
                    {
                        valueOrNull = Short.parseShort(propertyValueOrNull);
                    } else if (resultType == byte.class || resultType == Byte.class)
                    {
                        valueOrNull = Byte.parseByte(propertyValueOrNull);
                    }
                }
            }
            if (valueOrNull != null)
            {
                setter.invoke(destination, new Object[]
                { valueOrNull });
            }
        }
    }

    private static <T> void copyBean(final T destination, final Map<Object, Object> repository,
            final Object source, final Converter converter) throws IllegalAccessException,
            InvocationTargetException
    {
        if (destination == null)
        {
            return;
        }
        final Collection<Method> destinationSetters =
                scanForPublicMethods(destination, SETTER_PREFIX, 1).values();
        final Map<String, Method> destinationGetters =
                scanForPublicMethods(destination, GETTER_PREFIX, 0);
        final Map<String, Method> sourceGetters = scanForPublicMethods(source, GETTER_PREFIX, 0);
        scanForPublicMethods(source, sourceGetters, BOOLEAN_GETTER_PREFIX, 0, boolean.class,
                Boolean.class);
        for (final Method setter : destinationSetters)
        {
            final T newBean =
                    emergeNewBean(setter, source, repository, destination, sourceGetters,
                            destinationGetters, converter);
            if (newBean != null)
            {
                try
                {
                    setter.invoke(destination, new Object[]
                    { newBean });
                } catch (final IllegalArgumentException ex)
                {
                    final String defaultJavaArgumentTypeMismatchMessage = "argument type mismatch";
                    if (defaultJavaArgumentTypeMismatchMessage.equals(ex.getMessage()))
                    {
                        throw new IllegalArgumentException(defaultJavaArgumentTypeMismatchMessage
                                + ": method '" + setter.toGenericString()
                                + "': cannot assign from '" + newBean.getClass().getCanonicalName()
                                + "'.");
                    } else
                    {
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Emerges the new bean.
     * <p>
     * The logic of this method is the following:
     * <ol>
     * <li>If a converter could be found for transferring the value from source bean to destination bean, then use it</li>
     * <li>If the value is of primitive type or one of the immutable types specified, then use it tel quel</li>
     * <li>If the value is a complexe type, then it should be filled using
     * {@link #fillBean(Class, Object, Map, Object, ch.systemsx.cisd.common.reflection.BeanUtils.AnnotationMap, ch.systemsx.cisd.common.reflection.BeanUtils.Converter)}
     * before using it</li>
     * </ol>
     * </p>
     */
    @SuppressWarnings("unchecked")
    private static <T> T emergeNewBean(final Method setter, final Object source,
            final Map<Object, Object> repository, final T destination,
            final Map<String, Method> sourceGetters, final Map<String, Method> destinationGetters,
            final Converter converter) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        final AnnotationMap annotationMap = new SetterAnnotationMap(setter);
        final Method converterMethod = getConverterMethod(setter, source, converter);
        if (converterMethod != null)
        {
            return (T) converterMethod.invoke(converter, new Object[]
            { source });
        }
        final Object oldBean = getOldBean(setter, sourceGetters, source);
        if (oldBean == null)
        {
            return null;
        }
        final Class<T> parameterType = (Class<T>) setter.getParameterTypes()[0];
        if (parameterType.isPrimitive() || immutableTypes.contains(parameterType)
                || parameterType.isEnum() || oldBean.getClass().isAssignableFrom(parameterType))
        {
            return (T) oldBean;
        } else
        {
            // If a non-null value could be found in the destination bean for
            // <code>destinationOldBean</code>,
            // then take it.
            final T destinationOldBean = (T) getOldBean(setter, destinationGetters, destination);
            return fillBean(parameterType, destinationOldBean, repository, oldBean, annotationMap,
                    converter);
        }
    }

    /**
     * For given <var>setter</var> method, we try to get the corresponding <i>getter</i>. If this step succeeds, we invoke the found <i>getter</i> on
     * given <var>bean</var> and returns the value.
     */
    private final static Object getOldBean(final Method setter, final Map<String, Method> getters,
            final Object bean) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        final Method getter = getGetter(setter, getters);
        if (getter == null)
        {
            return null;
        }
        final boolean isAccessible = getter.isAccessible();
        if (isAccessible == false)
        {
            getter.setAccessible(true);
        }
        try
        {
            final Object oldBean = getter.invoke(bean, ArrayUtils.EMPTY_OBJECT_ARRAY);
            return oldBean;
        } finally
        {
            if (isAccessible == false)
            {
                getter.setAccessible(false);
            }
        }
    }

    private final static Map<Class<?>, Collection<Class<?>>> sourceBeanClassesCache = Collections
            .synchronizedMap(new HashMap<Class<?>, Collection<Class<?>>>());

    private final static Map<Class<?>, Method[]> converterMethodsCache = Collections
            .synchronizedMap(new HashMap<Class<?>, Method[]>());

    private static Method getConverterMethod(final Method setter, final Object sourceBean,
            final Converter converter)
    {
        if (converter != NULL_CONVERTER)
        {
            final String methodName =
                    "convertTo" + setter.getName().substring(SETTER_PREFIX.length());
            final Class<? extends Converter> converterClass = converter.getClass();
            final Class<?> beanClass = sourceBean.getClass();
            Collection<Class<?>> classes = sourceBeanClassesCache.get(beanClass);
            if (classes == null)
            {
                classes = ClassUtils.gatherAllCastableClassesAndInterfacesFor(sourceBean);
                sourceBeanClassesCache.put(beanClass, classes);
            }
            for (final Class<?> clazz : classes)
            {
                Method[] methods = converterMethodsCache.get(converterClass);
                if (methods == null)
                {
                    methods = converterClass.getMethods();
                    converterMethodsCache.put(converterClass, methods);
                }
                for (final Method method : methods)
                {
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    if (methodName.equals(method.getName()) && parameterTypes.length == 1
                            && parameterTypes[0].equals(clazz))
                    {
                        if (method.isAccessible() == false)
                        {
                            method.setAccessible(true);
                        }
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private static Method getGetter(final Method setter, final Map<String, Method> sourceGetters)
    {
        final String propertyName = setter.getName().substring(SETTER_PREFIX.length());
        final Method getter = sourceGetters.get(GETTER_PREFIX + propertyName);
        if (getter != null)
        {
            return getter;
        } else
        {
            final Class<?> type = setter.getParameterTypes()[0];
            // If parameter type is boolean, then try with 'is' as prefix
            if (type == boolean.class || type == Boolean.class)
            {
                return sourceGetters.get(BOOLEAN_GETTER_PREFIX + propertyName);
            }
        }
        return null;
    }

    private static Map<String, Method> scanForPublicMethods(final Object bean, final String prefix,
            final int numberOfParameters)
    {
        final Map<String, Method> methodMap = new LinkedHashMap<String, Method>();
        scanForPublicMethods(bean, methodMap, prefix, numberOfParameters, (Set<Class<?>>) null);
        return methodMap;
    }

    private static void scanForPublicMethods(final Object bean,
            final Map<String, Method> methodMap, final String prefix, final int numberOfParameters,
            final Class<?>... returnValueTypes)
    {
        final List<Class<?>> list = Arrays.asList(returnValueTypes);
        scanForPublicMethods(bean, methodMap, prefix, numberOfParameters,
                new LinkedHashSet<Class<?>>(list));
    }

    private static void scanForPublicMethods(final Object bean,
            final Map<String, Method> methodMap, final String prefix, final int numberOfParameters,
            final Set<Class<?>> returnValueTypes)
    {
        for (final Method method : bean.getClass().getMethods())
        {
            final String methodName = method.getName();
            if (methodName.startsWith(prefix)
                    && method.getParameterTypes().length == numberOfParameters
                    && Modifier.isPublic(method.getModifiers()))
            {
                if (returnValueTypes == null || returnValueTypes.contains(method.getReturnType()))
                {
                    methodMap.put(methodName, method);
                }
            }
        }
    }

    /**
     * Returns a map of <code>PropertyDescriptor</code>s keyed by {@link PropertyDescriptor#getName()}.
     * <p>
     * It introspects given class and remove each (bean) property that does not have a write method (like <code>class</code>).
     * </p>
     */
    public final static Map<String, PropertyDescriptor> getPropertyDescriptors(final Class<?> clazz)
    {
        try
        {
            final Map<String, PropertyDescriptor> map =
                    new LinkedHashMap<String, PropertyDescriptor>();
            final List<PropertyDescriptor> descriptors =
                    new ArrayList<PropertyDescriptor>(Arrays.asList(Introspector.getBeanInfo(clazz)
                            .getPropertyDescriptors()));
            for (final Iterator<PropertyDescriptor> iter = descriptors.iterator(); iter.hasNext(); /**/)
            {
                final PropertyDescriptor descriptor = iter.next();
                // If no write method, remove it. For instance 'class' property does not have any
                // write method.
                if (descriptor.getWriteMethod() != null)
                {
                    // Put the descriptor name.
                    map.put(descriptor.getName(), descriptor);
                }
            }
            return map;
        } catch (final IntrospectionException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    /**
     * Returns a deeply cloned java bean.
     * <p>
     * NOTE: Throws an exception one of the related objects is not serializable.
     * 
     * @param bean java bean to be cloned.
     * @return a new java bean cloned from fromBean.
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T bean) throws IOException, ClassNotFoundException
    {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(bean);
            oos.flush();
            ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return (T) ois.readObject();
        } finally
        {
            if (oos != null)
            {
                oos.close();
            }
            if (ois != null)
            {
                ois.close();
            }
        }
    }
}
