/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SessionWorkspaceUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Cache for files remotely retrieved from Data Store Servers.
 * 
 * @author Franz-Josef Elmer
 */
public class ContentCache implements IContentCache
{
    public static final String CACHE_WORKSPACE_FOLDER_KEY = "cache-workspace-folder";

    static final String CACHE_FOLDER = "cached";

    static final String DOWNLOADING_FOLDER = "downloading";

    public static ContentCache create(Properties properties)
    {
        String workspacePathOrNull = properties.getProperty(CACHE_WORKSPACE_FOLDER_KEY);
        boolean sessionCache = workspacePathOrNull == null;
        File cacheWorkspace;
        if (sessionCache)
        {
            cacheWorkspace = SessionWorkspaceUtil.getSessionWorkspace(properties);
        } else
        {
            cacheWorkspace = new File(workspacePathOrNull);
        }
        return new ContentCache(new DssServiceRpcGenericFactory(), cacheWorkspace, sessionCache,
                FileOperations.getInstance());
    }
    
    private final Map<String, Integer> dataSetLocks = new HashMap<String, Integer>();
    
    private final LockManager fileLockManager;
    
    private final IDssServiceRpcGenericFactory serviceFactory;

    private final boolean sessionCache;

    private final File workspace;
    
    ContentCache(IDssServiceRpcGenericFactory serviceFactory, File cacheWorkspace,
            boolean sessionCache, IFileOperations fileOperations)
    {
        this.serviceFactory = serviceFactory;
        this.workspace = cacheWorkspace;
        this.sessionCache = sessionCache;
        if (sessionCache == false)
        {
            fileOperations.removeRecursivelyQueueing(new File(cacheWorkspace, DOWNLOADING_FOLDER));
        }
        fileLockManager = new LockManager();
    }

    @Override
    public void lockDataSet(String sessionToken, String dataSetCode)
    {
        String dataSetPath = createDataSetPath(sessionToken, CACHE_FOLDER, dataSetCode);
        synchronized (dataSetLocks)
        {
            Integer count = dataSetLocks.get(dataSetPath);
            if (count == null)
            {
                count = 0;
            }
            count++;
            dataSetLocks.put(dataSetPath, count);
        }
    }

    @Override
    public void unlockDataSet(String sessionToken, String dataSetCode)
    {
        String dataSetPath = createDataSetPath(sessionToken, CACHE_FOLDER, dataSetCode);
        synchronized (dataSetLocks)
        {
            Integer count = dataSetLocks.remove(dataSetPath);
            if (count != null && count > 1)
            {
                dataSetLocks.put(dataSetPath, count - 1);
            }
        }
    }
    
    @Override
    public boolean isDataSetLocked(String sessionToken, String dataSetCode)
    {
        String dataSetPath = createDataSetPath(sessionToken, CACHE_FOLDER, dataSetCode);
        synchronized (dataSetLocks)
        {
            return dataSetLocks.containsKey(dataSetPath);
        }
    }

    @Override
    public File getFile(String sessionToken, IDatasetLocation dataSetLocation,
            DataSetPathInfo path)
    {
        String pathInWorkspace =
                createPathInWorkspace(sessionToken, CACHE_FOLDER, dataSetLocation, path);
        fileLockManager.lock(pathInWorkspace);
        try
        {
            File file = new File(workspace, pathInWorkspace);
            if (file.exists() == false)
            {
                downloadFile(sessionToken, dataSetLocation, path);
            }
            return file;
        } finally
        {
            fileLockManager.unlock(pathInWorkspace);
        }
    }

    private void downloadFile(String sessionToken,
            IDatasetLocation dataSetLocation, DataSetPathInfo path)
    {
        InputStream input = null;
        try
        {
            String dataStoreUrl = dataSetLocation.getDataStoreUrl();
            IDssServiceRpcGeneric service = serviceFactory.getService(dataStoreUrl);
            String dataSetCode = dataSetLocation.getDataSetCode();
            String relativePath = path.getRelativePath();
            String url =
                    service.getDownloadUrlForFileForDataSet(sessionToken, dataSetCode, relativePath);
            input = createURL(url).openStream();
            File downloadedFile =
                    createFileFromInputStream(sessionToken, dataSetLocation, path, input);
            String pathInWorkspace =
                    createPathInWorkspace(sessionToken, CACHE_FOLDER, dataSetLocation, path);
            File file = new File(workspace, pathInWorkspace);
            createFolder(file.getParentFile());
            downloadedFile.renameTo(file);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(input);
        }
    }

    private String createPathInWorkspace(String sessionToken, String folder,
            IDatasetLocation dataSetLocation, DataSetPathInfo path)
    {
        String dataSetCode = dataSetLocation.getDataSetCode();
        return createDataSetPath(sessionToken, folder, dataSetCode) + "/"
                + path.getRelativePath();
    }

    private String createDataSetPath(String sessionToken, String folder,
            String dataSetCode)
    {
        return (sessionCache ? sessionToken + "/dss-cache/" : "") + folder + "/" + dataSetCode;
    }

    private URL createURL(String url)
    {
        try
        {
            if (url.toLowerCase().startsWith("https"))
            {
                return new URL(null, url, new sun.net.www.protocol.https.Handler());
            }
            return new URL(url);
        } catch (MalformedURLException ex)
        {
            throw new ConfigurationFailureException("Malformed URL: " + url);
        }
    }

    private File createFileFromInputStream(String sessionToken,
            IDatasetLocation dataSetLocation, DataSetPathInfo path, InputStream inputStream)
    {
        File file =
                new File(workspace, createPathInWorkspace(sessionToken, DOWNLOADING_FOLDER,
                        dataSetLocation, path));
        createFolder(file.getParentFile());
        OutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(file);
            IOUtils.copyLarge(inputStream, ostream);
            return file;
        } catch (IOException ex)
        {
            file.delete();
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(ostream);
        }
    }

    private void createFolder(File folder)
    {
        if (folder.exists() == false)
        {
            boolean result = folder.mkdirs();
            if (result == false)
            {
                throw new EnvironmentFailureException("Couldn't create folder: " + folder);
            }
        }
    }

    
    private static final class LockManager
    {
        private final Map<String, ReentrantLock> locks = new HashMap<String, ReentrantLock>();

        void lock(String path)
        {
            ReentrantLock lock;
            synchronized (locks)
            {
                lock = locks.get(path);
                if (lock == null)
                {
                    lock = new ReentrantLock();
                    locks.put(path, lock);
                }
            }
            lock.lock();
        }

        synchronized void unlock(String path)
        {
            ReentrantLock lock = locks.get(path);
            if (lock != null)
            {
                lock.unlock();
                if (lock.isLocked() == false)
                {
                    locks.remove(path);
                }
            }
        }
        
    }

}
