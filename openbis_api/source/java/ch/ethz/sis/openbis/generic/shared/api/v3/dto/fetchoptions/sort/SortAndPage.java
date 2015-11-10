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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.view.AbstractCollectionView;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.view.ListView;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.view.SetView;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "rawtypes", "unchecked", "cast" })
public class SortAndPage
{

    private Set processed = new HashSet();

    private MethodsCache methodsCache = new MethodsCache();

    public <T, C extends Collection<T>> C sortAndPage(C objects, FetchOptions fo)
    {
        C newObjects = objects;

        if (objects instanceof AbstractCollectionView)
        {
            newObjects = (C) ((AbstractCollectionView) objects).getOriginalCollection();
        }

        newObjects = (C) sort(newObjects, fo);
        newObjects = (C) page(newObjects, fo);
        nest(newObjects, fo);

        return newObjects;
    }

    private Collection sort(Collection objects, FetchOptions fo)
    {
        if (objects == null || objects.isEmpty())
        {
            return objects;
        }

        boolean hasSorting = fo.getSortBy() != null && false == fo.getSortBy().getSortings().isEmpty();

        if (hasSorting)
        {
            Comparator comparator = getComparator(fo);
            Collection sorted = null;

            if (objects instanceof List)
            {
                sorted = new ArrayList(objects);
                Collections.sort((List) sorted, comparator);
            } else if (objects instanceof Set)
            {
                List temp = new ArrayList(objects);
                Collections.sort(temp, comparator);
                // if TreeSet was used then the comparator would be also serialized
                sorted = new LinkedHashSet(temp);
            } else if (objects instanceof Collection)
            {
                sorted = new ArrayList(objects);
                Collections.sort((List) sorted, comparator);
            }

            return sorted;
        } else
        {
            return objects;
        }
    }

    private Collection page(Collection objects, FetchOptions fo)
    {
        if (objects == null || objects.isEmpty())
        {
            return objects;
        }

        boolean hasPaging = fo.getFrom() != null || fo.getCount() != null;

        if (hasPaging)
        {
            Collection paged = null;

            if (objects instanceof List)
            {
                paged = new ListView(objects, fo.getFrom(), fo.getCount());
            } else if (objects instanceof Set)
            {
                paged = new SetView(objects, fo.getFrom(), fo.getCount());
            } else if (objects instanceof Collection)
            {
                paged = new ListView(objects, fo.getFrom(), fo.getCount());
            } else
            {
                throw new IllegalArgumentException("Unsupported collection: " + objects.getClass());
            }

            return paged;
        } else
        {
            return objects;
        }
    }

    private void nest(Collection objects, FetchOptions fo)
    {
        if (objects == null || objects.isEmpty())
        {
            return;
        }

        FetchOptionsMethods foMethods = methodsCache.getFetchOptionsMethods(fo);

        for (Object object : objects)
        {
            if (processed.contains(object))
            {
                continue;
            } else
            {
                processed.add(object);
            }

            ObjectMethods objectMethods = methodsCache.getObjectMethods(object);

            for (String fieldName : foMethods.getFieldNames())
            {
                try
                {
                    Method hasMethod = foMethods.getHasMethod(fieldName);
                    boolean has = (Boolean) hasMethod.invoke(fo);

                    if (has)
                    {
                        Method withMethod = foMethods.getWithMethod(fieldName);
                        FetchOptions subFo = (FetchOptions) withMethod.invoke(fo);

                        Method getMethod = objectMethods.getGetMethod(fieldName);
                        Method setMethod = objectMethods.getSetMethod(fieldName);

                        Object value = getMethod.invoke(object);

                        if (value != null)
                        {
                            if (value instanceof Collection)
                            {
                                Collection newValue = sortAndPage((Collection) value, subFo);
                                setMethod.invoke(object, newValue);
                            } else if (value instanceof Map)
                            {
                                sortAndPage(((Map) value).values(), subFo);
                            } else
                            {
                                Collection newValue = sortAndPage(Collections.singleton(value), subFo);
                                setMethod.invoke(object, newValue.iterator().next());
                            }
                        }
                    }
                } catch (Exception e)
                {
                    throw new RuntimeException("Sorting and paging failed for object: + " + object + " and fieldName: " + fieldName, e);
                }
            }
        }
    }

