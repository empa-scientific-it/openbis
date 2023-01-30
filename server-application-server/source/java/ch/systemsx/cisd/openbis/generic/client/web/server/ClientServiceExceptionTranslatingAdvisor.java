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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;

/**
 * Translates server side {@link ch.systemsx.cisd.common.exceptions.UserFailureException} thrown by all {@link IClientService} methods to client side
 * {@link ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException}.
 * 
 * @author Piotr Buczek
 */
public class ClientServiceExceptionTranslatingAdvisor extends DefaultPointcutAdvisor
{

    private static final long serialVersionUID = 1L;

    public ClientServiceExceptionTranslatingAdvisor()
    {
        super(new AllClientServiceMethodsPointcut(),
                new UserFailureExceptionTranslatingInterceptor());
    }

    private static class AllClientServiceMethodsPointcut implements Pointcut
    {
        @Override
        public MethodMatcher getMethodMatcher()
        {
            return MethodMatcher.TRUE;
        }

        @Override
        public ClassFilter getClassFilter()
        {
            return new RootClassFilter(IClientService.class);
        }
    }

    private static class UserFailureExceptionTranslatingInterceptor implements MethodInterceptor
    {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable
        {
            try
            {
                return invocation.proceed();
            } catch (ch.systemsx.cisd.common.exceptions.UserFailureException ex)
            {
                throw UserFailureExceptionTranslator.translate(ex);
            }
        }
    }

}
