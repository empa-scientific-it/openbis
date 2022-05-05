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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Operations on classes using reflection.
 * 
 * @author Christian Ribeaud
 */
public final class ClassUtils
{
    private ClassUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Gathers all classes and interfaces the specified object can be casted to.
     */
    public final static Collection<Class<?>> gatherAllCastableClassesAndInterfacesFor(
            final Object object)
    {
        assert object != null : "Unspecified object";

        final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        classes.add(object.getClass());
        classes.addAll(getAllSuperclasses(object));
        classes.addAll(getAllInterfaces(object));
        return classes;
    }

    @SuppressWarnings("unchecked")
    private final static List<Class<?>> getAllInterfaces(final Object object)
    {
        assert object != null : "Unspecified object";
        return org.apache.commons.lang3.ClassUtils.getAllInterfaces(object.getClass());
    }

    @SuppressWarnings("unchecked")
    private final static List<Class<?>> getAllSuperclasses(final Object object)
    {
        assert object != null : "Unspecified object";
        return org.apache.commons.lang3.ClassUtils.getAllSuperclasses(object.getClass());
    }

    /**
     * Asserts that the specified class is an interface which has only methods with no return value.
     * 
     * @throws AssertionError if it isn't an interface or at least one method has a return value.
     */
    public static void assertInterfaceWithOnlyVoidMethods(Class<?> clazz)
    {
        assert clazz != null : "Unspecified class.";
        assert clazz.isInterface() : "Is not an interface: " + clazz.getName();
        Method[] methods = clazz.getMethods();
        for (Method method : methods)
        {
            Class<?> returnType = method.getReturnType();
            if (Void.TYPE.equals(returnType) == false)
            {
                throw new AssertionError("Method " + clazz.getName() + "." + method.getName()
                        + " has non-void return type: " + returnType.getName());
            }
        }
    }

    /**
     * Returns <code>true</code>, if the <var>clazz</var> has a constructor with the given <var>arguments</var>.
     */
    public final static <C> boolean hasConstructor(final Class<C> clazz, final Object... arguments)
    {
        try
        {
            return tryGetConstructor(clazz, getClasses(arguments)) != null;
        } catch (NoSuchMethodException ex)
        {
            return false;
        }
    }

    /**
     * Creates a new instance of a class specified by its fully-qualified name.
     * 
     * @param superClazz Super class <code>className</code> has to be implemented or extended.
     * @param clazz Fully-qualified class.
     * @param arguments Optional constructor arguments. If <code>(Object[])</code> is an empty array, then the default constructor will be used.
     * @return an instance of type <code>interface</code>.
     */
    public final static <T, C> T create(final Class<T> superClazz, final Class<C> clazz,
            final Object... arguments)
    {
        assert superClazz != null : "Missing super class";
        assert clazz != null : "Missing class name";

        Exception exception;
        try
        {
            assert clazz.isInterface() == false : "Interface '" + clazz.getName()
                    + "' can not be instanciated as it is an interface.";
            assert superClazz.isAssignableFrom(clazz) : "Class '" + clazz.getName()
                    + "' does not implements/extends '" + superClazz.getName() + "'.";
            if (arguments == null || arguments.length == 0)
            {
                return cast(clazz.newInstance());
            }
            final Class<?>[] classes = getClasses(arguments);
            final Constructor<T> constructor = tryGetConstructor(clazz, classes);
            if (constructor == null)
            {
                if (classes.length == 0)
                {
                    throw new IllegalArgumentException("No default constructor found for " + clazz);
                }
                throw new IllegalArgumentException("No constructor found for " + clazz
                        + " with arguments of the following types: " + Arrays.asList(classes));
            }
            return constructor.newInstance(arguments);
        } catch (final InstantiationException ex)
        {
            exception = ex;
        } catch (final IllegalAccessException ex)
        {
            exception = ex;
        } catch (final InvocationTargetException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex.getCause());
        } catch (final NoSuchMethodException ex)
        {
            exception = ex;
        }

