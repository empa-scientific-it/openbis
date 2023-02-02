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

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author pkupczyk
 */
public class RetryInterceptor implements MethodInterceptor
{

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        Method method = invocation.getMethod();
        if (method.isAnnotationPresent(Retry.class))
        {
            RetryCaller<Object, Throwable> caller = new RetryCaller<Object, Throwable>()
                {
                    @Override
                    protected Object call() throws Throwable
                    {
                        return invocation.proceed();
                    }
                };
            return caller.callWithRetry();
        } else
        {
            return invocation.proceed();
        }
    }

}
