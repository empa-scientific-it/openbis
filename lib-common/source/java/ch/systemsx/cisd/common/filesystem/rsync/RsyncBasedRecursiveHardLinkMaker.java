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

package ch.systemsx.cisd.common.filesystem.rsync;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IInactivityObserver;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.CopyModeExisting;
import ch.systemsx.cisd.common.filesystem.IDirectoryImmutableCopier;
import ch.systemsx.cisd.common.filesystem.RemoteDirectoryCopyActivitySensor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * A hard link maker based on <code>rsync</code>.
 * 
 * @author Bernd Rinn
 */
public class RsyncBasedRecursiveHardLinkMaker implements IDirectoryImmutableCopier
{

    private static final String RSYNC_EXEC = "rsync";

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            RsyncBasedRecursiveHardLinkMaker.class);

    private final static int DEFAULT_MAX_ERRORS_TO_IGNORE = 3;

    private final TimingParameters timingParameters;

    private final int maxErrorsToIgnore;

    private final RsyncCopier rsyncCopier;

    public interface ILastChangedChecker
    {
        long lastChangedRelative(File destinationDirectory, long thresholdMillis);
    }

    public RsyncBasedRecursiveHardLinkMaker(List<String> additionalCmdLineFlagsOrNull)
    {
        this(null, TimingParameters.getDefaultParameters(), DEFAULT_MAX_ERRORS_TO_IGNORE, additionalCmdLineFlagsOrNull);
    }

    public RsyncBasedRecursiveHardLinkMaker(File rsyncExecutableOrNull, List<String> additionalCmdLineFlagsOrNull)
    {
        this(rsyncExecutableOrNull, TimingParameters.getDefaultParameters(),
                DEFAULT_MAX_ERRORS_TO_IGNORE, additionalCmdLineFlagsOrNull);
    }

    public RsyncBasedRecursiveHardLinkMaker(File rsyncExecutableOrNull,
            TimingParameters timingParameters, int maxErrorsToIgnore, List<String> additionalCmdLineFlagsOrNull)
    {
        final File rsyncExecutable =
                (rsyncExecutableOrNull == null) ? OSUtilities.findExecutable(RSYNC_EXEC)
                        : rsyncExecutableOrNull;
        if (rsyncExecutable == null)
        {
            throw new ConfigurationFailureException("No rsync executable available.");
        }
        if (rsyncExecutable.exists() == false)
        {
            throw new ConfigurationFailureException("rsync executable '" + rsyncExecutable
                    + "' does not exist.");
        }
        String[] additionalCmdLineFlags = new String[0];
        if (additionalCmdLineFlagsOrNull != null)
        {
            additionalCmdLineFlags = additionalCmdLineFlagsOrNull.toArray(new String[0]);
        }

        this.rsyncCopier = new RsyncCopier(rsyncExecutable, additionalCmdLineFlags);
        this.timingParameters = timingParameters;
        this.maxErrorsToIgnore = maxErrorsToIgnore;
    }

    //
    // IDirectoryImmutableCopier
    //

    @Override
    public Status copyDirectoryImmutably(File sourceDirectory, File destinationDirectory,
            String targetNameOrNull)
    {
        return copyDirectoryImmutably(sourceDirectory, destinationDirectory, targetNameOrNull,
                CopyModeExisting.ERROR);
    }

    @Override
    public Status copyDirectoryImmutably(final File sourceDirectory,
            final File destinationDirectory, final String targetNameOrNull,
            final CopyModeExisting mode)
    {
        final File target =
                new File(destinationDirectory,
                        (targetNameOrNull == null) ? sourceDirectory.getName() : targetNameOrNull);
        if (mode == CopyModeExisting.ERROR && target.exists())
        {
            return Status.createError("Target directory '" + target + "' already exists.");
        }
        final IInactivityObserver observer = new IInactivityObserver()
            {
                @Override
                public void update(long inactiveSinceMillis, String descriptionOfInactivity)
                {
                    machineLog.error(descriptionOfInactivity + ", terminating rsync");
                    rsyncCopier.terminate();
                }
            };
        Status status;
        int counter = 0;
        while (true)
        {
            status =
                    createHardLinks(sourceDirectory, destinationDirectory, targetNameOrNull,
                            target, mode, observer);
            if (status.isRetriableError() == false
                    || counter++ > timingParameters.getMaxRetriesOnFailure())
            {
                break;
            }
            if (status.isError())
            {
                ConcurrencyUtilities.sleep(timingParameters.getIntervalToWaitAfterFailureMillis());
            }
        }
        return status;
    }

    private Status createHardLinks(final File sourceDirectory, final File destinationDirectory,
            final String targetNameOrNull, final File target, final CopyModeExisting mode,
            final IInactivityObserver observer)
    {
        final InactivityMonitor monitor =
                new InactivityMonitor(new RemoteDirectoryCopyActivitySensor(target,
                        maxErrorsToIgnore), observer, timingParameters.getTimeoutMillis(), true);
        final Status result =
                rsyncCopier.copyDirectoryImmutably(sourceDirectory, destinationDirectory,
                        targetNameOrNull, mode);
        monitor.stop();
        return result;
    }

}
