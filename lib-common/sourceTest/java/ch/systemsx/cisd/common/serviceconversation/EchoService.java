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

package ch.systemsx.cisd.common.serviceconversation;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.serviceconversation.server.IService;
import ch.systemsx.cisd.common.serviceconversation.server.IServiceFactory;

class EchoService implements IService
{
    @Override
    public void run(IServiceMessenger messenger)
    {
        try
        {
            System.err.println(Thread.currentThread().getName() + " service: startup");
            while (true)
            {
                messenger.send(messenger.receive(String.class));
            }
        } catch (InterruptedExceptionUnchecked ex)
        {
            // This exception will be thrown when the client terminates the server.
            System.err.println(Thread.currentThread().getName() + " service: shutdown");
        } catch (RuntimeException ex)
        {
            ex.printStackTrace();
            // This doesn't matter: the exception goes into the void.
            throw ex;
        }
    }

    static IServiceFactory createFactory(final int timeoutMillis)
    {
        return new IServiceFactory()
            {
                @Override
                public IService create()
                {
                    return new EchoService();
                }

                @Override
                public int getClientTimeoutMillis()
                {
                    return timeoutMillis;
                }

                @Override
                public String getServiceTypeId()
                {
                    return "echo";
                }

                @Override
                public boolean interruptServiceOnClientException()
                {
                    return true;
                }
            };
    }
}