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

import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.base.namedthread.NamingThreadFactory;
import ch.systemsx.cisd.common.serviceconversation.server.ProgressInfo;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;

/**
 * Creates a separate thread that regularly sends a progress information without a need of calling IServiceConversationProgressListener.update()
 * method.
 * 
 * @author pkupczyk
 */
public class ServiceConversationAutomaticProgressListener implements
        IServiceConversationProgressListener
{

    private ScheduledThreadPoolExecutor executor;

    private ScheduledFuture<?> future;

    public ServiceConversationAutomaticProgressListener(final ServiceConversationServer server,
            final String conversationId, final int reportingInterval, final Method method)
    {
        NamingThreadFactory threadFactory =
                new NamingThreadFactory(Thread.currentThread().getName()
                        + "-automatic-progress-listener");
        threadFactory.setCreateDaemonThreads(true);

        executor = new ScheduledThreadPoolExecutor(1);
        executor.setThreadFactory(threadFactory);

        future = executor.scheduleAtFixedRate(new Runnable()
            {
                @Override
                public void run()
                {
                    server.reportProgress(conversationId, new ProgressInfo("processing "
                            + method.getDeclaringClass().getSimpleName() + "." + method.getName()));
                }
            }, 0, reportingInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void update(final String label, final int totalItemsToProcess,
            final int numItemsProcessed)
    {
        // ignore manually reported progress
    }

    @Override
    public void close()
    {
        this.executor.shutdown();
        this.future.cancel(false);
    }

}
