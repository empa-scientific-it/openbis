/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config;

import static org.apache.commons.io.FileUtils.ONE_KB;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * @author pkupczyk
 */
@Component
public class OperationExecutionConfig implements IOperationExecutionConfig
{

    // time constants

    private static final int DAY_IN_SECONDS = 24 * 60 * 60;

    private static final int MONTH_IN_SECONDS = 30 * DAY_IN_SECONDS;

    private static final int YEAR_IN_SECONDS = 365 * DAY_IN_SECONDS;

    // prefixes

    private static final String PREFIX = "api.v3.operation-execution.";

    private static final String STORE_PREFIX = PREFIX + "store.";

    private static final String THREAD_POOL_PREFIX = PREFIX + "thread-pool.";

    private static final String PROGRESS_PREFIX = PREFIX + "progress.";

    private static final String STATE_UPDATE_PREFIX = PREFIX + "state-update.";

    private static final String AVAILABILITY_TIME_PREFIX = PREFIX + "availability-time.";

    private static final String AVAILABILITY_UPDATE_PREFIX = PREFIX + "availability-update.";

    private static final String SUMMARY_AVAILABILITY_TIME_PREFIX = AVAILABILITY_TIME_PREFIX + "summary.";

    private static final String DETAILS_AVAILABILITY_TIME_PREFIX = AVAILABILITY_TIME_PREFIX + "details.";

    private static final String MARK_TIMEOUT_PENDING_TASK_PREFIX = AVAILABILITY_UPDATE_PREFIX + "mark-timeout-pending-task.";

    private static final String MARK_TIMED_OUT_OR_DELETED_TASK_PREFIX = AVAILABILITY_UPDATE_PREFIX + "mark-timed-out-or-deleted-task.";

    private static final String MARK_FAILED_AFTER_SERVER_RESTART_TASK_PREFIX = STATE_UPDATE_PREFIX + "mark-failed-after-server-restart-task.";

    private static final String CACHE_PREFIX = PREFIX + "cache.";

    // properties

    public static final String STORE_PATH = STORE_PREFIX + "path";

    public static final String THREAD_POOL_NAME = THREAD_POOL_PREFIX + "name";

    public static final String THREAD_POOL_CORE_SIZE = THREAD_POOL_PREFIX + "core-size";

    public static final String THREAD_POOL_MAX_SIZE = THREAD_POOL_PREFIX + "max-size";

    public static final String THREAD_POOL_KEEP_ALIVE_TIME = THREAD_POOL_PREFIX + "keep-alive-time";

    public static final String PROGRESS_THREAD_NAME = PROGRESS_PREFIX + "thread-name";

    public static final String PROGRESS_INTERVAL = PROGRESS_PREFIX + "interval";

    public static final String AVAILABILITY_TIME_DEFAULT = AVAILABILITY_TIME_PREFIX + "default";

    public static final String AVAILABILITY_TIME_MAX = AVAILABILITY_TIME_PREFIX + "max";

    public static final String SUMMARY_AVAILABILITY_TIME_DEFAULT = SUMMARY_AVAILABILITY_TIME_PREFIX + "default";

    public static final String SUMMARY_AVAILABILITY_TIME_MAX = SUMMARY_AVAILABILITY_TIME_PREFIX + "max";

    public static final String DETAILS_AVAILABILITY_TIME_DEFAULT = DETAILS_AVAILABILITY_TIME_PREFIX + "default";

    public static final String DETAILS_AVAILABILITY_TIME_MAX = DETAILS_AVAILABILITY_TIME_PREFIX + "max";

    public static final String MARK_TIMEOUT_PENDING_TASK_NAME = MARK_TIMEOUT_PENDING_TASK_PREFIX + "name";

    public static final String MARK_TIMEOUT_PENDING_TASK_INTERVAL = MARK_TIMEOUT_PENDING_TASK_PREFIX + "interval";

    public static final String MARK_TIMED_OUT_OR_DELETED_TASK_NAME = MARK_TIMED_OUT_OR_DELETED_TASK_PREFIX + "name";

