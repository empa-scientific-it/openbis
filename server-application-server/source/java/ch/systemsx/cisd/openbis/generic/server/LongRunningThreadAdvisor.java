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

package ch.systemsx.cisd.openbis.generic.server;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.core.Ordered;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * @author anttil
 */
public class LongRunningThreadAdvisor extends DefaultPointcutAdvisor
{
    private static final long serialVersionUID = 1L;

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE;

    LongRunningThreadAdvisor()
    {
        super(createPointcut(), createAdvice());
        setOrder(ORDER);
    }

    private final static Advice createAdvice()
    {
        LongRunningThreadInterceptor advice = new LongRunningThreadInterceptor();
        new Thread(new LongRunningThreadLogger(advice, LongRunningThreadLogConfiguration.getInstance())).start();
        return advice;
    }

    private final static Pointcut createPointcut()
    {
        return new AllServerMethodsPointcut();
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

}
