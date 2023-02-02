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

package ch.systemsx.cisd.common.concurrent;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * Message channel for controlling multiple threads in unit testing. The channel is a {@link BlockingQueue}.
 * 
 * @author Franz-Josef Elmer
 */
public class MessageChannel
{
    private final BlockingQueue<Object> _queue;

    private final long _timeOutInMilliSeconds;

    private String name = "?";

    private ISimpleLogger logger;

    /**
     * Creates an instance with time out 1 second.
     */
    public MessageChannel()
    {
        this(1000);
    }

    /**
     * Creates an instance with specified time out in milliseconds.
     */
    public MessageChannel(long timeOutInMilliSeconds)
    {
        _timeOutInMilliSeconds = isDebugMode() ? DateUtils.MILLIS_PER_HOUR : timeOutInMilliSeconds;
        _queue = new LinkedBlockingQueue<Object>();
    }

    private boolean isDebugMode()
    {
        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        return inputArguments.toString().contains("-agentlib:jdwp");
    }

    /**
     * Sets the name of this channel. Will be used if logging is enabled.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Sets an optional logger which log sending and receiving messages.
     */
    public void setLogger(ISimpleLogger logger)
    {
        this.logger = logger;
    }

    /**
     * Sends specified message. <code>null</code> are not allowed.
     */
    public void send(Object message)
    {
        if (message == null)
        {
            throw new IllegalArgumentException("Null message not allowed.");
        }
        _queue.offer(message);
        log("Message '" + message + "' has been sent.");
    }

    private void log(String message)
    {
        if (logger != null)
        {
            logger.log(LogLevel.INFO, "MessageChannel[" + name + "]: " + message);
        }
    }

    /**
     * Asserts specified expected message is next message to be received. Waits not longer than specified in the constructor.
     */
    public void assertNextMessage(Object expectedMessage)
    {
        try
        {
            assertEquals(expectedMessage, receivesMessage());
        } catch (InterruptedException e)
        {
            // ignored
        }
    }

    private Object receivesMessage() throws InterruptedException
    {
        Object message = _queue.poll(_timeOutInMilliSeconds, TimeUnit.MILLISECONDS);
        if (message == null)
        {
            log("No message could be received. Timeout: " + _timeOutInMilliSeconds + " msec");
        } else
        {
            log("Message '" + message + "' has been received.");
        }
        return message;
    }

    /**
     * Asserts specified expected message is part of next message to be received. Waits not longer than specified in the constructor.
     */
    public void assertNextMessageContains(Object expectedMessagePart)
    {
        try
        {
            Object receivedMessage = receivesMessage();
            assertTrue("Unexpected message: " + receivedMessage, receivedMessage.toString()
                    .contains(expectedMessagePart.toString()));
        } catch (InterruptedException e)
        {
            // ignored
        }
    }

    /**
     * Asserts empty message queue.
     */
    public void assertEmpty()
    {
        assertEquals(0, _queue.size());
    }

}
