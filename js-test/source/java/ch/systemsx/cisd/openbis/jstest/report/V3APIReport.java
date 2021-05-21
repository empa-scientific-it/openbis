package ch.systemsx.cisd.openbis.jstest.report;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.jstest.server.JsTestDataStoreServer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

/**
 * @author pkupczyk
 */
public class V3APIReport
{

    private static final String[] PUBLIC_PACKAGES = {
            "ch.ethz.sis.openbis.generic.dssapi.v3",
            "ch.ethz.sis.openbis.generic.asapi.v3"
    };

    private static final Collection<ClassFilter> CLASS_FILTERS = new ArrayList<ClassFilter>();

    static
    {
        CLASS_FILTERS.add(new ClassFilter("inner classes filter")
        {
            @Override
            public boolean accepts(Class<?> clazz)
            {
                return false == clazz.getName().contains("$");
            }
        });

        CLASS_FILTERS.add(new ClassFilter("ignored classes marked with @JsonIgnoreType")
        {
            @Override
            public boolean accepts(Class<?> clazz)
            {
                return clazz.getAnnotation(JsonIgnoreType.class) == null;
            }
        });

    }

    @Test
    public void test() throws Exception
    {
        System.out.println(getReport());
    }

    public List<EMail> getEmailsWith(String textSnippet)
    {
        List<EMail> emails = new ArrayList<>();
        File emailFolder = new File(JsTestDataStoreServer.EMAIL_FOLDER);
        java.io.File[] emailFiles = emailFolder.listFiles();
        if (emailFiles != null)
        {
            List<File> files = new ArrayList<>(Arrays.asList(emailFiles));
            Collections.sort(files);
            Collections.reverse(files);
            for (File emailFile : files)
            {
                String fullContent = FileUtilities.loadToString(emailFile);
                if (fullContent.contains(textSnippet))
                {
                    emails.add(new EMail(fullContent));
                }
            }
        }
        return emails;
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
        FilterBuilder filterBuilder = new FilterBuilder();
        Set<URL> urls = new HashSet<URL>();

        for (String prefix : PUBLIC_PACKAGES)
        {
            urls.addAll(ClasspathHelper.forPackage(prefix));
            filterBuilder.include(FilterBuilder.prefix(prefix));
        }

        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setScanners(new SubTypesScanner(false));
        configBuilder.addUrls(urls);
        configBuilder.filterInputsBy(filterBuilder);

        Reflections reflections = new Reflections(configBuilder);

        Multimap<String, String> map = reflections.getStore().get(SubTypesScanner.class.getSimpleName());

        Collection<String> uniqueClassNames = new TreeSet<String>(map.values());
        Collection<Class<?>> uniqueClasses = ImmutableSet.copyOf(ReflectionUtils.forNames(uniqueClassNames));
        Collection<Class<?>> filteredClasses = new LinkedHashSet<Class<?>>(uniqueClasses);

        for (final ClassFilter filter : CLASS_FILTERS)
        {
            filteredClasses = Collections2.filter(filteredClasses, new Predicate<Class<?>>()
            {
                @Override
                public boolean apply(Class<?> clazz)
                {
                    boolean result = filter.apply(clazz);
                    if (!result)
                    {
                        System.out.println("Filtered out class '" + clazz.getName() + "' by filter '" + filter.getFilterName() + "'");
                    }
                    return result;
                }
            });
        }

        for (Class<?> filteredClass : filteredClasses)
        {
            System.out.println("Found V3 public class:\t" + filteredClass.getName());
        }

        System.out.println();

        return filteredClasses;
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
            boolean hasJsonIgnore = method.getAnnotation(JsonIgnore.class) != null;

            if (false == isAbstract && isPublic && false == hasJsonIgnore)
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

    static abstract class ClassFilter implements Predicate<Class<?>>
    {

        private String filterName;

        public ClassFilter(String filterName)
        {
            this.filterName = filterName;
        }

        public abstract boolean accepts(Class<?> clazz);

        @Override
        public boolean apply(Class<?> clazz)
        {
            return accepts(clazz);
        }

        public String getFilterName()
        {
            return filterName;
        }

    }

    public static final class EMail
    {
        private static final String TO = "To:";

        private static final String SUBJECT = "Subject:";

        private static final String CONTENT = "Content:";

        private String content;

        private String to;

        private String subject;

        private String fullContent;

        EMail(String fullContent)
        {
            this.fullContent = fullContent;
            int toIndex = fullContent.indexOf(TO);
            int subjectIndex = fullContent.indexOf(SUBJECT);
            int contentIndex = fullContent.indexOf(CONTENT);
            if (0 < toIndex && toIndex < subjectIndex && subjectIndex < contentIndex)
            {
                to = fullContent.substring(toIndex + TO.length(), subjectIndex).trim();
                subject = fullContent.substring(subjectIndex + SUBJECT.length(), contentIndex).trim();
                content = fullContent.substring(contentIndex + CONTENT.length()).trim();
            }
        }

        public String getContent()
        {
            return content;
        }

        public String getTo()
        {
            return to;
        }

        public String getSubject()
        {
            return subject;
        }

        public String getFullContent()
        {
            return fullContent;
        }
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println(new V3APIReport().getReport());
    }

}
