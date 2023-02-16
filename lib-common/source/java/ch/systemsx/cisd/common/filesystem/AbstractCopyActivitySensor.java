/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.filesystem;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IDescribingActivitySensor;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;

/**
 * A super class for {@link IDescribingActivitySensor}s that sense changes in some sort of copy operation to a "target".
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractCopyActivitySensor implements IDescribingActivitySensor
{
    protected static final int DEFAULT_MAX_ERRORS_TO_IGNORE = 3;

    protected final int maxErrorsToIgnore;

    protected final long timeOfCreation = System.currentTimeMillis();

    protected long timeOfLastConfirmedActivity = timeOfCreation;

    protected long timeOfLastReportedActivity = timeOfCreation;

    protected long lastNonErrorResult = -1L;

    protected StatusWithResult<Long> currentResult;

    protected int errorCount = 0;

    protected AbstractCopyActivitySensor()
    {
        this(DEFAULT_MAX_ERRORS_TO_IGNORE);
    }

    protected AbstractCopyActivitySensor(int maxErrorsToIgnore)
    {
        this.maxErrorsToIgnore = maxErrorsToIgnore;
        this.currentResult = null;
    }

    /**
     * Returns the result of obtaining the last activity of the target that is more recent than <var>thresholdMillis</var> (relative to the current
     * point in time).
     * <p>
     * If the status of the result is {@link StatusFlag#OK}, the result must be the time of last activity in milli-seconds (and <i>must not</i> be
     * <code>null</code>).
     */
    protected abstract StatusWithResult<Long> getTargetTimeOfLastActivityMoreRecentThan(
            long thresholdMillis);

    /**
     * Returns a textual description of the target.
     */
    protected abstract String getTargetDescription();

    /**
     * Returns the machine log for the concrete implementation.
     */
    protected abstract Logger getMachineLog();

    //
    // IActivitySensor
    //

    @Override
    public long getLastActivityMillisMoreRecentThan(long thresholdMillis)
    {
        currentResult = getTargetTimeOfLastActivityMoreRecentThan(thresholdMillis);
        final long now = System.currentTimeMillis();
        if (currentResult.isError())
        {
            ++errorCount;
            if (errorCount <= maxErrorsToIgnore)
            {
                timeOfLastReportedActivity = now;
                getMachineLog().warn(
                        currentResult.tryGetErrorMessage()
                                + String.format(" (error count: %d <= %d, goes unreported)",
                                        errorCount, maxErrorsToIgnore));
            } else
            {
                getMachineLog().error(
                        describeInactivity(now)
                                + String.format(" (error count: %s, reported to monitor)",
                                        errorCount));
            }
        } else
        {
            if (currentResult.tryGetResult() != lastNonErrorResult)
            {
                timeOfLastConfirmedActivity = now;
                lastNonErrorResult = currentResult.tryGetResult();
                if (getMachineLog().isDebugEnabled())
                {
                    getMachineLog().debug("Observing write activity on " + getTargetDescription());
                }
            }
            // Implementation note: This means we can report an older time of activity than what we
            // reported the last time if the last time we had an error. This is on purpose as it
            // helps avoiding a situation where error and non-error situations do "flip-flop" and we
            // could report progress where there is no progress.
            timeOfLastReportedActivity = timeOfLastConfirmedActivity;
            errorCount = 0;
        }

        return timeOfLastReportedActivity;
    }

    @Override
    public boolean hasActivityMoreRecentThan(long thresholdMillis)
    {
        final long now = System.currentTimeMillis();
        return (now - getLastActivityMillisMoreRecentThan(thresholdMillis)) < thresholdMillis;
    }

    //
    // IDescribingActivitySensor
    //

    @Override
    public String describeInactivity(long now)
    {
        if (currentResult.isError())
        {
            return "Error: Unable to determine the time of write activity on "
                    + getTargetDescription() + "\n" + currentResult;
        } else
        {
            final String inactivityPeriod =
                    DurationFormatUtils.formatDuration(now - timeOfLastConfirmedActivity, "H:mm:ss.SSS");
            return "No write activity on " + getTargetDescription() + " for " + inactivityPeriod;
        }
    }
}
