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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtilReaderPool.ReaderUtil;

/**
 * @author Jakub Straszewski
 */
public class ImageUtilReaderPoolTest extends AssertJUnit
{
    Mockery context;

    ReaderUtil<Object> readerUtil;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        readerUtil = context.mock(ImageUtilReaderPool.ReaderUtil.class);
    }

    @Test
    public void testTwoSessionsSimple()
    {
        ImageUtilReaderPool<Object> pool = new ImageUtilReaderPool<>(readerUtil);

        final Object reader1 = new Object();
        final Object reader2 = new Object();
        final Object reader3 = new Object();

        context.checking(new Expectations()
            {
                {
                    one(readerUtil).create("a", "a");
                    will(returnValue(reader1));
                    one(readerUtil).create("a", "a");
                    will(returnValue(reader2));
                    one(readerUtil).create("a", "a");
                    will(returnValue(reader3));

                    allowing(readerUtil).isSameLibraryAndReader(with(any(String.class)), with(any(String.class)), with(any(String.class)));
                    will(returnValue(true));

                    one(readerUtil).close(reader1);
                    one(readerUtil).close(reader2);
                }
            });

        Object result1 = pool.get("session", "a", "a");
        Object result2 = pool.get("session", "a", "a");
        pool.put("session", result1);

        Object resultOther = pool.get("session2", "a", "a");

        Object result3 = pool.get("session", "a", "a");

        pool.put("session", result2);
        pool.put("session", result3);

        pool.releaseSession("session");

        assertSame(reader1, result1);
        assertSame(reader1, result3);
        assertSame(reader2, result2);

        assertSame(reader3, resultOther);

        context.assertIsSatisfied();
    }

    @Test
    public void testOneSessionMultipleThreads() throws InterruptedException
    {
        final ImageUtilReaderPool<Object> pool = new ImageUtilReaderPool<>(readerUtil);

        final int numberOfThreads = 10;

        final Object[] readers = new Object[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++)
        {
            readers[i] = new Object();
        }

        context.checking(new Expectations()
            {
                {
                    // we expect creation of exactly as many readers as there are threads fighting for the resource
                    for (int i = 0; i < numberOfThreads; i++)
                    {
                        Object reader = readers[i];
                        one(readerUtil).create("a", "a");
                        will(returnValue(reader));
                    }

                    allowing(readerUtil).isSameLibraryAndReader(with(any(String.class)), with(any(String.class)), with(any(String.class)));
                    will(returnValue(true));
                }
            });

        final Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++)
        {
            threads[i] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (int x = 0; x < 10; x++)
                        {
                            Object reader = pool.get("session", "a", "a");
                            // sleep long enough, so that all threads will fight for resources
                            try
                            {
                                Thread.sleep(100);
                            } catch (InterruptedException ex)
                            {
                            }
                            pool.put("session", reader);
                        }
                    }
                });
        }

        for (int i = 0; i < numberOfThreads; i++)
        {
            threads[i].start();
        }

        for (int i = 0; i < numberOfThreads; i++)
        {
            threads[i].join();
        }

        context.assertIsSatisfied();
    }
}
