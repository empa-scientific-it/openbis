/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.reflection;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.beans.PropertyDescriptor;
import java.lang.Thread.State;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.reflection.BeanUtils;

/**
 * Test cases for the {@link BeanUtils} class.
 * 
 * @author Christian Ribeaud
 * @author Bernd Rinn
 */
public final class BeanUtilsTest
{

    @Test
    public final void testGetPropertyDescriptors() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException
    {
        final List<PropertyDescriptor> descriptors =
                new ArrayList<PropertyDescriptor>(BeanUtils.getPropertyDescriptors(FooBean.class)
                        .values());
        assertEquals(1, descriptors.size());
        PropertyDescriptor outerDescriptor = null;
        // Play with property 'description'
        for (final Iterator<PropertyDescriptor> iter = descriptors.iterator(); iter.hasNext();)
        {
            final PropertyDescriptor innerDescriptor = iter.next();
            if (innerDescriptor.getName().equals("foo"))
            {
                outerDescriptor = innerDescriptor;
                break;
            }
        }
        assertNotNull(outerDescriptor);
        assertEquals(outerDescriptor.getDisplayName(), "foo");
        assertEquals(outerDescriptor.getName(), "foo");
        assertNotNull(outerDescriptor.getWriteMethod());
        // Setting the property 'foo' live on an object
        final FooBean fooBean = new FooBean();
        final Method method = outerDescriptor.getWriteMethod();
        final String description = "This is a foolish description.";
        method.invoke(fooBean, new Object[]
        { description });
        assertEquals(fooBean.getFoo(), description);
    }

    public static class Bean1a
    {
        private int i;

        private String s;

        private boolean b;

        private float f;

        private boolean bb;

        public boolean isB()
        {
            return b;
        }

        public void setB(final boolean b)
        {
            this.b = b;
        }

        public float getF()
        {
            return f;
        }

        public void setF(final float f)
        {
            this.f = f;
        }

        public int getI()
        {
            return i;
        }

        public void setI(final int i)
        {
            this.i = i;
        }

        public String getS()
        {
            return s;
        }

        public void setS(final String s)
        {
            this.s = s;
        }

        public final boolean getBb()
        {
            return bb;
        }

        public final void setBb(final boolean bb)
        {
            this.bb = bb;
        }

    }

    public static class Bean1b
    {
        private Integer i;

        private String s;

        private Boolean b;

        private Float f;

        private Boolean bb;

        public final Boolean getBb()
        {
            return bb;
        }

        public final void setBb(final Boolean bb)
        {
            this.bb = bb;
        }

        public Boolean isB()
        {
            return b;
        }

        public void setB(final Boolean b)
        {
            this.b = b;
        }

        public Float getF()
        {
            return f;
        }

        public void setF(final Float f)
        {
            this.f = f;
        }

        public Integer getI()
        {
            return i;
        }

        public void setI(final Integer i)
        {
            this.i = i;
        }

        public String getS()
        {
            return s;
        }

        public void setS(final String s)
        {
            this.s = s;
        }

    }

    public static class Bean2a
    {
        private int i;

        private String s;

        private boolean b;

        private float f;

        private boolean bb;

        public boolean isB()
        {
            return b;
        }

        public void setB(final boolean b)
        {
            this.b = b;
        }

        public float getF()
        {
            return f;
        }

        public void setF(final float f)
        {
            this.f = f;
        }

        public int getI()
        {
            return i;
        }

        public void setI(final int i)
        {
            this.i = i;
        }

        public String getS()
        {
            return s;
        }

        public void setS(final String s)
        {
            this.s = s;
        }

        public final void setBb(final boolean bb)
        {
            this.bb = bb;
        }

        public final boolean getBb()
        {
            return bb;
        }
    }

    public static class Bean2b
    {
        private Integer i;

        private String s;

        private Boolean b;

        private Float f;

        private Boolean bb;

        public final Boolean getBb()
        {
            return bb;
        }

        public final void setBb(final Boolean bb)
        {
            this.bb = bb;
        }

        public Boolean isB()
        {
            return b;
        }

        public void setB(final Boolean b)
        {
            this.b = b;
        }

        public Float getF()
        {
            return f;
        }

        public void setF(final Float f)
        {
            this.f = f;
        }

