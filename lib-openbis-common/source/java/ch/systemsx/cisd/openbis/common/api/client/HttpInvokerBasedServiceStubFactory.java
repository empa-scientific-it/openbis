/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.common.api.client;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

/**
 * Implementation of the service stub factory based on Spring's HttpInvoker.
 * 
 * @author Franz-Josef Elmer
 */
public class HttpInvokerBasedServiceStubFactory implements IServiceStubFactory
{

    @Override
    public <S> S createServiceStub(Class<S> serviceClass, String serverUrl, long timeoutInMillis)
    {
        return HttpInvokerUtils.createServiceStub(serviceClass, serverUrl, timeoutInMillis);
    }

}
