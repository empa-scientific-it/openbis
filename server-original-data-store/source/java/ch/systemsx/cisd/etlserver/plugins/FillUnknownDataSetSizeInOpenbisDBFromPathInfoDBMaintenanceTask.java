/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.etlserver.path.PathEntryDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Queries openBIS database to find data sets without a size filled in, then queries the pathinfo DB to see if the size info is available there; if it
 * is available, it fills in the size from the pathinfo information. If it is not available, it does nothing. Data sets from openBIS database are
 * fetched in chunks (see data-set-chunk-size property). After each chunk the maintenance tasks checks whether a time limit has been reached (see
 * time-limit property). If so, it stops processing. A code of the last processed data set is stored in a file (see last-seen-data-set-file property).
 * The next run of the maintenance task will process data sets with a code greater than the one saved in the "last-seen-data-set-file". This file is
 * deleted periodically (see delete-last-seen-data-set-file-interval) to handle a situation where codes of new data sets are lexicographically smaller
 * than the codes of the old datasets. Deleting the file is also needed when pathinfo database entries are added after a data set has been already
 * processed by the maintenance task.
 * 
 * @author pkupczyk
 */
public class FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask implements IMaintenanceTask
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.class);

    static final String CHUNK_SIZE_PROPERTY = "data-set-chunk-size";

    static final int CHUNK_SIZE_DEFAULT = 100;

    static final String TIME_LIMIT_PROPERTY = "time-limit";

    static final long TIME_LIMIT_DEFAULT = DateUtils.MILLIS_PER_HOUR;

    static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";

    static final String LAST_SEEN_DATA_SET_FILE_DEFAULT = "fillUnknownDataSetSizeTaskLastSeen";

    static final String DELETE_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY = "delete-last-seen-data-set-file-interval";

    static final long DELETE_LAST_SEEN_DATA_SET_FILE_INTERVAL_DEFAULT = DateUtils.MILLIS_PER_DAY * 7;

    private IEncapsulatedOpenBISService service;

    private IPathsInfoDAO dao;

    private ITimeProvider timeProvider;

    private IConfigProvider configProvider;

    private int chunkSize;

    private long timeLimit;

    private File lastSeenDataSetFile;

    private long deleteLastSeenDataSetFileInterval;

    public FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask()
    {
        service = ServiceProvider.getOpenBISService();
        dao = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
        timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
        configProvider = ServiceProvider.getConfigProvider();
    }

    public FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask(IEncapsulatedOpenBISService service, IPathsInfoDAO dao,
            ITimeProvider timeProvider, IConfigProvider configProvider)
    {
        this.service = service;
        this.dao = dao;
        this.timeProvider = timeProvider;
        this.configProvider = configProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        chunkSize = PropertyUtils.getInt(properties, CHUNK_SIZE_PROPERTY, CHUNK_SIZE_DEFAULT);
        timeLimit = DateTimeUtils.getDurationInMillis(properties, TIME_LIMIT_PROPERTY, TIME_LIMIT_DEFAULT);

        String lastSeenDataSetFileProperty =
                properties.getProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY);

        if (lastSeenDataSetFileProperty == null)
        {
            lastSeenDataSetFile =
                    new File(configProvider.getStoreRoot(), LAST_SEEN_DATA_SET_FILE_DEFAULT);
        } else
        {
            lastSeenDataSetFile = new File(lastSeenDataSetFileProperty);
        }

        deleteLastSeenDataSetFileInterval =
                DateTimeUtils.getDurationInMillis(properties, DELETE_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY,
                        DELETE_LAST_SEEN_DATA_SET_FILE_INTERVAL_DEFAULT);

        StringBuilder logBuilder = new StringBuilder();

        logBuilder.append("Maintenance task '" + pluginName + "' was initialized with the following property values:\n");
        logBuilder.append("\t" + CHUNK_SIZE_PROPERTY + ": " + chunkSize + "\n");
        logBuilder.append("\t" + TIME_LIMIT_PROPERTY + ": " + DateTimeUtils.renderDuration(timeLimit) + "\n");
        logBuilder.append("\t" + LAST_SEEN_DATA_SET_FILE_PROPERTY + ": " + lastSeenDataSetFile.getAbsolutePath() + "\n");
        logBuilder.append("\t" + DELETE_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY + ": "
                + DateTimeUtils.renderDuration(deleteLastSeenDataSetFileInterval));

        operationLog.info(logBuilder.toString());
    }

    @Override
    public void execute()
    {
        operationLog.info("Start filling.");

        List<SimpleDataSetInformationDTO> dataSets = null;
        boolean fixedAllDataSets = true;

        long startTime = timeProvider.getTimeInMilliseconds();
        boolean reachedTimeLimit = false;
        int chunkIndex = 1;

        prepareLastSeenDataSetFile();

        LastSeenDataSetFileContent initialLastSeenContent = LastSeenDataSetFileContent.readFromFile(lastSeenDataSetFile);

        do
        {
            LastSeenDataSetFileContent currentLastSeenContent = LastSeenDataSetFileContent.readFromFile(lastSeenDataSetFile);

            dataSets = findDataSetsWithUnknownSizeInOpenbisDB(currentLastSeenContent.getLastSeenDataSetCode());

            logWithChunkInfo("Found " + dataSets.size() + " dataset(s) with unknown size in openbis database", chunkIndex,
                    currentLastSeenContent.getLastSeenDataSetCode());

            if (false == dataSets.isEmpty())
            {
                Set<String> codes = new HashSet<String>();

                for (SimpleDataSetInformationDTO dataSet : dataSets)
                {
                    codes.add(dataSet.getDataSetCode());
                }

                Map<String, Long> sizeMap = findDataSetsSizeInPathInfoDB(codes);

                logWithChunkInfo("Found " + sizeMap.size() + " size(s) in pathinfo database", chunkIndex,
                        currentLastSeenContent.getLastSeenDataSetCode());

                if (false == sizeMap.isEmpty())
                {
                    service.updatePhysicalDataSetsSize(sizeMap);
                }

                fixedAllDataSets = fixedAllDataSets && codes.equals(sizeMap.keySet());

                LastSeenDataSetFileContent newLastSeenContent = new LastSeenDataSetFileContent();
                newLastSeenContent.setFileCreationTime(currentLastSeenContent.getFileCreationTime());
                newLastSeenContent.setLastSeenDataSetCode(Collections.max(codes));
                newLastSeenContent.writeToFile(lastSeenDataSetFile);
            }

            reachedTimeLimit = timeProvider.getTimeInMilliseconds() > startTime + timeLimit;

            if (reachedTimeLimit)
            {
                logWithChunkInfo("Reached time limit of " + DateTimeUtils.renderDuration(timeLimit) + ".", chunkIndex,
                        currentLastSeenContent.getLastSeenDataSetCode());
            }

            chunkIndex++;

        } while (false == dataSets.isEmpty() && false == reachedTimeLimit);

        if (initialLastSeenContent.getLastSeenDataSetCode() == null && fixedAllDataSets && false == reachedTimeLimit)
        {
            operationLog
                    .info("All data sets with unknown size in openbis database have been fixed. The maintenance task can be now disabled.");
        } else
        {
            operationLog
                    .info("Some data sets with unknown size in openbis database have not been fixed yet. Do not disable the maintenance task yet.");
        }

        operationLog.info("Filling finished.");
    }

    private void prepareLastSeenDataSetFile()
    {
        LastSeenDataSetFileContent content = LastSeenDataSetFileContent.readFromFile(lastSeenDataSetFile);

        if (content == null)
        {
            content = new LastSeenDataSetFileContent();
            content.setFileCreationTime(timeProvider.getTimeInMilliseconds());
            content.writeToFile(lastSeenDataSetFile);

            operationLog.info("Created last seen data set file.");

        } else if (content.getFileCreationTime() == null
                || timeProvider.getTimeInMilliseconds() > content.getFileCreationTime() + deleteLastSeenDataSetFileInterval)
        {
            lastSeenDataSetFile.delete();

            operationLog.info("Deleted last seen data set file because its age was unknown or its age was greater than "
                    + DateTimeUtils.renderDuration(deleteLastSeenDataSetFileInterval) +
                    " ('" + DELETE_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY + "' property value).");

            content = new LastSeenDataSetFileContent();
            content.setFileCreationTime(timeProvider.getTimeInMilliseconds());
            content.writeToFile(lastSeenDataSetFile);
        }
    }

    private List<SimpleDataSetInformationDTO> findDataSetsWithUnknownSizeInOpenbisDB(String lastSeenDataSetCode)
    {
        List<SimpleDataSetInformationDTO> dataSets = service.listPhysicalDataSetsWithUnknownSize(chunkSize, lastSeenDataSetCode);

        if (dataSets == null)
        {
            return Collections.emptyList();
        } else
        {
            return dataSets;
        }
    }

    private Map<String, Long> findDataSetsSizeInPathInfoDB(Collection<String> dataSetCodes)
    {
        List<PathEntryDTO> entries = dao.listDataSetsSize(dataSetCodes.toArray(new String[dataSetCodes.size()]));
        Map<String, Long> map = new HashMap<String, Long>();

        if (entries != null && false == entries.isEmpty())
        {
            for (PathEntryDTO pathInfoEntry : entries)
            {
                if (pathInfoEntry.getSizeInBytes() != null)
                {
                    map.put(pathInfoEntry.getDataSetCode(), pathInfoEntry.getSizeInBytes());
                }
            }
        }
        dao.commit(); // Needed because DAO is a TransactionQuery. Otherwise there will be an idle connection
        return map;
    }

    private void logWithChunkInfo(String msg, int chunkIndex, String lastSeenDataSetCode)
    {
        operationLog.info(msg + " (chunkIndex: " + chunkIndex + ", lastSeenDataSetCode: " + lastSeenDataSetCode + ").");
    }

    public static class LastSeenDataSetFileContent
    {
        private static final String FILE_CREATION_TIME_PROPERTY = "file-creation-time";

        private static final String LAST_SEEN_DATA_SET_CODE_PROPERTY = "last-seen-data-set-code";

        private Long fileCreationTime;

        private String lastSeenDataSetCode;

        public void setFileCreationTime(Long fileCreationTime)
        {
            this.fileCreationTime = fileCreationTime;
        }

        public Long getFileCreationTime()
        {
            return fileCreationTime;
        }

        public void setLastSeenDataSetCode(String lastSeenDataSetCode)
        {
            this.lastSeenDataSetCode = lastSeenDataSetCode;
        }

        public String getLastSeenDataSetCode()
        {
            return lastSeenDataSetCode;
        }

        @SuppressWarnings("unchecked")
        public static LastSeenDataSetFileContent readFromFile(File file)
        {
            if (file.exists())
            {
                Map<String, Object> map = null;

                try
                {
                    map = new ObjectMapper().readValue(file, Map.class);
                } catch (Exception e)
                {
                    throw new IllegalArgumentException("Could not read the last seen data set file", e);
                }

                LastSeenDataSetFileContent content = new LastSeenDataSetFileContent();
                content.setFileCreationTime(readPropertyAsLong(map, FILE_CREATION_TIME_PROPERTY));
                content.setLastSeenDataSetCode(readPropertyAsString(map, LAST_SEEN_DATA_SET_CODE_PROPERTY));
                return content;

            } else
            {
                return null;
            }
        }

        private static Long readPropertyAsLong(Map<String, Object> propertyMap, String propertyName)
        {
            try
            {
                Object propertyValue = propertyMap.get(propertyName);

                if (propertyValue == null)
                {
                    return null;
                } else if (propertyValue instanceof String)
                {
                    return Long.parseLong((String) propertyValue);
                } else if (propertyValue instanceof Number)
                {
                    return ((Number) propertyValue).longValue();
                } else
                {
                    throw new IllegalArgumentException("Cannot convert value: " + propertyValue + " of type: " + propertyValue.getClass()
                            + " to long.");
                }
            } catch (Exception e)
            {
                throw new IllegalArgumentException("Could not read " + propertyName + " property", e);
            }
        }

        private static String readPropertyAsString(Map<String, Object> propertyMap, String propertyName)
        {
            Object value = propertyMap.get(propertyName);

            try
            {
                return (String) value;
            } catch (Exception e)
            {
                throw new IllegalArgumentException("Could not read " + propertyName + " property", e);
            }
        }

        public void writeToFile(File file)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(FILE_CREATION_TIME_PROPERTY, getFileCreationTime());
            map.put(LAST_SEEN_DATA_SET_CODE_PROPERTY, getLastSeenDataSetCode());

            try
            {
                new ObjectMapper().writeValue(file, map);
            } catch (Exception e)
            {
                throw new IllegalArgumentException("Could not write the last seen data set file", e);
            }
        }
    }

}