        public Integer getI()
        {
            return i;
        }

        public void setI(final Integer i)
        {
            this.i = i;
        }

        public String getS()
        {
            return s;
        }

        public void setS(final String s)
        {
            this.s = s;
        }
    }

    @Test
    public void testFillSimpleBean()
    {
        final Bean1a b1 = createBean1a();
        b1.setBb(true);
        final Bean2a b2 = BeanUtils.createBean(Bean2a.class, b1);
        assertBeansAreEqual("Beans are not equal", b1, b2);
    }

    @Test
    public void testFillPreinstantiatedBean()
    {
        final Bean1a b1 = createBean1a();
        b1.setBb(true);
        final Bean2a b2 = new Bean2a();
        assertSame(b2, BeanUtils.fillBean(Bean2a.class, b2, b1));
        assertBeansAreEqual("Beans are not equal", b1, b2);

    }

    @Test
    public void testFillSimpleBeanWithNativeWrapper1()
    {
        final Bean1b b1 = new Bean1b();
        b1.setB(true);
        b1.setF(0.2f);
        b1.setI(17);
        b1.setS("test");
        b1.setBb(Boolean.TRUE);
        final Bean2a b2 = BeanUtils.createBean(Bean2a.class, b1);
        assertEquals(b1.isB().booleanValue(), b2.isB());
        assertEquals(b1.getF().floatValue(), b2.getF());
        assertEquals(b1.getI().intValue(), b2.getI());
        assertEquals(b1.getS(), b2.getS());
        assertEquals(b1.getBb().booleanValue(), b2.getBb());
    }

    @Test
    public void testFillSimpleBeanWithNativeWrapper2()
    {
        final Bean1a b1 = createBean1a();
        b1.setBb(true);
        final Bean2b b2 = BeanUtils.createBean(Bean2b.class, b1);
        assertEquals(b1.isB(), b2.isB().booleanValue());
        assertEquals(b1.getF(), b2.getF().floatValue());
        assertEquals(b1.getI(), b2.getI().intValue());
        assertEquals(b1.getS(), b2.getS());
        assertEquals(b1.getBb(), b2.getBb().booleanValue());
    }

    @Test
    public void testFillSimpleBeanWithNativeWrapper3()
    {
        final Bean1b b1 = new Bean1b();
        b1.setB(true);
        b1.setF(0.2f);
        b1.setI(17);
        b1.setS("test");
        b1.setBb(true);
        final Bean2b b2 = BeanUtils.createBean(Bean2b.class, b1);
        assertEquals(b1.isB().booleanValue(), b2.isB().booleanValue());
        assertEquals(b1.getF().floatValue(), b2.getF().floatValue());
        assertEquals(b1.getI().intValue(), b2.getI().intValue());
        assertEquals(b1.getS(), b2.getS());
        assertEquals(b1.getBb(), b2.getBb());
    }

    @Test
    public void testFillSimpleBeanArray()
    {
        final Bean1a b1a = createBean1a();
        final Bean1a b1b = new Bean1a();
        b1b.setB(false);
        b1b.setF(0.3f);
        b1b.setI(42);
        b1b.setS("ttt");
        final Bean1a[] b1Array = new Bean1a[]
        { b1a, b1b };
        final Bean2a[] b2Array = BeanUtils.createBean(Bean2a[].class, b1Array);
        assertEquals(b1Array.length, b2Array.length);
        for (int i = 0; i < b1Array.length; ++i)
        {
            final Bean1a b1 = b1Array[i];
            final Bean2a b2 = b2Array[i];
            assertNotNull("Element " + i, b2);
            assertEquals("Element " + i, b1.isB(), b2.isB());
            assertEquals("Element " + i, b1.getF(), b2.getF());
            assertEquals("Element " + i, b1.getI(), b2.getI());
            assertEquals("Element " + i, b1.getS(), b2.getS());
        }
    }

    @Test
    public void testFillPrimitiveArray()
    {
        final int[] array = new int[]
        { -5, 17, 0, 88 };
        final int[] array2 = BeanUtils.createBean(int[].class, array);
        assert Arrays.equals(array, array2);
    }