        throw new IllegalArgumentException(String.format("Cannot instantiate class '%s' with given arguments '%s'.",
                clazz.getName(), Arrays.asList(arguments)), exception);
    }

    /**
     * Creates a new instance of a class specified by its fully-qualified name.
     * 
     * @param superClazz Super class <code>className</code> has to be implemented or extended.
     * @param className Fully-qualified class name.
     * @param arguments Optional constructor arguments. If <code>(Object[]) null</code> then the empty constructor will be used. Note that
     *            <code>(Object) null</code> is not interpreted as <code>null</code> arguments but rather as <code>new Object[]{null}</code>.
     * @return an instance of type <code>interface</code>.
     */
    public final static <T> T create(final Class<T> superClazz, final String className,
            final Object... arguments)
    {
        assert superClazz != null : "Missing super class";
        assert className != null : "Missing class name";

        try
        {
            return create(superClazz, Class.forName(className), arguments);
        } catch (final ClassNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(final Object newInstance)
    {
        return (T) newInstance;
    }

    private final static Class<?>[] getClasses(final Object... initargs)
    {
        final Class<?>[] classes = new Class<?>[initargs.length];
        int i = 0;
        for (final Object initarg : initargs)
        {
            classes[i++] = initarg.getClass();
        }
        return classes;
    }

    private final static <T> Constructor<T> tryGetConstructor(final Class<?> clazz,
            final Class<?>[] classes) throws NoSuchMethodException
    {
        final Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> returned = null;
        for (final Constructor<?> constructor : constructors)
        {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            final int len = parameterTypes.length;
            if (len != classes.length)
            {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < len; i++)
            {
                final Class<?> parameterType = parameterTypes[i];
                final Class<?> c = classes[i];
                match &= parameterType.equals(c) || parameterType.isAssignableFrom(c);
            }
            if (match)
            {
                returned = constructor;
            }
        }
        return toGenericType(returned);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> toGenericType(final Constructor<?> returned)
    {
        return (Constructor<T>) returned;
    }

    /**
     * Creates a new instance of given <var>clazz</var> and get rid of any checked exception.
     * <p>
     * Wraps any checked exception in a {@link CheckedExceptionTunnel}.
     * </p>
     */
    public final static <T> T createInstance(final Class<T> clazz)
    {
        try
        {
            return clazz.newInstance();
        } catch (final InstantiationException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (final IllegalAccessException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Invokes given <var>method</var> and get rid of any checked exception.
     * <p>
     * Wraps any checked exception in a {@link CheckedExceptionTunnel}.
     * </p>
     */
    public final static Object invokeMethod(final Method method, final Object obj,
            final Object... args)
    {
        try
        {
            return method.invoke(obj, args);
        } catch (final IllegalAccessException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } catch (final InvocationTargetException ex)
        {
            // We are interested in the cause exception.
            throw CheckedExceptionTunnel.wrapIfNecessary(ex.getCause());
        }
    }

    /**
     * Sets declared field named <var>fieldName</var> of given <var>object</var> to given new value <var>newValue</var>.
     * <p>
     * This is useful when you want to set a <code>private</code> field on which you do not have access. Note that this method should only be used in
     * very special cases. You should consider it as a hack.
     * </p>
     * 
     * @return a <code>true</code> if <code>fieldName</code> has been modified.
     */
    public final static boolean setFieldValue(final Object object, final String fieldName,
            final Object newValue)
    {
        assert object != null : "Unspecified object.";
        final Class<?> clazz = object.getClass();
        try
        {
            final Field field = tryGetDeclaredField(clazz, fieldName);
            if (field != null)
            {
                field.set(object, newValue);
                return true;
            }
        } catch (final SecurityException ex)
        {
        } catch (final IllegalArgumentException ex)
        {
        } catch (final IllegalAccessException ex)
        {
        } catch (final InaccessibleObjectException ex)
        {
        }
        return false;
    }

    /**
     * Gets declared field named <var>fieldName</var> in given class or superclass of it.
     * <p>
     * Before returning it, it call {@link Field#setAccessible(boolean)} with <code>true</code>.
     * </p>
     * 
     * @return <code>null</code> if given <var>fieldName</var> could not be found.
     */
    public final static Field tryGetDeclaredField(final Class<?> c, final String fieldName)
    {
        assert c != null : "Unspecified class.";
        assert StringUtils.isNotBlank(fieldName) : "Blank field name.";
        Field field = null;
        Class<?> clazz = c;
        do
        {
            try
            {
                field = clazz.getDeclaredField(fieldName);
            } catch (final SecurityException ex)
            {
            } catch (final NoSuchFieldException ex)
            {
            }
            clazz = clazz.getSuperclass();
        } while (field == null && clazz != null);
        if (field != null)
        {
            field.setAccessible(true);
        }
        return field;
    }

    /**
     * Creates an array of given {@link Class} type and of given <var>len</var>.
     */
    @SuppressWarnings("unchecked")
    public final static <T> T[] createArray(final Class<T> clazz, final int len)
    {
        assert clazz != null : "Unspecified class.";
        assert len > -1 : "Negative array length.";
        return (T[]) Array.newInstance(clazz, len);
    }

    /**
     * Lists all the classes in the given <var>packageName</var> that passes the given {@link IClassFilter}.
     * <p>
     * This method does not work recursively.
     * </p>
     */
    public final static List<Class<?>> listClasses(final String packageName,
            final IClassFilter classFilterOrNull)
    {
        assert packageName != null : "Unspecified package name.";
        assert packageName.endsWith(".") == false : "Remove the last dot from package name.";

        final IClassFilter classFilter =
                classFilterOrNull == null ? ClassFilterUtils.createTrueClassFilter()
                        : classFilterOrNull;
        final List<URL> urls = getUrls(packageName);
        final Map<URL, List<String>> classNamesByUrl = new HashMap<URL, List<String>>();
        for (final URL url : urls)
        {
            ArrayList<String> classNames = new ArrayList<String>();
            classNamesByUrl.put(url, classNames);
            final String protocol = url.getProtocol();
            if ("file".equals(protocol))
            {
                final List<File> classFiles = listClasses(toPackageFile(url));
                for (final File classFile : classFiles)
                {
                    final String className =
                            packageName + "." + FilenameUtils.getBaseName(classFile.getName());
                    classNames.add(className);
                }
            } else if ("jar".equals(protocol))
            {
                classNames.addAll(listEntries(toJarFile(url), packageName));
            } else
            {
                throw new UnsupportedOperationException(String.format("Protocol '%s' unsupported.",
                        protocol));
            }
        }
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Entry<URL, List<String>> entry : classNamesByUrl.entrySet())
        {
            List<String> classNames = entry.getValue();
            for (String className : classNames)
            {
                try
                {
                    if (classFilter.accept(className))
                    {
                        final Class<?> clazz =
                                org.apache.commons.lang3.ClassUtils.getClass(className, false);
                        if (classFilter.accept(clazz))
                        {
                            classes.add(clazz);
                        }
                    }
                } catch (final ClassNotFoundException ex)
                {
                    throw new CheckedExceptionTunnel(ex);
                } catch (Throwable ex)
                {
                    throw new IllegalArgumentException("Couldn't load class " + className
                            + " from resource " + entry.getKey() + ": " + ex, ex);
                }
            }
        }
        return classes;
    }

    @SuppressWarnings("unchecked")
    private final static List<File> listClasses(final File packageFile)
    {
        return (List<File>) FileUtils.listFiles(packageFile, new String[]
        { "class" }, false);
    }

    private final static List<String> listEntries(final JarFile jarFile, final String packageName)
    {
        final List<String> classNames = new ArrayList<String>();
        final String packageNamePath = packageName.replace('.', '/') + "/";
        final String classExtension = ".class";
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements())
        {
            final JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(packageNamePath)
                    && name.lastIndexOf('/') == packageNamePath.length() - 1
                    && name.endsWith(classExtension))
            {
                final String className =
                        name.substring(packageName.length() + 1, name.length()
                                - classExtension.length());
                classNames.add(packageName + "." + className);
            }
        }
        return classNames;
    }

    private final static List<URL> getUrls(final String packageName)
    {
        try
        {
            final List<URL> urls =
                    Collections.list(Thread.currentThread().getContextClassLoader().getResources(
                            packageName.replace(".", "/")));
            final int size = urls.size();
            if (size == 0)
            {
                throw new IllegalArgumentException(String.format(
                        "Given package '%s' does not exist.", packageName));
            }
            return urls;
        } catch (final IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    private final static JarFile toJarFile(final URL url)
    {
        assert url != null : "Unspecified URL.";
        assert "jar".equals(url.getProtocol()) : "Wrong protocol.";
        try
        {
            return ((JarURLConnection) url.openConnection()).getJarFile();
        } catch (final IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    private final static File toPackageFile(final URL url)
    {
        assert url != null : "Unspecified URL.";
        assert "file".equals(url.getProtocol()) : "Wrong protocol.";
        try
        {
            return new File(url.toURI());
        } catch (final URISyntaxException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

}