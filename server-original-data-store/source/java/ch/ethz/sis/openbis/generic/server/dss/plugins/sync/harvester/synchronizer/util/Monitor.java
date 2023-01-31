/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class Monitor
{
    private String name;

    private Logger operationLog;

    private StopWatch stopWatch;

    public Monitor(String name, Logger operationLog)
    {
        this.name = name;
        this.operationLog = operationLog;
        stopWatch = new StopWatch();
        stopWatch.start();
        operationLog.info("-------- monitoring started for '" + name + "'.");
    }

    public void log()
    {
        logInfo(createReport());
    }

    public void log(String message)
    {
        logInfo(createReport() + " " + message);
    }
    
    private void logInfo(String info)
    {
        operationLog.info("[" + name + "] " + info);
    }

    private String createReport()
    {
        Runtime runtime = Runtime.getRuntime();
        String max = FileUtilities.byteCountToDisplaySize(runtime.maxMemory());
        String free = FileUtilities.byteCountToDisplaySize(runtime.freeMemory());
        String used = FileUtilities.byteCountToDisplaySize(runtime.maxMemory() - runtime.freeMemory());
        long time = stopWatch.getTime(TimeUnit.SECONDS);
        return String.format("[time: %5d sec, used: %9s, free: %9s, max: %9s]", time, used, free, max);
    }
}