    @Test
    public void testFillPrimitiveArrayToWrapper()
    {
        final int[] array = new int[]
        { -5, 17, 0, 88 };
        final Integer[] array2 = BeanUtils.createBean(Integer[].class, array);
        for (int i = 0; i < array.length; ++i)
        {
            assertEquals("Element " + i, array[i], array2[i].intValue());
        }
    }

    @Test
    public void testFillPrimitiveArrayFromWrapper()
    {
        final Integer[] array = new Integer[]
        { -5, 17, 0, 88 };
        final int[] array2 = BeanUtils.createBean(int[].class, array);
        for (int i = 0; i < array.length; ++i)
        {
            assertEquals("Element " + i, array[i].intValue(), array2[i]);
        }
    }

    @Test
    public void testFillImmutableArray()
    {
        final String[] array = new String[]
        { "apple", "orange", "banana" };
        final String[] array2 = BeanUtils.createBean(String[].class, array);
        for (int i = 0; i < array.length; ++i)
        {
            assertEquals("Element " + i, array[i], array2[i]);
        }
    }

    public static class ArrayWrapper1
    {
        private byte[] array;

        public byte[] getArray()
        {
            return array;
        }

        public void setArray(final byte[] array)
        {
            this.array = array;
        }
    }

    public static class ArrayWrapper2
    {
        private Byte[] array;

        public Byte[] getArray()
        {
            return array;
        }

        public void setArray(final Byte[] array)
        {
            this.array = array;
        }
    }

    public static class ArrayWrapper3
    {
        private String[] array;

        public String[] getArray()
        {
            return array;
        }

        public void setArray(final String[] array)
        {
            this.array = array;
        }
    }

    @Test
    public void testFillBeanWithPrimitiveArray()
    {
        final ArrayWrapper1 awrapper = new ArrayWrapper1();
        awrapper.setArray(new byte[]
        { -1, 0, 100, -88 });
        final ArrayWrapper1 awrapper2 = BeanUtils.createBean(ArrayWrapper1.class, awrapper);
        final byte[] array = awrapper.getArray();
        final byte[] array2 = awrapper2.getArray();
        assertNotNull(array2);
        assertEquals(array.length, array.length);
        for (int i = 0; i < array.length; ++i)
        {
            assertEquals("Element " + i, array[i], array2[i]);
        }
    }

    @Test
    public void testFillBeanWithPrimitiveWrapperArray()
    {
        final ArrayWrapper1 awrapper = new ArrayWrapper1();
        awrapper.setArray(new byte[]
        { -1, 0, 100, -88 });
        final ArrayWrapper2 awrapper2 = BeanUtils.createBean(ArrayWrapper2.class, awrapper);
        final byte[] array = awrapper.getArray();
        final Byte[] array2 = awrapper2.getArray();
        assertNotNull(array2);
        assertEquals(array.length, array.length);
        for (int i = 0; i < array.length; ++i)
        {
            assertEquals("Element " + i, array[i], array2[i].byteValue());
        }
    }

    public static class CollectionWrapper1
    {
        private List<String> array;

        public List<String> getArray()
        {
            return array;
        }

        @CollectionMapping(collectionClass = ArrayList.class, elementClass = String.class)
        public void setArray(final List<String> array)
        {
            this.array = array;
        }
    }

    @Test
    public void testFillArrayBeanFromCollectionBean()
    {
        final CollectionWrapper1 colWrapper = new CollectionWrapper1();
        final List<String> list = Arrays.asList("blue", "yellow", "green");
        colWrapper.setArray(list);
        final ArrayWrapper3 aWrapper = BeanUtils.createBean(ArrayWrapper3.class, colWrapper);
        final String[] array = aWrapper.getArray();
        assertNotNull(array);
        assertEquals(list.size(), array.length);
        for (int i = 0; i < array.length; ++i)
        {
            assertEquals("Element " + i, list.get(i), array[i]);
        }
    }

    @Test
    public void testFillCollectionBeanFromArrayBean()
    {
        final ArrayWrapper3 aWrapper = new ArrayWrapper3();
        final String[] array = new String[]
        { "hot", "warm", "cool", "icy" };
        aWrapper.setArray(array);
        final CollectionWrapper1 colWrapper =
                BeanUtils.createBean(CollectionWrapper1.class, aWrapper);
        final List<String> list = colWrapper.getArray();
        assertNotNull(list);
        assertEquals(array.length, list.size());
        for (int i = 0; i < array.length; ++i)
        {
            assertEquals("Element " + i, array[i], list.get(i));
        }
    }

