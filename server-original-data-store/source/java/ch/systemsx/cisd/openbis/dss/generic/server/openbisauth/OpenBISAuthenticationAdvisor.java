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

package ch.systemsx.cisd.openbis.dss.generic.server.openbisauth;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ManagedAuthentication;

/**
 * A {@link Pointcut} for {@link OpenBISAuthenticationInterceptor}.
 * 
 * @author Kaloyan Enimanev
 */
public class OpenBISAuthenticationAdvisor extends DefaultPointcutAdvisor
{

    private static final long serialVersionUID = 1L;

    public OpenBISAuthenticationAdvisor(OpenBISAuthenticationInterceptor methodInterceptor)
    {
        super(createPointcut(), methodInterceptor);
    }

    private static Pointcut createPointcut()
    {
        return new Pointcut()
            {

                @Override
                public MethodMatcher getMethodMatcher()
                {
                    return new AnnotationMethodMatcher(ManagedAuthentication.class);
                }

                @Override
                public ClassFilter getClassFilter()
                {
                    return new RootClassFilter(IEncapsulatedOpenBISService.class);
                }
            };
    }

}
