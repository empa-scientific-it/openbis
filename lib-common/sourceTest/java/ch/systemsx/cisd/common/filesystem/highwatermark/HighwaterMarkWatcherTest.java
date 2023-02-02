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

package ch.systemsx.cisd.common.filesystem.highwatermark;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.filesystem.highwatermark.HighwaterMarkWatcher.HighwaterMarkState;
import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * Test cases for the {@link HighwaterMarkWatcher}.
 * 
 * @author Christian Ribeaud
 */
public final class HighwaterMarkWatcherTest
{
    private static final HostAwareFile DEFAULT_PATH = new HostAwareFile(new File("/my/path"));

    private static final int DEFAULT_WATERMARK = 100;

    private Mockery context;

    private HighwaterMarkWatcher highwaterMarkWatcher;

    private IFreeSpaceProvider freeSpaceProvider;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        logRecorder = new BufferedAppender("%m", Level.INFO, ".*NotificationLogChangeListener");
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        highwaterMarkWatcher = createHighwaterMarkWatcher(DEFAULT_WATERMARK);
    }

    private final HighwaterMarkWatcher createHighwaterMarkWatcher(final long highwaterMark)
    {
        return new HighwaterMarkWatcher(highwaterMark, freeSpaceProvider);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @DataProvider(name = "highwaterMarks")
    public final Object[][] getHighwaterMarks()
    {
        return new Object[][]
        {
                { 0 },
                { -1 },
                { 100 } };
    }

    @Test(dataProvider = "highwaterMarks")
    public final void testConstructor(final long highwaterMark)
    {
        final HighwaterMarkWatcher watcher = new HighwaterMarkWatcher(highwaterMark);
        assertEquals(highwaterMark, watcher.getHighwaterMark());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRun()
    {
        boolean fail = true;
        try
        {
            highwaterMarkWatcher.run();
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final HighwaterMarkWatcher watcher = new HighwaterMarkWatcher(-1);
        watcher.setPath(DEFAULT_PATH);
        // -1 means infinity, so no call to IFreeSpaceProvider
        watcher.run();
    }

    @DataProvider(name = "freeSpaces")
    public final Object[][] getFreeSpaces(Method m)
    {
        int size = Integer.parseInt(m.getName().substring(m.getName().indexOf("Below") + 5));
        return new Object[][]
        {
                { size - 1, true },
                { size, false },
                { size + 1, false } };
    }

    @Test(dataProvider = "freeSpaces")
    public final void testIsBelow100(final long freeSpace, final boolean isBelow)
            throws IOException
    {
        assertFalse(highwaterMarkWatcher.isBelow());
        highwaterMarkWatcher.setPath(DEFAULT_PATH);
        context.checking(new Expectations()
            {
                {
                    one(freeSpaceProvider).freeSpaceKb(DEFAULT_PATH);
                    will(returnValue(freeSpace));
                }
            });
        highwaterMarkWatcher.run();
        assertEquals(isBelow, highwaterMarkWatcher.isBelow());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "freeSpaces")
    public final void testIsBelow50(final long freeSpace, final boolean isBelow) throws IOException
    {
        assertFalse(highwaterMarkWatcher.isBelow());

        final HostAwareFileWithHighwaterMark path =
                new HostAwareFileWithHighwaterMark(DEFAULT_PATH.getPath(), 50);
        highwaterMarkWatcher.setPath(path);
        context.checking(new Expectations()
            {
                {
                    one(freeSpaceProvider).freeSpaceKb(path);
                    will(returnValue(freeSpace));
                }
            });
        highwaterMarkWatcher.run();
        assertEquals(isBelow, highwaterMarkWatcher.isBelow());
        context.assertIsSatisfied();
    }

    @Test
    public final void testIsBelowWithNegativeValue() throws IOException
    {
        final HighwaterMarkWatcher watcher = createHighwaterMarkWatcher(-1);
        watcher.setPath(DEFAULT_PATH);
        watcher.run();
        assertEquals(false, highwaterMarkWatcher.isBelow());
        context.assertIsSatisfied();
    }

    @Test
    public final void testIsBelowWithZero() throws IOException
    {
        final HighwaterMarkWatcher watcher = createHighwaterMarkWatcher(0);
        watcher.setPath(DEFAULT_PATH);
        context.checking(new Expectations()
            {
                {
                    one(freeSpaceProvider).freeSpaceKb(DEFAULT_PATH);
                    will(returnValue(0L));
                }
            });
        watcher.run();
        assertEquals(false, highwaterMarkWatcher.isBelow());
        context.assertIsSatisfied();
    }

    private int i;

    @Test
    public final void testNofiticationChangeListener() throws IOException
    {
        i = 0;
        final long[] freeSpaces =
        { 99L, 101L };
        context.checking(new Expectations()
            {
                {
                    allowing(freeSpaceProvider).freeSpaceKb(DEFAULT_PATH);
                    will(new CustomAction("Free space provider")
                        {

                            //
                            // CustomAction
                            //

                            @Override
                            public final Object invoke(final Invocation invocation)
                                    throws Throwable
                            {
                                return freeSpaces[i];
                            }
                        });
                }
            });
        // Space becomes tight. So inform the administrator.
        highwaterMarkWatcher.setPathAndRun(DEFAULT_PATH);
        final long missingSpace = DEFAULT_WATERMARK - freeSpaces[i];
        assertEquals(String.format(
                HighwaterMarkWatcher.NotificationLogChangeListener.WARNING_LOG_FORMAT,
                DEFAULT_PATH.getPathDescription(),
                HighwaterMarkWatcher.displayKilobyteValue(DEFAULT_WATERMARK),
                HighwaterMarkWatcher.displayKilobyteValue(freeSpaces[i]),
                HighwaterMarkWatcher.displayKilobyteValue(missingSpace)),
                logRecorder.getLogContent());
        // Space still "red". Do not inform the administrator. He already knows it.
        logRecorder.resetLogContent();
        highwaterMarkWatcher.setPathAndRun(DEFAULT_PATH);
        assertEquals("", logRecorder.getLogContent());
        // Space becomes again OK. So inform the administrator.
        i++;
        logRecorder.resetLogContent();
        highwaterMarkWatcher.setPathAndRun(DEFAULT_PATH);
        assertEquals(String.format(
                HighwaterMarkWatcher.NotificationLogChangeListener.INFO_LOG_FORMAT,
                DEFAULT_PATH.getPathDescription(),
                HighwaterMarkWatcher.displayKilobyteValue(DEFAULT_WATERMARK),
                HighwaterMarkWatcher.displayKilobyteValue(freeSpaces[i])),
                logRecorder.getLogContent());
        // Space still OK. Do not inform the administrator.
        logRecorder.resetLogContent();
        highwaterMarkWatcher.setPathAndRun(DEFAULT_PATH);
        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public final void testHighwaterMarkState() throws IOException
    {
        final long freeSpace = 123L;
        context.checking(new Expectations()
            {
                {
                    one(freeSpaceProvider).freeSpaceKb(DEFAULT_PATH);
                    will(returnValue(freeSpace));
                }
            });
        final HighwaterMarkState highwaterMarkState =
                highwaterMarkWatcher.getHighwaterMarkState(DEFAULT_PATH);
        assertEquals(highwaterMarkState.getFreeSpace(), freeSpace);
        assertEquals(highwaterMarkState.getPath(), DEFAULT_PATH.getPath());
        assertEquals(highwaterMarkState.getHighwaterMark(), DEFAULT_WATERMARK);
        assertFalse(HighwaterMarkWatcher.isBelow(highwaterMarkState));
        context.assertIsSatisfied();
    }
}