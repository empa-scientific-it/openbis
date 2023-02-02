/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class which allows to record method invocations.
 * 
 * @author Franz-Josef Elmer
 */
public class InvocationRecordingWrapper<T>
{
    /**
     * Creates an instance which contains a proxy of the specified object.
     * 
     * @param type Interface the specified object implements. The proxy will implement the same interface.
     * @param returnTypesToWrap Interfaces return values of invocations might implement. Invocations on such return values will also be recorded. This
     *            is done recursively. If the return value is an array or a list of such types the elements of the returned array/list will also be
     *            wrapped.
     */
    @SuppressWarnings("unchecked")
    public static <T> InvocationRecordingWrapper<T> wrap(final T object, Class<T> type,
            final Class<?>... returnTypesToWrap)
    {
        final InvocationRecordingWrapper<T> wrapper = new InvocationRecordingWrapper<T>();
        wrapper.proxy = (T) createProxy(wrapper, null, object, type, returnTypesToWrap);
        return wrapper;
    }

    private static Object createProxy(final InvocationRecordingWrapper<?> wrapper,
            final String prefix, final Object object, Class<?> type,
            final Class<?>... returnTypesToWrap)
    {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]
        { type }, new InvocationHandler()
            {
                @Override
                public Object invoke(Object obj, Method method, Object[] parameters)
                        throws Throwable
                {
                    String record = wrapper.record(prefix, method, parameters);
                    Object returnValue = method.invoke(object, parameters);
                    if (returnValue != null)
                    {
                        Class<?> returnValueClass = returnValue.getClass();
                        Class<?> matchingClass =
                                tryGetMatchingClass(returnValueClass, returnTypesToWrap);
                        if (matchingClass != null)
                        {
                            return createProxy(wrapper, record, returnValue, matchingClass,
                                    returnTypesToWrap);
                        }
                        if (returnValueClass.isArray())
                        {
                            return handleArray(wrapper, record, returnValue, returnValueClass,
                                    returnTypesToWrap);
                        }
                        if (returnValue instanceof List)
                        {
                            return handleList(wrapper, record, returnValue, returnTypesToWrap);
                        }
                    }
                    return returnValue;
                }
            });
    }

    private static Object handleList(final InvocationRecordingWrapper<?> wrapper, String prefix,
            Object returnValue, final Class<?>... returnTypesToWrap)
    {
        List<?> returnList = (List<?>) returnValue;
        if (returnList.isEmpty())
        {
            return returnValue;
        }
        List<Object> result = new ArrayList<Object>(returnList.size());
        for (int i = 0; i < returnList.size(); i++)
        {
            Object element = returnList.get(i);
            Class<?> mc = tryGetMatchingClass(element.getClass(), returnTypesToWrap);
            if (mc == null)
            {
                result.add(element);
            } else
            {
                result.add(createProxy(wrapper, prefix + ".get(" + i + ")", element, mc,
                        returnTypesToWrap));
            }
        }
        return result;
    }

    private static Object handleArray(final InvocationRecordingWrapper<?> wrapper, String prefix,
            Object returnValue, Class<?> returnValueClass, final Class<?>... returnTypesToWrap)
    {
        Class<?> componentType = returnValueClass.getComponentType();
        Class<?> clazz = tryGetMatchingClass(componentType, returnTypesToWrap);
        if (clazz == null)
        {
            return returnValue;
        }
        int size = Array.getLength(returnValue);
        Object newArray = Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++)
        {
            Array.set(
                    newArray,
                    i,
                    createProxy(wrapper, prefix + "[" + i + "]", Array.get(returnValue, i), clazz,
                            returnTypesToWrap));
        }
        return newArray;
    }

    private static Class<?> tryGetMatchingClass(Class<?> clazz, Class<?>... classes)
    {
        for (Class<?> c : classes)
        {
            if (c.isAssignableFrom(clazz))
            {
                return c;
            }
        }
        return null;
    }

    private List<String> records = new ArrayList<String>();

    private T proxy;

    private InvocationRecordingWrapper()
    {
    }

    /**
     * Returns the proxy of the original object.
     */
    public T getProxy()
    {
        return proxy;
    }

    /**
     * Returns all recorded invocations as a list.
     */
    public List<String> getRecords()
    {
        return records;
    }

    /**
     * Returns all recorded invocations as a multi-line text where each line contains an invocation.
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (String record : records)
        {
            if (builder.length() > 0)
            {
                builder.append("\n");
            }
            builder.append(record);
        }
        return builder.toString();
    }

    private String record(String prefix, Method method, Object[] parameters)
    {
        StringBuilder builder = new StringBuilder();
        if (parameters != null)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            for (Object parameter : parameters)
            {
                builder.append(parameter);
            }
        }
        String record =
                (prefix == null ? "" : prefix + ".") + method.getName() + "(" + builder + ")";
        records.add(record);
        return record;
    }
}
