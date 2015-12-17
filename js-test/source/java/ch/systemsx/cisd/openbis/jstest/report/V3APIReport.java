package ch.systemsx.cisd.openbis.jstest.report;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.testng.annotations.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * @author pkupczyk
 */
public class V3APIReport
{

    private static final String[] PUBLIC_PACKAGES = {
            "ch.ethz.sis.openbis.generic.dss.api.v3",
            "ch.ethz.sis.openbis.generic.as.api.v3"
    };

    @Test
    public void test() throws Exception
    {
        System.out.println(getReport());
    }

    public String getReport() throws Exception
    {
        Report report = new Report();
        Collection<Class<?>> publicClasses = getPublicClasses();
        createReport(report, publicClasses);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        return jsonValue;
    }

    private Collection<Class<?>> getPublicClasses()
    {
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.getContextClassLoader());
        classLoadersList.add(ClasspathHelper.getStaticClassLoader());

        SubTypesScanner subTypesScanner = new SubTypesScanner();
        subTypesScanner.filterResultsBy(new FilterBuilder().include(".*"));

        FilterBuilder filterBuilder = new FilterBuilder();
        for (String v3PublicPackage : PUBLIC_PACKAGES)
        {
            filterBuilder.include(FilterBuilder.prefix(v3PublicPackage));
        }

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(subTypesScanner, new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(filterBuilder));

        Multimap<String, String> map = reflections.getStore().get(SubTypesScanner.class);

        Collection<String> nonInnerClassNames = Collections2.filter(map.values(), new Predicate<String>()
            {

                @Override
                public boolean apply(String item)
                {
                    return false == item.contains("$");
                }
            });
        Collection<String> uniqueClassNames = new TreeSet<String>(nonInnerClassNames);
        Collection<Class<?>> uniqueClasses = ImmutableSet.copyOf(ReflectionUtils.forNames(uniqueClassNames));

        for (Class<?> uniqueClass : uniqueClasses)
        {
            System.out.println("Found V3 public class:\t" + uniqueClass.getName());
        }
        System.out.println();

        return uniqueClasses;
    }

    private String getJSONObjectAnnotation(Class<?> clazz)
    {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations)
        {
            Class<? extends Annotation> type = annotation.annotationType();
            String name = type.getName();
            if (name.equals("ch.systemsx.cisd.base.annotation.JsonObject"))
            {

                for (Method method : type.getDeclaredMethods())
                {
                    try
                    {
                        Object value = method.invoke(annotation, (Object[]) null);
                        return (String) value;
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private Collection<Field> getPublicFields(Class<?> clazz)
    {
        Collection<Field> fields = new ArrayList<Field>();
        for (Field field : clazz.getDeclaredFields())
        {
            boolean isPublic = Modifier.isPublic(field.getModifiers());
            boolean hasJsonIgnore = field.getAnnotation(JsonIgnore.class) != null;
            boolean hasJsonProperty = field.getAnnotation(JsonProperty.class) != null;

            if (hasJsonProperty || (isPublic && false == hasJsonIgnore))
            {
                fields.add(field);
            }
        }
        return fields;
    }

    private Collection<Method> getPublicMethods(Class<?> clazz)
    {
        Collection<Method> methods = new ArrayList<Method>();

        for (Method method : clazz.getDeclaredMethods())
        {
            boolean isPublic = Modifier.isPublic(method.getModifiers());
            boolean isAbstract = Modifier.isAbstract(method.getModifiers());

            if (false == isAbstract && isPublic)
            {
                methods.add(method);
            }
        }
        return methods;
    }

    private void createReport(Report report, Collection<Class<?>> classes)
    {
        for (Class<?> clazz : classes)
        {
            Entry entry = new Entry(clazz.getName());
            entry.setJsonObjAnnotation(getJSONObjectAnnotation(clazz));
            addFields(entry, getPublicFields(clazz));
            addMethods(entry, getPublicMethods(clazz));
            report.add(entry);
        }
    }

    private void addFields(Entry entryReport, Collection<Field> fields)
    {
        for (Field field : fields)
        {
            entryReport.addField(field.getName());
        }
    }

    private void addMethods(Entry entryReport, Collection<Method> methods)
    {
        for (Method method : methods)
        {
            entryReport.addMethod(method.getName());
        }
    }

    static class Report
    {

        private List<Entry> entries = new LinkedList<Entry>();

        public void add(Entry entry)
        {
            entries.add(entry);
        }

        public List<Entry> getEntries()
        {
            return entries;
        }

    }

    static class Entry
    {
        private String Name;

        private String jsonObjAnnotation;

        private List<String> fields = new ArrayList<String>();

        private List<String> methods = new ArrayList<String>();

        public Entry(String name)
        {
            super();
            this.Name = name;
        }

        public String getName()
        {
            return this.Name;
        }

        public String getJsonObjAnnotation()
        {
            return this.jsonObjAnnotation;
        }

        public void setJsonObjAnnotation(String jsonObjAnnotation)
        {
            this.jsonObjAnnotation = jsonObjAnnotation;
        }

        public List<String> getFields()
        {
            return this.fields;
        }

        public void addField(String field)
        {
            fields.add(field);
        }

        public List<String> getMethods()
        {
            return this.methods;
        }

        public void addMethod(String method)
        {
            methods.add(method);
        }
    }

}
