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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A monitor for heap and non-heap memory usage.
 * <p>
 * A high-water mark (in percent) can be specified when a notification warning should be sent out.
 * 
 * @author Bernd Rinn
 */
public class JMXMemoryMonitor
{
    private static final double BYTES_PER_GIGABYTE = 1024 * 1024 * 1024;

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            JMXMemoryMonitor.class);

    private static final Logger notifyLog = LogFactory.getLogger(LogCategory.NOTIFY,
            JMXMemoryMonitor.class);

    private final long logIntervalMillis;

    private final int memoryHighwaterMarkPercent;

    private long lastLoggedMillis;

    private boolean heapMemoryExhaustionNotificationSent;

    private boolean nonHeapMemoryExhaustionNotificationSent;

    /**
     * Starts the memory monitor.
     * 
     * @param monitoringIntervalMillis Interval (in ms) for monitoring memory consumption.
     * @param logIntervalMillis Interval (in ms) for regular logging of memory consumption.
     * @param memoryHighWatermarkPercent High-water mark for heap and non-heap memory consumption (in percent of the maximal memory). If this mark is
     *            exceeded, a warning notification will be sent.
     */
    public static final void startMonitor(long monitoringIntervalMillis, long logIntervalMillis,
            int memoryHighWatermarkPercent)
    {
        final Timer timer = new Timer("memory monitor", true);
        final JMXMemoryMonitor monitor =
                new JMXMemoryMonitor(logIntervalMillis, memoryHighWatermarkPercent);
        timer.schedule(monitor.getTimerTask(), 0L, monitoringIntervalMillis);
    }

    JMXMemoryMonitor(long logIntervallMillis, int memoryHighWatermarkPercent)
    {
        this.logIntervalMillis = logIntervallMillis;
        this.memoryHighwaterMarkPercent = memoryHighWatermarkPercent;
        machineLog.info("Maximum heap size: " + FileUtilities.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()));
        logMaxPermSize();
    }

    private void logMaxPermSize()
    {
        RuntimeMXBean memMXBean = ManagementFactory.getRuntimeMXBean();
        String prefix = "-XX:MaxPermSize=";
        List<String> jvmArgs = memMXBean.getInputArguments();
        for (final String jvmArg : jvmArgs)
        {
            if (jvmArg.startsWith(prefix))
            {
                machineLog.info("MaxPermSize: " + jvmArg.substring(prefix.length()));
            }
        }
    }

    private int percentageUsed(MemoryUsage usage)
    {
        return (int) Math.ceil(100 * (usage.getUsed() / (float) usage.getMax()));
    }

    TimerTask getTimerTask()
    {
        return new TimerTask()
            {
                @Override
                public void run()
                {
                    final long now = System.currentTimeMillis();
                    final MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
                    final int percentageUsedHeap = percentageUsed(mbean.getHeapMemoryUsage());
                    if (percentageUsedHeap > memoryHighwaterMarkPercent)
                    {
                        if (heapMemoryExhaustionNotificationSent == false)
                        {
                            notifyLog.warn(String.format(
                                    "JVM exceeded high-water mark for heap memory: %.1f GB (%d%%)",
                                    mbean.getHeapMemoryUsage().getUsed() / BYTES_PER_GIGABYTE,
                                    percentageUsedHeap));
                            heapMemoryExhaustionNotificationSent = true;
                        }
                    } else
                    {
                        heapMemoryExhaustionNotificationSent = false;
                    }
                    final int percentageUsedNonHeap = percentageUsed(mbean.getNonHeapMemoryUsage());
                    if (percentageUsedNonHeap > memoryHighwaterMarkPercent)
                    {
                        if (nonHeapMemoryExhaustionNotificationSent == false)
                        {
                            notifyLog
                                    .warn(String
                                            .format("JVM exceeded high-water mark for non-heap memory: %.1f GB (%d%%)",
                                                    mbean.getNonHeapMemoryUsage().getUsed()
                                                            / BYTES_PER_GIGABYTE,
                                                    percentageUsedNonHeap));
                            nonHeapMemoryExhaustionNotificationSent = true;
                        }
                    } else
                    {
                        nonHeapMemoryExhaustionNotificationSent = false;
                    }
                    final long timeSinceLastLoggedMillis = now - lastLoggedMillis;
                    if (logIntervalMillis >= 0 && timeSinceLastLoggedMillis > logIntervalMillis)
                    {
                        machineLog
                                .info(String
                                        .format("Heap memory used: %.1f GB, non-heap memory used: %.1f GB. Number of active threads %s",
                                                mbean.getHeapMemoryUsage().getUsed()
                                                        / BYTES_PER_GIGABYTE, mbean
                                                        .getNonHeapMemoryUsage().getUsed()
                                                        / BYTES_PER_GIGABYTE, Thread.activeCount()));
                        lastLoggedMillis = now;
                    }
                }
            };
    }

}
