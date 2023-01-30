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

package ch.systemsx.cisd.openbis.generic.server;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.postgresql.util.PSQLException;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Translates deeply nested exceptions thrown on server side e.g. by Spring that contain user readable error messages (like
 * {@link TransactionSystemException} or {@link DataAccessException}) into {UserFailureException} with message taken from the root exception cause.
 * <p>
 * The most important reason why this advisor was introduced was to translate exceptions that happen just before commit/rollback of transactions, like
 * {@link TransactionSystemException}. Such exceptions can't be handled inside the server methods because the commit/rollback is invoked outside the
 * server class by Spring AOP (see {@link Transactional}). Without this advisor the translation would need to be done in all clients - web, command
 * line, APIs.
 * 
 * @author Piotr Buczek
 */
public class ServerExceptionTranslatingAdvisor extends DefaultPointcutAdvisor
{

    private static final long serialVersionUID = 1L;

    public ServerExceptionTranslatingAdvisor()
    {
        super(new AllServerMethodsPointcut(), new UserFailureExceptionTranslatingInterceptor());
    }

    private static class AllServerMethodsPointcut implements Pointcut
    {
        @Override
        public MethodMatcher getMethodMatcher()
        {
            return MethodMatcher.TRUE;
        }

        @Override
        public ClassFilter getClassFilter()
        {
            return new RootClassFilter(IServer.class);
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
            } catch (NestedRuntimeException ex)
            {
                if (ex instanceof TransactionSystemException || ex instanceof DataAccessException)
                {
                    Throwable cause = ex.getMostSpecificCause();
                    String message = cause.getMessage();
                    if (cause instanceof PSQLException)
                    {
                        int indexOfWhere = message.indexOf("Where: PL/pgSQL");
                        if (indexOfWhere > 0)
                        {
                            message = message.substring(0, indexOfWhere).trim();
                        }
                    }
                    throw new UserFailureException(message, ex);
                } else
                {
                    throw ex; // don't expose query syntax errors etc.
                }
            }
        }
    }

}
