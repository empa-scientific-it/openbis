/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.io.FullLengthReadingStream;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Cache for files remotely retrieved from Data Store Servers.
 * 
 * @author Franz-Josef Elmer
 */
public class ContentCache implements IContentCache, InitializingBean
{

    private static final int DEFAULT_MAX_WORKSPACE_SIZE = 1024;

    private static final String DEFAULT_CACHE_WORKSPACE_FOLDER = "../../data/dss-cache";

    private static final long DEFAULT_MINIMUM_KEEPING_TIME = DateUtils.MILLIS_PER_DAY;

    public static final String CACHE_WORKSPACE_FOLDER_KEY = "cache-workspace-folder";

    public static final String CACHE_WORKSPACE_MAX_SIZE_KEY = "cache-workspace-max-size";

    public static final String CACHE_WORKSPACE_MIN_KEEPING_TIME_KEY =
            "cache-workspace-min-keeping-time";

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ContentCache.class);

    static final String CACHE_FOLDER = "cached";

    static final String DOWNLOADING_FOLDER = "downloading";

    static final String DATA_SET_INFOS_FILE = ".dataSetInfos";

    private final class ProxyInputStream extends InputStream
    {
        private final InputStream inputStream;

        private final OutputStream fileOutputStream;

        private final IDatasetLocation dataSetLocation;

        private final File tempFile;

        private final String pathInWorkspace;

        private boolean closed;

        private boolean eof;

        private ProxyInputStream(InputStream inputStream, OutputStream fileOutputStream,
                IDatasetLocation dataSetLocation, File tempFile, String pathInWorkspace)
        {
            this.inputStream = inputStream;
            this.fileOutputStream = fileOutputStream;
            this.dataSetLocation = dataSetLocation;
            this.tempFile = tempFile;
            this.pathInWorkspace = pathInWorkspace;
        }

        @Override
        public int read() throws IOException
        {
            if (eof)
            {
                return -1;
            }
            int b = inputStream.read();
            if (b < 0)
            {
                eof = true;
            } else
            {
                fileOutputStream.write(b);
            }
            closeIfEndOfFile();
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (eof)
            {
                return -1;
            }
            int count = inputStream.read(b, off, len);
            if (count >= 0)
            {
                fileOutputStream.write(b, off, count);
            } else
            {
                eof = true;
            }
            closeIfEndOfFile();
            return count;
        }

        private void closeIfEndOfFile() throws IOException
        {
            if (eof)
            {
                close();
            }
        }

        @Override
        public void close() throws IOException
        {
            if (closed)
            {
                return;
            }
            inputStream.close();
            fileOutputStream.close();
            if (eof)
            {
                moveDownloadedFileToCache(tempFile, pathInWorkspace,
                        dataSetLocation.getDataSetCode());
                persistenceManager.requestPersistence();
            } else
            {
                tempFile.delete();
            }
            closed = true;
            fileLockManager.unlock(pathInWorkspace);
        }

        @Override
        protected void finalize() throws Throwable
        {
            if (closed == false)
            {
                tempFile.delete();
                fileLockManager.unlock(pathInWorkspace);
            }
            super.finalize();
        }
    }

    static final class DataSetInfo implements Serializable
    {
        private static final long serialVersionUID = 1L;

        long lastModified;

        long size;

        @Override
        public String toString()
        {
            return size + ":" + lastModified;
        }
    }

    private static final Comparator<Entry<String, DataSetInfo>> LAST_MODIFIED_COMPARATOR =
            new Comparator<Entry<String, DataSetInfo>>()
                {
                    @Override
                    public int compare(Entry<String, DataSetInfo> e1, Entry<String, DataSetInfo> e2)
                    {
                        return (int) (e1.getValue().lastModified - e2.getValue().lastModified);
                    }
                };

    public static ContentCache create(Properties properties)
    {
        String workspacePath =
                properties.getProperty(CACHE_WORKSPACE_FOLDER_KEY, DEFAULT_CACHE_WORKSPACE_FOLDER);
        long maxWorkspaceSize =
                PropertyUtils.getInt(properties, CACHE_WORKSPACE_MAX_SIZE_KEY,
                        DEFAULT_MAX_WORKSPACE_SIZE) * FileUtils.ONE_MB;
        long minimumKeepingTimeInMillis =
                DateTimeUtils.getDurationInMillis(properties, CACHE_WORKSPACE_MIN_KEEPING_TIME_KEY,
                        DEFAULT_MINIMUM_KEEPING_TIME);
        if (minimumKeepingTimeInMillis <= DateUtils.MILLIS_PER_MINUTE)
        {
            throw new EnvironmentFailureException(
                    "Minimum keeping time has to be a larger than a minute: "
                            + minimumKeepingTimeInMillis);
        }

        File cacheWorkspace = new File(workspacePath);
        File dataSetInfosFile = new File(cacheWorkspace, DATA_SET_INFOS_FILE);
        DelayedPersistenceManager persistenceManager =
                new DelayedPersistenceManager(new SimpleFileBasePersistenceManager(
                        dataSetInfosFile, "data set infos"));
        return new ContentCache(new DssServiceRpcGenericFactory(), cacheWorkspace,
                maxWorkspaceSize, minimumKeepingTimeInMillis, FileOperations.getInstance(),
                SystemTimeProvider.SYSTEM_TIME_PROVIDER, persistenceManager);
    }

    private final LockManager fileLockManager;

    private final IDssServiceRpcGenericFactory serviceFactory;

    private final File workspace;

    private final ITimeProvider timeProvider;

    private final IFileOperations fileOperations;

    private final IPersistenceManager persistenceManager;

    private final long maxWorkspaceSize;

    private final long minimumKeepingTime;

    private HashMap<String, DataSetInfo> dataSetInfos;

    ContentCache(IDssServiceRpcGenericFactory serviceFactory, File cacheWorkspace,
            long maxWorkspaceSize, long minimumKeepingTime, IFileOperations fileOperations,
            ITimeProvider timeProvider, IPersistenceManager persistenceManager)
    {
        this.serviceFactory = serviceFactory;
        this.workspace = cacheWorkspace;
        this.maxWorkspaceSize = maxWorkspaceSize;
        this.minimumKeepingTime = minimumKeepingTime;
        this.fileOperations = fileOperations;
        this.timeProvider = timeProvider;
        this.persistenceManager = persistenceManager;
        fileLockManager = new LockManager();
        operationLog.info("Content cache created. Workspace: " + cacheWorkspace.getAbsolutePath());
    }

    @Override
    public void afterPropertiesSet()
    {
        fileOperations.removeRecursivelyQueueing(new File(workspace, DOWNLOADING_FOLDER));
        int dataSetCount = initializeDataSetInfos();
        long totalSize = getTotalSize();
        operationLog.info("Content cache initialized. It contains "
                + FileUtilities.byteCountToDisplaySize(totalSize) + " from " + dataSetCount
                + " data sets.");
    }

    private int initializeDataSetInfos()
    {
        dataSetInfos = loadDataSetSize();
        File[] dataSetFolders =
                new File(workspace, CACHE_FOLDER)
                        .listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        if (dataSetFolders == null)
        {
            return 0;
        }
        Arrays.sort(dataSetFolders, new Comparator<File>()
            {
                @Override
                public int compare(File f1, File f2)
                {
                    return f1.getName().compareTo(f2.getName());
                }
            });
        boolean cachedFilesRemoved = false;
        for (File dataSetFolder : dataSetFolders)
        {
            String dataSetCode = dataSetFolder.getName();
            DataSetInfo dataSetInfo = dataSetInfos.get(dataSetCode);
            if (dataSetInfo == null)
            {
                dataSetInfo = new DataSetInfo();
                dataSetInfo.lastModified = dataSetFolder.lastModified();
                dataSetInfo.size = FileUtilities.getSizeOf(dataSetFolder);
                operationLog.info("Data set info recreated for data set " + dataSetCode + ".");
                dataSetInfos.put(dataSetCode, dataSetInfo);
                cachedFilesRemoved = true;
            }
        }
        if (cachedFilesRemoved)
        {
            persistenceManager.requestPersistence();
        }
        return dataSetFolders.length;
    }

    @Override
    public File getFile(String sessionToken, IDatasetLocation dataSetLocation, DataSetPathInfo path)
    {
        String pathInWorkspace = createPathInWorkspace(CACHE_FOLDER, dataSetLocation, path);
        fileLockManager.lock(pathInWorkspace);
        try
        {
            File file = new File(workspace, pathInWorkspace);
            if (file.exists() == false)
            {
                downloadFile(sessionToken, dataSetLocation, path);
            } else
            {
                touchDataSetFolder(dataSetLocation.getDataSetCode());
            }
            persistenceManager.requestPersistence();
            return file;
        } finally
        {
            fileLockManager.unlock(pathInWorkspace);
        }
    }

    @Override
    public InputStream getInputStream(String sessionToken, final IDatasetLocation dataSetLocation,
            DataSetPathInfo path)
    {
        final String pathInWorkspace = createPathInWorkspace(CACHE_FOLDER, dataSetLocation, path);
        fileLockManager.lock(pathInWorkspace);
        final File file = new File(workspace, pathInWorkspace);
        if (file.exists())
        {
            try
            {
                return new FileInputStream(getFile(sessionToken, dataSetLocation, path));
            } catch (FileNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                fileLockManager.unlock(pathInWorkspace);
            }
        }
        try
        {
            final File tempFile = createTempFile();
            final InputStream inputStream = createInputStream(sessionToken, dataSetLocation, path);
            final OutputStream fileOutputStream = createFileOutputStream(tempFile);
            return new ProxyInputStream(inputStream, fileOutputStream, dataSetLocation, tempFile,
                    pathInWorkspace);
        } catch (Throwable t)
        {
            fileLockManager.unlock(pathInWorkspace);
            throw CheckedExceptionTunnel.wrapIfNecessary(t);
        }
    }

    private void downloadFile(String sessionToken, IDatasetLocation dataSetLocation,
            DataSetPathInfo path)
    {
        InputStream input = null;
        try
        {
            input = createInputStream(sessionToken, dataSetLocation, path);
            String pathInWorkspace = createPathInWorkspace(CACHE_FOLDER, dataSetLocation, path);
            File downloadedFile = createFileFromInputStream(input);
            moveDownloadedFileToCache(downloadedFile, pathInWorkspace,
                    dataSetLocation.getDataSetCode());
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(input);
        }
    }

    private void moveDownloadedFileToCache(File downloadedFile, String pathInWorkspace,
            String dataSetCode)
    {
        File file = new File(workspace, pathInWorkspace);
        createFolder(file.getParentFile());
        boolean success = downloadedFile.renameTo(file);
        String msg = "'" + pathInWorkspace + "' successfully downloaded ";
        if (success)
        {
            touchDataSetFolder(dataSetCode);
            synchronized (dataSetInfos)
            {
                DataSetInfo dataSetInfo = getDataSetInfo(dataSetCode);
                dataSetInfo.size += file.length();
                maintainCacheSize();
            }
            operationLog.debug(msg + "and successfully moved to cache.");
        } else
        {
            operationLog.warn(msg + "but couldn't move to cache.");
        }
    }

    private void touchDataSetFolder(String dataSetCode)
    {
        File dataSetFolder = new File(workspace, createDataSetPath(CACHE_FOLDER, dataSetCode));
        long lastModified = timeProvider.getTimeInMilliseconds();
        dataSetFolder.setLastModified(lastModified);
        synchronized (dataSetInfos)
        {
            getDataSetInfo(dataSetCode).lastModified = lastModified;
        }
    }

    private InputStream createInputStream(String sessionToken, IDatasetLocation dataSetLocation,
            DataSetPathInfo path)
    {
        String dataStoreUrl = dataSetLocation.getDataStoreUrl();
        IDssServiceRpcGeneric service = serviceFactory.getService(dataStoreUrl);
        String dataSetCode = dataSetLocation.getDataSetCode();
        String relativePath = path.getRelativePath();
        URL url =
                createURL(service.getDownloadUrlForFileForDataSet(sessionToken, dataSetCode,
                        relativePath));
        InputStream openStream = null;
        try
        {
            openStream = url.openStream();
            return new FullLengthReadingStream(openStream);
        } catch (IOException ex)
        {
            IOUtils.closeQuietly(openStream);
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private OutputStream createFileOutputStream(File file)
    {
        OutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(file);
            return outputStream;
        } catch (FileNotFoundException ex)
        {
            IOUtils.closeQuietly(outputStream);
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private String createPathInWorkspace(String folder, IDatasetLocation dataSetLocation,
            DataSetPathInfo path)
    {
        String dataSetCode = dataSetLocation.getDataSetCode();
        return createDataSetPath(folder, dataSetCode + "/" + path.getRelativePath());
    }

    private static String createDataSetPath(String folder, String dataSetCode)
    {
        return folder + "/" + dataSetCode;
    }

    private URL createURL(String url)
    {
        try
        {
            return new URL(url);
        } catch (MalformedURLException ex)
        {
            throw new ConfigurationFailureException("Malformed URL: " + url);
        }
    }

    private File createFileFromInputStream(InputStream inputStream)
    {
        File file = createTempFile();
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

    private File createTempFile()
    {
        File downLoadingFolder = new File(workspace, DOWNLOADING_FOLDER);
        createFolder(downLoadingFolder);
        try
        {
            File file = File.createTempFile("file-", null, downLoadingFolder);
            return file;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
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

    private long getTotalSize()
    {
        synchronized (dataSetInfos)
        {
            long sum = 0;
            Collection<DataSetInfo> infos = dataSetInfos.values();
            for (DataSetInfo dataSetInfo : infos)
            {
                sum += dataSetInfo.size;
            }
            return sum;
        }
    }

    private DataSetInfo getDataSetInfo(String dataSetCode)
    {
        synchronized (dataSetInfos)
        {
            DataSetInfo dataSetInfo = dataSetInfos.get(dataSetCode);
            if (dataSetInfo == null)
            {
                dataSetInfo = new DataSetInfo();
                dataSetInfos.put(dataSetCode, dataSetInfo);
            }
            return dataSetInfo;
        }
    }

    private void maintainCacheSize()
    {
        long totalSize = getTotalSize();
        if (totalSize < maxWorkspaceSize)
        {
            return;
        }
        List<Entry<String, DataSetInfo>> entrySet;
        synchronized (dataSetInfos)
        {
            entrySet = new ArrayList<Map.Entry<String, DataSetInfo>>(dataSetInfos.entrySet());
        }
        Collections.sort(entrySet, LAST_MODIFIED_COMPARATOR);
        long nowMinusKeepingTime = timeProvider.getTimeInMilliseconds() - minimumKeepingTime;
        for (Entry<String, DataSetInfo> entry : entrySet)
        {
            DataSetInfo info = entry.getValue();
            String dataSet = entry.getKey();
            if (info.lastModified < nowMinusKeepingTime
                    && fileLockManager.isDataSetLocked(dataSet) == false)
            {
                File fileToRemove = new File(workspace, createDataSetPath(CACHE_FOLDER, dataSet));
                boolean success = fileOperations.removeRecursivelyQueueing(fileToRemove);
                if (success)
                {
                    synchronized (dataSetInfos)
                    {
                        dataSetInfos.remove(dataSet);
                    }
                    totalSize -= info.size;
                    operationLog.info("Cached files for data set " + dataSet
                            + " have been removed.");
                    if (totalSize < maxWorkspaceSize)
                    {
                        break;
                    }
                } else
                {
                    operationLog.error("Couldn't remove " + fileToRemove + ".");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, DataSetInfo> loadDataSetSize()
    {
        return (HashMap<String, DataSetInfo>) persistenceManager
                .load(new HashMap<String, DataSetInfo>());
    }

    private static final class LockManager
    {
        private static final class LockWithCounter
        {
            private Lock lock = new ReentrantLock();

            private int count;
        }

        private final Map<String, LockWithCounter> locks = new HashMap<String, LockWithCounter>();

        void lock(String path)
        {
            LockWithCounter lock;
            synchronized (locks)
            {
                lock = locks.get(path);
                if (lock == null)
                {
                    lock = new LockWithCounter();
                    locks.put(path, lock);
                }
                lock.count++;
            }
            lock.lock.lock();
        }

        synchronized void unlock(String path)
        {
            LockWithCounter lock = locks.get(path);
            if (lock != null)
            {
                lock.lock.unlock();
                if (--lock.count == 0)
                {
                    locks.remove(path);
                }
            }
        }

        synchronized boolean isDataSetLocked(String dataSetCode)
        {
            Set<String> keySet = locks.keySet();
            for (String key : keySet)
            {
                if (key.startsWith(createDataSetPath(CACHE_FOLDER, dataSetCode) + "/"))
                {
                    return true;
                }
            }
            return false;
        }

    }

}
