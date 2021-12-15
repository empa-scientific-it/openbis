package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.search;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.FileCache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.OperationExecutionConfig;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
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
        final Properties properties = new Properties();
        final File workingDirectory = createDirectoryInUnitTestRoot(getClass().getName());
        properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

        final String sessionToken = context.getSession().getSessionToken();
        final FileCache<Object> fileCache = new FileCache<>(
                new CacheOptionsVO(cacheSize, properties, sessionToken, false, new MockTimeProvider()));

        synchronized (FileCacheFactory.class)
        {
            if (!applyCalled)
            {
                applyCalled = true;
                assertCacheDirectoryEmpty(properties, sessionToken);
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
    private void assertCacheDirectoryEmpty(final Properties properties, final String sessionToken)
    {
        final String cacheDirString = PropertyUtils.getProperty(properties,
                OperationExecutionConfig.CACHE_DIRECTORY, OperationExecutionConfig.CACHE_DIRECTORY_DEFAULT) +
                File.separator + sessionToken.replaceAll("\\W+", "");
        final File cacheDir = new File(cacheDirString);

        assertTrue(cacheDir.isDirectory());
        assertEquals(cacheDir.listFiles().length, 0);
    }

    protected final File createDirectoryInUnitTestRoot(final String dirName)
    {
        final File directory = new File(UNIT_TEST_ROOT_DIRECTORY, dirName);
        directory.mkdirs();
        directory.deleteOnExit();
        return directory;
    }

}
