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

package ch.systemsx.cisd.common.api.retry;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.ClassUtils;

/**
 * @author pkupczyk
 */
public class RetryProxyFactory
{

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T proxyTarget)
    {
        if (proxyTarget == null)
        {
            return null;
        }
        if (proxyTarget instanceof RetryProxy)
        {
            return proxyTarget;
        } else
        {
            Class<?>[] proxyTargetInterfaces = ClassUtils.getAllInterfaces(proxyTarget);

            if (proxyTargetInterfaces == null || proxyTargetInterfaces.length == 0)
            {
                // We could ask CGLIB library to create a proxy even if the object doesn't
                // implement any interfaces, but we don't want to use this library.
                // Instead, we just return an unchanged object.
                return proxyTarget;
            } else
            {
                ProxyFactory proxyFactory = new ProxyFactory(proxyTarget);
                proxyFactory.addInterface(RetryProxy.class);
                proxyFactory.addAdvice(new RetryInterceptor());
                return (T) proxyFactory.getProxy(proxyTarget.getClass().getClassLoader());
            }

        }
    }

}
