/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.BuildAndEnvironmentInfo;

/**
 * Test cases for {@link RandomAccessFileImpl}.
 *
 * @author Bernd Rinn
 */
public class ByteBufferRandomAccessFileTests extends IRandomAccessFileTests
{

    @Override
    protected IRandomAccessFile createRandomAccessFile(String name)
    {
        return new ByteBufferRandomAccessFile(4096);
    }

    @Override
    protected IRandomAccessFile createRandomAccessFile(String name, byte[] content)
    {
        return new ByteBufferRandomAccessFile(content);
    }

    public static void main(String[] args) throws Throwable
    {
        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
        System.out.println("Test class: " + ByteBufferRandomAccessFileTests.class.getSimpleName());
        System.out.println();
        final ByteBufferRandomAccessFileTests test = new ByteBufferRandomAccessFileTests();
        try
        {
            for (Method m : ByteBufferRandomAccessFileTests.class.getMethods())
            {
                final Test testAnnotation = m.getAnnotation(Test.class);
                if (testAnnotation == null)
                {
                    continue;
                }
                if (m.getParameterTypes().length == 0)
                {
                    System.out.println("Running " + m.getName());
                    test.setUp();
                    try
                    {
                        m.invoke(test);
                    } catch (InvocationTargetException wrapperThrowable)
                    {
                        final Throwable th = wrapperThrowable.getCause();
                        boolean exceptionFound = false;
                        for (Class<?> expectedExClazz : testAnnotation.expectedExceptions())
                        {
                            if (expectedExClazz == th.getClass())
                            {
                                exceptionFound = true;
                                break;
                            }
                        }
                        if (exceptionFound == false)
                        {
                            throw th;
                        }
                    }
                }
            }
            System.out.println("Tests OK!");
        } finally
        {
            test.afterClass();
        }
    }

}
