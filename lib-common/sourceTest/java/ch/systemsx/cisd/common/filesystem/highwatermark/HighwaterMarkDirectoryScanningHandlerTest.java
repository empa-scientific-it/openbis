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
import static org.testng.AssertJUnit.assertNull;

import java.io.File;
import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask.IScannedStore;
import ch.systemsx.cisd.common.filesystem.IDirectoryScanningHandler.HandleInstruction;
import ch.systemsx.cisd.common.filesystem.IDirectoryScanningHandler.HandleInstructionFlag;
import ch.systemsx.cisd.common.filesystem.highwatermark.HighwaterMarkDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.highwatermark.HighwaterMarkWatcher;

/**
 * Test cases for the {@link HighwaterMarkDirectoryScanningHandler}.
 * 
 * @author Christian Ribeaud
 */
public final class HighwaterMarkDirectoryScanningHandlerTest
{
    private static final String STORE_NAME = "store-name";

    private static final long HIGHWATER_MARK = 100L;

    private Mockery context;

    private IDirectoryScanningHandler directoryScanningHandler;

    private IScannedStore scannedStore;

    private HighwaterMarkWatcher highwaterMarkWatcher;

    private IFreeSpaceProvider freeSpaceProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        directoryScanningHandler = context.mock(IDirectoryScanningHandler.class);
        scannedStore = context.mock(IScannedStore.class);

        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        highwaterMarkWatcher = new HighwaterMarkWatcher(HIGHWATER_MARK, freeSpaceProvider);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private final HighwaterMarkDirectoryScanningHandler createDirectoryScanningHandler(
            final File... files)
    {
        return new HighwaterMarkDirectoryScanningHandler(directoryScanningHandler,
                highwaterMarkWatcher, files);
    }

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new HighwaterMarkDirectoryScanningHandler(null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @DataProvider(name = "freeSpaceProvider")
    public final Object[][] getFreeSpaces()
    {
        return new Object[][]
        {
                { HIGHWATER_MARK - 1 },
                { HIGHWATER_MARK + 1 } };
    }

    @Test(dataProvider = "freeSpaceProvider")
    public final void testWithoutFiles(final long freeSpace) throws IOException
    {
        final HighwaterMarkDirectoryScanningHandler scanningHandler =
                createDirectoryScanningHandler();
        final StoreItem storeItem = new StoreItem(STORE_NAME);
        boolean fail = true;
        try
        {
            scanningHandler.mayHandle(scannedStore, storeItem);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        final HostAwareFile file = new HostAwareFile(null, "temp", null);
        highwaterMarkWatcher.setPath(file);
        context.checking(new Expectations()
            {
                {
                    one(freeSpaceProvider).freeSpaceKb(file);
                    will(returnValue(freeSpace));

                    if (freeSpace > HIGHWATER_MARK)
                    {
                        one(directoryScanningHandler).mayHandle(scannedStore, storeItem);
                        will(returnValue(HandleInstruction.PROCESS));
                    }
                }
            });
        final HandleInstruction instruction = scanningHandler.mayHandle(scannedStore, storeItem);
        boolean mayHandleExpected = (freeSpace > HIGHWATER_MARK);
        boolean mayHandleObserved = HandleInstructionFlag.PROCESS.equals(instruction.getFlag());
        assertEquals(mayHandleExpected, mayHandleObserved);
        if (mayHandleExpected)
        {
            assertNull(instruction.tryGetMessage());
        } else
        {
            assertEquals("Not enough disk space on store 'iScannedStore'.", instruction
                    .tryGetMessage());
        }
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "freeSpaceProvider")
    public final void testWithFiles(final long freeSpace) throws IOException
    {
        final File[] files = new File[]
        { new File("a"), new File("b") };
        final HighwaterMarkDirectoryScanningHandler scanningHandler =
                createDirectoryScanningHandler(files);
        final StoreItem storeItem = new StoreItem(STORE_NAME);
        context.checking(new Expectations()
            {
                {
                    one(freeSpaceProvider).freeSpaceKb(new HostAwareFile(files[0]));
                    will(returnValue(freeSpace));

                    if (freeSpace > HIGHWATER_MARK)
                    {
                        one(freeSpaceProvider).freeSpaceKb(new HostAwareFile(files[1]));
                        will(returnValue(freeSpace));

                        one(directoryScanningHandler).mayHandle(scannedStore, storeItem);
                        will(returnValue(HandleInstruction.PROCESS));
                    }
                }
            });
        final HandleInstruction instruction = scanningHandler.mayHandle(scannedStore, storeItem);
        boolean mayHandleExpected = (freeSpace > HIGHWATER_MARK);
        boolean mayHandleObserved = HandleInstructionFlag.PROCESS.equals(instruction.getFlag());
        assertEquals(mayHandleExpected, mayHandleObserved);
        if (mayHandleExpected)
        {
            assertNull(instruction.tryGetMessage());
        } else
        {
            assertEquals("Not enough disk space on store 'iScannedStore'.", instruction
                    .tryGetMessage());
        }
        context.assertIsSatisfied();
    }
}