    @Test
    public void testFillCollectionBeanFromCollectionBean()
    {
        final CollectionWrapper1 colWrapper = new CollectionWrapper1();
        final List<String> list = new ArrayList<String>(Arrays.asList(new String[]
        { "hot", "warm", "cool", "icy" }));
        colWrapper.setArray(list);
        final CollectionWrapper1 colWrapper2 =
                BeanUtils.createBean(CollectionWrapper1.class, colWrapper);
        final List<String> list2 = colWrapper2.getArray();
        assertNotNull(list2);
        assertEquals(list.size(), list2.size());
        for (int i = 0; i < list.size(); ++i)
        {
            assertEquals("Element " + i, list.get(i), list2.get(i));
        }
    }

    public static class BeanWithBean1
    {
        private Bean1a bean;

        public Bean1a getBean()
        {
            return bean;
        }

        public void setBean(final Bean1a bean)
        {
            this.bean = bean;
        }
    }

    public static class BeanWithBean2
    {
        private Bean2a bean;

        public Bean2a getBean()
        {
            return bean;
        }

        public void setBean(final Bean2a bean)
        {
            this.bean = bean;
        }
    }

    @Test
    public void testFillComplexBeanWithNull()
    {
        final BeanWithBean1 b3 = new BeanWithBean1();
        final BeanWithBean2 b4 = BeanUtils.createBean(BeanWithBean2.class, b3);
        assertEquals(null, b4.getBean());
    }

    @Test
    public void testFillComplexBean()
    {
        final Bean1a b1 = createBean1a();
        final BeanWithBean1 b3 = new BeanWithBean1();
        b3.setBean(b1);
        final BeanWithBean2 b4 = BeanUtils.createBean(BeanWithBean2.class, b3);
        final Bean2a b2 = b4.getBean();
        assertBeansAreEqual("Bean comparison", b1, b2);
    }

    private final static Bean1a createBean1a()
    {
        final Bean1a b1 = new Bean1a();
        b1.setB(true);
        b1.setF(0.2f);
        b1.setI(17);
        b1.setS("test");
        return b1;
    }

    @Test(dependsOnMethods = "testFillComplexBean")
    public final void testFillComplexBeanWithNonNullInnerBean()
    {
        final BeanWithBean1 b1 = new BeanWithBean1();
        final Bean1a bean1a = createBean1a();
        b1.setBean(bean1a);
        final BeanWithBean2 b2 = new BeanWithBean2();
        final Bean2a bean2a = new Bean2a();
        b2.setBean(bean2a);
        BeanUtils.fillBean(BeanWithBean2.class, b2, b1);
        assertBeansAreEqual("Bean comparison", bean1a, b2.getBean());
        // Here is the main difference to 'testFillComplexBean': no new bean has been created but
        // we 'recycled' the already present one.
        assertTrue(bean2a == b2.getBean());
    }

    public static class BeanWithBeanArray1
    {
        private Bean1a[] bean;

        public Bean1a[] getBeanArray()
        {
            return bean;
        }

        public void setBeanArray(final Bean1a[] bean)
        {
            this.bean = bean;
        }
    }

    public static class BeanWithBeanArray2
    {
        private Bean2a[] bean;

        public Bean2a[] getBeanArray()
        {
            return bean;
        }

        public void setBeanArray(final Bean2a[] bean)
        {
            this.bean = bean;
        }
    }

    public static class BeanWithBeanCollection1
    {
        private Collection<Bean1a> bean;

        public Collection<Bean1a> getBeanArray()
        {
            return bean;
        }

        @CollectionMapping(collectionClass = LinkedHashSet.class, elementClass = Bean1a.class)
        public void setBeanArray(final Collection<Bean1a> bean)
        {
            this.bean = bean;
        }
    }

    public static class BeanWithBeanCollection2
    {
        private Collection<Bean2a> bean;

        public Collection<Bean2a> getBeanArray()
        {
            return bean;
        }

        @CollectionMapping(collectionClass = LinkedList.class, elementClass = Bean2a.class)
        public void setBeanArray(final Collection<Bean2a> bean)
        {
            this.bean = bean;
        }
    }

