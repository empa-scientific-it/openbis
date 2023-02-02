/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.api.server.json.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;

import com.google.common.base.Predicate;

/**
 * A utility class that searches for classes and interfaces that are potentially converted in JSON form.
 * 
 * @author anttil
 */
public class ClassReferences
{

    public static Reflections ref = new Reflections("ch");

    /**
     * Returns all the classes and interfaces that are referenced by the public methods declared by the given class. A class considered to be
     * referenced by a method if either of the following holds 1) The class or its superclass or an interface it implements is mentioned in the
     * signature of the method as an argument or as a return type either directly or as a type of an array or a collection. 2) The class or its
     * superclass or an interface it implements is used as a return type of a getter method in a class referenced (note recursion here) by a method.
     * However, the following kind of classes never considered to be referenced: 1) Anonymous classes 2) Classes that are not defined within package
     * "ch."
     * 
     * @param clazz class whose references should be searched.
     * @returns referenced classes
     */
    public static Collection<Class<?>> search(Class<?> clazz, Predicate<Class<?>> filter)
    {
        Set<Class<?>> results = new HashSet<Class<?>>();
        for (Method method : clazz.getDeclaredMethods())
        {
            searchIfMethodIsPublic(method, results);
        }
        if (filter != null)
        {
            Set<Class<?>> filteredResults = new HashSet<Class<?>>();
            for (Class<?> result : results)
            {
                if (filter.apply(result))
                {
                    filteredResults.add(result);
                }
            }
            results = filteredResults;
        }
        return results;
    }

    private static void searchIfMethodIsPublic(Method method, Set<Class<?>> handled)
    {
        if (Modifier.isPublic(method.getModifiers()))
        {
            handled.addAll(search(method, handled));
        }
    }

    private static Set<Class<?>> search(Method method, Set<Class<?>> handled)
    {
        for (Class<?> clazz : getClassesReferencedBy(method))
        {
            searchIfClassIsInteresting(clazz, handled);
        }
        return handled;
    }

    private static void searchIfClassIsInteresting(Class<?> clazz, Set<Class<?>> handled)
    {
        if (interesting(clazz, handled))
        {
            handled.add(clazz);
            handled.addAll(search(clazz, handled));
        }
    }

    private static Set<Class<?>> search(Class<?> clazz, Set<Class<?>> handled)
    {
        for (Method method : clazz.getMethods())
        {
            searchIfMethodIsGetter(method, handled);
        }
        for (Class<?> subclass : ref.getSubTypesOf(clazz))
        {
            searchIfClassIsInteresting(subclass, handled);
        }
        return handled;
    }

    private static void searchIfMethodIsGetter(Method method, Set<Class<?>> handled)
    {
        if (method.getName().startsWith("get") && method.getParameterTypes().length == 0)
        {
            handled.addAll(search(method, handled));
        }
    }

    private static Set<Class<?>> getClassesReferencedBy(Method method)
    {
        Set<Class<?>> references = new HashSet<Class<?>>();
        references.add(method.getReturnType());
        references.addAll(Arrays.asList(method.getParameterTypes()));
        references.addAll(classesFromTypes(method.getGenericReturnType()));
        references.addAll(classesFromTypes(method.getGenericParameterTypes()));

        Set<Class<?>> arrayTypes = new HashSet<Class<?>>();
        for (Class<?> clazz : references)
        {
            arrayTypes.add(clazz.getComponentType());
        }
        references.addAll(arrayTypes);
        return references;
    }

    private static boolean interesting(Class<?> clazz, Set<Class<?>> handled)
    {
        return (clazz != null) &&
                (!clazz.isArray()) &&
                (!clazz.isAnonymousClass()) &&
                (!clazz.isPrimitive()) &&
                (clazz.getPackage().getName().startsWith("ch")) &&
                (!handled.contains(clazz));
    }

    private static Set<Class<?>> classesFromTypes(Type... types)
    {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Type type : types)
        {
            Collection<Type> parameters = getActualTypeArguments(type);
            classes.addAll(getClassesFromTypeArguments(parameters));
        }
        return classes;
    }

    private static Collection<Type> getActualTypeArguments(Type type)
    {
        if (type instanceof ParameterizedType)
        {
            return new HashSet<Type>(Arrays.asList(((ParameterizedType) type)
                    .getActualTypeArguments()));
        } else
        {
            return Collections.emptySet();
        }
    }

    private static Collection<Class<?>> getClassesFromTypeArguments(Collection<Type> types)
    {
        Collection<Class<?>> classes = new HashSet<Class<?>>();
        for (Type type : types)
        {
            classes.addAll(getClass(type));
        }
        return classes;
    }

    private static Collection<? extends Class<?>> getClass(Type type)
    {
        String name = type.toString();
        if (name.indexOf("ch.") == -1)
        {
            return Collections.emptySet();
        }

        name = name.substring(name.indexOf("ch"));
        if (name.indexOf(">") != -1)
        {
            name = name.substring(0, name.indexOf(">"));
        }

        try
        {
            return Collections.singleton(Class.forName(name));
        } catch (ClassNotFoundException ex)
        {
            throw new IllegalArgumentException("Cannot find class " + name + " from type "
                    + type.toString());
        }
    }
}
