/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = CacheManager.class)
public class CacheManagerTest extends AbstractFileSystemTestCase
{
    private static final String TECHNOLOGY = "test-technology";

    private static final String CACHE_VERSION = "42";

    private static final long DAY = 24 * 60 * 60 * 1000;

    private Mockery context;

    private IFreeSpaceProvider freeSpaceProvider;

    private WebClientConfiguration webClientConfiguration;

    private File cacheFolder;

    private ITimeProvider timeProvider;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        timeProvider = context.mock(ITimeProvider.class);
        webClientConfiguration = new WebClientConfiguration();
        Map<String, String> properties = new HashMap<String, String>();
        cacheFolder = new File(workingDirectory, "cache");
        FileUtilities.deleteRecursively(cacheFolder);
        properties.put(CacheManager.CACHE_FOLDER_KEY, cacheFolder.getPath());
        properties.put(CacheManager.MINIMUM_FREE_DISK_SPACE_KEY, "1");
        properties.put(CacheManager.MAXIMUM_RETENTION_TIME_KEY, "1");
        webClientConfiguration.addPropertiesForTechnology(TECHNOLOGY, properties);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testEmptyCache()
    {
        CacheManager cacheManager = createCacheManager();

        Object data = cacheManager.tryToGetData(new Key("a"));

        assertEquals(null, data);
        checkCacheFolder(CACHE_VERSION);
        context.assertIsSatisfied();
    }

    @Test
    public void testStoreDataButNotEnoughMemory()
    {
        prepareTimeProvider(1L, 2L, 3L);
        prepareFreeSpaceProvider(0L);
        CacheManager cacheManager = createCacheManager();

        String data = "hello";
        cacheManager.storeData(new Key("a"), data);

        assertEquals(null, cacheManager.tryToGetData(new Key("a")));
        checkCacheFolder(CACHE_VERSION);
        context.assertIsSatisfied();
    }

    @Test
    public void testStoreAndRetrieveFromCache()
    {
        prepareTimeProvider(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        prepareFreeSpaceProvider(2048L, 2048L);
        CacheManager cacheManager = createCacheManager();

        String data = "hello";
        cacheManager.storeData(new Key("a"), data);

        assertEquals(data, cacheManager.tryToGetData(new Key("a")));
        checkCacheFolder(CACHE_VERSION, "19700101010000007-1");
        assertEquals(data, cacheManager.tryToGetData(new Key("a")));
        assertEquals(null, cacheManager.tryToGetData(new Key("b")));
        checkCacheFolder(CACHE_VERSION, "19700101010000010-2");
        context.assertIsSatisfied();
    }

    @Test
    public void testStoreShutdownAndRetrieveFromCache()
    {
        prepareTimeProvider(1L, 2L, 3L, 4L, 5L, 6L, 7L);
        prepareFreeSpaceProvider(2048L, 2048L);
        CacheManager cacheManager = createCacheManager();

        String data = "hello";
        cacheManager.storeData(new Key("a"), data);

        cacheManager = createCacheManager();
        assertEquals(data, cacheManager.tryToGetData(new Key("a")));
        assertEquals(null, cacheManager.tryToGetData(new Key("b")));
        checkCacheFolder(CACHE_VERSION, "19700101010000007-0");
        context.assertIsSatisfied();
    }

    @Test
    public void testStoreShutdownAndTryToRetrieveFromCacheWithChangedCacheVersion()
    {
        prepareTimeProvider(1L, 2L, 3L, 4L);
        prepareFreeSpaceProvider(2048L, 2048L);
        CacheManager cacheManager = createCacheManager("1");

        String data = "hello";
        cacheManager.storeData(new Key("a"), data);

        cacheManager = createCacheManager("2");
        assertEquals(null, cacheManager.tryToGetData(new Key("a")));
        checkCacheFolder("2");
        context.assertIsSatisfied();
    }

    @Test
    public void testCleanUpForLargeFiles()
    {
        prepareTimeProvider(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
        prepareFreeSpaceProvider(2048L, 2048L, 0L, 2048L, 2048L);
        CacheManager cacheManager = createCacheManager();

        String data1 = "hello";
        cacheManager.storeData(new Key("a"), data1);
        String data2 = "hi";
        cacheManager.storeData(new Key("b"), data2);

        assertEquals(null, cacheManager.tryToGetData(new Key("a")));
        assertEquals(data2, cacheManager.tryToGetData(new Key("b")));
        checkCacheFolder(CACHE_VERSION, "19700101010000011-1");
        context.assertIsSatisfied();
    }

    @Test
    public void testCleanUpForOldFiles()
    {
        prepareTimeProvider(1L, 2L, 3L, 4L, DAY + 5L, DAY + 6L, DAY + 7L, DAY + 8L, DAY + 9L, DAY + 10L, DAY + 11L);
        prepareFreeSpaceProvider(2048L, 2048L, 2048L, 2048L);
        CacheManager cacheManager = createCacheManager();

        String data1 = "hello";
        cacheManager.storeData(new Key("a"), data1);
        String data2 = "hi";
        cacheManager.storeData(new Key("b"), data2);

        assertEquals(null, cacheManager.tryToGetData(new Key("a")));
        assertEquals(data2, cacheManager.tryToGetData(new Key("b")));
        checkCacheFolder(CACHE_VERSION, "19700102010000011-1");
        context.assertIsSatisfied();
    }

    private void checkCacheFolder(String cacheVersion, String... fileNames)
    {
        File versionFile = new File(cacheFolder, CacheManager.CACHE_VERSION_FILE_NAME);
        assertEquals(cacheVersion, FileUtilities.loadToString(versionFile).trim());
        List<String> expectedFiles = new ArrayList<String>();
        for (String fileName : fileNames)
        {
            expectedFiles.add(fileName + CacheManager.KEY_FILE_TYPE);
            expectedFiles.add(fileName + CacheManager.DATA_FILE_TYPE);
        }
        Collections.sort(expectedFiles);
        List<String> files = new ArrayList<String>(Arrays.asList(cacheFolder.list(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(CacheManager.KEY_FILE_TYPE)
                            || name.endsWith(CacheManager.DATA_FILE_TYPE);
                }
            })));
        Collections.sort(files);
        assertEquals(expectedFiles.toString(), files.toString());
    }

    protected void prepareFreeSpaceProvider(final long... freeSpaces)
    {
        final Sequence sequence = context.sequence("space");
        context.checking(new Expectations()
            {
                {
                    try
                    {
                        for (long freeSpace : freeSpaces)
                        {
                            one(freeSpaceProvider).freeSpaceKb(new HostAwareFile(cacheFolder));
                            will(returnValue(freeSpace));
                            inSequence(sequence);
                        }
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });
    }

    private void prepareTimeProvider(final long... timeStamps)
    {
        final Sequence timeSequence = context.sequence("time");
        context.checking(new Expectations()
            {
                {
                    for (long timeStamp : timeStamps)
                    {
                        one(timeProvider).getTimeInMilliseconds();
                        will(returnValue(timeStamp));
                        inSequence(timeSequence);
                    }
                }
            });
    }

    private CacheManager createCacheManager()
    {
        return createCacheManager(CACHE_VERSION);
    }

    protected CacheManager createCacheManager(String cacheVersion)
    {
        return new CacheManager(webClientConfiguration, TECHNOLOGY, timeProvider,
                freeSpaceProvider, cacheVersion);
    }

}
