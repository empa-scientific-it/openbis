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

package ch.ethz.sis.openbis.generic.sharedapi.v3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.shared.basic.string.IgnoreCaseComparator;

/**
 * @author pkupczyk
 */
public class EnglishCheck
{

    @Test
    public void test() throws Exception
    {
        Report report = new Report();
        checkClasses(report, ApiClassesProvider.getPublicClasses());
        System.out.println(new ReportFormat().showCorrect().showIncorrect().format(report));
        Assert.assertEquals(new ReportFormat().showIncorrect().showOnlyWords().format(report), "");
    }

    private void checkClasses(Report report, Collection<Class<?>> classes)
    {
        for (Class<?> clazz : classes)
        {
            report.add(new ClassEntry(clazz));
            checkFields(report, ApiClassesProvider.getPublicFields(clazz));
            checkMethods(report, ApiClassesProvider.getPublicMethods(clazz));
        }
    }

    private void checkFields(Report report, Collection<Field> fields)
    {
        for (Field field : fields)
        {
            report.add(new FieldEntry(field));
        }
    }

    private void checkMethods(Report report, Collection<Method> methods)
    {
        for (Method method : methods)
        {
            report.add(new MethodEntry(method));
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

    static abstract class Entry
    {

        public abstract String getWord();

        public abstract String getContext();

    }

    static class ClassEntry extends Entry
    {

        private Class<?> clazz;

        public ClassEntry(Class<?> clazz)
        {
            this.clazz = clazz;
        }

        @Override
        public String getWord()
        {
            return clazz.getSimpleName();
        }

        @Override
        public String getContext()
        {
            return clazz.getSimpleName();
        }

    }

    static class FieldEntry extends Entry
    {

        private Field field;

        public FieldEntry(Field field)
        {
            this.field = field;
        }

        @Override
        public String getWord()
        {
            return field.getName();
        }

        @Override
        public String getContext()
        {
            return field.getDeclaringClass().getSimpleName();
        }

    }

    static class MethodEntry extends Entry
    {

        private Method method;

        public MethodEntry(Method method)
        {
            this.method = method;
        }

        @Override
        public String getWord()
        {
            return method.getName();
        }

        @Override
        public String getContext()
        {
            return method.getDeclaringClass().getSimpleName();
        }

    }

    static class ReportFormat
    {

        private boolean showCorrect;

        private boolean showIncorrect;

        private boolean showOnlyWords;

        public ReportFormat showCorrect()
        {
            this.showCorrect = true;
            return this;
        }

        public ReportFormat showIncorrect()
        {
            this.showIncorrect = true;
            return this;
        }

        public ReportFormat showOnlyWords()
        {
            this.showOnlyWords = true;
            return this;
        }

        public String format(Report report)
        {
            Map<String, Set<String>> contextsMap = new HashMap<String, Set<String>>();

            for (Entry entry : report.getEntries())
            {
                Set<String> contexts = contextsMap.get(entry.getWord());

                if (contexts == null)
                {
                    contexts = new TreeSet<String>();
                    contextsMap.put(entry.getWord(), contexts);
                }

                contexts.add(entry.getContext());
            }

            List<String> sortedWords = new ArrayList<String>(contextsMap.keySet());
            Collections.sort(sortedWords, new IgnoreCaseComparator());

            String longestWord = Collections.max(sortedWords, new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        return Integer.valueOf(o1.length()).compareTo(Integer.valueOf(o2.length()));
                    }
                });

            StringBuilder content = new StringBuilder();

            for (String word : sortedWords)
            {
                Set<String> contexts = contextsMap.get(word);
                boolean correct = EnglishDictionary.getInstance().contains(word);

                if ((correct && showCorrect) || (false == correct && showIncorrect))
                {
                    if (showOnlyWords)
                    {
                        content.append(word);
                    } else
                    {
                        content.append(StringUtils.rightPad(word, longestWord.length()) + "\t" + (correct ? "OK" : "WRONG") + " " + contexts);
                    }

                    content.append("\n");
                }
            }

            return content.toString();
        }
    }

}
