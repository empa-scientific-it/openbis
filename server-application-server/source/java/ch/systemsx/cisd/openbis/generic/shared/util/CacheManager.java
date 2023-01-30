/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * {@link ICacheManager} based on local file system.
 *
 * @author Franz-Josef Elmer
 */
public class CacheManager implements ICacheManager
{
    private static final class FileName
    {
        private static final String DELIMITER = "-";

        private static final String TIME_STAMP_FORMAT = "yyyyMMddHHmmssSSS";

        private static final String FILE_NAME_FORMAT = "{0,date," + TIME_STAMP_FORMAT + "}"
                + DELIMITER + "{1}";

        private Date timeStamp;

        private String fileName;

        private int counter;

        FileName(ITimeProvider timeProvider)
        {
            createTimeStampAndFileName(timeProvider);
        }

        FileName(String fileName)
        {
            this.fileName = fileName;
            String[] fileNameParts = fileName.split("-");
            if (fileNameParts.length != 2)
            {
                throw new IllegalArgumentException("Missing '" + DELIMITER + "' in file name: "
                        + fileName);
            }
            try
            {
                timeStamp = new SimpleDateFormat(TIME_STAMP_FORMAT).parse(fileNameParts[0]);
            } catch (ParseException ex)
            {
                throw new IllegalArgumentException("Invalid time stamp part of file name: "
                        + fileName);
            }
        }

        Date getTimeStamp()
        {
            return timeStamp;
        }

        void touch(File cacheFolder, ITimeProvider timeProvider)
        {
            String oldFileName = fileName;
            createTimeStampAndFileName(timeProvider);
            rename(cacheFolder, oldFileName, KEY_FILE_TYPE);
            rename(cacheFolder, oldFileName, DATA_FILE_TYPE);
        }

        protected void rename(File cacheFolder, String oldFileName, String fileType)
        {
            new File(cacheFolder, oldFileName + fileType).renameTo(new File(cacheFolder, fileName + fileType));
        }

        private void createTimeStampAndFileName(ITimeProvider timeProvider)
        {
            timeStamp = new Date(timeProvider.getTimeInMilliseconds());
            fileName = new MessageFormat(FILE_NAME_FORMAT).format(new Object[]
            { timeStamp, counter++ });
        }

        @Override
        public String toString()
        {
            return fileName;
        }
    }

    private static final int DAY = 24 * 60 * 60 * 1000;

    @Private
    public static final String CACHE_FOLDER_KEY = "cache-folder";

    private static final String CACHE_FOLDER_DEFAULT_VALUE = "cache";

    @Private
    static final String MINIMUM_FREE_DISK_SPACE_KEY = "minimum-free-disk-space-in-MB";

    private static final int MINIMUM_FREE_DISK_SPACE_DEFAULT_VALUE = 1024;

    @Private
    static final String MAXIMUM_RETENTION_TIME_KEY = "maximum-retention-time-in-days";

    private static final int MAXIMUM_RETENTION_TIME_DEFAULT_VALUE = 7;

    @Private
    static final String CACHE_VERSION_FILE_NAME = ".cache-version";

    @Private
    static final String KEY_FILE_TYPE = ".key";