    @Test
    public void testCreateBeanWithBeanCollectionFromBeanWithBeanArray1()
    {
        final Bean1a b1a = new Bean1a();
        b1a.setB(true);
        b1a.setF(0.2f);
        b1a.setI(17);
        b1a.setS("test");
        final Bean1a b1b = new Bean1a();
        b1b.setB(false);
        b1b.setF(1.1f);
        b1b.setI(31);
        b1b.setS("test2");
        final BeanWithBeanArray1 b1Array = new BeanWithBeanArray1();
        final Bean1a[] arrayb1 = new Bean1a[]
        { b1a, b1b };
        b1Array.setBeanArray(arrayb1);
        final BeanWithBeanCollection2 b2Collection =
                BeanUtils.createBean(BeanWithBeanCollection2.class, b1Array);
        final Collection<Bean2a> colb2 = b2Collection.getBeanArray();
        assertNotNull(colb2);
        assertEquals(arrayb1.length, colb2.size());
        final Iterator<Bean2a> itb2 = colb2.iterator();
        for (int i = 0; i < arrayb1.length; ++i)
        {
            assertBeansAreEqual("Element " + i, arrayb1[i], itb2.next());
        }
    }

    @Test
    public void testCreateBeanWithBeanCollectionFromBeanWithBeanArray2()
    {
        final Bean2a b2a = createBean2a();
        final Bean2a b2b = new Bean2a();
        b2b.setB(false);
        b2b.setF(1.1f);
        b2b.setI(31);
        b2b.setS("test2");
        final BeanWithBeanArray2 b2Array = new BeanWithBeanArray2();
        final Bean2a[] arrayb2 = new Bean2a[]
        { b2a, b2b };
        b2Array.setBeanArray(arrayb2);
        final BeanWithBeanCollection1 b1Collection =
                BeanUtils.createBean(BeanWithBeanCollection1.class, b2Array);
        final Collection<Bean1a> colb1 = b1Collection.getBeanArray();
        assertNotNull(colb1);
        assertEquals(arrayb2.length, colb1.size());
        final Iterator<Bean1a> itb1 = colb1.iterator();
        for (int i = 0; i < arrayb2.length; ++i)
        {
            assertBeansAreEqual("Element " + i, arrayb2[i], itb1.next());
        }
    }

    private final static Bean2a createBean2a()
    {
        final Bean2a b2a = new Bean2a();
        b2a.setB(true);
        b2a.setF(0.2f);
        b2a.setI(17);
        b2a.setS("test");
        return b2a;
    }

    @Test
    public void testCreateBeanWithBeanArrayFromBeanWithBeanCollection()
    {
        final Bean1a b1a = createBean1a();
        final Bean1a b1b = createOtherBean1a();
        final BeanWithBeanCollection1 b1Collection = new BeanWithBeanCollection1();
        final Collection<Bean1a> colb1 = new LinkedHashSet<Bean1a>(Arrays.asList(new Bean1a[]
        { b1a, b1b }));
        b1Collection.setBeanArray(colb1);
        final BeanWithBeanArray2 b2Array =
                BeanUtils.createBean(BeanWithBeanArray2.class, b1Collection);
        final Bean2a[] arrayb2 = b2Array.getBeanArray();
        assertNotNull(arrayb2);
        assertEquals(colb1.size(), arrayb2.length);
        int i = 0;
        for (final Bean1a b1 : colb1)
        {
            assertBeansAreEqual("Element " + i, b1, arrayb2[i]);
            ++i;
        }
    }

    private final static Bean1a createOtherBean1a()
    {
        final Bean1a b1b = new Bean1a();
        b1b.setB(false);
        b1b.setF(1.1f);
        b1b.setI(31);
        b1b.setS("test2");
        return b1b;
    }

    @Test
    public void testCreateBeanWithBeanArrayFromBeanWithBeanArray()
    {
        final Bean1a b1a = createBean1a();
        final Bean1a b1b = createOtherBean1a();
        final BeanWithBeanArray1 b1Array = new BeanWithBeanArray1();
        final Bean1a[] arrayb1 = new Bean1a[]
        { b1a, b1b };
        b1Array.setBeanArray(arrayb1);
        final BeanWithBeanArray2 b2Array = BeanUtils.createBean(BeanWithBeanArray2.class, b1Array);
        final Bean2a[] arrayb2 = b2Array.getBeanArray();
        assertNotNull(arrayb2);
        assertBeanArraysAreEqual(arrayb1, arrayb2);
    }

