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

package ch.systemsx.cisd.common.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.Serializable;
import java.util.List;

import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class InterfaceForRMICheckerTest
{
    public static class NonSerializableClass
    {
    }

    @Test
    public void testRMIReadyInterface()
    {
        InterfaceForRMIChecker.assertInterfaceForRMI(RMIReadyInterface.class);
    }

    public interface RMIReadyInterface
    {
        public int get();

        public void set(int a);

        public Void set2(String s);

        public Number get2();
    }

    @Test
    public void testRMIReadyInterfaceWhichRefersToItself()
    {
        InterfaceForRMIChecker.assertInterfaceForRMI(RMIReadyInterfaceWhichRefersToItself.class);
    }

    public interface RMIReadyInterfaceWhichRefersToItself extends Serializable
    {
        public void set(RMIReadyInterfaceWhichRefersToItself interfaze);
    }

    @Test
    public void testInterfaceWithNonSerializableParameter()
    {
        checkNonRMIReadyInterface(InterfaceWithNonSerializableParameter.class);
    }

    public interface InterfaceWithNonSerializableParameter
    {
        public void set(NonSerializableClass a);
    }

    @Test
    public void testInterfaceWithNonSerializableReturnValue()
    {
        checkNonRMIReadyInterface(InterfaceWithNonSerializableParameter.class);
    }

    public interface InterfaceWithNonSerializableReturnValue
    {
        public NonSerializableClass get();
    }

    @Test
    public void testInterfaceExtendsNonRMIReadyInterface()
    {
        checkNonRMIReadyInterface(InterfaceExtendsNonRMIReadyInterface.class);
    }

    public interface InterfaceExtendsNonRMIReadyInterface extends
            InterfaceWithNonSerializableParameter
    {
        public int get();
    }

    @Test
    public void testSerializablityOfSerializableClass()
    {
        InterfaceForRMIChecker.assertSerializable(SerializableClass.class);
    }

    public static class SerializableClass implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private static NonSerializableClass non;

        private int n;

        private transient NonSerializableClass transientNon;

        private List<String> list;

        @Override
        public String toString()
        {
            return non.toString() + n + transientNon;
        }

        public List<String> getList()
        {
            return list;
        }
    }

    @Test
    public void testSerializablityOfSerializableClassExtendsNonSerializableClass()
    {
        InterfaceForRMIChecker
                .assertSerializable(SerializableClassExtendsNonSerializableClass.class);
    }

    public static class SerializableClassExtendsNonSerializableClass extends NonSerializableClass
            implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private static NonSerializableClass non;

        private int n;

        private transient NonSerializableClass transientNon;

        private List<String> list;

        @Override
        public String toString()
        {
            return non.toString() + n + transientNon;
        }

        public List<String> getList()
        {
            return list;
        }
    }

    @Test
    public void testSerializablityOfNonSerializableClass()
    {
        checkNonSerializableClass(NonSerializableClass.class);
    }

    @Test
    public void testSerializablityOfClassWithNonSerializableAttribute()
    {
        checkNonSerializableClass(ClassWithNonSerializableAttribute.class);
    }

    public static class ClassWithNonSerializableAttribute implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private NonSerializableClass nonSerializable;

        @Override
        public String toString()
        {
            return nonSerializable.toString();
        }
    }

    @Test
    public void testSerializablityOfClassWithAnArrayOfNonSerializables()
    {
        checkNonSerializableClass(ClassWithAnArrayOfNonSerializables.class);
    }

    public static class ClassWithAnArrayOfNonSerializables implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private NonSerializableClass[] nonSerializables;

        @Override
        public String toString()
        {
            return nonSerializables.toString();
        }
    }

    @Test
    public void testSerializablityOfClassWithAListOfNonSerializables()
    {
        checkNonSerializableClass(ClassWithAListOfNonSerializables.class);
    }

    public static class ClassWithAListOfNonSerializables implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private List<NonSerializableClass> nonSerializables;

        @Override
        public String toString()
        {
            return nonSerializables.toString();
        }
    }

    @Test
    public void testClassWithExtendingClassWithNonSerializableAttribute()
    {
        checkNonSerializableClass(ClassWithExtendingClassWithNonSerializableAttribute.class);
    }

    public static class ClassWithExtendingClassWithNonSerializableAttribute extends
            ClassWithNonSerializableAttribute
    {
        private static final long serialVersionUID = 1L;
    }

    private void checkNonRMIReadyInterface(Class<?> interfaze)
    {
        boolean failed = false;
        try
        {
            InterfaceForRMIChecker.assertInterfaceForRMI(interfaze);
            // Can not use fail() directly because it throws also an AssertionError
            failed = true;
        } catch (AssertionError e)
        {
            checkErrorMessage(e);
        }
        if (failed)
        {
            fail("AssertionError expected");
        }
    }

    private void checkNonSerializableClass(Class<?> clazz)
    {
        boolean failed = false;
        try
        {
            InterfaceForRMIChecker.assertSerializable(clazz);
            // Can not use fail() directly because it throws also an AssertionError
            failed = true;
        } catch (AssertionError e)
        {
            checkErrorMessage(e);
        }
        if (failed)
        {
            fail("AssertionError expected");
        }
    }

    private void checkErrorMessage(AssertionError e)
    {
        assertEquals(NonSerializableClass.class + " does not implement java.io.Serializable", e
                .getMessage());
    }

}
