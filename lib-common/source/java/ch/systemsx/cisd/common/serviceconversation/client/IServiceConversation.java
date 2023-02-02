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

package ch.systemsx.cisd.common.serviceconversation.client;

import java.io.Closeable;

import ch.systemsx.cisd.common.serviceconversation.IServiceMessenger;

/**
 * The service conversation.
 * 
 * @author Bernd Rinn
 */
public interface IServiceConversation extends IServiceMessenger, Closeable
{
    /**
     * Tells the service to terminate. Use this for calls that have no inherent definition of "finished".
     */
    public void terminate();

    /**
     * Closes this messenger. Do not call any other method after this call.
     */
    @Override
    public void close();

    /**
     * Returns the size of the server work queue at startup time of this conversation.
     * <p>
     * A value > 0 means that the conversation started queued.
     */
    public int getServerWorkQueueSizeAtStartup();
}