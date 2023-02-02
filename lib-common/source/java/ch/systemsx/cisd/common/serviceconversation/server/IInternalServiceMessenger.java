/*
 * Copyright 2013 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.serviceconversation.IServiceMessenger;

/**
 * An internal version of {@link IServiceMessenger} which allows to send raw messages as well.
 * 
 * @author anttil
 */
interface IInternalServiceMessenger extends IServiceMessenger
{
    /**
     * Sends an exception to the client.
     */
    public void sendException(String errorMsg);

    /**
     * Sends progress updates to the client.
     */
    public void sendProgress(ProgressInfo progress);

}
