/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * @author pkupczyk
 */
@Component
public class ConcurrentOperationLimiterConfig
{
    private static final String CONCURRENT_OPERATION_LIMITER_PROPERTY_PREFIX = "concurrent-operation-limiter";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ConcurrentOperationLimiter.class);

    private static final String TIMEOUT_PROPERTY = "timeout";

    private static final String TIMEOUT_ASYNC_PROPERTY = "timeout-async";

    private static final String LIMITS_PROPERTY = "limits";

    private static final String OPERATION_PROPERTY_SUFFIX = ".operation";

    private static final String LIMIT_PROPERTY_SUFFIX = ".limit";

    private static final long TIMEOUT_DEFAULT = 30 * DateUtils.MILLIS_PER_SECOND;

    private static final long TIMEOUT_ASYNC_DEFAULT = DateUtils.MILLIS_PER_DAY;

    private static final int LIMIT_DEFAULT = -1;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private long timeout;

    private long timeoutAsync;

    private List<ConcurrentOperationLimit> limits;

    @SuppressWarnings("unused")
    private ConcurrentOperationLimiterConfig()
    {
    }

    public ConcurrentOperationLimiterConfig(Properties properties)
    {
        init(properties);
    }

    @PostConstruct
    private void init()
    {
        init(configurer.getResolvedProps());
    }

    private void init(Properties allProperties)
    {
        List<ConcurrentOperationLimit> limits = new ArrayList<ConcurrentOperationLimit>();
        Properties properties = PropertyParametersUtil.extractSingleSectionProperties(allProperties, 
                CONCURRENT_OPERATION_LIMITER_PROPERTY_PREFIX, false).getProperties();
        for (String limitKey : PropertyUtils.getList(properties, LIMITS_PROPERTY))
        {
            String operation = PropertyUtils.getProperty(properties, limitKey + OPERATION_PROPERTY_SUFFIX);
            int limit = PropertyUtils.getInt(properties, limitKey + LIMIT_PROPERTY_SUFFIX, LIMIT_DEFAULT);

            if (false == StringUtils.isBlank(operation) && limit > 0)
            {
                limits.add(new ConcurrentOperationLimit(operation, limit));
                operationLog.info("Configured limit: " + limit + " for operation: " + operation);
            }
        }

        long timeout = PropertyUtils.getLong(properties, TIMEOUT_PROPERTY, TIMEOUT_DEFAULT);
        if (timeout <= 0)
        {
            timeout = TIMEOUT_DEFAULT;
        }

        long timeoutAsync = PropertyUtils.getLong(properties, TIMEOUT_ASYNC_PROPERTY, TIMEOUT_ASYNC_DEFAULT);
        if (timeoutAsync <= 0)
        {
            timeoutAsync = TIMEOUT_ASYNC_DEFAULT;
        }

        operationLog.info("Configured timeout: " + DurationFormatUtils.formatDuration(timeout, "H:mm:ss.SSS"));
        operationLog.info("Configured timeout async: " + DurationFormatUtils.formatDuration(timeoutAsync, "H:mm:ss.SSS"));

        this.timeout = timeout;
        this.timeoutAsync = timeoutAsync;
        this.limits = limits;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public long getTimeoutAsync()
    {
        return timeoutAsync;
    }

    public List<ConcurrentOperationLimit> getLimits()
    {
        return limits;
    }

}
