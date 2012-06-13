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

import org.reflections.Reflections;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.JsonUniqueCheckIgnore;
import ch.systemsx.cisd.common.api.server.json.util.ClassReferences;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IWebInformationService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;

/**
 * This class contains tests that make sure that Jackson annotations are used correctly and
 * consistently in all the classes that are exposed through openBIS JSON-RPC APIs.
 * 
 * @author anttil
 */
public class JsonAnnotationTest
{
    private Collection<Class<?>> allJsonClasses = new HashSet<Class<?>>();

    private Reflections reflections = new Reflections("");

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
            allJsonClasses.addAll(ClassReferences.search(jsonClass));
        }
    }

    @Test
    public void jsonClassesAreAnnotatedWithJsonObject()
    {
        Collection<Class<?>> classesWithoutJsonObject = getAllJsonRpcClassesWithoutJsonObject();
        assertThat(classesWithoutJsonObject, is(empty));
    }

    @Test
    public void jsonTypeNamesAreUnique()
    {
        Map<String, Collection<Class<?>>> names = new HashMap<String, Collection<Class<?>>>();
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(JsonObject.class))
        {
            if (clazz.getAnnotation(JsonUniqueCheckIgnore.class) != null)
            {
                continue;
            }

            String name = clazz.getAnnotation(JsonObject.class).value();
            addValueToCollectionMap(names, name, clazz);
        }

        assertThat(duplicatedValuesIn(names), is(emptyMap));
    }

    @Test(enabled = false)
    public void jsonClassesWithSubClassesAreAnnotatedWithJsonSubTypes()
    {
        Collection<Class<?>> classesWithoutAnnotation = new HashSet<Class<?>>();
        for (Class<?> clazz : allJsonClasses)
        {
            if (clazz.isEnum() == false && reflections.getSubTypesOf(clazz).isEmpty() == false)
            {
                if (clazz.getAnnotation(JsonSubTypes.class) == null)
                {
                    classesWithoutAnnotation.add(clazz);
                }
            }
        }
        assertThat(classesWithoutAnnotation, is(empty));
    }

    @Test(enabled = false)
    public void jsonSubTypesAnnotationsContainAllDirectSubClasses()
    {
        Map<String, Collection<Class<?>>> missingSubtypeAnnotations =
                new PrettyPrintingCollectionMap<String, Collection<Class<?>>>();
        for (Class<?> main : reflections.getTypesAnnotatedWith(JsonSubTypes.class))
        {
            Collection<Class<?>> annotatedSubtypes = getAnnotatedSubTypes(main);

            for (Class<?> subtype : reflections.getSubTypesOf(main))
            {
                if (subtypeIsMissing(main, subtype, annotatedSubtypes))
                {
                    addValueToCollectionMap(missingSubtypeAnnotations, main.getCanonicalName(),
                            subtype);
                }
            }
        }

        assertThat(missingSubtypeAnnotations, is(emptyMap));
    }

    private static class PrettyPrintingCollectionMap<K, V extends Collection<?>> extends
            HashMap<K, V>
    {

        private static final long serialVersionUID = 2615134692782526120L;

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

    private static boolean subtypeIsMissing(Class<?> main, Class<?> subtype,
            Collection<Class<?>> classes)
    {

        if (subtype.isAnonymousClass())
        {
            return false;
        }

        if (classes.contains(subtype))
        {
            return false;
        }

        if (subtype.isInterface())
        {
            return true;
        } else
        {
            return subtype.getSuperclass().equals(main);
        }
    }

    private static Collection<Class<?>> getAnnotatedSubTypes(Class<?> clazz)
    {
        Collection<Class<?>> annotated = new HashSet<Class<?>>();
        JsonSubTypes types = clazz.getAnnotation(JsonSubTypes.class);
        for (Type type : types.value())
        {
            annotated.add(type.value());
        }
        return annotated;
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
