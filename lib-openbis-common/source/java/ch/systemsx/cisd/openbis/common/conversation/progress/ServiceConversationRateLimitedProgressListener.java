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

package ch.systemsx.cisd.openbis.common.conversation.progress;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.base.namedthread.NamingThreadFactory;
import ch.systemsx.cisd.common.serviceconversation.server.ProgressInfo;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;

/**
 * Sends incoming progress updates to the remote client through the ServiceConversationServer. The maximum reporting rate is limited by the value
 * given as an argument. If a progress update is received while the previous update is still waiting to be sent (because of the maximum reporting
 * rate), the previous update will be discarded.
 * 
 * @author anttil
 */
public class ServiceConversationRateLimitedProgressListener implements
        IServiceConversationProgressListener
{

    private ServiceConversationServer server;

    private String conversationId;

    private ScheduledThreadPoolExecutor executor;

    private Update lastUpdate;

    private int interval;

    private ScheduledFuture<?> future;

    public ServiceConversationRateLimitedProgressListener(ServiceConversationServer server,
            String conversationId, int reportingInterval)
    {
        this.server = server;
        this.conversationId = conversationId;
        this.interval = reportingInterval;

        NamingThreadFactory threadFactory =
                new NamingThreadFactory(Thread.currentThread().getName()
                        + "-rate-limited-progress-listener");
        threadFactory.setCreateDaemonThreads(true);

        executor = new ScheduledThreadPoolExecutor(1);
        executor.setThreadFactory(threadFactory);
    }

    @Override
    public synchronized void update(final String label, final int totalItemsToProcess,
            final int numItemsProcessed)
    {
        if (future != null)
        {
            future.cancel(false);
        }

        long lastExecution = 0;
        if (this.lastUpdate != null)
        {
            lastExecution = this.lastUpdate.getLastExecution();
        }

        Update update =
                new Update(this.server, this.conversationId, new ProgressInfo(label,
                        totalItemsToProcess, numItemsProcessed), lastExecution);

        long timeSinceLastExecution = System.currentTimeMillis() - lastExecution;

        if (timeSinceLastExecution > this.interval)
        {
            future = this.executor.schedule(update, 0, TimeUnit.MILLISECONDS);
        } else
        {
            future =
                    this.executor.schedule(update, this.interval - timeSinceLastExecution,
                            TimeUnit.MILLISECONDS);
        }

        this.lastUpdate = update;
    }

    @Override
    public synchronized void close()
    {
        if (future != null)
        {
            future.cancel(false);
        }
        this.executor.shutdown();
    }

    private static class Update implements Runnable
    {

        private ServiceConversationServer server;

        private String conversationId;

        private ProgressInfo progress;

        private long lastExecution;

        public Update(ServiceConversationServer server, String conversationId,
                ProgressInfo progress, long lastExecution)
        {
            this.server = server;
            this.conversationId = conversationId;
            this.progress = progress;
            this.lastExecution = lastExecution;
        }

        @Override
        public void run()
        {
            this.lastExecution = System.currentTimeMillis();
            server.reportProgress(conversationId, progress);
        }

        public long getLastExecution()
        {
            return this.lastExecution;
        }
    }
}
