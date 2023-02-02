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

import ch.systemsx.cisd.common.concurrent.ITerminableFuture;

/**
 * The record holding information about a service conversation.
 * 
 * @author Bernd Rinn
 */
class ServiceConversationRecord
{
    private final BidirectionalServiceMessenger messenger;

    private final boolean interruptServerOnClientException;

    private ITerminableFuture<Void> controller;

    ServiceConversationRecord(BidirectionalServiceMessenger messenger,
            boolean interruptServerOnClientException)
    {
        this.messenger = messenger;
        this.interruptServerOnClientException = interruptServerOnClientException;
    }

    BidirectionalServiceMessenger getMessenger()
    {
        return messenger;
    }

    ITerminableFuture<Void> getController()
    {
        return controller;
    }

    void setController(ITerminableFuture<Void> controller)
    {
        this.controller = controller;
    }

    boolean isInterruptServerOnClientException()
    {
        return interruptServerOnClientException;
    }
}
