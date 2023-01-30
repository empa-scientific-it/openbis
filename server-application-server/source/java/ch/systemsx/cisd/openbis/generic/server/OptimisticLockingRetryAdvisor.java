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

package ch.systemsx.cisd.openbis.generic.server;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;

import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.spring.LogAdvisor;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * @author Franz-Josef Elmer
 */
public class OptimisticLockingRetryAdvisor extends DefaultPointcutAdvisor
{

    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            OptimisticLockingRetryAdvisor.class);

    public OptimisticLockingRetryAdvisor()
    {
        super(new AllServerMethodsPointcut(), new RetryInterceptor());
        setOrder(LogAdvisor.ORDER + 1);
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

    private static class RetryInterceptor implements MethodInterceptor
    {
        private static final int MAX_WAITING_TIME_FOR_RETRY = 5000;

        private static final int NUMBER_OF_TRIES = 5;

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable
        {
            Exception latestException = new IllegalStateException();
            for (int i = 0; i < NUMBER_OF_TRIES; i++)
            {
                try
                {
                    if (invocation instanceof ReflectiveMethodInvocation)
                    {
                        return ((ReflectiveMethodInvocation) invocation).invocableClone().proceed();
                    } else
                    {
                        return invocation.proceed();
                    }
                } catch (Exception ex)
                {
                    latestException = ex;
                    if (causedByOptimisticLockingOrDeadlockLoserException(ex) == false)
                    {
                        throw ex;
                    }
                    boolean retry = i < NUMBER_OF_TRIES - 1;
                    if (retry)
                    {
                        operationLog.info("Retry after the " + (i + 1) + ". failed invocation of "
                                + invocation.getMethod());
                        try
                        {
                            Thread.sleep((int) (Math.random() * MAX_WAITING_TIME_FOR_RETRY));
                        } catch (InterruptedException e)
                        {
                            // Ignored
                        }
                    } else
                    {
                        operationLog.error("Giving up after " + (i + 1) + ". failed invocation of "
                                + invocation.getMethod() + ". Reason: " + ex);
                    }
                }
            }
            throw latestException;
        }

        private boolean causedByOptimisticLockingOrDeadlockLoserException(Exception ex)
        {
            HibernateOptimisticLockingFailureException optimisticLockingException =
                    ExceptionUtils.tryGetThrowableOfClass(ex,
                            HibernateOptimisticLockingFailureException.class);
            if (optimisticLockingException != null)
            {
                return true;
            }
            DeadlockLoserDataAccessException deadlockLoserException =
                    ExceptionUtils.tryGetThrowableOfClass(ex,
                            DeadlockLoserDataAccessException.class);
            return deadlockLoserException != null;
        }
    }

}
