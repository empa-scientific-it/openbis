/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.string;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.string.ReflectionStringTraverser;
import ch.systemsx.cisd.common.string.ReflectionStringTraverser.ReflectionFieldVisitor;

/**
 * Tests for {@link ReflectionStringTraverser}
 * 
 * @author Tomasz Pylak
 */
// FIXME [LMS-1893] fix broken tests handling collections of strings and maps
public class ReflectionFieldTraverserTest extends AssertJUnit
{
    private static class ReflectionStringCapitalizerVisitor implements ReflectionFieldVisitor
    {
        @Override
        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
            return value.toUpperCase();
        }
    }

    private static class TestStaticFinal
    {
        private static String dummyStaticString = "static";

        @SuppressWarnings("unused")
        private final String dummyFinalString = "final";
    }

    private static class TestClass
    {
        @SuppressWarnings("unused")
        private TestStaticFinal dummyFinalStatic = new TestStaticFinal();

        @SuppressWarnings("unused")
        boolean dummyBoolean = false;

        private List<TestClass> list;

        private Set<TestClass> set;

        protected TestClass[] array;

        private List<String> stringList;

        private Set<String> stringSet;

        protected String[] stringArray;

        public TestClass complexChild;

        private String text;

    }

    @Test
    public void test()
    {
        TestClass object = create("object");
        object.list = new ArrayList<TestClass>(Arrays.asList(create("list1"), create("list2")));
        object.set =
                new LinkedHashSet<TestClass>(Arrays.asList(create("set1"), create("set2"),
                        create("set3")));
        object.array = new TestClass[]
        { create("array1"), create("array2") };
        object.complexChild = create("complexChild");
        object.stringList = new ArrayList<String>(Arrays.asList("stringList1", "stringList2"));
        object.stringSet =
                new TreeSet<String>(Arrays.asList("stringSet2", "stringSet1", "stringSet3"));
        object.stringArray = new String[]
        { "stringArray1", "stringArray2" };

        ReflectionStringTraverser.traverseDeep(object, new ReflectionStringCapitalizerVisitor());

        assertEquals("OBJECT", object.text);

        assertEquals(2, object.list.size());
        assertEquals("LIST1", object.list.get(0).text);
        assertEquals("LIST2", object.list.get(1).text);

        assertEquals(3, object.set.size());
        List<TestClass> setAsList = new ArrayList<TestClass>(object.set);
        assertEquals("SET1", setAsList.get(0).text);
        assertEquals("SET2", setAsList.get(1).text);
        assertEquals("SET3", setAsList.get(2).text);

        assertEquals(2, object.array.length);
        assertEquals("ARRAY1", object.array[0].text);
        assertEquals("ARRAY2", object.array[1].text);

        assertEquals(2, object.stringList.size());
        assertEquals("STRINGLIST1", object.stringList.get(0));
        assertEquals("STRINGLIST2", object.stringList.get(1));

        // escaped set should preserved order
        assertEquals(3, object.stringSet.size());
        List<String> stringSetAsList = new ArrayList<String>(object.stringSet);
        assertEquals("STRINGSET1", stringSetAsList.get(0));
        assertEquals("STRINGSET2", stringSetAsList.get(1));
        assertEquals("STRINGSET3", stringSetAsList.get(2));

        assertEquals(2, object.stringArray.length);
        assertEquals("STRINGARRAY1", object.stringArray[0]);
        assertEquals("STRINGARRAY2", object.stringArray[1]);

        assertEquals("COMPLEXCHILD", object.complexChild.text);
        assertEquals("static", TestStaticFinal.dummyStaticString);
    }

    // BROKEN: lists of strings are not handled properly
    @Test(groups = "broken")
    public void testTraverseListOfStrings()
    {
        List<String> stringList = new ArrayList<String>(Arrays.asList("el1", "el2", "el3"));
        ReflectionStringTraverser
                .traverseDeep(stringList, new ReflectionStringCapitalizerVisitor());

        assertEquals(3, stringList.size());
        assertEquals("EL1", stringList.get(0));
        assertEquals("EL2", stringList.get(1));
        assertEquals("EL3", stringList.get(2));
    }

    // BROKEN: sets of strings are not handled properly
    @Test(groups = "broken")
    public void testTraverseSetOfStrings()
    {
        Set<String> stringSet = new LinkedHashSet<String>(Arrays.asList("el1", "el2", "el3"));
        ReflectionStringTraverser.traverseDeep(stringSet, new ReflectionStringCapitalizerVisitor());

        List<String> stringList = new ArrayList<String>(stringSet);
        assertEquals(3, stringList.size());
        assertEquals("EL1", stringList.get(0));
        assertEquals("EL2", stringList.get(1));
        assertEquals("EL3", stringList.get(2));
    }

    // BROKEN: currently only values are escaped - there should be special handling of maps
    @Test(groups = "broken")
    public void testTraverseMapWithStrings()
    {
        List<String> stringList = new ArrayList<String>(Arrays.asList("el1", "el2", "el3"));
        TestClass testClass = create("text");
        Map<TestClass, List<String>> stringMap = new HashMap<TestClass, List<String>>();
        stringMap.put(testClass, stringList);
        ReflectionStringTraverser.traverseDeep(stringMap, new ReflectionStringCapitalizerVisitor());

        assertEquals(1, stringMap.entrySet().size());
        TestClass key = stringMap.keySet().iterator().next();
        List<String> value = stringMap.values().iterator().next();
        assertEquals("TEXT", key.text);
        assertEquals(3, value.size());
        assertEquals("EL1", value.get(0));
        assertEquals("EL2", value.get(1));
        assertEquals("EL3", value.get(2));
    }

    private static TestClass create(String value)
    {
        TestClass object = new TestClass();
        object.text = value;
        return object;
    }
}
