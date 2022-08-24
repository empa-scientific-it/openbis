/*
 * Copyright 2009 ETH Zuerich, CISD
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

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.Ordered;

public class PersonalAccessTokenAdvisor extends DefaultPointcutAdvisor
{

    private static final long serialVersionUID = 1L;

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE;

    PersonalAccessTokenAdvisor()
    {
        super(createPointcut(), createAdvice());
        setOrder(ORDER);
    }

    private static Advice createAdvice()
    {
        return new PersonalAccessTokenInterceptor();
    }

    private static Pointcut createPointcut()
    {
        return new PersonalAccessTokenAwarePointcut();
    }

    private static class PersonalAccessTokenAwarePointcut implements Pointcut
    {
        @Override
        public MethodMatcher getMethodMatcher()
        {
            return MethodMatcher.TRUE;
        }

        @Override
        public ClassFilter getClassFilter()
        {
            return new RootClassFilter(IPersonalAccessTokenAware.class);
        }
    }

}
