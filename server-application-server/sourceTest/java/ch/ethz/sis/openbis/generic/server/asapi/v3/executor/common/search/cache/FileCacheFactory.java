/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;
import ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider;

public final class FileCacheFactory implements CacheFactory
{

    private static final String UNIT_TEST_WORKING_DIRECTORY = "unit-test-wd";

    private static final String TARGETS_DIRECTORY = "targets";

    private static final File UNIT_TEST_ROOT_DIRECTORY = new File(TARGETS_DIRECTORY + File.separator +
            UNIT_TEST_WORKING_DIRECTORY);

    /**
     * Whether at least one instance of this cache has been created.
     */
    private static boolean applyCalled = false;

    private final int cacheSize;

    public FileCacheFactory(final int cacheSize)
    {
        this.cacheSize = cacheSize;
    }

    @Override
    public Class<?> getCacheClass()
    {
        return FileCache.class;
    }

    @Override
    public ICache<Object> getCache(final IOperationContext context)
    {
        return getCache(context, createDirectoryInUnitTestRoot(getClass().getName()));
    }

    public FileCache<Object> getCache(final IOperationContext context, final File workingDirectory)
    {
        final Properties properties = new Properties();
        properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

        final String sessionToken = context.getSession().getSessionToken();
        final FileCache<Object> fileCache = new FileCache<>(
                new CacheOptionsVO(cacheSize, properties, sessionToken, false, TIME_PROVIDER),
                workingDirectory.getPath());

        synchronized (FileCacheFactory.class)
        {
            if (!applyCalled)
            {
                applyCalled = true;
                assertCacheDirectoryEmpty(properties, sessionToken, workingDirectory);
            }
        }

        return fileCache;
    }

    /**
     * Asserts that cache is cleared before start.
     *
     * @param properties   configuration properties.
     * @param sessionToken current session token.
     */
    @SuppressWarnings("ConstantConditions")
    private void assertCacheDirectoryEmpty(final Properties properties, final String sessionToken,
            final File workingDirectory)
    {
        assertTrue(workingDirectory.isDirectory());
        assertEquals(workingDirectory.listFiles().length, 0);
    }

    protected final File createDirectoryInUnitTestRoot(final String dirName)
    {
        final File directory = new File(UNIT_TEST_ROOT_DIRECTORY, dirName);
        directory.mkdirs();
        directory.deleteOnExit();
        return directory;
    }

}
