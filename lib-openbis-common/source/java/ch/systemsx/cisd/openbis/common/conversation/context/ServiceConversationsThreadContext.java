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

package ch.systemsx.cisd.openbis.common.conversation.context;

import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.common.conversation.progress.ServiceConversationNullProgressListener;

/**
 * This class provides methods for accessing information about the current service conversation. All the information is stored in thread local
 * variables.
 * 
 * @author Jakub Straszewski
 */
public class ServiceConversationsThreadContext
{
    private static ThreadLocal<IServiceConversationProgressListener> progressListener;

    static
    {
        progressListener = new ThreadLocal<IServiceConversationProgressListener>();
    }

    /**
     * Sets the current service conversation progress listener.
     */
    public static void setProgressListener(IServiceConversationProgressListener listener)
    {
        progressListener.set(listener);
    }

    /**
     * Removes the current service conversation progress listener.
     */
    public static void unsetProgressListener()
    {
        progressListener.remove();
    }

    /**
     * Get the current service conversation progress listener. If there is no service conversation available then a dummy progress listener is
     * returned. The dummy listener is returned just for a convenience to eliminate all the not null checks. Calling methods on the dummy listener
     * doesn't have any effect.
     */
    public static IServiceConversationProgressListener getProgressListener()
    {
        IServiceConversationProgressListener listener = progressListener.get();

        if (listener == null)
        {
            return new ServiceConversationNullProgressListener();
        } else
        {
            return listener;
        }
    }
}
