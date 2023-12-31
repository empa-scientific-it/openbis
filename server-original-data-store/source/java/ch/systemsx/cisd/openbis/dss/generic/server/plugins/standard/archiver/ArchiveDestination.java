/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetFileOperationsExecutor;

/**
 * @author Jakub Straszewski
 */
public class ArchiveDestination
{
    /**
     * Path to the destination
     */
    private final String destination;

    private final IDataSetFileOperationsExecutor executor;

    private final boolean isHosted;

    private final long timeoutInMillis;

    public ArchiveDestination(String destination, IDataSetFileOperationsExecutor executor, boolean isHosted, long timeoutInMillis)
    {
        this.destination = destination;
        this.executor = executor;
        this.isHosted = isHosted;
        this.timeoutInMillis = timeoutInMillis;
    }

    public String getDestination()
    {
        return destination;
    }

    public IDataSetFileOperationsExecutor getExecutor()
    {
        return executor;
    }

    public boolean isHosted()
    {
        return isHosted;
    }

    public long getTimeoutInMillis()
    {
        return timeoutInMillis;
    }

}