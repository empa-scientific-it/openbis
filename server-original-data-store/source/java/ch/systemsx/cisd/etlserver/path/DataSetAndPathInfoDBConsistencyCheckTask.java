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
package ch.systemsx.cisd.etlserver.path;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.PhysicalDataSearchCriteria;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ICredentials;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.plugins.AbstractMaintenanceTaskWithStateFile;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetAndPathInfoDBConsistencyChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetAndPathInfoDBConsistencyCheckTask extends AbstractMaintenanceTaskWithStateFile
{
    private static final Comparator<DataSet> DATA_SET_COMPARATOR = new Comparator<DataSet>()
        {
            @Override
            public int compare(DataSet ds1, DataSet ds2)
            {
                long t1 = ds1.getRegistrationDate().getTime();
                long t2 = ds2.getRegistrationDate().getTime();
                if (t1 == t2)
                {
                    return ds1.getCode().compareTo(ds2.getCode());
                }
                return t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
            }
        };

    private static final String TIME_FORMAT = "HH:mm";

    static final String CHECKING_TIME_INTERVAL_KEY = "checking-time-interval";

    static final String CHUNK_SIZE_KEY = "chunk-size";

    static final String PAUSING_TIME_POINT_KEY = "pausing-time-point";

    static final String CONTINUING_TIME_POINT_KEY = "continuing-time-point";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetAndPathInfoDBConsistencyCheckTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DataSetAndPathInfoDBConsistencyCheckTask.class);

    private IApplicationServerApi service;

    private IHierarchicalContentProvider fileProvider;

    private IHierarchicalContentProvider pathInfoProvider;

    private long timeInterval;

    private ITimeProvider timeProvider;

    private IDataSetDirectoryProvider directoryProvider;

    private Date pausingTime;

    private Date continuingTime;

    private int chunkSize;

    public DataSetAndPathInfoDBConsistencyCheckTask()
    {
    }

    DataSetAndPathInfoDBConsistencyCheckTask(IHierarchicalContentProvider fileProvider,
            IHierarchicalContentProvider pathInfoProvider, IDataSetDirectoryProvider directoryProvider,
            IApplicationServerApi service, ITimeProvider timeProvider)
    {
        this.fileProvider = fileProvider;
        this.pathInfoProvider = pathInfoProvider;
        this.directoryProvider = directoryProvider;
        this.service = service;
        this.timeProvider = timeProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        if (service == null)
        {
            service = ServiceProvider.getV3ApplicationService();
            timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
            directoryProvider = ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
        }
        chunkSize = PropertyUtils.getInt(properties, CHUNK_SIZE_KEY, 1000);
        pausingTime = getTime(properties, PAUSING_TIME_POINT_KEY, null);
        continuingTime = getTime(properties, CONTINUING_TIME_POINT_KEY, null);
        defineStateFile(properties, directoryProvider.getStoreRoot());
        timeInterval =
                DateTimeUtils.getDurationInMillis(properties, CHECKING_TIME_INTERVAL_KEY,
                        DateUtils.MILLIS_PER_DAY);
    }

    @Override
    public void execute()
    {
        if (pausingTime != null)
        {
            executeInterruptingMode();
        } else
        {
            Date youngerThanDate = getOriginalStartingTime();
            List<DataSet> dataSets = getNextDataSets(youngerThanDate, null, readTimestampAndCodeFromStateFile());
            operationLog.info("Check " + dataSets.size() + " data sets registered since "
                    + DATE_FORMAT.format(youngerThanDate));
            DataSetAndPathInfoDBConsistencyChecker checker =
                    new DataSetAndPathInfoDBConsistencyChecker(fileProvider, pathInfoProvider);
            for (DataSet dataSet : dataSets)
            {
                checker.checkDataSet(dataSet.getCode());
            }
            reportErrorOrInconsistencies(checker, youngerThanDate, null);

        }
    }

    private void reportErrorOrInconsistencies(DataSetAndPathInfoDBConsistencyChecker checker,
            Date youngerThanDate, Date olderThanDateOrNull)
    {
        if (checker.noErrorAndInconsistencyFound() == false)
        {
            StringBuilder builder = new StringBuilder();
            if (olderThanDateOrNull == null)
            {
                builder.append("File system and path info DB consistency check report for all data sets since ");
                builder.append(DATE_FORMAT.format(youngerThanDate)).append("\n\n");
            } else
            {
                builder.append("File system and path info DB consistency check report for all data sets between ");
                builder.append(DATE_FORMAT.format(youngerThanDate)).append(" and ");
                builder.append(DATE_FORMAT.format(olderThanDateOrNull)).append("\n\n");
            }
            builder.append(checker.createReport());
            notificationLog.error(builder.toString());
        }
    }

    private void executeInterruptingMode()
    {
        Date now = new Date(timeProvider.getTimeInMilliseconds());
        TimeInterval interval = new TimeInterval(continuingTime, pausingTime, now);
        if (interval.isInTimeInterval(now) == false)
        {
            return;
        }
        DataSetAndPathInfoDBConsistencyChecker checker =
                new DataSetAndPathInfoDBConsistencyChecker(fileProvider, pathInfoProvider);
        Date originalStartingTime = getOriginalStartingTime();
        Date lastRegistrationDate = getLastRegistrationDate(originalStartingTime);
        Date registrationDate = lastRegistrationDate;
        DataSetIterable dataSetIterable = new DataSetIterable(registrationDate, originalStartingTime);
        for (DataSet dataSet : dataSetIterable)
        {
            checker.checkDataSet(dataSet.getCode());
            registrationDate = dataSet.getRegistrationDate();
            updateTimeStampFile(renderTimeStampAndCode(registrationDate, dataSet.getCode()));
            if (isInInterval(interval) == false)
            {
                break;
            }
        }
        reportErrorOrInconsistencies(checker, lastRegistrationDate, registrationDate);
    }

    private class DataSetIterable implements Iterable<DataSet>
    {
        private Date lastRegistrationDate;

        private Date originalStartingTime;

        DataSetIterable(Date lastRegistrationDate, Date originalStartingTime)
        {
            this.lastRegistrationDate = lastRegistrationDate;
            this.originalStartingTime = originalStartingTime;
        }

        @Override
        public Iterator<DataSet> iterator()
        {
            return new Iterator<DataSet>()
                {
                    private List<DataSet> currentChunk;

                    private int index;

                    private boolean canRestart = true;

                    @Override
                    public boolean hasNext()
                    {
                        if (currentChunk == null || index == currentChunk.size())
                        {
                            String timestampAndCode = readTimestampAndCodeFromStateFile();
                            currentChunk = getNextDataSets(lastRegistrationDate, chunkSize, timestampAndCode);
                            if (currentChunk.isEmpty() && canRestart)
                            {
                                canRestart = false;
                                lastRegistrationDate = originalStartingTime;
                                currentChunk = getNextDataSets(lastRegistrationDate, chunkSize, null);
                            }
                            operationLog.info("Check " + currentChunk.size() + " data sets registered since "
                                    + DATE_FORMAT.format(lastRegistrationDate));
                            index = 0;
                        }
                        return currentChunk.isEmpty() == false;
                    }

                    @Override
                    public DataSet next()
                    {
                        DataSet dataSet = currentChunk.get(index++);
                        lastRegistrationDate = dataSet.getRegistrationDate();
                        return dataSet;
                    }
                };
        }

    }

    private boolean isInInterval(TimeInterval interval)
    {
        return interval.isInTimeInterval(new Date(timeProvider.getTimeInMilliseconds()));
    }

    private List<DataSet> getNextDataSets(Date registrationDate, Integer count, String timestampAndCodeOrNull)
    {
        String sessionToken = login();
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(registrationDate);
        PhysicalDataSearchCriteria physicalDataSearchCriteria = searchCriteria.withPhysicalData();
        physicalDataSearchCriteria.withStorageConfirmation().thatEquals(true);
        physicalDataSearchCriteria.withStatus().thatEquals(ArchivingStatus.AVAILABLE);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.sortBy().registrationDate();
        if (count != null)
        {
            fetchOptions.count(count);
        }
        List<DataSet> dataSets = service.searchDataSets(sessionToken, searchCriteria, fetchOptions).getObjects();
        service.logout(sessionToken);
        Collections.sort(dataSets, DATA_SET_COMPARATOR);
        return dataSets.stream().filter(dataSet -> notAlreadySeenDataSet(dataSet, timestampAndCodeOrNull))
                .collect(Collectors.toList());
    }

    private String readTimestampAndCodeFromStateFile()
    {
        if (stateFile == null || stateFile.exists() == false)
        {
            return null;
        }
        return FileUtilities.loadToString(stateFile).trim();
    }

    private boolean notAlreadySeenDataSet(DataSet dataSet, String timestampAndCodeOrNull)
    {
        if (timestampAndCodeOrNull == null)
        {
            return true;
        }
        String timeStampAndCodeOfDataSet = renderTimeStampAndCode(dataSet.getRegistrationDate(), dataSet.getCode());
        return timeStampAndCodeOfDataSet.compareTo(timestampAndCodeOrNull) > 0;
    }

    private Date getOriginalStartingTime()
    {
        return new Date(timeProvider.getTimeInMilliseconds() - timeInterval);
    }

    String login()
    {
        ICredentials credentials = getEtlServerCredentials();
        String sessionToken = service.login(credentials.getUserId(), credentials.getPassword());
        return sessionToken;
    }

    private Date getTime(Properties properties, String key, Date defaultOrNull)
    {
        String timeString = properties.getProperty(key);
        if (timeString == null)
        {
            return defaultOrNull;
        }
        DateFormat format = new SimpleDateFormat(TIME_FORMAT);
        try
        {
            return format.parse(timeString);
        } catch (ParseException ex)
        {
            throw new UserFailureException("Invalid format of property '" + key + "': " + timeString);
        }

    }
}
