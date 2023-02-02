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

package ch.systemsx.cisd.openbis.screening.systemtests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.base.Predicate;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.common.api.server.json.util.ClassReferences;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IWebInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.GenericObjectMapper;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.json.ScreeningObjectMapper;

/**
 * This class contains tests that make sure that Jackson annotations are used correctly and consistently in all the classes that are exposed through
 * openBIS JSON-RPC APIs.
 * 
 * @author anttil
 */
public class JsonAnnotationTest
{
    private Collection<Class<?>> allJsonClasses = new HashSet<Class<?>>();

    private Collection<Class<?>> empty = Collections.emptySet();

    private Map<String, Collection<Class<?>>> emptyMap =
            new HashMap<String, Collection<Class<?>>>();

    // Used by TestNG
    @SuppressWarnings("unused")
    @BeforeClass
    private void findAllClassesUsedByJsonRpcApi()
    {
        Class<?>[] jsonRpcInterfaces =
        { IDssServiceRpcGeneric.class, IScreeningApiServer.class,
                IGeneralInformationChangingService.class,
                IGeneralInformationService.class, IWebInformationService.class,
                IQueryApiServer.class, IDssServiceRpcScreening.class };

        for (Class<?> jsonClass : jsonRpcInterfaces)
        {
            allJsonClasses.addAll(ClassReferences.search(jsonClass, new Predicate<Class<?>>()
                {
                    @Override
                    public boolean apply(Class<?> clazz)
                    {
                        return (clazz.getPackage().getName()
                                .startsWith("ch.systemsx.sybit.imageviewer") == false);
                    }
                }));
        }
    }

    @Test
    public void jsonClassesAreAnnotatedWithJsonObject()
    {
        Collection<Class<?>> classesWithoutJsonObject = getAllJsonRpcClassesWithoutJsonObject();
        assertThat(classesWithoutJsonObject, is(empty));
    }

    @Test
    public void jsonTypeNamesAreUniqueInV1()
    {
        // Classes that are allowed to have already existing @JsonTypeName
        Collection<String> whiteList = new HashSet<String>();
        whiteList
                .add("ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeALegalDuplicate");
        whiteList
                .add("ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeBIllegalDuplicate");

        Map<String, Collection<Class<?>>> names = new HashMap<String, Collection<Class<?>>>();
        for (Class<?> clazz : ClassReferences.ref.getTypesAnnotatedWith(JsonObject.class, true))
        {
            if (whiteList.contains(clazz.getCanonicalName()) == false && clazz.getCanonicalName().contains(".v3.") == false)
            {
                String name = clazz.getAnnotation(JsonObject.class).value();
                addValueToCollectionMap(names, name, clazz);
            }
        }

        assertThat(duplicatedValuesIn(names), is(emptyMap));
    }

    @Test
    public void jsonTypeNamesAreUniqueInV3()
    {
        // Classes that are allowed to have already existing @JsonTypeName
        Collection<String> whiteList = new HashSet<String>();
        whiteList
                .add("ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeALegalDuplicate");
        whiteList
                .add("ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeBIllegalDuplicate");

        Map<String, Collection<Class<?>>> names = new HashMap<String, Collection<Class<?>>>();
        for (Class<?> clazz : ClassReferences.ref.getTypesAnnotatedWith(JsonObject.class, true))
        {
            if (whiteList.contains(clazz.getCanonicalName()) == false && clazz.getCanonicalName().contains(".v3."))
            {
                String name = clazz.getAnnotation(JsonObject.class).value();
                addValueToCollectionMap(names, name, clazz);
            }
        }

        assertThat(duplicatedValuesIn(names), is(emptyMap));
    }

    @Test
    public void jsonClassesDoNotContainLongProperties()
    {
        IPropertyFilter longFilter = new IPropertyFilter()
            {
                @Override
                public boolean accept(BeanDescription bean, BeanPropertyDefinition property)
                {
                    if (property.getField() != null
                            && isLongClass(property.getField().getRawType()))
                    {
                        return true;
                    }
                    if (property.getGetter() != null
                            && isLongClass(property.getGetter().getRawType()))
                    {
                        return true;
                    }
                    if (property.getSetter() != null
                            && property.getSetter().getParameterCount() == 1
                            && isLongClass(property.getSetter().getParameter(0).getRawType()))
                    {
                        return true;
                    }

                    return false;
                }

                private boolean isLongClass(Class<?> clazz)
                {
                    return long.class.equals(clazz) || Long.class.equals(clazz);
                }

            };

        Set<String> longProperties = getProperties(longFilter);
        Set<String> emptySet = new TreeSet<String>();
        assertThat(longProperties, is(emptySet));
    }