    public static final String MARK_TIMED_OUT_OR_DELETED_TASK_INTERVAL = MARK_TIMED_OUT_OR_DELETED_TASK_PREFIX + "interval";

    public static final String MARK_FAILED_AFTER_SERVER_RESTART_TASK_NAME = MARK_FAILED_AFTER_SERVER_RESTART_TASK_PREFIX + "name";

    public static final String CACHE_CLEARANCE_TASK_NAME = CACHE_PREFIX + "clearance-task-name";

    public static final String CACHE_CLEARANCE_TASK_INTERVAL = CACHE_PREFIX + "timeout-check-interval";

    public static final String CACHE_CLEARANCE_TIMEOUT = CACHE_PREFIX + "timeout";

    public static final String CACHE_CLASS = CACHE_PREFIX + "class";

    public static final String CACHE_CAPACITY = CACHE_PREFIX + "capacity";

    // defaults

    private static final String STORE_PATH_DEFAULT = "operation-execution-store";

    private static final String THREAD_POOL_NAME_DEFAULT = "operation-execution-pool";

    private static final int THREAD_POOL_CORE_SIZE_DEFAULT = 10;

    private static final int THREAD_POOL_MAX_SIZE_DEFAULT = 10;

    private static final int THREAD_POOL_KEEP_ALIVE_TIME_DEFAULT = 0;

    private static final String PROGRESS_THREAD_NAME_DEFAULT = "operation-execution-progress";

    private static final int PROGRESS_INTERVAL_DEFAULT = 5;

    private static final int AVAILABILITY_TIME_DEFAULT_DEFAULT = YEAR_IN_SECONDS;

    private static final int AVAILABILITY_TIME_MAX_DEFAULT = YEAR_IN_SECONDS;

    private static final int SUMMARY_AVAILABILITY_TIME_DEFAULT_DEFAULT = MONTH_IN_SECONDS;

    private static final int SUMMARY_AVAILABILITY_TIME_MAX_DEFAULT = MONTH_IN_SECONDS;

    private static final int DETAILS_AVAILABILITY_TIME_DEFAULT_DEFAULT = DAY_IN_SECONDS;

    private static final int DETAILS_AVAILABILITY_TIME_MAX_DEFAULT = DAY_IN_SECONDS;

    private static final String MARK_TIMEOUT_PENDING_TASK_NAME_DEFAULT = "operation-execution-mark-timeout-pending-task";

    private static final int MARK_TIMEOUT_PENDING_TASK_INTERVAL_DEFAULT = 60;

    private static final String MARK_TIMED_OUT_OR_DELETED_TASK_NAME_DEFAULT = "operation-execution-mark-timed-out-or-deleted-task";

    private static final int MARK_TIMED_OUT_OR_DELETED_TASK_INTERVAL_DEFAULT = 300;

    private static final String MARK_FAILED_AFTER_SERVER_RESTART_TASK_NAME_DEFAULT = "operation-execution-mark-failed-after-server-restart-task";

    private static final String CACHE_CLEARANCE_TASK_NAME_DEFAULT = "cache-clearance-task";

    private static final int CACHE_CLEARANCE_TASK_INTERVAL_DEFAULT = 300;

    public static final int CACHE_CLEARANCE_TIMEOUT_DEFAULT = 3600;

    public static final int CACHE_CAPACITY_DEFAULT = (int) (10 * ONE_KB);

    // fields

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private boolean initialized = false;

    private String storePath;

    private String threadPoolName;

    private int threadPoolCoreSize;

    private int threadPoolMaxSize;

    private int threadPoolKeepAliveTime;

    private String progressThreadName;

    private int progressInterval;

    private int availabilityTimeDefault;

    private int availabilityTimeMax;

    private int summaryAvailabilityTimeDefault;

    private int summaryAvailabilityTimeMax;

    private int detailsAvailabilityTimeDefault;

    private int detailsAvailabilityTimeMax;

    private String markFailedAfterServerRestartTaskName;

    private String markTimeOutPendingTaskName;

    private int markTimeOutPendingTaskInterval;

    private String markTimedOutOrDeletedTaskName;

