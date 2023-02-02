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

package ch.systemsx.cisd.common.test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for checking an interface or a class to be ready for RMI.
 * 
 * @author Franz-Josef Elmer
 */
public class InterfaceForRMIChecker
{
    /**
     * Asserts that the specified interface is ready for RMI. That is, for all methods the parameter types and the return type is {@link Serializable}
     * .
     * 
     * @throws AssertionError if <code>interfaze</code> is not an interface or a parameter type or a return value is not serializable.
     */
    public static void assertInterfaceForRMI(Class<?> interfaze)
    {
        assert interfaze != null : "Unspecified interface.";
        assert interfaze.isInterface() : "Not an interface " + interfaze;

        Set<Class<?>> visitedClasses = new HashSet<Class<?>>();
        Method[] methods = interfaze.getMethods();
        for (Method method : methods)
        {
            assertMethodForRMI(method, visitedClasses);
        }
    }

    private static void assertMethodForRMI(Method method, Set<Class<?>> visitedClasses)
    {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes)
        {
            assertSerializable(parameterType, visitedClasses, true);
        }
        Class<?> returnType = method.getReturnType();
        if (Void.class.isAssignableFrom(returnType) == false)
        {
            assertSerializable(returnType, visitedClasses, true);
        }
    }

    /**
     * Asserts that the specified class is serializable.
     * 
     * @throws AssertionError if <code>clazz</code> is not serializable or a non-transient non-static attribute isn't serializable.
     */
    public static void assertSerializable(Class<?> clazz)
    {
        assertSerializable(clazz, new HashSet<Class<?>>(), true);
    }

    private static void assertSerializable(Class<?> clazz, Set<Class<?>> visitedClasses,
            boolean checkImplementsSerializable)
    {
        assert clazz != null : "Unspecified class.";
        if (clazz.isPrimitive() || visitedClasses.contains(clazz))
        {
            return;
        }
        visitedClasses.add(clazz);
        if (clazz.isArray())
        {
            assertSerializable(clazz.getComponentType(), visitedClasses, true);
        } else
        {
            if (checkImplementsSerializable)
            {
                assert Serializable.class.isAssignableFrom(clazz) : clazz
                        + " does not implement java.io.Serializable";
            }
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields)
            {
                int modifiers = field.getModifiers();
                boolean isNative = Modifier.isNative(modifiers);
                boolean isStatic = Modifier.isStatic(modifiers);
                boolean isTransient = Modifier.isTransient(modifiers);
                if (isNative == false && isStatic == false && isTransient == false)
                {
                    Class<?> attributeClass = field.getType();
                    if (attributeClass.isInterface() == false)
                    {
                        assertSerializable(attributeClass, visitedClasses, true);
                    } else
                    {
                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType)
                        {
                            ParameterizedType pt = (ParameterizedType) genericType;
                            Type[] actualTypeArguments = pt.getActualTypeArguments();
                            for (Type type : actualTypeArguments)
                            {
                                if (type instanceof Class<?>)
                                {
                                    assertSerializable((Class<?>) type, visitedClasses, true);
                                }
                            }
                        }
                    }
                }
            }
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass.equals(Object.class) == false)
            {
                assertSerializable(superclass, visitedClasses, false);
            }
        }
    }
}
