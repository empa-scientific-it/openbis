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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IDescribingActivitySensor;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.exceptions.UnknownLastChangedException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A {@link IDescribingActivitySensor} that senses changes in copy operations to a directory.
 * 
 * @author Bernd Rinn
 */
public final class RemoteDirectoryCopyActivitySensor extends AbstractCopyActivitySensor
{
    private final static Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RemoteDirectoryCopyActivitySensor.class);

    private final File target;

    public RemoteDirectoryCopyActivitySensor(File target)
    {
        super();
        this.target = target;
    }

    public RemoteDirectoryCopyActivitySensor(File target, int maxErrorsToIgnore)
    {
        super(maxErrorsToIgnore);
        this.target = target;
    }

    @Override
    protected StatusWithResult<Long> getTargetTimeOfLastActivityMoreRecentThan(long thresholdMillis)
    {
        try
        {
            final long lastChanged =
                    FileOperations.getMonitoredInstanceForCurrentThread().lastChangedRelative(
                            target, true, thresholdMillis);
            return StatusWithResult.create(lastChanged);
        } catch (UnknownLastChangedException ex)
        {
            return StatusWithResult
                    .<Long> createRetriableErrorWithResult("Cannot determine time of last change of "
                            + getTargetDescription());
        }
    }

    @Override
    protected String getTargetDescription()
    {
        return String.format("target '%s'", target);
    }

    @Override
    protected Logger getMachineLog()
    {
        return machineLog;
    }

}