    private final static void assertBeanArraysAreEqual(final Bean1a[] arrayb1,
            final Bean2a[] arrayb2)
    {
        assertEquals(arrayb1.length, arrayb2.length);
        for (int i = 0; i < arrayb1.length; ++i)
        {
            assertBeansAreEqual("Element " + i, arrayb1[i], arrayb2[i]);
        }
    }

    @Test
    public void testCreateBeanWithBeanCollectionFromBeanWithBeanCollection()
    {
        final Bean1a b1a = createBean1a();
        final Bean1a b1b = createOtherBean1a();
        final BeanWithBeanCollection1 b1Collection = new BeanWithBeanCollection1();
        final Collection<Bean1a> colb1 = new LinkedHashSet<Bean1a>(Arrays.asList(new Bean1a[]
        { b1a, b1b }));
        b1Collection.setBeanArray(colb1);
        final BeanWithBeanCollection2 b2Collection =
                BeanUtils.createBean(BeanWithBeanCollection2.class, b1Collection);
        final Collection<Bean2a> colb2 = b2Collection.getBeanArray();
        assertNotNull(colb2);
        assertBeanCollectionsAreEqual(colb1, colb2);
    }

    private final static void assertBeanCollectionsAreEqual(final Collection<Bean1a> colb1,
            final Collection<Bean2a> colb2)
    {
        assertEquals(colb1.size(), colb2.size());
        final Iterator<Bean2a> itb2 = colb2.iterator();
        int i = 0;
        for (final Bean1a b1 : colb1)
        {
            assertBeansAreEqual("Element " + (i++), b1, itb2.next());
        }
    }

    @Test
    public final void testFillBeanWithComplexBeanCollection()
    {
        final Bean1a b1a = createBean1a();
        final Bean1a b1b = createOtherBean1a();
        final BeanWithBeanCollection1 b1 = new BeanWithBeanCollection1();
        final Collection<Bean1a> colb1 = new LinkedHashSet<Bean1a>(Arrays.asList(new Bean1a[]
        { b1a, b1b }));
        b1.setBeanArray(colb1);
        final BeanWithBeanCollection2 b2 = new BeanWithBeanCollection2();
        // With empty collection
        b2.setBeanArray(new LinkedHashSet<Bean2a>(Arrays.asList(new Bean2a[0])));
        BeanUtils.fillBean(BeanWithBeanCollection2.class, b2, b1);
        assertBeanCollectionsAreEqual(b1.getBeanArray(), b2.getBeanArray());
        // With null
        b2.setBeanArray(null);
        BeanUtils.fillBean(BeanWithBeanCollection2.class, b2, b1);
        assertBeanCollectionsAreEqual(b1.getBeanArray(), b2.getBeanArray());
    }

    @Test
    public final void testFillBeanWithComplexBeanArray()
    {
        final Bean1a b1a = createBean1a();
        final Bean1a b1b = createOtherBean1a();
        final BeanWithBeanArray1 b1 = new BeanWithBeanArray1();
        final Bean1a[] colb1 = new Bean1a[]
        { b1a, b1b };
        b1.setBeanArray(colb1);
        final BeanWithBeanArray2 b2 = new BeanWithBeanArray2();
        // With empty collection
        b2.setBeanArray(new Bean2a[0]);
        BeanUtils.fillBean(BeanWithBeanArray2.class, b2, b1);
        assertBeanArraysAreEqual(b1.getBeanArray(), b2.getBeanArray());
        // With null
        b2.setBeanArray(null);
        BeanUtils.fillBean(BeanWithBeanArray2.class, b2, b1);
        assertBeanArraysAreEqual(b1.getBeanArray(), b2.getBeanArray());
    }

    private final static void assertBeansAreEqual(final String msg, final Bean1a b1, final Bean2a b2)
    {
        assertNotNull(msg, b1);
        assertNotNull(msg, b2);
        assertEquals(msg, b1.isB(), b2.isB());
        assertEquals(msg, b1.getF(), b2.getF());
        assertEquals(msg, b1.getI(), b2.getI());
        assertEquals(msg, b1.getS(), b2.getS());
        assertEquals(msg, b1.getBb(), b2.getBb());
    }

