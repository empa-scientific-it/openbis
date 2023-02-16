/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A <code>WriterAppender</code> extension that buffers its output in a <code>ByteArrayOutputStream</code> until you ask for it by using
 * {@link #getLogContent()}.
 * <p>
 * It internally uses a <code>ByteArrayOutputStream</code> which collect the log output and can return it using {@link #getLogContent()}. It is a good
 * idea to reset the log recorder by calling {@link #resetLogContent()} before calling a unit test method.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class BufferedAppender extends WriterAppender
{
    private final ByteArrayOutputStream logRecorder;

    private final List<Pattern> patternOfSuppressedLogEvents = new LinkedList<Pattern>();

    /**
     * Constructor with default pattern layout (which is {@link PatternLayout#DEFAULT_CONVERSION_PATTERN}) and {@link Level#DEBUG} as log level.
     */
    public BufferedAppender()
    {
        this(Level.DEBUG);
    }

    /**
     * Constructor with default pattern layout (which is {@link PatternLayout#DEFAULT_CONVERSION_PATTERN}).
     * 
     * @param logLevel
     */
    public BufferedAppender(final Level logLevel)
    {
        this(null, logLevel);
    }

    public BufferedAppender(final String pattern, final Level logLevel)
    {
        this(pattern, logLevel, null);
    }

    /**
     * Creates an instance for specified optional layout pattern, log level and regex of the logger name onto which log entries are filtered.
     */
    public BufferedAppender(final String patternOrNull, final Level logLevel, final String loggerNameRegex)
    {
        logRecorder = new ByteArrayOutputStream();
        if (loggerNameRegex != null)
        {
            final Pattern pattern = Pattern.compile(loggerNameRegex);
            this.addFilter(new Filter()
                {
                    @Override
                    public int decide(LoggingEvent event)
                    {
                        String loggerName = event.getLoggerName();
                        return pattern.matcher(loggerName).matches() ? Filter.ACCEPT : Filter.DENY;
                    }
                });
        }
        setWriter(createWriter(logRecorder));
        setLayout(createLayout(patternOrNull));
        configureRootLogger();
        setThreshold(logLevel);
    }

    public void addRegexForLoggingEventsToBeDropped(String loggerNameRegex)
    {
        final Pattern pattern = Pattern.compile(loggerNameRegex);
        this.addFilter(new Filter()
            {
                @Override
                public int decide(LoggingEvent event)
                {
                    String loggerName = event.getLoggerName();
                    return pattern.matcher(loggerName).matches() ? Filter.DENY : Filter.ACCEPT;
                }
            });
        patternOfSuppressedLogEvents.add(Pattern.compile(".*" + loggerNameRegex + ".*"));

    }

    private final void configureRootLogger()
    {
        Logger.getRootLogger().addAppender(this);
    }

    protected Layout createLayout(final String pattern)
    {
        return new PatternLayout(pattern);
    }

    /**
     * Returns the content of this log appender.
     * <p>
     * Never returns <code>null</code> but could return an empty string.
     * </p>
     */
    public final String getLogContent()
    {
        String content = new String(logRecorder.toByteArray()).trim();
        String[] split = content.split("\n");
        StringBuilder builder = new StringBuilder();
        for (String line : split)
        {
            if (shouldBeSuppressed(line) == false)
            {
                builder.append(line).append("\n");
            }
        }
        return builder.toString().trim();
    }

    private boolean shouldBeSuppressed(String line)
    {
        for (Pattern pattern : patternOfSuppressedLogEvents)
        {
            if (pattern.matcher(line).matches())
            {
                return true;
            }
        }
        return false;
    }

    public List<String> getLogLines()
    {
        return Arrays.asList(getLogContent().split("\n"));
    }

    public final void resetLogContent()
    {
        logRecorder.reset();
    }

    //
    // WriterAppender
    //

    @Override
    public final void reset()
    {
        Logger.getRootLogger().removeAppender(this);
        super.reset();
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getEncoding();
    }
}