    private int markTimedOutOrDeletedTaskInterval;

    private String cacheClearanceTaskName;

    private int cacheClearanceTaskInterval;

    @PostConstruct
    private void init()
    {
        if (initialized)
        {
            return;
        } else
        {
            initialized = true;
        }

        Properties properties = getProperties();

        storePath = getStringPropertyOrDefault(properties, STORE_PATH, STORE_PATH_DEFAULT);
        threadPoolName = getStringPropertyOrDefault(properties, THREAD_POOL_NAME, THREAD_POOL_NAME_DEFAULT);

        threadPoolCoreSize = getIntegerPropertyOrDefault(properties, THREAD_POOL_CORE_SIZE, THREAD_POOL_CORE_SIZE_DEFAULT);
        checkPositiveProperty(properties, THREAD_POOL_CORE_SIZE, threadPoolCoreSize);

        threadPoolMaxSize = getIntegerPropertyOrDefault(properties, THREAD_POOL_MAX_SIZE, THREAD_POOL_MAX_SIZE_DEFAULT);
        checkGreaterThanOrEqualToOtherProperty(properties, THREAD_POOL_MAX_SIZE, threadPoolMaxSize, THREAD_POOL_CORE_SIZE, threadPoolCoreSize);

        threadPoolKeepAliveTime = getIntegerPropertyOrDefault(properties, THREAD_POOL_KEEP_ALIVE_TIME, THREAD_POOL_KEEP_ALIVE_TIME_DEFAULT);
        checkPositiveOrZeroProperty(properties, THREAD_POOL_KEEP_ALIVE_TIME, threadPoolKeepAliveTime);

        progressThreadName = getStringPropertyOrDefault(properties, PROGRESS_THREAD_NAME, PROGRESS_THREAD_NAME_DEFAULT);

        progressInterval = getIntegerPropertyOrDefault(properties, PROGRESS_INTERVAL, PROGRESS_INTERVAL_DEFAULT);
        checkPositiveProperty(properties, PROGRESS_INTERVAL, progressInterval);

        availabilityTimeDefault = getIntegerPropertyOrDefault(properties, AVAILABILITY_TIME_DEFAULT, AVAILABILITY_TIME_DEFAULT_DEFAULT);
        checkPositiveOrZeroProperty(properties, AVAILABILITY_TIME_DEFAULT, availabilityTimeDefault);

        availabilityTimeMax = getIntegerPropertyOrDefault(properties, AVAILABILITY_TIME_MAX, AVAILABILITY_TIME_MAX_DEFAULT);
        checkGreaterThanOrEqualToOtherProperty(properties, AVAILABILITY_TIME_MAX, availabilityTimeMax, AVAILABILITY_TIME_DEFAULT,
                availabilityTimeDefault);

        summaryAvailabilityTimeDefault =
                getIntegerPropertyOrDefault(properties, SUMMARY_AVAILABILITY_TIME_DEFAULT, SUMMARY_AVAILABILITY_TIME_DEFAULT_DEFAULT);
        checkPositiveOrZeroProperty(properties, SUMMARY_AVAILABILITY_TIME_DEFAULT, summaryAvailabilityTimeDefault);

        summaryAvailabilityTimeMax = getIntegerPropertyOrDefault(properties, SUMMARY_AVAILABILITY_TIME_MAX, SUMMARY_AVAILABILITY_TIME_MAX_DEFAULT);
        checkGreaterThanOrEqualToOtherProperty(properties, SUMMARY_AVAILABILITY_TIME_MAX, summaryAvailabilityTimeMax,
                SUMMARY_AVAILABILITY_TIME_DEFAULT, summaryAvailabilityTimeDefault);

        detailsAvailabilityTimeDefault =
                getIntegerPropertyOrDefault(properties, DETAILS_AVAILABILITY_TIME_DEFAULT, DETAILS_AVAILABILITY_TIME_DEFAULT_DEFAULT);
        checkPositiveOrZeroProperty(properties, DETAILS_AVAILABILITY_TIME_DEFAULT, detailsAvailabilityTimeDefault);

        detailsAvailabilityTimeMax = getIntegerPropertyOrDefault(properties, DETAILS_AVAILABILITY_TIME_MAX, DETAILS_AVAILABILITY_TIME_MAX_DEFAULT);
        checkGreaterThanOrEqualToOtherProperty(properties, DETAILS_AVAILABILITY_TIME_MAX, detailsAvailabilityTimeMax,
                DETAILS_AVAILABILITY_TIME_DEFAULT, detailsAvailabilityTimeDefault);

        markTimeOutPendingTaskName = getStringPropertyOrDefault(properties, MARK_TIMEOUT_PENDING_TASK_NAME, MARK_TIMEOUT_PENDING_TASK_NAME_DEFAULT);

        markTimeOutPendingTaskInterval = getIntegerPropertyOrDefault(properties, MARK_TIMEOUT_PENDING_TASK_INTERVAL,
                MARK_TIMEOUT_PENDING_TASK_INTERVAL_DEFAULT);
        checkPositiveProperty(properties, MARK_TIMEOUT_PENDING_TASK_INTERVAL, markTimeOutPendingTaskInterval);

        markTimedOutOrDeletedTaskName =
                getStringPropertyOrDefault(properties, MARK_TIMED_OUT_OR_DELETED_TASK_NAME, MARK_TIMED_OUT_OR_DELETED_TASK_NAME_DEFAULT);

        markTimedOutOrDeletedTaskInterval = getIntegerPropertyOrDefault(properties, MARK_TIMED_OUT_OR_DELETED_TASK_INTERVAL,
                MARK_TIMED_OUT_OR_DELETED_TASK_INTERVAL_DEFAULT);
        checkPositiveProperty(properties, MARK_TIMED_OUT_OR_DELETED_TASK_INTERVAL, markTimedOutOrDeletedTaskInterval);

        markFailedAfterServerRestartTaskName = getStringPropertyOrDefault(properties, MARK_FAILED_AFTER_SERVER_RESTART_TASK_NAME,
                MARK_FAILED_AFTER_SERVER_RESTART_TASK_NAME_DEFAULT);

        cacheClearanceTaskName =
                getStringPropertyOrDefault(properties, CACHE_CLEARANCE_TASK_NAME, CACHE_CLEARANCE_TASK_NAME_DEFAULT);

        cacheClearanceTaskInterval = getIntegerPropertyOrDefault(properties, CACHE_CLEARANCE_TASK_INTERVAL,
                CACHE_CLEARANCE_TASK_INTERVAL_DEFAULT);
        checkPositiveProperty(properties, CACHE_CLEARANCE_TASK_INTERVAL, cacheClearanceTaskInterval);

    }