    private final static void assertBeansAreEqual(final String msg, final Bean2a b2, final Bean1a b1)
    {
        assertNotNull(msg, b1);
        assertNotNull(msg, b2);
        assertEquals(msg, b2.isB(), b1.isB());
        assertEquals(msg, b2.getF(), b1.getF());
        assertEquals(msg, b2.getI(), b1.getI());
        assertEquals(msg, b2.getS(), b1.getS());
    }

    private static interface IFoo
    {
        public String getFoo();
    }

    public static class FooBean implements IFoo
    {
        private String foo;

        @Override
        public String getFoo()
        {
            return foo;
        }

        public void setFoo(final String foo)
        {
            this.foo = foo;
        }
    }

    public static class BarBean
    {
        private String bar;

        public String getBar()
        {
            return bar;
        }

        public void setBar(final String bar)
        {
            this.bar = bar;
        }
    }

    public final static class BeanWithEnum
    {
        State state;

        public final State getState()
        {
            return state;
        }

        public final void setState(final State state)
        {
            this.state = state;
        }
    }

    @Test
    public void testConverter()
    {
        final FooBean tofuBean = new FooBean();
        tofuBean.setFoo("some tofu");
        final BarBean toFooBean =
                BeanUtils.createBean(BarBean.class, tofuBean, new BeanUtils.Converter()
                    {
                        @SuppressWarnings("unused")
                        public String convertToBar(FooBean foo)
                        {
                            return StringUtils.replace(foo.getFoo(), "tofu", "to Foo");
                        }
                    });
        assertEquals("some to Foo", toFooBean.getBar());
    }

    @Test
    public void testConverterWithArgumentOfInterfaceType()
    {
        final FooBean tofuBean = new FooBean();
        tofuBean.setFoo("some tofu");
        final BarBean toFooBean =
                BeanUtils.createBean(BarBean.class, tofuBean, new BeanUtils.Converter()
                    {
                        @SuppressWarnings("unused")
                        public String convertToBar(IFoo foo)
                        {
                            return StringUtils.replace(foo.getFoo(), "tofu", "to Foo");
                        }
                    });
        assertEquals("some to Foo", toFooBean.getBar());
    }

    @Test
    public final void testWithEnum()
    {
        final BeanWithEnum bean1 = new BeanWithEnum();
        bean1.setState(State.BLOCKED);
        final BeanWithEnum bean2 = BeanUtils.createBean(BeanWithEnum.class, bean1);
        assertEquals(State.BLOCKED, bean2.getState());
    }

    public static class CyclicBeanA1
    {
        private String name;

        private CyclicBeanB1 cyclicBeanB;

        private CyclicBeanA1[] cyclicBeans;

        public final String getName()
        {
            return name;
        }

        public final void setName(String name)
        {
            this.name = name;
        }

        public final CyclicBeanB1 getCyclicBeanB()
        {
            return cyclicBeanB;
        }

        public final void setCyclicBeanB(CyclicBeanB1 cyclicBeanB)
        {
            this.cyclicBeanB = cyclicBeanB;
        }

        public final CyclicBeanA1[] getCyclicBeans()
        {
            return cyclicBeans;
        }

        public final void setCyclicBeans(CyclicBeanA1... cyclicBeans)
        {
            this.cyclicBeans = cyclicBeans;
        }
    }

    public static class CyclicBeanB1
    {
        private String name;

        private CyclicBeanA1 cyclicBeanA;

        private List<CyclicBeanB1> cyclicBeans;

        public final String getName()
        {
            return name;
        }

        public final void setName(String name)
        {
            this.name = name;
        }

        public final CyclicBeanA1 getCyclicBeanA()
        {
            return cyclicBeanA;
        }

        public final void setCyclicBeanA(CyclicBeanA1 cyclicBeanA)
        {
            this.cyclicBeanA = cyclicBeanA;
        }

        public final List<CyclicBeanB1> getCyclicBeans()
        {
            return cyclicBeans;
        }

        public final void setCyclicBeans(List<CyclicBeanB1> cyclicBeans)
        {
            this.cyclicBeans = cyclicBeans;
        }
    }

