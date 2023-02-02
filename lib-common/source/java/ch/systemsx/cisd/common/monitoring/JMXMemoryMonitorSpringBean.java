/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.monitoring;

import java.util.Properties;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * A starter for {@link JMXMemoryMonitor}. Can be used to start the memory monitor as a Spring bean.
 * 
 * @author Bernd Rinn
 */
public class JMXMemoryMonitorSpringBean implements InitializingBean
{
    private static final String MEMORY_MONITORING_INTERVAL_PROP =
            "memorymonitor-monitoring-interval";

    private static final String MEMORY_MONITORING_LOG_INTERVAL_PROP = "memorymonitor-log-interval";

    private static final String MEMORY_MONITORING_HIGH_WATERMARK_PERCENT_PROP =
            "memorymonitor-high-watermark-percent";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JMXMemoryMonitorSpringBean.class);

    private static final long MILLISECONDS = 1000L;

    private final static long DEFAULT_MONITORING_INTERVALL_MILLIS = 60 * MILLISECONDS;

    private final static long DEFAULT_LOG_INTERVAL_MILLIS =
            60 * DEFAULT_MONITORING_INTERVALL_MILLIS;

    private final static int DEFAULT_MEMORY_HIGH_WATERMARK_PERCENT = 90;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        final Properties props = configurer.getResolvedProps();
        final long monitoringIntervalMillis =
                (props.getProperty(MEMORY_MONITORING_INTERVAL_PROP) == null) ? DEFAULT_MONITORING_INTERVALL_MILLIS
                        : Integer.parseInt(props.getProperty(MEMORY_MONITORING_INTERVAL_PROP))
                                * MILLISECONDS;
        final long logIntervalMillis =
                (props.getProperty(MEMORY_MONITORING_LOG_INTERVAL_PROP) == null) ? DEFAULT_LOG_INTERVAL_MILLIS
                        : Integer.parseInt(props.getProperty(MEMORY_MONITORING_LOG_INTERVAL_PROP))
                                * MILLISECONDS;
        final int memoryHighWatermarkPercent =
                (props.getProperty(MEMORY_MONITORING_HIGH_WATERMARK_PERCENT_PROP) == null) ? DEFAULT_MEMORY_HIGH_WATERMARK_PERCENT
                        : Integer.parseInt(props
                                .getProperty(MEMORY_MONITORING_HIGH_WATERMARK_PERCENT_PROP));
        if (monitoringIntervalMillis > 0)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog
                        .info(String
                                .format("Starting JMX Memory monitor with parameters %s=%s, %s=%s, %s=%s",
                                        MEMORY_MONITORING_INTERVAL_PROP,
                                        monitoringIntervalMillis / 1000,
                                        MEMORY_MONITORING_LOG_INTERVAL_PROP,
                                        (logIntervalMillis < 0) ? "DISABLED"
                                                : logIntervalMillis / 1000,
                                        MEMORY_MONITORING_HIGH_WATERMARK_PERCENT_PROP,
                                        (memoryHighWatermarkPercent >= 100) ? "DISABLED"
                                                : memoryHighWatermarkPercent));
            }
            JMXMemoryMonitor.startMonitor(monitoringIntervalMillis, logIntervalMillis,
                    memoryHighWatermarkPercent);
        }
    }

}
