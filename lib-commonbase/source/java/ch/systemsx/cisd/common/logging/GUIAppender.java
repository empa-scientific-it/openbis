/*
 * Copyright 2008 ETH Zuerich, CISD
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

import javax.swing.JTextArea;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * An {@link Appender} which logs log events into a {@link JTextArea}.
 *
 * @author Franz-Josef Elmer
 */
public class GUIAppender extends AppenderSkeleton
{
    private final JTextArea textArea;

    /**
     * Creates an instance for the log level {@link Level#DEBUG}. Default pattern layout {@link PatternLayout#DEFAULT_CONVERSION_PATTERN} is used.
     */
    public GUIAppender()
    {
        this(Level.DEBUG);
    }

    /**
     * Creates an instance for the specified log level. Default pattern layout {@link PatternLayout#DEFAULT_CONVERSION_PATTERN} is used.
     */
    public GUIAppender(Level logLevel)
    {
        this(logLevel, null);
    }

    /**
     * Creates an instance for the specified log level and pattern for formating the log message.
     */
    public GUIAppender(Level logLevel, String pattern)
    {
        setLayout(new PatternLayout(pattern));
        Logger.getRootLogger().addAppender(this);
        setThreshold(logLevel);
        textArea = new JTextArea();
        textArea.setEditable(false);
    }

    /**
     * Returns the text area with log messages.
     */
    public final JTextArea getTextArea()
    {
        return textArea;
    }

    @Override
    protected void append(LoggingEvent event)
    {
        String logMessage = layout.format(event);
        textArea.append(logMessage);
    }

    @Override
    public void close()
    {
    }

    @Override
    public boolean requiresLayout()
    {
        return true;
    }

}
