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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.Test;
import org.testng.remote.SuiteSlave;

import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.common.time.TimingParametersTest;

/**
 * Test cases for the {@link ClassUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class ClassUtilsTest
{

    @Test
    public void testGatherAllCastableClassesAndInterfacesFor()
    {
        final ExtendingExtendingA object = new ExtendingExtendingA();
        final Collection<Class<?>> classes =
                ClassUtils.gatherAllCastableClassesAndInterfacesFor(object);
        final Iterator<Class<?>> iterator = classes.iterator();
        assertSame(ExtendingExtendingA.class, iterator.next());
        assertSame(ExtendingA.class, iterator.next());
        assertSame(A.class, iterator.next());
        assertSame(Object.class, iterator.next());
        assertSame(IB.class, iterator.next());
        assertSame(IA.class, iterator.next());
        assertSame(IExtendingIA.class, iterator.next());

        assertEquals(false, iterator.hasNext());
    }

    @Test
    public void testCreateWithDefaultConstructor()
    {
        final CharSequence cs =
                ClassUtils
                        .create(CharSequence.class, StringBuffer.class.getName(), (Object[]) null);
        assertTrue(cs instanceof StringBuffer);
        assertEquals(0, cs.length());
    }

    @Test
    public void testCreateWithPropertiesConstructor()
    {
        final Properties properties = new Properties();
        final Appendable appendable =
                ClassUtils.create(Appendable.class, MyClass.class.getName(), properties);
        assertTrue(appendable instanceof MyClass);
        assertSame(properties, ((MyClass) appendable).properties);
    }

    @Test
    public void testCreateWithIncompatibleSuperclass()
    {
        try
        {
            ClassUtils.create(Float.class, Integer.class.getName(), (Object[]) null);
            fail("AssertionError expected.");
        } catch (final AssertionError e)
        {
            assertEquals(
                    "Class 'java.lang.Integer' does not implements/extends 'java.lang.Float'.", e
                            .getMessage());
        }
    }

    @Test
    public void testCreateInstanceOfAnInterface()
    {
        try
        {
            ClassUtils.create(Float.class, CharSequence.class.getName(), (Object[]) null);
            fail("AssertionError expected.");
        } catch (final AssertionError e)
        {
            assertEquals(
                    "Interface 'java.lang.CharSequence' can not be instanciated as it is an interface.",
                    e.getMessage());
        }
    }

    @Test
    public final void testCreateInstanceWithAnInterfaceAsConstructorArgument()
    {
        final List<String> list = new ArrayList<String>();
        list.add("Hello");
        final Appendable appendable =
                ClassUtils.create(Appendable.class, MyClass.class.getName(), list);
        assertSame(list, ((MyClass) appendable).iterable);
    }

    private static interface AnInterfaceWithOnlyVoidMethods
    {
        public void exec();

        public void print(String message);
    }

    private static interface AnInterfaceWithNotOnlyVoidMethods
    {
        public int exec();

        public void print(String message);
    }

    @Test
    public void testAssertInterfaceWithOnlyVoidMethodsWithAnInterfaceWithOnlyVoidMethods()
    {
        ClassUtils.assertInterfaceWithOnlyVoidMethods(AnInterfaceWithOnlyVoidMethods.class);
    }

    @Test
    public void testAssertInterfaceWithOnlyVoidMethodsWithAnInterfaceWithNotOnlyVoidMethods()
    {
        try
        {
            ClassUtils.assertInterfaceWithOnlyVoidMethods(AnInterfaceWithNotOnlyVoidMethods.class);
            fail("AssertionError expected");
        } catch (AssertionError e)
        {
            assertEquals("Method " + AnInterfaceWithNotOnlyVoidMethods.class.getName()
                    + ".exec has non-void return type: int", e.getMessage());
        }
    }

    @Test
    public void testAssertInterfaceWithOnlyVoidMethodsWithAClass()
    {
        try
        {
            ClassUtils.assertInterfaceWithOnlyVoidMethods(String.class);
            fail("AssertionError expected");
        } catch (AssertionError e)
        {
            assertEquals("Is not an interface: java.lang.String", e.getMessage());
        }
    }

    @Test
    public final void testSetFieldValueWithExpectedThrowable()
    {
        final MyClass myClass = new MyClass((Iterable<String>) null);
        try
        {
            ClassUtils.setFieldValue(myClass, "", null);
            fail("Blank field name.");
        } catch (final AssertionError error)
        {
        }
        assertFalse(ClassUtils.setFieldValue(myClass, "doesNotExist", null));
    }

    @Test
    public final void testSetFieldValue()
    {
        final MyClass myClass = new MyClass((Iterable<String>) null);
        assertNull(myClass.iterable);
        final List<String> list = new ArrayList<String>();
        ClassUtils.setFieldValue(myClass, "iterable", list);
        assertNotNull(myClass.iterable);
        assertSame(list, myClass.iterable);
        ClassUtils.setFieldValue(myClass, "iterable", null);
        assertNull(myClass.iterable);
    }

    @Test
    public final void testSetFieldValueWithSubclass()
    {
        final MyExtendedClass myExtendedClass = new MyExtendedClass((Iterable<String>) null);
        assertNull(myExtendedClass.iterable);
        final List<String> list = new ArrayList<String>();
        ClassUtils.setFieldValue(myExtendedClass, "iterable", list);
        assertNotNull(myExtendedClass.iterable);
        assertSame(list, myExtendedClass.iterable);
        final Object object = new Object();
        ClassUtils.setFieldValue(myExtendedClass, "finalObject", object);
        assertSame(object, myExtendedClass.finalObject);
    }

    @Test
    public final void testListClassesFailed()
    {
        boolean fail = true;
        try
        {
            ClassUtils.listClasses(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        try
        {
            ClassUtils.listClasses("does.not.exist", null);
            fail("'" + IllegalArgumentException.class.getName() + "' expected.");
        } catch (final IllegalArgumentException e)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testListClasses()
    {
        // With 'file' protocol
        List<Class<?>> classes = ClassUtils.listClasses("ch.systemsx.cisd.common.time", null);
        assertTrue(classes.size() > 0);
        assertTrue(classes.contains(TimingParameters.class));
        assertTrue(classes.contains(TimingParametersTest.class));
        // With 'jar' protocol
        classes = ClassUtils.listClasses("org.testng.annotations", null);
        assertTrue(classes.size() > 0);
        assertTrue(classes.contains(Test.class));
        classes = ClassUtils.listClasses("org.testng.remote", new IClassFilter()
            {

                //
                // IClassFilter
                //

                @Override
                public final boolean accept(final Class<?> clazz)
                {
                    return clazz.equals(SuiteSlave.class) == false;
                }

                @Override
                public boolean accept(String fullyQualifiedClassName)
                {
                    return true;
                }
            });
        assertTrue(classes.size() > 0);
        assertFalse(classes.contains(Test.class));
    }

    //
    // Helper Classes
    //

    public static class MyClass implements Appendable
    {
        Properties properties;

        Iterable<String> iterable;

        public MyClass(final Properties properties)
        {
            this.properties = properties;
        }

        public MyClass(final Iterable<String> iterable)
        {
            this.iterable = iterable;
        }

        //
        // Appendable
        //

        @Override
        public Appendable append(final char c) throws IOException
        {
            return null;
        }

        @Override
        public Appendable append(final CharSequence csq, final int start, final int end)
                throws IOException
        {
            return null;
        }

        @Override
        public Appendable append(final CharSequence csq) throws IOException
        {
            return null;
        }

    }

    public final static class MyExtendedClass extends MyClass
    {

        private final Object finalObject = null;

        public MyExtendedClass(final Iterable<String> iterable)
        {
            super(iterable);
        }

        public MyExtendedClass(final Properties properties)
        {
            super(properties);
        }

    }

    private static interface IA<T>
    {
    }

    private static interface IExtendingIA<T> extends IA<T>
    {
    }

    private static interface IB
    {
    }

    private static class A
    {
    }

    private static class ExtendingA extends A implements IExtendingIA<String>
    {
    }

    private static class ExtendingExtendingA extends ExtendingA implements IB, IA<String>
    {
    }

}