    protected Comparator getComparator(FetchOptions fetchOptions)
    {
        if (fetchOptions == null)
        {
            return null;
        }

        SortOptions sortBy = (SortOptions) fetchOptions.getSortBy();

        if (sortBy != null)
        {
            final List<Sorting> sortings = sortBy.getSortings();
            if (sortings != null && sortings.size() > 0)
            {
                final Comparator[] comparators = new Comparator[sortings.size()];
                final int[] directions = new int[sortings.size()];

                int index = 0;
                for (Sorting sorting : sortings)
                {
                    if (sorting.getField() != null)
                    {
                        Comparator aComparator = sortBy.getComparator(sorting.getField());
                        if (aComparator == null)
                        {
                            throw new IllegalArgumentException("Comparator for field " + sorting.getField() + " not found");
                        }

                        comparators[index] = aComparator;
                        directions[index] = sorting.getOrder().isAsc() ? 1 : -1;
                        index++;
                    }
                }

                return new Comparator()
                    {
                        @Override
                        public int compare(Object o1, Object o2)
                        {
                            for (int i = 0; i < sortings.size(); i++)
                            {
                                Comparator c = comparators[i];
                                int d = directions[i];

                                int result = d * c.compare(o1, o2);
                                if (result != 0)
                                {
                                    return result;
                                }
                            }
                            return 0;
                        }
                    };
            }
        }
        return null;
    }

    private class MethodsCache
    {

        private Map<Class, Object> cache = new HashMap<Class, Object>();

        public FetchOptionsMethods getFetchOptionsMethods(Object fo)
        {
            FetchOptionsMethods methods = (FetchOptionsMethods) cache.get(fo.getClass());

            if (methods == null)
            {
                methods = new FetchOptionsMethods(fo.getClass());
                cache.put(fo.getClass(), methods);
            }

            return methods;
        }

        public ObjectMethods getObjectMethods(Object object)
        {
            ObjectMethods methods = (ObjectMethods) cache.get(object.getClass());

            if (methods == null)
            {
                methods = new ObjectMethods(object.getClass());
                cache.put(object.getClass(), methods);
            }

            return methods;
        }

    }

    private class FetchOptionsMethods
    {

        private Collection<String> fieldNames = new HashSet<String>();

        private Map<String, Method> hasMethods = new HashMap<String, Method>();

        private Map<String, Method> withMethods = new HashMap<String, Method>();

        public FetchOptionsMethods(Class clazz)
        {
            try
            {
                for (Method method : clazz.getMethods())
                {
                    if (method.getName().startsWith("has") && false == method.getName().equals("hashCode"))
                    {
                        String fieldName = method.getName().substring(3);

                        hasMethods.put(fieldName, method);

                        Method withMethod = clazz.getMethod("with" + fieldName);
                        withMethods.put(fieldName, withMethod);

                        fieldNames.add(fieldName);
                    }
                }
            } catch (Exception e)
            {
                throw new RuntimeException("Finding methods for fetch options class: " + clazz.getName() + " failed.", e);
            }
        }

        public Collection<String> getFieldNames()
        {
            return fieldNames;
        }

        public Method getHasMethod(String fieldName)
        {
            return hasMethods.get(fieldName);
        }

        public Method getWithMethod(String fieldName)
        {
            return withMethods.get(fieldName);
        }

    }

    private class ObjectMethods
    {

        private Map<String, Method> getMethods = new HashMap<String, Method>();

        private Map<String, Method> setMethods = new HashMap<String, Method>();

        public ObjectMethods(Class clazz)
        {
            PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(clazz);

            for (PropertyDescriptor descriptor : descriptors)
            {
                String fieldName = descriptor.getName().substring(0, 1).toUpperCase() + descriptor.getName().substring(1);
                getMethods.put(fieldName, descriptor.getReadMethod());
                setMethods.put(fieldName, descriptor.getWriteMethod());
            }
        }

        public Method getGetMethod(String fieldName)
        {
            return getMethods.get(fieldName);
        }

        public Method getSetMethod(String fieldName)
        {
            return setMethods.get(fieldName);
        }

    }

}
