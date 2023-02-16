/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.filetransfer;

/**
 * @author pkupczyk
 */

public class DefaultRetryProvider implements IRetryProvider
{

    private ILogger logger;

    private int maximumNumberOfRetries;

    private int waitingTimeBetweenRetries;

    private int waitingTimeBetweenRetriesIncreasingFactor;

    public DefaultRetryProvider(ILogger logger)
    {
        this(logger, 5, 1000, 2);
    }

    public DefaultRetryProvider(ILogger logger, int maximumNumberOfRetries, int waitingTimeBetweenRetries,
            int waitingTimeBetweenRetriesIncreasingFactor)
    {
        if (maximumNumberOfRetries < 0)
        {
            throw new IllegalArgumentException("MaximumNumberOfRetries must be >= 0");
        }
        if (waitingTimeBetweenRetries <= 0)
        {
            throw new IllegalArgumentException("WaitingTimeBetweenRetries must be > 0");
        }
        if (waitingTimeBetweenRetriesIncreasingFactor <= 0)
        {
            throw new IllegalArgumentException(
                    "WaitingTimeBetweenRetriesIncreasingFactor must be > 0");
        }

        this.logger = logger;
        this.maximumNumberOfRetries = maximumNumberOfRetries;
        this.waitingTimeBetweenRetries = waitingTimeBetweenRetries;
        this.waitingTimeBetweenRetriesIncreasingFactor = waitingTimeBetweenRetriesIncreasingFactor;
    }

    @Override
    public <T> T executeWithRetry(IRetryAction<T> action) throws DownloadException
    {
        return new Retry().callWithRetry(action);
    }

    private class Retry
    {

        private int retryCounter;

        private int waitingTime;

        public Retry()
        {
            this.waitingTime = waitingTimeBetweenRetries;
        }

        public <T> T callWithRetry(IRetryAction<T> action) throws DownloadException
        {
            while (true)
            {
                try
                {
                    T result = action.execute();
                    retryCounter = 0;
                    return result;
                } catch (Exception e)
                {
                    if (isRetriable(e))
                    {
                        if (retryCounter < maximumNumberOfRetries)
                        {
                            if (logger.isEnabled(LogLevel.WARN))
                            {
                                logger.log(getClass(), LogLevel.WARN, "Call failed - will retry in " + (waitingTime / 1000) + " second(s)", e);
                            }

                            waitForRetry();

                            if (logger.isEnabled(LogLevel.WARN))
                            {
                                logger.log(getClass(), LogLevel.WARN, "Retrying (" + retryCounter + "/" + maximumNumberOfRetries + ")");
                            }
                        } else
                        {
                            if (logger.isEnabled(LogLevel.WARN))
                            {
                                logger.log(getClass(), LogLevel.WARN,
                                        "Call failed - will NOT retry (maximum number of " + maximumNumberOfRetries
                                                + " reties has been already reached)",
                                        e);
                            }
                            throw e;
                        }
                    } else
                    {
                        throw e;
                    }
                }
            }

        }

        private void waitForRetry()
        {
            try
            {
                Thread.sleep(waitingTime);
                waitingTime *= waitingTimeBetweenRetriesIncreasingFactor;
                retryCounter++;
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        protected boolean isRetriable(Exception e)
        {
            if (e instanceof DownloadException)
            {
                DownloadException de = (DownloadException) e;
                return de.isRetriable() != null && de.isRetriable();
            } else
            {
                return false;
            }
        }

    }

}
