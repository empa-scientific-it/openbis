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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Message channel for controlling multiple threads in unit testing. The channel is a
 * {@link BlockingQueue}.
 * 
 * @author Franz-Josef Elmer
 */
public class MessageChannel
{
    private final BlockingQueue<Object> _queue;

    private final long _timeOutInMilliSeconds;

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
        _timeOutInMilliSeconds = timeOutInMilliSeconds;
        _queue = new LinkedBlockingQueue<Object>();
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
    }

    /**
     * Asserts specified expected message is next message to be received. Waits not longer than
     * specified in the constructor.
     */
    public void assertNextMessage(Object expectedMessage)
    {
        try
        {
            assertEquals(expectedMessage,
                    _queue.poll(_timeOutInMilliSeconds, TimeUnit.MILLISECONDS));
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
