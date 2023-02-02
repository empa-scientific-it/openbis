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

package ch.systemsx.cisd.common.logging;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * A logger which records logging events. Log messages also sent to a {@link MessageChannel}.
 *
 * @author Franz-Josef Elmer
 */
public final class MockLogger implements ISimpleLogger
{
    private final StringBuilder builder = new StringBuilder();

    private final MessageChannel messageChannel = new MessageChannel();

    @Override
    public void log(LogLevel level, String message)
    {
        log(level, message, null);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwableOrNull)
    {
        builder.append(level).append(": ").append(message).append('\n');
        messageChannel.send(message);
        if (throwableOrNull != null)
        {
            throwableOrNull.printStackTrace(System.out);
        }
    }

    public void assertNextLogMessage(String expectedMessage)
    {
        messageChannel.assertNextMessage(expectedMessage);
    }

    public void assertNextLogMessageContains(String expectedMessagePart)
    {
        messageChannel.assertNextMessageContains(expectedMessagePart);
    }

    public void assertNoMoreLogMessages()
    {
        messageChannel.assertEmpty();
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }
}