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

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerFactory;
import ch.systemsx.cisd.openbis.generic.server.LongRunningThreadInterceptor.InvocationStart;

public class LongRunningThreadLogger implements Runnable
{
    private final static Logger logger =
            LogFactory.getLogger(LogCategory.ACCESS, LongRunningThreadLogger.class);

    private final LongRunningThreadLogConfiguration config;

    private final LongRunningThreadInterceptor interceptor;

    public LongRunningThreadLogger(LongRunningThreadInterceptor interceptor, LongRunningThreadLogConfiguration config)
    {
        this.interceptor = interceptor;
        this.config = config;
    }

    @Override
    public void run()
    {
        while (true)
        {
            Set<InvocationStart> invocations = interceptor.getInvocationsRunningLongerThan(config.maxValidInvocationLength());
            if (config.isLoggingEnabled())
            {
                log(invocations);
            }

            try
            {
                Thread.sleep(config.logInterval());
            } catch (InterruptedException ex)
            {
            }
        }
    }

    private void log(Set<InvocationStart> invocations)
    {
        if (invocations.size() != 0)
        {
            logger.warn("Long running invocation report");
            logger.warn("------------------------------");
            for (final InvocationStart invocation : invocations)
            {

                final Object wrappedObject = invocation.invocation.getThis();
                if (wrappedObject instanceof IInvocationLoggerFactory<?> == false)
                {
                    continue;
                }

                final IInvocationLoggerFactory<?> loggerFactory =
                        (IInvocationLoggerFactory<?>) wrappedObject;
                final Object serviceLogger = loggerFactory.createLogger(new IInvocationLoggerContext()
                    {

                        @Override
                        public String tryToGetSessionToken()
                        {
                            return null;
                        }

                        @Override
                        public boolean invocationFinished()
                        {
                            return false;
                        }

                        @Override
                        public boolean invocationWasSuccessful()
                        {
                            return false;
                        }

                        @Override
                        public long getElapsedTime()
                        {
                            return System.currentTimeMillis() - invocation.startTime;
                        }
                    });
                final Method method = invocation.invocation.getMethod();

                long duration = (System.currentTimeMillis() - invocation.startTime) / 1000;
                logger.warn("Thread " + invocation.threadName + " has been executing " + method.getDeclaringClass().getCanonicalName() + "."
                        + method.getName() + " for " + String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60)));
                try
                {
                    method.invoke(serviceLogger, invocation.invocation.getArguments());
                } catch (Exception e)
                {
                    logger.error(e);
                }
            }
            logger.info("-----------");
        }
    }
}
