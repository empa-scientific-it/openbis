/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.lang.reflect.Method;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.reflection.MethodUtils;

/**
 * Test cases for the {@link MethodUtilsTest}.
 * 
 * @author Franz-Josef Elmer
 */
public final class MethodUtilsTest extends AssertJUnit
{
    private final void privateMethodOnStack()
    {
        // Because we use <code>Class.getDeclaredMethods</code> were used instead of
        // <code>Class.getMethods</code>, we get
        // 'ch.systemsx.cisd.common.utilities.MethodUtilsTest.privateMethodOnStack()' here.
        assertEquals("privateMethodOnStack", MethodUtils.getMethodOnStack(1).getName());
    }

    @Test
    public void testMethodWithNoParameters() throws Exception
    {
        assertEquals("toString()", MethodUtils.toString(String.class.getMethod("toString")));
    }

    @Test
    public void testMethodWithOneParameter() throws Exception
    {
        assertEquals("startsWith(String)", MethodUtils.toString(String.class.getMethod(
                "startsWith", String.class)));
    }

    @Test
    public void testMethodWithTwoParameters() throws Exception
    {
        assertEquals("split(String, int)", MethodUtils.toString(String.class.getMethod("split",
                String.class, Integer.TYPE)));
    }

    @Test
    public final void testGetCurrentMethod()
    {
        assertEquals("testGetCurrentMethod", MethodUtils.getCurrentMethod().getName());
        // Border cases
        assertEquals(new SameMethodName().getMethodName(), new SameMethodName().getMethodName(
                new Object(), new Object()));
    }

    @Test
    public final void testGetMethodOnStack()
    {
        assertEquals("getMethodOnStack", MethodUtils.getMethodOnStack(0).getName());
        assertEquals("testGetMethodOnStack", MethodUtils.getMethodOnStack(1).getName());
        assertEquals("invoke0", MethodUtils.getMethodOnStack(2).getName());
        privateMethodOnStack();
    }

    @Test
    public final void testDescribeMethod()
    {
        final Method method = Object.class.getMethods()[0];
        final String methodDescription = MethodUtils.describeMethod(method);
        assertEquals("Object." + Object.class.getMethods()[0].getName(), methodDescription);
    }

    //
    // Helper classes
    //

    private final static class SameMethodName
    {

        public final String getMethodName()
        {
            final StackTraceElement[] elements = new Throwable().getStackTrace();
            return elements[0].getMethodName();
        }

        public final String getMethodName(final Object one, final Object two)
        {
            final StackTraceElement[] elements = new Throwable().getStackTrace();
            return elements[0].getMethodName();
        }
    }
}
