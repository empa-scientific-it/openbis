/*
 * Copyright 2012 ETH Zuerich, CISD
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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;

/**
 * Builder to build a {@link MessageChannel} object.
 *
 * @author Franz-Josef Elmer
 */
public class MessageChannelBuilder
{
    private final MessageChannel channel;

    public MessageChannelBuilder()
    {
        channel = new MessageChannel();
    }

    public MessageChannelBuilder(long timeOutInMilliSeconds)
    {
        channel = new MessageChannel(timeOutInMilliSeconds);
    }

    public MessageChannelBuilder name(String name)
    {
        channel.setName(name);
        return this;
    }

    public MessageChannelBuilder logger(ISimpleLogger logger)
    {
        channel.setLogger(logger);
        return this;
    }

    public MessageChannelBuilder logger(Logger logger)
    {
        return logger(new Log4jSimpleLogger(logger));
    }

    public MessageChannel getChannel()
    {
        return channel;
    }
}
