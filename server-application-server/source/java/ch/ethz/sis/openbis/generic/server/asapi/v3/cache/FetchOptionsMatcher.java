/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.cache;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchIgnore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchProperty;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.IFetchOptionsMatcher;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.IFetchPropertyHandler;

/**
 * @author pkupczyk
 */
public class FetchOptionsMatcher implements IFetchOptionsMatcher
{

    private Set<Pair> checked = new HashSet<Pair>();

    public boolean areMatching(Object o1, Object o2)
    {
        Pair pair = new Pair(o1, o2);

        // keep information which objects have been already checked not to loop forever on recursive fetch options

        if (checked.contains(pair))
        {
            return true;
        } else
        {
            checked.add(pair);
        }

        return arePartsAccessibleViaMethodsMatching(o1, o2) && arePartsAccessibleViaFieldsMatching(o1, o2);
    }

    private boolean arePartsAccessibleViaMethodsMatching(Object o1, Object o2)
    {
        Class<?> clazz = o1.getClass();
        Method method = null;

        try
        {
            Method[] methods = clazz.getMethods();

            for (int i = 0; i < methods.length; i++)
            {
                method = methods[i];
                method.setAccessible(true);

                if (method.getAnnotation(FetchIgnore.class) != null)
                {
                    continue;
                }

                if (method.getName().startsWith("has") && false == method.getName().equals("hashCode"))
                {
                    boolean has1 = (boolean) method.invoke(o1);
                    boolean has2 = (boolean) method.invoke(o2);

                    if (has1 != has2)
                    {
                        return false;
                    } else if (has1 && has2)
                    {
                        Method withMethod = clazz.getMethod("with" + method.getName().substring(3));

                        Object with1 = withMethod.invoke(o1);
                        Object with2 = withMethod.invoke(o2);

                        if (with1 != null && with2 != null && false == areMatching(with1, with2))
                        {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            throw new RuntimeException("Couldn't check if fetch options are matching for class: " + clazz + " and method: " + method, e);
        }

        return true;
    }

    private boolean arePartsAccessibleViaFieldsMatching(Object o1, Object o2)
    {
        Class<?> clazz = o1.getClass();
        Field field = null;

        try
        {
            Field[] fields = clazz.getDeclaredFields();

            for (int i = 0; i < fields.length; i++)
            {
                field = fields[i];
                field.setAccessible(true);

                FetchProperty annotation = field.getAnnotation(FetchProperty.class);

                if (annotation == null)
                {
                    continue;
                }

                Class<? extends IFetchPropertyHandler> handlerClass = annotation.handler();

                if (handlerClass == null)
                {
                    continue;
                }

                Constructor<? extends IFetchPropertyHandler> handlerConstructor = handlerClass.getDeclaredConstructor();
                handlerConstructor.setAccessible(true);

                IFetchPropertyHandler handler = handlerConstructor.newInstance();

                // pass "this" for the handler to be able to do nested matches and take advantage of the already "checked" set

                if (false == handler.areMatching(o1, o2, this))
                {
                    return false;
                }
            }

        } catch (Exception e)
        {
            throw new RuntimeException("Couldn't check if fetch options are matching for class: " + clazz + " and field: " + field, e);
        }

        return true;
    }

    private static class Pair
    {

        public Object object1;

        public Object object2;

        public Pair(Object object1, Object object2)
        {
            this.object1 = object1;
            this.object2 = object2;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((object1 == null) ? 0 : object1.hashCode());
            result = prime * result + ((object2 == null) ? 0 : object2.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Pair other = (Pair) obj;

            // regard objects as equal only if they are the same object in memory, otherwise check if parts are equal (i.e. do not assume equals is
            // properly implemented for all fetch options)

            return object1 == other.object1 && object2 == other.object2;
        }

    }

}
