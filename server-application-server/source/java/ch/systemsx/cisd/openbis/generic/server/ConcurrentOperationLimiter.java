/*
 * Copyright 2018 ETH Zuerich, CISD
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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
@Component(value = "concurrent-operation-limiter")
public class ConcurrentOperationLimiter implements IConcurrentOperationLimiter
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ConcurrentOperationLimiter.class);

    @Autowired
    private ConcurrentOperationLimiterConfig config;

    private Map<Pattern, Semaphore> semaphores;

    private Set<Thread> threads;

    @SuppressWarnings("unused")
    private ConcurrentOperationLimiter()
    {
    }

    public ConcurrentOperationLimiter(ConcurrentOperationLimiterConfig config)
    {
        this.config = config;
        init();
    }

    @PostConstruct
    private void init()
    {
        Map<Pattern, Semaphore> semaphores = new LinkedHashMap<Pattern, Semaphore>();

        for (ConcurrentOperationLimit limit : config.getLimits())
        {
            Semaphore semaphore = new Semaphore(limit.getLimit());
            Pattern pattern = Pattern.compile(limit.getOperation());
            semaphores.put(pattern, semaphore);
        }

        this.semaphores = semaphores;
        this.threads = new HashSet<Thread>();
    }

    @Override
    public <T> T executeLimitedWithTimeout(String operationName, ConcurrentOperation<T> operation)
    {
        return executeLimited(operationName, operation, config.getTimeout());
    }

    @Override
    public <T> T executeLimitedWithTimeoutAsync(String operationName, ConcurrentOperation<T> operation)
    {
        return executeLimited(operationName, operation, config.getTimeoutAsync());
    }

    private <T> T executeLimited(String operationName, ConcurrentOperation<T> operation, long timeout)
    {
        // To prevent blocking itself, a thread that has acquired a permit to execute an operation
        // must not try to acquire another permit until this operation is finished.

        if (threads.contains(Thread.currentThread()))
        {
            return operation.execute();
        }

        Semaphore semaphore = getSemaphore(operationName);

        if (semaphore == null)
        {
            return operation.execute();
        } else
        {
            try
            {
                operationLog.info("Operation '" + operationName + "' will try to acquire an execution permit.");

                if (semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS))
                {
                    try
                    {
                        operationLog.info("Operation '" + operationName + "' successfully acquired an execution permit.");
                        threads.add(Thread.currentThread());
                        return operation.execute();
                    } finally
                    {
                        operationLog.info("Operation '" + operationName + "' released an execution permit.");
                        threads.remove(Thread.currentThread());
                        semaphore.release();
                    }
                } else
                {
                    operationLog.info(
                            "Operation '" + operationName + "' failed to acquire an execution permit within "
                                    + DurationFormatUtils.formatDuration(timeout, "H:mm:ss.SSS") + ".");
                    throw new UserFailureException(
                            "Sorry, the server is very loaded at the moment. Your request can not be currently processed. Please try again later.");
                }
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    private Semaphore getSemaphore(String operationName)
    {
        for (Map.Entry<Pattern, Semaphore> entry : semaphores.entrySet())
        {
            if (entry.getKey().matcher(operationName).matches())
            {
                return entry.getValue();
            }
        }

        return null;
    }

}
