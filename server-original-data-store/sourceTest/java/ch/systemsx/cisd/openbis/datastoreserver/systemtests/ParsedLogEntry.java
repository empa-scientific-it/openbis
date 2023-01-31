/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.text.MessageFormat;
import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

public final class ParsedLogEntry
{
    private static final String FORMAT_TEMPLATE = "[{0,date," + BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN + "}][{1}][{2}][{3}]";

    private int lineIndex;

    private Date timestamp;

    private String logLevel;

    private String threadName;

    private String logMessage;

    ParsedLogEntry(int lineIndex, Date timestamp, String logLevel, String threadName, String logMessage)
    {
        this.lineIndex = lineIndex;
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.threadName = threadName;
        this.logMessage = logMessage;
    }

    public void appendToMessage(String logLine)
    {
        logMessage += "\n" + logLine;
    }

    public int getLineIndex()
    {
        return lineIndex;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public String getLogLevel()
    {
        return logLevel;
    }

    public String getThreadName()
    {
        return threadName;
    }

    public String getLogMessage()
    {
        return logMessage;
    }

    @Override
    public String toString()
    {
        return new MessageFormat(FORMAT_TEMPLATE).format(new Object[] { timestamp, logLevel, threadName, logMessage });
    }

}