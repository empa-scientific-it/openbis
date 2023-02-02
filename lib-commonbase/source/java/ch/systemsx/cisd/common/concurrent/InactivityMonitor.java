/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.concurrent;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * An <code>InactivityMonitor</code> monitors some form of activity of a write activity on a <var>destinationStore</var> and triggers an alarm if
 * there was a period of inactivity that exceeds a given inactivity period.
 * 
 * @author Bernd Rinn
 */
public class InactivityMonitor
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, InactivityMonitor.class);

    /**
     * The sensor to get activity information from.
     */
    public interface IDescribingActivitySensor extends IActivitySensor
    {
        /**
         * Returns a string that describes the kind (and possibly reason) of recent inactivity.
         * <p>
         * Used for log messages. It can generally be assumed that this method is called after {@link #getLastActivityMillisMoreRecentThan(long)}.
         * 
         * @param now The current time as it should be used in the description.
         */
        String describeInactivity(long now);
    }

    /**
     * The observer that gets updated when the activity monitor has exceeded the inactivity threshold.
     */
    public interface IInactivityObserver
    {
        /**
         * Method which is called to inform the observer of a period of inactivity above a threshold.
         * 
         * @param inactiveSinceMillis The period of inactivity.
         * @param descriptionOfInactivity A description of inactivity, supposed to be used for logging.
         */
        void update(long inactiveSinceMillis, String descriptionOfInactivity);
    }

    private final IDescribingActivitySensor sensor;

    private final IInactivityObserver observer;

    private final Timer activityMonitoringTimer;

    private final long inactivityThresholdMillis;

    private final boolean stopAfterFirstEvent;

    /**
     * Creates an inactivity monitor.
     * 
     * @param sensor The sensor to get the activity information from. Note that this store needs to detect and signal time out conditions itself.
     *            <i>If an operation on this sensor hangs infinitely, then the InactivityMonitor hangs, too!</i>
     * @param observer The observer to inform when the inactivity threshold has been exceeded.
     * @param inactivityThresholdMillis The threshold of a period of inactivity that needs to be exceeded before the inactivity observer gets
     *            informed.
     * @param stopAfterFirstEvent If <code>true</code>, the monitor will stop itself after the first event of exceeded inactivity threshold has
     *            happened, otherwise, the monitor will continue to look for such events.
     */
    public InactivityMonitor(IDescribingActivitySensor sensor, IInactivityObserver observer,
            long inactivityThresholdMillis, boolean stopAfterFirstEvent)
    {
        assert sensor != null;
        assert observer != null;
        assert inactivityThresholdMillis > 0;

        this.sensor = sensor;
        this.observer = observer;
        this.inactivityThresholdMillis = inactivityThresholdMillis;
        this.stopAfterFirstEvent = stopAfterFirstEvent;

        final String currentThreadName = Thread.currentThread().getName();
        final String threadNamePrefix;
        if ("main".equals(currentThreadName))
        {
            threadNamePrefix = "";
        } else
        {
            threadNamePrefix = currentThreadName + " - ";
        }
        activityMonitoringTimer = new Timer(threadNamePrefix + "Activity Monitor", true);
        final InactivityMonitoringTimerTask inactivityMonitoringTimerTask =
                new InactivityMonitoringTimerTask();
        activityMonitoringTimer.schedule(inactivityMonitoringTimerTask, 0L,
                inactivityThresholdMillis / 2);
    }

    /**
     * Stops the activity monitoring. The activity monitor must not be used after calling this method.
     */
    public void stop()
    {
        activityMonitoringTimer.cancel();
    }

    /**
     * A {@link TimerTask} that monitors inactivity by means of some {@link IActivitySensor}.
     */
    private final class InactivityMonitoringTimerTask extends TimerTask
    {
        private long timeOfLastActivity = System.currentTimeMillis();

        private long computePeriodOfInactivity(final long now)
        {
            return now - timeOfLastActivity;
        }

        /**
         * Potentially time consuming as the sensor might need some time to determine the time of last activity.
         */
        private void updateTimeOfActivity()
        {
            timeOfLastActivity =
                    sensor.getLastActivityMillisMoreRecentThan(inactivityThresholdMillis);
        }

        private boolean isInactivityThresholdExceeded(final long now)
        {
            return computePeriodOfInactivity(now) > inactivityThresholdMillis;
        }

        @Override
        public void run()
        {
            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Start activity monitoring run.");
            }
            try
            {
                final long now = System.currentTimeMillis();
                if (isInactivityThresholdExceeded(now) == false)
                {
                    return;
                }
                updateTimeOfActivity();
                if (isInactivityThresholdExceeded(now))
                {
                    observer.update(computePeriodOfInactivity(now), sensor.describeInactivity(now));
                    if (stopAfterFirstEvent)
                    {
                        stop();
                    }
                }
            } catch (Exception ex)
            {
                operationLog.error("Exception when monitoring for activity.", ex);
            } finally
            {
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace("Finished activity monitoring run.");
                }
            }
        }
    }

}
