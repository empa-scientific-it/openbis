/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.pat;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;

public final class PersonalAccessTokenInterceptor implements MethodInterceptor, Serializable
{

    private static final long serialVersionUID = 1L;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        final IPersonalAccessTokenAware aware = (IPersonalAccessTokenAware) invocation.getThis();

        final Object handler =
                aware.createPersonalAccessTokenInvocationHandler(new PersonalAccessTokenInvocation((ProxyMethodInvocation) invocation));

        try
        {
            return invocation.getMethod().invoke(handler, invocation.getArguments());
        } catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof PersonalAccessTokenInvocationException)
            {
                throw e.getCause().getCause();
            } else
            {
                throw e.getCause();
            }
        }
    }

    private static final class PersonalAccessTokenInvocation implements IPersonalAccessTokenInvocation
    {

        private final ProxyMethodInvocation originalInvocation;

        private PersonalAccessTokenInvocation(ProxyMethodInvocation originalInvocation)
        {
            this.originalInvocation = originalInvocation;
        }

        @Override public <T> T proceedWithOriginalArguments()
        {
            try
            {
                return (T) originalInvocation.proceed();
            } catch (Throwable e)
            {
                throw new PersonalAccessTokenInvocationException(e);
            }
        }

        @Override public <T> T proceedWithNewFirstArgument(final Object argument)
        {
            try
            {
                Object[] arguments = originalInvocation.getArguments();
                arguments[0] = argument;
                originalInvocation.setArguments(arguments);
                return (T) originalInvocation.proceed();
            } catch (Throwable e)
            {
                throw new PersonalAccessTokenInvocationException(e);
            }
        }
    }

    private static final class PersonalAccessTokenInvocationException extends RuntimeException
    {

        PersonalAccessTokenInvocationException(Throwable cause)
        {
            super(cause);
        }

    }

}