    @Test
    public void jsonClassesDoNotContainAsStringProperties()
    {
        IPropertyFilter asStringFilter = new IPropertyFilter()
            {
                @Override
                public boolean accept(BeanDescription bean, BeanPropertyDefinition property)
                {
                    return property.getName().endsWith("AsString");
                }
            };

        Set<String> asStringProperties = getProperties(asStringFilter);
        Set<String> emptySet = new TreeSet<String>();
        assertThat(asStringProperties, is(emptySet));
    }

    private static interface IPropertyFilter
    {

        boolean accept(BeanDescription bean, BeanPropertyDefinition property);

    }

    private Set<String> getProperties(IPropertyFilter filter)
    {
        GenericObjectMapper genericMapper = new GenericObjectMapper();
        ScreeningObjectMapper screeningMapper = new ScreeningObjectMapper();

        Set<String> properties = new TreeSet<String>();

        for (Class<?> jsonClass : allJsonClasses)
        {
            JavaType jsonJavaType = SimpleType.construct(jsonClass);

            BeanDescription genericSerializationBean =
                    genericMapper.getSerializationConfig().introspect(jsonJavaType);
            BeanDescription genericDeserializationBean =
                    genericMapper.getDeserializationConfig().introspect(jsonJavaType);
            BeanDescription screeningSerializationBean =
                    screeningMapper.getSerializationConfig().introspect(jsonJavaType);
            BeanDescription screeningDeserializationBean =
                    screeningMapper.getDeserializationConfig().introspect(jsonJavaType);

            addProperties(genericSerializationBean, filter, properties);
            addProperties(genericDeserializationBean, filter, properties);
            addProperties(screeningSerializationBean, filter, properties);
            addProperties(screeningDeserializationBean, filter, properties);
        }

        return properties;
    }

    private static void addProperties(BeanDescription bean, IPropertyFilter propertyFilter,
            Set<String> acceptedProperties)
    {
        for (BeanPropertyDefinition property : bean.findProperties())
        {
            if (propertyFilter.accept(bean, property))
            {
                acceptedProperties.add(bean.getBeanClass().getName() + "#" + property.getName());
            }
        }
    }

    private static class PrettyPrintingCollectionMap<K, V extends Collection<?>> extends
            HashMap<K, V>
    {

        private static final long serialVersionUID = 1L;

        @Override
        public String toString()
        {
            String value = "\n";
            for (K key : this.keySet())
            {
                value += key.toString() + "\n";
                for (Object o : this.get(key))
                {
                    value += "\t" + o + "\n";
                }
                value += "\n";
            }
            return value;
        }
    }

    private Collection<Class<?>> getAllJsonRpcClassesWithoutJsonObject()
    {
        Collection<Class<?>> classesWithoutJsonObject = new HashSet<Class<?>>();
        for (Class<?> clazz : allJsonClasses)
        {
            if (clazz.getAnnotation(JsonObject.class) == null)
            {
                classesWithoutJsonObject.add(clazz);
            }
        }
        return classesWithoutJsonObject;
    }

    private static <K, V> void addValueToCollectionMap(Map<K, Collection<V>> map, K key, V value)
    {
        Collection<V> col = map.get(key);
        if (col == null)
        {
            col = new HashSet<V>();
        }
        col.add(value);
        map.put(key, col);
    }

    private static <K, V> Map<K, Collection<V>> duplicatedValuesIn(Map<K, Collection<V>> original)
    {
        Map<K, Collection<V>> map = new PrettyPrintingCollectionMap<K, Collection<V>>();
        for (K key : original.keySet())
        {
            if (original.get(key).size() > 1)
            {
                map.put(key, original.get(key));
            }
        }
        return map;
    }

}