    @Private
    static final String DATA_FILE_TYPE = ".data";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            CacheManager.class);

    private final ITimeProvider timeProvider;

    private final Map<Key, FileName> keyToFileNameMap;

    private final IFreeSpaceProvider freeSpaceProvider;

    private final File cacheFolder;

    private final long minimumFreeDiskSpaceInKB;

    private final long maximumRetentionTimeInMillis;

    public CacheManager(WebClientConfiguration configuration, String technologyName,
            ITimeProvider timeProvider, IFreeSpaceProvider freeSpaceProvider, String cacheVersion)
    {
        this.timeProvider = timeProvider;
        this.freeSpaceProvider = freeSpaceProvider;
        keyToFileNameMap = new HashMap<Key, FileName>();
        cacheFolder =
                new File(getProperty(configuration, technologyName, CACHE_FOLDER_KEY,
                        CACHE_FOLDER_DEFAULT_VALUE));
        minimumFreeDiskSpaceInKB =
                getIntegerProperty(configuration, technologyName, MINIMUM_FREE_DISK_SPACE_KEY,
                        MINIMUM_FREE_DISK_SPACE_DEFAULT_VALUE) * 1024;
        maximumRetentionTimeInMillis =
                DAY
                        * getIntegerProperty(configuration, technologyName,
                                MAXIMUM_RETENTION_TIME_KEY, MAXIMUM_RETENTION_TIME_DEFAULT_VALUE);
        String actualCacheVersion = tryToGetActualCacheVersion();
        if (cacheVersion.equals(actualCacheVersion))
        {
            File[] keyFiles = cacheFolder.listFiles(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(KEY_FILE_TYPE);
                    }
                });
            for (File keyFile : keyFiles)
            {
                String name = keyFile.getName();
                String fileName = name.substring(0, name.length() - KEY_FILE_TYPE.length());
                Key key = getKey(keyFile);
                keyToFileNameMap.put(key, new FileName(fileName));
            }
        } else
        {
            FileUtilities.deleteRecursively(cacheFolder);
            cacheFolder.mkdirs();
            FileUtilities.writeToFile(new File(cacheFolder, CACHE_VERSION_FILE_NAME), cacheVersion);
        }
    }

    private String tryToGetActualCacheVersion()
    {
        File file = new File(cacheFolder, CACHE_VERSION_FILE_NAME);
        if (file.exists() == false)
        {
            return null;
        }
        return FileUtilities.loadToString(file).trim();
    }

    private Key getKey(File keyFile)
    {
        Key key;
        try
        {
            key = (Key) SerializationUtils.deserialize(FileUtils.readFileToByteArray(keyFile));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return key;
    }

    private int getIntegerProperty(WebClientConfiguration configuration, String technologyName, String key, int defaultValue)
    {
        String value = configuration.getPropertyOrNull(technologyName, key);
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex)
        {
            throw new ConfigurationFailureException("Web client configuration parameter '" + key
                    + "' isn't a number: " + value);
        }
    }

    private String getProperty(WebClientConfiguration configuration, String technologyName, String key,
            String defaultValue)
    {
        String value = configuration.getPropertyOrNull(technologyName, key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Object tryToGetData(Key key)
    {
        synchronized (keyToFileNameMap)
        {
            FileName fileName = keyToFileNameMap.get(key);
            if (fileName == null)
            {
                return null;
            }
            File dataFile = new File(cacheFolder, fileName + DATA_FILE_TYPE);
            try
            {
                long t0 = timeProvider.getTimeInMilliseconds();
                byte[] bytes = FileUtils.readFileToByteArray(dataFile);
                Object object = SerializationUtils.deserialize(bytes);
                long duration = timeProvider.getTimeInMilliseconds() - t0;
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(duration + " msec for retrieving cached data ("
                            + bytes.length + " bytes) for key " + key + " from file "
                            + dataFile);
                }
                fileName.touch(cacheFolder, timeProvider);
                return object;
            } catch (IOException ex)
            {
                operationLog.warn("Couldn't read data file '" + fileName + "' key " + key
                        + ": " + ex.toString());
                return null;
            }
        }
    }

    @Override
    public void storeData(Key key, Object object)
    {
        synchronized (keyToFileNameMap)
        {
            FileName fileName = new FileName(timeProvider);
            try
            {
                if (object instanceof Serializable == false)
                {
                    operationLog.warn("Can not store unserializable data for key " + key + ": "
                            + object);
                } else
                {
                    long t0 = timeProvider.getTimeInMilliseconds();
                    cleanUp();
                    save(fileName + KEY_FILE_TYPE, key);
                    File dataFile = save(fileName + DATA_FILE_TYPE, (Serializable) object);
                    keyToFileNameMap.put(key, fileName);
                    long duration = timeProvider.getTimeInMilliseconds() - t0;
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info(duration + " msec for storing data for key " + key
                                + " in file " + dataFile);
                    }
                }
            } catch (Exception ex)
            {
                // ignored
            }
        }
    }

    private void cleanUp()
    {
        long currentTime = timeProvider.getTimeInMilliseconds();
        for (Entry<Key, FileName> entry : keyToFileNameMap.entrySet())
        {
            long time = entry.getValue().getTimeStamp().getTime();
            if (time + maximumRetentionTimeInMillis < currentTime)
            {
                removeFromCache(entry.getKey());
            }
        }
        HostAwareFile file = new HostAwareFile(cacheFolder);
        try
        {
            while (keyToFileNameMap.isEmpty() == false
                    && freeSpaceProvider.freeSpaceKb(file) < minimumFreeDiskSpaceInKB)
            {
                long oldestTimeStamp = Long.MAX_VALUE;
                Key oldestKey = null;
                for (Entry<Key, FileName> entry : keyToFileNameMap.entrySet())
                {
                    long time = entry.getValue().getTimeStamp().getTime();
                    if (time < oldestTimeStamp)
                    {
                        oldestTimeStamp = time;
                        oldestKey = entry.getKey();
                    }
                }
                removeFromCache(oldestKey);
            }
        } catch (IOException ex)
        {
            operationLog.warn("Can not obtain available free disk space.", ex);
        }
    }

    private void removeFromCache(Key key)
    {
        FileName fileName = keyToFileNameMap.remove(key);
        if (fileName == null)
        {
            return;
        }
        deleteFile(new File(cacheFolder, fileName + KEY_FILE_TYPE), key);
        deleteFile(new File(cacheFolder, fileName + DATA_FILE_TYPE), key);
    }

    private void deleteFile(File file, Key key)
    {
        boolean result = file.delete();
        if (result)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("For key " + key + " successfully removed from cache: " + file);
            }
        } else
        {
            operationLog.warn("For key " + key + " removing from cache caused some unknown error: " + file);
        }
    }

    private void assertFreeSpaceAvailableFor(int numberOfBytes) throws IOException
    {
        long freeSpace = 1024 * freeSpaceProvider.freeSpaceKb(new HostAwareFile(cacheFolder));
        if (numberOfBytes > freeSpace)
        {
            throw new IOException("Can not store " + numberOfBytes + " because only " + freeSpace
                    + " bytes are available on disk.");
        }
    }

    private File save(String fileName, Serializable object)
    {
        File file = new File(cacheFolder, fileName);
        try
        {
            byte[] bytes = SerializationUtils.serialize(object);
            assertFreeSpaceAvailableFor(bytes.length);
            FileUtils.writeByteArrayToFile(file, bytes);
            return file;
        } catch (Throwable t)
        {
            file.delete();
            operationLog.warn("Caching error: Couldn't save '" + file + "': " + t, t);
            throw CheckedExceptionTunnel.wrapIfNecessary(t);
        }
    }
}