/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.IPause;
import ch.systemsx.cisd.common.utilities.ITimeAndWaitingProvider;
import ch.systemsx.cisd.common.utilities.IWaitingCondition;
import ch.systemsx.cisd.common.utilities.WaitingHelper;

/**
 * @author Franz-Josef Elmer
 */
public class FileBasedPause implements IPause
{
    private File pauseFile;

    private ITimeAndWaitingProvider timeAndWaitingProvider;

    private WaitingHelper waitingHelper;

    private String pausedActionDescriptionOrNull;

    public FileBasedPause(File pauseFile, long pauseFilePollingTime,
            ITimeAndWaitingProvider timeAndWaitingProvider, ISimpleLogger loggerOrNull,
            String pausedActionDescriptionOrNull)
    {
        this.pauseFile = pauseFile;
        this.timeAndWaitingProvider = timeAndWaitingProvider;
        this.pausedActionDescriptionOrNull = pausedActionDescriptionOrNull;
        waitingHelper = new WaitingHelper(null, pauseFilePollingTime, timeAndWaitingProvider, loggerOrNull, false);
    }

    @Override
    public long pause()
    {
        long startTime = timeAndWaitingProvider.getTimeInMilliseconds();
        waitingHelper.waitOn(new IWaitingCondition()
            {
                @Override
                public boolean conditionFulfilled()
                {
                    return pauseFile.exists() == false;
                }

                @Override
                public String toString()
                {
                    if (conditionFulfilled())
                    {
                        return "Pause file '" + pauseFile + "' has been removed."
                                + (pausedActionDescriptionOrNull == null ? ""
                                        : " The following action continues: " + pausedActionDescriptionOrNull);
                    }
                    return "Pause file '" + pauseFile + "' is present."
                            + (pausedActionDescriptionOrNull == null ? ""
                                    : " The following action is paused: " + pausedActionDescriptionOrNull);
                }
            });
        return timeAndWaitingProvider.getTimeInMilliseconds() - startTime;
    }

}
