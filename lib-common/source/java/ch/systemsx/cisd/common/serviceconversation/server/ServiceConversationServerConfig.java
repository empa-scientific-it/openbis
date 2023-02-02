/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.serviceconversation.server;

/**
 * A configuration object for service conversations.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationServerConfig
{
    private int numberOfCoreThreads = 10;

    private int maxNumberOfThreads = Integer.MAX_VALUE;

    private int workQueueSize = 0;

    private int shutdownTimeoutMillis = 60000;

    private boolean daemonize = true;

    public int getNumberOfCoreThreads()
    {
        return numberOfCoreThreads;
    }

    /**
     * Creates a new configuration with default values.
     */
    public static ServiceConversationServerConfig create()
    {
        return new ServiceConversationServerConfig();
    }

    /**
     * Configures the number of core threads being spawned for service conversations (default: 10). Core threads are not shut down and all filled
     * before any incoming service conversation request is queued.
     */
    public ServiceConversationServerConfig numberOfCoreThreads(@SuppressWarnings("hiding") int numberOfCoreThreads)
    {
        this.numberOfCoreThreads = numberOfCoreThreads;
        if (this.maxNumberOfThreads < numberOfCoreThreads)
        {
            this.maxNumberOfThreads = numberOfCoreThreads;
        }
        return this;
    }

    public int getMaxNumberOfThreads()
    {
        return maxNumberOfThreads;
    }

    /**
     * Configures the maximum number of threads being spawned for service conversations (default: {@link Integer#MAX_VALUE}).
     */
    public ServiceConversationServerConfig maxNumberOfThreads(@SuppressWarnings("hiding") int maxNumberOfThreads)
    {
        this.maxNumberOfThreads = maxNumberOfThreads;
        return this;
    }

    public int getWorkQueueSize()
    {
        return workQueueSize;
    }

    /**
     * Configures the length of the work queue (default 0). If set to a value larger than 0, new service conversations will be queued rather than new
     * threads being spawned when all core threads are busy.
     */
    public ServiceConversationServerConfig workQueueSize(@SuppressWarnings("hiding") int workQueueSize)
    {
        this.workQueueSize = workQueueSize;
        this.maxNumberOfThreads = this.numberOfCoreThreads;
        return this;
    }

    public int getShutdownTimeoutMillis()
    {
        return shutdownTimeoutMillis;
    }

    /**
     * Configures the shutdown timeout in milli-seconds (default: 60000).
     */
    public ServiceConversationServerConfig shutdownTimeoutMillis(@SuppressWarnings("hiding") int shutdownTimeoutMillis)
    {
        this.shutdownTimeoutMillis = shutdownTimeoutMillis;
        return this;
    }

    public boolean isDaemonize()
    {
        return daemonize;
    }

    /**
     * Configures whether the service conversation threads are daemonized (default: <code>true</code>).
     */
    public ServiceConversationServerConfig daemonize(@SuppressWarnings("hiding") boolean daemonize)
    {
        this.daemonize = daemonize;
        return this;
    }

    /**
     * Configures that the service conversation threads are not daemonized.
     */
    public ServiceConversationServerConfig undaemonize()
    {
        this.daemonize = false;
        return this;
    }
}