    public static class CyclicBeanA2
    {
        private String name;

        private CyclicBeanB2 cyclicBeanB;

        private CyclicBeanA2[] cyclicBeans;

        public final String getName()
        {
            return name;
        }

        public final void setName(String name)
        {
            this.name = name;
        }

        public final CyclicBeanB2 getCyclicBeanB()
        {
            return cyclicBeanB;
        }

        public final void setCyclicBeanB(CyclicBeanB2 cyclicBeanB)
        {
            this.cyclicBeanB = cyclicBeanB;
        }

        public final CyclicBeanA2[] getCyclicBeans()
        {
            return cyclicBeans;
        }

        public final void setCyclicBeans(CyclicBeanA2... cyclicBeans)
        {
            this.cyclicBeans = cyclicBeans;
        }
    }

    public static class CyclicBeanB2
    {
        private String name;

        private CyclicBeanA2 cyclicBeanA;

        private List<CyclicBeanB2> cyclicBeans;

        public final String getName()
        {
            return name;
        }

        public final void setName(String name)
        {
            this.name = name;
        }

        public final CyclicBeanA2 getCyclicBeanA()
        {
            return cyclicBeanA;
        }

        public final void setCyclicBeanA(CyclicBeanA2 cyclicBeanA)
        {
            this.cyclicBeanA = cyclicBeanA;
        }

        public final List<CyclicBeanB2> getCyclicBeans()
        {
            return cyclicBeans;
        }

        @CollectionMapping(collectionClass = ArrayList.class, elementClass = CyclicBeanB2.class)
        public final void setCyclicBeans(List<CyclicBeanB2> cyclicBeans)
        {
            this.cyclicBeans = cyclicBeans;
        }
    }

    @Test
    public void testConversionOfCyclicBeans()
    {
        CyclicBeanA1 cyclicBeanA1 = new CyclicBeanA1();
        CyclicBeanB1 cyclicBeanB1 = new CyclicBeanB1();
        cyclicBeanA1.setName("a");
        cyclicBeanA1.setCyclicBeanB(cyclicBeanB1);
        cyclicBeanA1.setCyclicBeans(cyclicBeanA1);
        cyclicBeanB1.setName("b");
        cyclicBeanB1.setCyclicBeanA(cyclicBeanA1);
        List<CyclicBeanB1> beans = new ArrayList<CyclicBeanB1>();
        beans.add(cyclicBeanB1);
        cyclicBeanB1.setCyclicBeans(beans);

        CyclicBeanA2 cyclicBeanA2 = BeanUtils.createBean(CyclicBeanA2.class, cyclicBeanA1);
        assertEquals("a", cyclicBeanA2.getName());
        CyclicBeanB2 cyclicBeanB2 = cyclicBeanA2.getCyclicBeanB();
        assertEquals("b", cyclicBeanB2.getName());
        assertSame(cyclicBeanA2, cyclicBeanB2.getCyclicBeanA());
        assertEquals(1, cyclicBeanA2.getCyclicBeans().length);
        assertEquals("a", cyclicBeanA2.getCyclicBeans()[0].getName());
        assertSame(cyclicBeanA2, cyclicBeanA2.getCyclicBeans()[0]);
        assertEquals(1, cyclicBeanB2.getCyclicBeans().size());
        assertEquals("b", cyclicBeanB2.getCyclicBeans().get(0).getName());
        assertSame(cyclicBeanB2, cyclicBeanB2.getCyclicBeans().get(0));
    }

    @Test
    public void testFillBeanFromProperties()
    {
        final Properties props = new Properties();
        props.setProperty("i", "17");
        props.setProperty("s", "bla");
        props.setProperty("b", "true");
        props.setProperty("f", "3.14159");
        props.setProperty("bb", "1");
        final Bean2a b = BeanUtils.createBean(Bean2a.class, props, new BeanUtils.Converter()
            {
                @SuppressWarnings("unused")
                public boolean convertToBb(Properties myProps)
                {
                    return "1".equals(myProps.get("bb"));
                }
            });
        assertEquals(17, b.getI());
        assertEquals("bla", b.getS());
        assertTrue(b.isB());
        assertEquals(3.14159f, b.getF());
        assertTrue(b.getBb());
    }
}