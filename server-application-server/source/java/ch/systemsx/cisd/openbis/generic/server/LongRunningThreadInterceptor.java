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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author anttil
 */
public class LongRunningThreadInterceptor implements MethodInterceptor, Serializable
{

    private static final long serialVersionUID = 1L;

    private final Map<String, InvocationStart> invocations;

    public LongRunningThreadInterceptor()
    {
        this.invocations = new ConcurrentHashMap<String, InvocationStart>();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        String threadName = Thread.currentThread().getName();
        invocations.put(threadName, new InvocationStart(System.currentTimeMillis(), invocation, threadName));
        try
        {
            return invocation.proceed();
        } finally
        {
            invocations.remove(threadName);
        }
    }

    public Set<InvocationStart> getInvocationsRunningLongerThan(long limit)
    {
        Set<InvocationStart> overdue = new HashSet<InvocationStart>();
        for (String threadName : invocations.keySet())
        {
            InvocationStart invocation = invocations.get(threadName);
            if (invocation.startTime + limit < System.currentTimeMillis())
            {
                overdue.add(invocation);
            }
        }
        return overdue;
    }

    static class InvocationStart
    {
        public final long startTime;

        public final MethodInvocation invocation;

        public final String threadName;

        public InvocationStart(long startTime, MethodInvocation invocation, String threadName)
        {
            this.startTime = startTime;
            this.invocation = invocation;
            this.threadName = threadName;

        }
    }
}
