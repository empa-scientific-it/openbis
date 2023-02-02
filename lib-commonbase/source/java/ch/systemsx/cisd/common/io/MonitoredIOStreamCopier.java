/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * Helper class which copies bytes from an {@link InputStream} to an {@link OutputStream}. Copying is either done in a series of reading/writing a
 * specified amount of bytes (i.e. bufferSize) or in parallel with an ad-hoc writing thread. If {@link ISimpleLogger} is injected (via
 * {@link #setLogger(ISimpleLogger)} statistical information especially reading/writing speed will be logged after copying has been finished.
 * <p>
 * In order to get accurate speed information the buffer size should be set relatively large, e.g. one MB.
 * 
 * @author Franz-Josef Elmer
 */
public class MonitoredIOStreamCopier
{
    private int bufferSize;

    private ISimpleLogger logger;

    private ITimeProvider timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;

    private Statistics readStatistics;

    private Statistics writeStatistics;

    private LinkedBlockingQueue<WritingItem> queue;

    private Throwable writingException;

    private Thread writingThread;

    /**
     * Creates an instance for copying in a series of reading/writing chunks of specified size.
     */
    public MonitoredIOStreamCopier(int bufferSize)
    {
        this(bufferSize, null);
    }

    /**
     * Creates an instance for copying in chunks of specified size in parallel using a queue of specified size.
     */
    public MonitoredIOStreamCopier(int bufferSize, Long maxQueueSizeInBytes)
    {
        this.bufferSize = bufferSize;
        if (maxQueueSizeInBytes != null)
        {
            long maxQueueSize = maxQueueSizeInBytes / bufferSize;
            if (maxQueueSize < 1)
            {
                throw new ConfigurationFailureException("Maximum queue size "
                        + maxQueueSizeInBytes + " should be larger than buffer size " + bufferSize + ".");
            }
            queue = new LinkedBlockingQueue<WritingItem>((int) maxQueueSize);
        }
    }

    public void setLogger(ISimpleLogger logger)
    {
        this.logger = logger;
        if (logger != null)
        {
            readStatistics = new Statistics(bufferSize);
            writeStatistics = new Statistics(bufferSize);
        }
    }

    void setTimeProvider(ITimeProvider timeProvider)
    {
        this.timeProvider = timeProvider;
    }

    public long copy(InputStream input, OutputStream output)
    {
        long totalNumberOfBytes = 0;
        try
        {
            startWritingThread();
            int numberOfBytes = 0;
            byte[] buffer = new byte[bufferSize];
            while (-1 != (numberOfBytes = readBytes(input, buffer)))
            {
                writeBytes(output, numberOfBytes, buffer);
                totalNumberOfBytes += numberOfBytes;
                buffer = new byte[bufferSize];
            }
            waitOnFinished();
            return totalNumberOfBytes;
        } catch (Throwable ex)
        {
            throw new EnvironmentFailureException("Error after " + totalNumberOfBytes
                    + " bytes copied: " + ex, ex);
        }
    }

    public void close()
    {
        if (logger != null)
        {
            logger.log(LogLevel.INFO, "Reading statistics for input stream: " + readStatistics);
            logger.log(LogLevel.INFO, "Writing statistics for output stream: " + writeStatistics);
        }
    }

    private int readBytes(InputStream input, byte[] buffer) throws IOException
    {
        long t0 = startIO();
        int numberOfBytes = input.read(buffer);
        recordTime(numberOfBytes, t0, readStatistics);
        return numberOfBytes;
    }

    private void writeBytes(OutputStream output, int numberOfBytes, byte[] buffer) throws Throwable
    {
        WritingItem writingItem = new WritingItem(output, buffer, numberOfBytes);
        if (queue == null)
        {
            writingItem.write();
        } else
        {
            addToQueue(writingItem);
        }
    }

    private void startWritingThread()
    {
        if (queue == null)
        {
            return;
        }
        writingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (true)
                    {
                        try
                        {
                            WritingItem writingItem = queue.take();
                            if (writingItem.data == null)
                            {
                                break;
                            }
                            writingItem.write();
                        } catch (InterruptedException ex)
                        {
                            // silently ignored
                        } catch (Throwable ex)
                        {
                            writingException = ex;
                            break;
                        }
                    }
                }
            });
        writingThread.start();
    }

    private void waitOnFinished() throws Throwable
    {
        if (queue == null)
        {
            return;
        }
        addToQueue(new WritingItem(null, null, 0));
        try
        {
            writingThread.join();
        } catch (InterruptedException ex)
        {
            // silently ignored
        }
        if (writingException != null)
        {
            throw writingException;
        }
    }

    private void addToQueue(WritingItem writingItem) throws Throwable
    {
        try
        {
            if (writingException != null)
            {
                throw writingException;
            }
//            queue.put(writingItem);
            int timeout = 60;
            boolean successful = queue.offer(writingItem, timeout, TimeUnit.SECONDS);
            if (successful == false)
            {
                if (writingException != null)
                {
                    throw writingException;
                }
                throw new EnvironmentFailureException("Writing item couldn't be added. Time out " + timeout + " sec.");
            }
        } catch (InterruptedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private long startIO()
    {
        return logger != null ? timeProvider.getTimeInMilliseconds() : -1;
    }

    private void recordTime(int numberOfBytes, long t0, Statistics statistics)
    {
        if (logger != null)
        {
            statistics.add(numberOfBytes, t0, timeProvider.getTimeInMilliseconds());
        }
    }

    private final class WritingItem
    {
        private OutputStream output;

        private byte[] data;

        private int numberOfBytes;

        public WritingItem(OutputStream output, byte[] data, int numberOfBytes)
        {
            this.output = output;
            this.data = data;
            this.numberOfBytes = numberOfBytes;
        }

        public void write() throws IOException
        {
            long t0 = startIO();
            output.write(data, 0, numberOfBytes);
            recordTime(numberOfBytes, t0, writeStatistics);
        }
    }

    private static final class Statistics
    {
        private final List<NumberOfBytesAndIOTime> data = new ArrayList<NumberOfBytesAndIOTime>();

        private final int minNumberOfBytes;

        public Statistics(int minNumberOfBytes)
        {
            this.minNumberOfBytes = minNumberOfBytes;
        }

        void add(int numberOfBytes, long startTime, long endTime)
        {
            if (numberOfBytes > 0)
            {
                data.add(new NumberOfBytesAndIOTime(numberOfBytes, endTime - startTime));
            }
        }

        @Override
        public String toString()
        {
            List<Double> speed = new ArrayList<Double>(data.size());
            long totalNumberOfBytes = 0;
            long totalTime = 0;
            for (NumberOfBytesAndIOTime nt : data)
            {
                totalNumberOfBytes += nt.numberOfBytes;
                totalTime += nt.ioTime;
                if (nt.numberOfBytes >= minNumberOfBytes && nt.ioTime > 0)
                {
                    speed.add((1000.0 * nt.numberOfBytes) / nt.ioTime);
                }
            }
            String medianSpeedInfo = "";
            if (speed.isEmpty() == false)
            {
                long medianBytesPerSecond = Math.round(calculateMedian(speed));
                medianSpeedInfo = " Median speed: " + FileUtilities.byteCountToDisplaySize(medianBytesPerSecond)
                        + "/sec.";
            }
            String averageSpeedInfo = "";
            if (totalNumberOfBytes >= minNumberOfBytes && totalTime > 0)
            {
                long averageBytesPerSecond = Math.round((1000.0 * totalNumberOfBytes) / totalTime);
                averageSpeedInfo = " Average speed: " + FileUtilities.byteCountToDisplaySize(averageBytesPerSecond)
                        + "/sec.";
            }
            return FileUtilities.byteCountToDisplaySize(totalNumberOfBytes) + " in " + data.size()
                    + " chunks took " + DateTimeUtils.renderDuration(totalTime) + "."
                    + averageSpeedInfo + medianSpeedInfo;
        }

        private double calculateMedian(List<Double> speed)
        {
            Collections.sort(speed);
            int size = speed.size();
            double medianSpeed = speed.get(size / 2);
            if (size % 2 == 0)
            {
                medianSpeed = 0.5 * (speed.get(size / 2 - 1) + speed.get(size / 2));
            }
            return medianSpeed;
        }
    }

    private static final class NumberOfBytesAndIOTime
    {
        private int numberOfBytes;

        private long ioTime;

        NumberOfBytesAndIOTime(int numberOfBytes, long ioTime)
        {
            this.numberOfBytes = numberOfBytes;
            this.ioTime = ioTime;
        }
    }

}
