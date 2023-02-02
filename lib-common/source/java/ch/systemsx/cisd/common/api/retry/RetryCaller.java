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

package ch.systemsx.cisd.common.api.retry;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.common.api.retry.config.DefaultRetryConfiguration;
import ch.systemsx.cisd.common.api.retry.config.RetryConfiguration;
import ch.systemsx.cisd.common.logging.ConsoleLogger;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * @author pkupczyk
 */
public abstract class RetryCaller<T, E extends Throwable>
{
    private final RetryConfiguration configuration;

    private int retryCounter;

    private int waitingTime;

    private final ISimpleLogger logger;

    public RetryCaller()
    {
        this(DefaultRetryConfiguration.getInstance());
    }

    public RetryCaller(RetryConfiguration configuration)
    {
        this(configuration, new ConsoleLogger(System.err));
    }

    public RetryCaller(RetryConfiguration configuration, ISimpleLogger logger)
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException("Configuration was null");
        }
        if (configuration.getMaximumNumberOfRetries() < 0)
        {
            throw new IllegalArgumentException("MaximumNumberOfRetries must be >= 0");
        }
        if (configuration.getWaitingTimeBetweenRetries() <= 0)
        {
            throw new IllegalArgumentException("WaitingTimeBetweenRetries must be > 0");
        }
        if (configuration.getWaitingTimeBetweenRetriesIncreasingFactor() <= 0)
        {
            throw new IllegalArgumentException(
                    "WaitingTimeBetweenRetriesIncreasingFactor must be > 0");
        }

        this.configuration = configuration;
        this.waitingTime = configuration.getWaitingTimeBetweenRetries();
        this.logger = logger;
    }

    protected abstract T call() throws E;

    public T callWithRetry() throws E
    {
        while (true)
        {
            try
            {
                T result = call();
                return result;
            } catch (RuntimeException e)
            {
                if (isRetryableException(e))
                {
                    if (shouldRetry())
                    {
                        logger.log(LogLevel.WARN, "Call failed - will retry");
                        waitForRetry();
                    } else
                    {
                        logger.log(LogLevel.WARN, "Call failed - will NOT retry");
                        throw e;
                    }
                } else
                {
                    throw e;
                }
            }
        }
    }

    protected boolean isRetryableException(RuntimeException e)
    {
        if (e instanceof RemoteConnectFailureException)
        {
            return true;
        }
        if (e instanceof RemoteAccessException)
        {
            Throwable cause = e.getCause();
            Throwable rootCause = ExceptionUtils.getRootCause(e);

            return isRetryableRemoteAccessCause(cause) || isRetryableRemoteAccessCause(rootCause);
        }
        return false;
    }

    private boolean isRetryableRemoteAccessCause(Throwable cause)
    {
        return cause instanceof SocketTimeoutException || cause instanceof SocketException;
    }

    private boolean shouldRetry()
    {
        return retryCounter < configuration.getMaximumNumberOfRetries();
    }

    private void waitForRetry()
    {
        try
        {
            Thread.sleep(waitingTime);
            waitingTime *= configuration.getWaitingTimeBetweenRetriesIncreasingFactor();
            retryCounter++;
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

}
