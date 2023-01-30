package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.TestResources;

public class ProgressDetailsToStringBuilderTest
{

    @Test
    public void testWithTopLevelNull()
    {
        assertToStringEquals(null, "testWithTopLevelNull");
    }

    @Test
    public void testWithTopLevelMap()
    {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("o", new TestClass());

        assertToStringEquals(map, "testWithTopLevelMap");
    }

    @Test
    public void testWithTopLevelCollection()
    {
        Collection<Object> map = new ArrayList<Object>();
        map.add(new TestClass());

        assertToStringEquals(map, "testWithTopLevelCollection");
    }

    @Test
    public void testWithNulls()
    {
        TestClass o = new TestClass();
        assertToStringEquals(o, "testWithNulls");
    }

    @Test
    public void testWithPrimitive()
    {
        TestClass o = new TestClass();
        o.primitive = 1;
        assertToStringEquals(o, "testWithPrimitive");
    }

    @Test
    public void testWithString()
    {
        TestClass o = new TestClass();
        o.string = "abc";
        assertToStringEquals(o, "testWithString");
    }

    @Test
    public void testWithInteger()
    {
        TestClass o = new TestClass();
        o.integer = 1;
        assertToStringEquals(o, "testWithInteger");
    }

    @Test
    public void testWithObject()
    {
        TestClass o = new TestClass();
        o.object = new TestClass();
        assertToStringEquals(o, "testWithObject");
    }

    @Test
    public void testWithFieldUpdateValueModified()
    {
        TestClass o = new TestClass();
        o.fieldUpdateValue = new FieldUpdateValue<String>();
        o.fieldUpdateValue.setValue("modified");
        assertToStringEquals(o, "testWithFieldUpdateValueModified");
    }

    @Test
    public void testWithFieldUpdateValueNotModified()
    {
        TestClass o = new TestClass();
        o.fieldUpdateValue = new FieldUpdateValue<String>();
        assertToStringEquals(o, "testWithFieldUpdateValueNotModified");
    }

    @Test
    public void testWithListUpdateValueModified()
    {
        TestClass o = new TestClass();
        o.listUpdateValue = new ListUpdateValue<String, String, String, String>();
        o.listUpdateValue.add("added");
        o.listUpdateValue.remove("removed");
        assertToStringEquals(o, "testWithListUpdateValueModified");
    }

    @Test
    public void testWithListUpdateValueNotModified()
    {
        TestClass o = new TestClass();
        o.listUpdateValue = new ListUpdateValue<String, String, String, String>();
        assertToStringEquals(o, "testWithListUpdateValueNotModified");
    }

    @Test
    public void testWithPrimitiveArray()
    {
        TestClass o = new TestClass();
        o.primitiveArray = new int[] { 0, 1, 2 };
        assertToStringEquals(o, "testWithPrimitiveArray");
    }

    @Test
    public void testWithStringArray()
    {
        TestClass o = new TestClass();
        o.stringArray = new String[] { "abc", "def", "ghi" };
        assertToStringEquals(o, "testWithStringArray");
    }

    @Test
    public void testWithIntegerArray()
    {
        TestClass o = new TestClass();
        o.integerArray = new Integer[] { 1, 2, 3 };
        assertToStringEquals(o, "testWithIntegerArray");
    }

    @Test
    public void testWithObjectArray()
    {
        TestClass nested1 = new TestClass();
        nested1.primitive = 1;
        TestClass nested2 = new TestClass();
        nested2.primitive = 2;

        TestClass o = new TestClass();
        o.objectArray = new TestClass[] { nested1, nested2 };

        assertToStringEquals(o, "testWithObjectArray");
    }

    @Test
    public void testWithNullCollectionItem()
    {
        TestClass o = new TestClass();
        o.stringCollection = Arrays.asList("abc", null, "ghi");
        assertToStringEquals(o, "testWithNullCollectionItem");
    }

    @Test
    public void testWithStringCollection()
    {
        TestClass o = new TestClass();
        o.stringCollection = Arrays.asList("abc", "def", "ghi");
        assertToStringEquals(o, "testWithStringCollection");
    }

    @Test
    public void testWithIntegerCollection()
    {
        TestClass o = new TestClass();
        o.integerCollection = Arrays.asList(1, 2, 3);
        assertToStringEquals(o, "testWithIntegerCollection");
    }

    @Test
    public void testWithObjectCollection()
    {
        TestClass nested1 = new TestClass();
        nested1.primitive = 1;
        TestClass nested2 = new TestClass();
        nested2.primitive = 2;

        TestClass o = new TestClass();
        o.objectCollection = Arrays.asList(nested1, nested2);

        assertToStringEquals(o, "testWithObjectCollection");
    }

    @Test
    public void testWithNullMapKeyAndValue()
    {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("k1", "v1");
        map.put(null, "v2");

        TestClass o = new TestClass();
        o.stringMap = map;

        assertToStringEquals(o, "testWithNullMapKeyAndValue");
    }

    @Test
    public void testWithStringMap()
    {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        TestClass o = new TestClass();
        o.stringMap = map;

        assertToStringEquals(o, "testWithStringMap");
    }

    @Test
    public void testWithIntegerMap()
    {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("1", 11);
        map.put("2", 22);

        TestClass o = new TestClass();
        o.integerMap = map;

        assertToStringEquals(o, "testWithIntegerMap");
    }

    @Test
    public void testWithObjectMap()
    {
        TestClass nested1 = new TestClass();
        nested1.primitive = 1;
        TestClass nested2 = new TestClass();
        nested2.primitive = 2;

        Map<String, TestClass> map = new LinkedHashMap<String, TestClass>();
        map.put("1", nested1);
        map.put("2", nested2);

        TestClass o = new TestClass();
        o.objectMap = map;

        assertToStringEquals(o, "testWithObjectMap");
    }

    @Test
    public void testWithCircularReferences()
    {
        TestClass o1 = new TestClass();
        o1.primitive = 1;
        TestClass o2 = new TestClass();
        o2.primitive = 2;

        o1.object = o2;
        o2.object = o1;

        assertToStringEquals(o1, "testWithCircularReferences");
    }

    private void assertToStringEquals(Object object, String expectedFileName)
    {
        TestResources resources = new TestResources(getClass());
        String json = FileUtilities.loadToString(resources.getResourceFile(expectedFileName));
        assertEquals(ProgressDetailsToStringBuilder.getInstance().toString(object), json.trim());
    }

}
