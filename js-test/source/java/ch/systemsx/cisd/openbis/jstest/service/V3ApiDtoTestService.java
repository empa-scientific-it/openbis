/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.jstest.service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.ICustomASServiceExecutor;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.context.CustomASServiceContext;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper;

/**
 * @author Franz-Josef Elmer
 */
public class V3ApiDtoTestService implements ICustomASServiceExecutor
{
    private static final String PACKAGE_PREFIX = "ch.ethz.sis.";

    public V3ApiDtoTestService(Properties properties)
    {
    }

    @Override
    public Object executeService(CustomASServiceContext context, CustomASServiceExecutionOptions options)
    {
        Map<String, Object> parameters = options.getParameters();
        System.out.println("PARAMETERS:");
        Object obj = parameters.get("object");
        boolean echo = parameters.containsKey("echo");
        String name = obj.getClass().getName();
        System.out.println("echo: " + echo + ", object: " + obj + " (" + name + ")");
        if (name.startsWith(PACKAGE_PREFIX) == false)
        {
            if (name.startsWith("java.") && name.endsWith("Map"))
            {
                throw new IllegalArgumentException("Map class detected (" + name + "). This is a hint "
                        + "that no appropriate class found for deserialization. "
                        + "This is most probably caused by missing default constructor. Object: " + obj);
            }
            throw new IllegalArgumentException("Fully qualified class named doesn't start with '" + PACKAGE_PREFIX
                    + "': " + name + ". Object: " + obj);
        }
        return echo ? obj : populate(obj);
    }

    public static Object populate(Object obj)
    {
        List<String> ignore = new ArrayList<>();
        for (Method ign : Object.class.getMethods())
        {
            ignore.add(ign.getName());
        }

        List<String> methds = new ArrayList<>();

        for (Method method : obj.getClass().getMethods())
        {
            if (ignore.contains(method.getName()))
            {
                continue;
            }
            methds.add(method.getName());

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1)
            {
                Class<?> type = parameterTypes[0];
                // remove this if, we also want to set complex ones to null and see if they still work
                if (type.isPrimitive() || type.equals(String.class) || type.equals(Date.class))
                {
                    try
                    {
                        setItUp(obj, method, type);
                    } catch (Exception e)
                    {
                        // return method.getName() + " " + getValue(type);
                        // e.printStackTrace();
                    }
                }
            } else
            {

            }
        }
        return obj;
    }

    public static void setItUp(Object obj, Method method, Class<?> type) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException
    {
        method.invoke(obj, getValue(type));
    }

    public static Object getValue(Class<?> type)
    {
        double random = Math.random();
        long rnd = (long) (random * 1000000);
        if (type == String.class)
        {
            return String.valueOf(random);
        }

        if (type == Date.class)
        {
            return new Date(rnd);
        }

        if (type == Long.class || type == Long.TYPE)
        {
            return rnd;
        }

        if (type == Integer.class || type == Integer.TYPE)
        {
            return (int) rnd;
        }

        if (type == Boolean.class || type == Boolean.TYPE)
        {
            return random < 0.5;
        }
        System.out.println("Complex type: " + type.getName());

        return null;
    }

    public static void main(String[] args)
    {
        GenericObjectMapper gom = new GenericObjectMapper();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            Object readValue2 =
                    gom.readValue(
                            "{\"@id\":2,\"fieldName\":\"anything\",\"fieldType\":\"ANY_FIELD\",\"fieldValue\":null,\"@type\":\"as.dto.global.search.GlobalSearchTextCriteria\"}",
                            GlobalSearchTextCriteria.class);
            Object populate = populate(readValue2);
            gom.writeValue(out, populate);
            String json = out.toString();
            System.out.println(json);

            Object readValue =
                    gom.readValue(
                            "{\"@id\":2,\"fieldName\":\"anything\",\"fieldType\":\"ANY_FIELD\",\"fieldValue\":null,\"@type\":\"as.dto.global.search.GlobalSearchTextCriteria\"}",
                            GlobalSearchTextCriteria.class);
            System.out.println(readValue);
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
