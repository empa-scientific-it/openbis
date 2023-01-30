/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.string.ReflectingStringEscaper;
import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;

/**
 * The advisor for automatically escaping HTML strings in the values returned by implementations of {@link IClientService}.
 * <p>
 * Though it is not necessary to subclass DefaultPointcutAdvisor for the implementation, we subclass here to make the configuration in spring a bit
 * simpler.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class StringHtmlEscapingPointcutAdvisor extends DefaultPointcutAdvisor
{
    private static final long serialVersionUID = 1L;

    private static final Logger escapeLog = LogFactory.getLogger(LogCategory.OPERATION,
            StringHtmlEscapingPointcutAdvisor.class);

    /**
     * The public constructor.
     */
    public StringHtmlEscapingPointcutAdvisor()
    {
        this(new StringHtmlEscapingPointcutAdvisorMethodInterceptor());
    }

    /**
     * Constructor for testing purposes.
     * 
     * @param methodInterceptor
     */
    public StringHtmlEscapingPointcutAdvisor(MethodInterceptor methodInterceptor)
    {
        super(new ClientServiceMatchingPointcut(), methodInterceptor);
    }

    private static class ClientServiceMatchingPointcut extends StaticMethodMatcherPointcut
    {

        @Override
        public ClassFilter getClassFilter()
        {
            return new RootClassFilter(IClientService.class);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean matches(Method method, Class targetClass)
        {
            if (method.getReturnType() == Void.TYPE)
            {
                return false;
            }

            if (method.getReturnType().isAnnotationPresent(DoNotEscape.class))
            {
                return false;
            }

            return true;
        }
    }

    /**
     * Class for verifying authorization. Made public so it can be extended in tests.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class StringHtmlEscapingPointcutAdvisorMethodInterceptor implements
            MethodInterceptor
    {
        /**
         * Get the session token and any guarded parameters and invoke the guards on those parameters.
         */
        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            Object result = methodInvocation.proceed();

            if (result != null)
            {
                result = escapeObject(methodInvocation, result);
            }
            return result;
        }

        private Object escapeObject(MethodInvocation methodInvocation, Object originalResult)
        {
            Object result = originalResult;
            // Need to log unescaped result here, since it might be modified below
            escapeLog.debug(methodInvocation.getMethod().getName() + " converting   "
                    + originalResult);
            if (originalResult instanceof String)
            {
                // TODO 2010-11-15, CR: Do we need to escape strings in general?
                // need to handle prepareExport then, e.g.:
                // if (methodInvocation.getMethod().isAnnotationPresent(DoNotEscape.class) == false)
                // {
                // result = StringEscapeUtils.escapeHtml((String) originalResult);
                // }
            } else
            {
                // Escape the result objects
                ReflectingStringEscaper.escapeDeep(originalResult);
            }
            escapeLog.debug(methodInvocation.getMethod().getName() + " converted to " + result);
            return result;
        }
    }
}