    @Override
    public String getStorePath()
    {
        init();
        return storePath;
    }

    @Override
    public String getThreadPoolName()
    {
        init();
        return threadPoolName;
    }

    @Override
    public int getThreadPoolCoreSize()
    {
        init();
        return threadPoolCoreSize;
    }

    @Override
    public int getThreadPoolMaxSize()
    {
        init();
        return threadPoolMaxSize;
    }

    @Override
    public int getThreadPoolKeepAliveTime()
    {
        init();
        return threadPoolKeepAliveTime;
    }

    @Override
    public String getProgressThreadName()
    {
        init();
        return progressThreadName;
    }

    @Override
    public int getProgressInterval()
    {
        init();
        return progressInterval;
    }

    @Override
    public int getAvailabilityTimeOrDefault(Integer availabilityTimeOrNull)
    {
        init();
        return getAvailabilityTimeOrDefault(availabilityTimeOrNull, getAvailabilityTimeDefault(), getAvailabilityTimeMax());
    }

    @Override
    public int getAvailabilityTimeDefault()
    {
        init();
        return availabilityTimeDefault;
    }

    @Override
    public int getAvailabilityTimeMax()
    {
        init();
        return availabilityTimeMax;
    }

    @Override
    public int getSummaryAvailabilityTimeOrDefault(Integer summaryAvailabilityTimeOrNull)
    {
        init();
        return getAvailabilityTimeOrDefault(summaryAvailabilityTimeOrNull, getSummaryAvailabilityTimeDefault(), getSummaryAvailabilityTimeMax());
    }

