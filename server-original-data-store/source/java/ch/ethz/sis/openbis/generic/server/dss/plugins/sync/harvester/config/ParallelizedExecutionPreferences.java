/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class ParallelizedExecutionPreferences
{
    private final Double machineLoad;

    private final Integer maxThreads;

    private final Integer retriesOnFail;

    private final Boolean stopOnFailure;

    public double getMachineLoad()
    {
        return machineLoad;
    }

    public int getMaxThreads()
    {
        return maxThreads;
    }

    public int getRetriesOnFail()
    {
        return retriesOnFail;
    }

    public boolean isStopOnFailure()
    {
        return stopOnFailure;
    }

    public ParallelizedExecutionPreferences(Double machineLoad, Integer maxThreads, Integer retriesOnFail, Boolean stopOnFailure)
    {
        this.machineLoad = machineLoad;
        this.maxThreads = maxThreads;
        this.retriesOnFail = retriesOnFail;
        this.stopOnFailure = stopOnFailure;
    }
}