    @Override
    public int getSummaryAvailabilityTimeDefault()
    {
        init();
        return summaryAvailabilityTimeDefault;
    }

    @Override
    public int getSummaryAvailabilityTimeMax()
    {
        init();
        return summaryAvailabilityTimeMax;
    }

    @Override
    public int getDetailsAvailabilityTimeOrDefault(Integer detailsAvailabilityTimeOrNull)
    {
        init();
        return getAvailabilityTimeOrDefault(detailsAvailabilityTimeOrNull, getDetailsAvailabilityTimeDefault(), getDetailsAvailabilityTimeMax());
    }

    @Override
    public int getDetailsAvailabilityTimeDefault()
    {
        init();
        return detailsAvailabilityTimeDefault;
    }

    @Override
    public int getDetailsAvailabilityTimeMax()
    {
        init();
        return detailsAvailabilityTimeMax;
    }

    @Override
    public String getMarkFailedAfterServerRestartTaskName()
    {
        init();
        return markFailedAfterServerRestartTaskName;
    }

    @Override
    public String getMarkTimeOutPendingTaskName()
    {
        init();
        return markTimeOutPendingTaskName;
    }

    @Override
    public int getMarkTimeOutPendingTaskInterval()
    {
        init();
        return markTimeOutPendingTaskInterval;
    }

    @Override
    public String getMarkTimedOutOrDeletedTaskName()
    {
        init();
        return markTimedOutOrDeletedTaskName;
    }

    @Override
    public int getMarkTimedOutOrDeletedTaskInterval()
    {
        init();
        return markTimedOutOrDeletedTaskInterval;
    }

    @Override
    public String getCacheClearanceTaskName()
    {
        init();
        return cacheClearanceTaskName;
    }

    @Override public int getCacheClearanceTaskInterval()
    {
        init();
        return cacheClearanceTaskInterval;
    }

    private static int getAvailabilityTimeOrDefault(Integer availabilityTimeOrNull, int availabilityTimeDefault, int availabilityTimeMax)
    {
        if (availabilityTimeOrNull == null)
        {
            return availabilityTimeDefault;
        } else
        {
            return Math.min(availabilityTimeMax, Math.max(0, availabilityTimeOrNull));
        }
    }

    private static String getStringPropertyOrDefault(Properties properties, String propertyName, String defaultValue)
    {
        String value = properties.getProperty(propertyName);

        if (value == null || value.trim().isEmpty())
        {
            return defaultValue;
        } else
        {
            return value.trim();
        }
    }

    private static Integer getIntegerPropertyOrDefault(Properties properties, String propertyName, Integer defaultValue)
    {
        String stringValue = getStringPropertyOrDefault(properties, propertyName, String.valueOf(defaultValue));

        if (NumberUtils.isNumber(stringValue))
        {
            return NumberUtils.createNumber(stringValue).intValue();
        } else
        {
            return null;
        }
    }

    private static void checkPositiveProperty(Properties properties, String propertyName, Integer propertyValue)
    {
        if (propertyValue <= 0)
        {
            throw new IllegalArgumentException(String.format("Property '%s' value must be > 0 but was '%s'", propertyName, propertyValue));
        }
    }

    private static void checkPositiveOrZeroProperty(Properties properties, String propertyName, Integer propertyValue)
    {
        if (propertyValue < 0)
        {
            throw new IllegalArgumentException(String.format("Property '%s' value must be >= 0 but was '%s'", propertyName, propertyValue));
        }
    }

    private static void checkGreaterThanOrEqualToOtherProperty(Properties properties, String propertyName, Integer propertyValue,
            String otherPropertyName, Integer otherPropertyValue)
    {
        if (propertyValue < otherPropertyValue)
        {
            throw new IllegalArgumentException(String.format("Property '%s' value must be >= %s (property '%s' value) but was '%s'",
                    propertyName, otherPropertyValue, otherPropertyName, propertyValue));
        }
    }

    protected Properties getProperties()
    {
        return configurer.getResolvedProps();
    }

}
