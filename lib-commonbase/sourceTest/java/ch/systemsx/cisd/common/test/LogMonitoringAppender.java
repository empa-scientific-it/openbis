/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.IToStringConverter;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;

/**
 * A class that allows to monitor the log for unit tests.
 * 
 * @author Bernd Rinn
 */
public final class LogMonitoringAppender extends AppenderSkeleton
{

    private static Map<LogMonitoringAppender, String> appenderMap =
            new HashMap<LogMonitoringAppender, String>();

    private final StringBuilder eventRecorder = new StringBuilder();

    private final Set<PatternCounter> patternCounters;

    private LogMonitoringAppender(final Pattern... patterns)
    {
        this.patternCounters = createPatternCounters(patterns);
    }

    private final static Set<PatternCounter> createPatternCounters(final Pattern[] patterns)
    {
        final Set<PatternCounter> patternCounters = new HashSet<PatternCounter>(patterns.length);
        for (final Pattern pattern : patterns)
        {
            patternCounters.add(new PatternCounter(pattern));
        }
        return patternCounters;
    }

    /**
     * Creates an appender that monitors for given <var>messageParts</var> and adds it to the {@link Logger} for <code>category</code> and
     * <code>clazz</code>.
     * 
     * @return The created appender.
     */
    public final static synchronized LogMonitoringAppender addAppender(final LogCategory category,
            final String... messageParts)
    {
        final int len = messageParts.length;
        final Pattern[] patterns = new Pattern[len];
        for (int i = 0; i < len; i++)
        {
            patterns[i] = Pattern.compile(Pattern.quote(messageParts[i]));
        }
        return addAppender(category, patterns);
    }

    /**
     * Creates an appender that monitors for <var>messagePart</var> and adds it to the {@link Logger} for <code>category</code> and <code>clazz</code>
     * .
     * 
     * @return The created appender.
     */
    public final static synchronized LogMonitoringAppender addAppender(final LogCategory category,
            final Pattern... regex)
    {
        final LogMonitoringAppender appender = new LogMonitoringAppender(regex);
        final String loggerName = category.name();
        Logger.getLogger(loggerName).addAppender(appender);
        appenderMap.put(appender, loggerName);
        return appender;
    }

    /**
     * Removes the given <var>appender</var>.
     */
    public final static synchronized void removeAppender(final LogMonitoringAppender appender)
    {
        final String loggerName = appenderMap.get(appender);
        if (loggerName != null)
        {
            Logger.getLogger(loggerName).removeAppender(appender);
            appenderMap.remove(appender);
        } else
        {
            // This means that the caller tries to remove the appender twice - nothing to do here
            // really.
        }
    }

    private final String getThrowableStr(final LoggingEvent event)
    {
        final ThrowableInformation info = event.getThrowableInformation();
        if (info == null)
        {
            return StringUtils.EMPTY;
        } else
        {
            return StringUtils.join(info.getThrowableStrRep(), "\n");
        }
    }

    private final String describePatterns()
    {
        return CollectionUtils.abbreviate(patternCounters, -1, true,
                new IToStringConverter<PatternCounter>()
                    {

                        //
                        // IToStringConverter
                        //

                        @Override
                        public final String toString(final PatternCounter value)
                        {
                            return value.pattern.pattern();
                        }

                    });
    }

    public final void verifyLogHasNotHappened()
    {
        boolean hasNotHappened = true;
        for (final PatternCounter patternCounter : patternCounters)
        {
            hasNotHappened &= (patternCounter.count == 0);
        }
        assert hasNotHappened : String.format("Regex '%s' has been unexpectedly found in log:\n%s",
                describePatterns(), eventRecorder);
    }

    public final void verifyLogHasHappened()
    {
        boolean hasHappened = true;
        for (final PatternCounter patternCounter : patternCounters)
        {
            hasHappened &= (patternCounter.count > 0);
        }
        assert hasHappened : String.format("Regex '%s' has been missed in log:\n%s",
                describePatterns(), eventRecorder);
    }

    public final void reset()
    {
        for (final PatternCounter patternCounter : patternCounters)
        {
            patternCounter.count = 0;
        }
    }

    //
    // AppenderSkeleton
    //

    @Override
    protected final void append(final LoggingEvent event)
    {
        final String eventMessage = event.getMessage().toString();
        eventRecorder.append("event message: ").append(eventMessage).append('\n');
        final String throwableStr = getThrowableStr(event);
        if (throwableStr.length() > 0)
        {
            eventRecorder.append("event throwable: ").append(throwableStr).append('\n');
        }
        for (final PatternCounter patternCounter : patternCounters)
        {
            final Pattern pattern = patternCounter.pattern;
            int count = 0;
            if (pattern.matcher(eventMessage).find() || pattern.matcher(throwableStr).find())
            {
                ++count;
            }
            patternCounter.count += count;
        }
    }

    @Override
    public final void close()
    {
    }

    @Override
    public final boolean requiresLayout()
    {
        return false;
    }

    //
    // Helper classes
    //

    private final static class PatternCounter
    {
        final Pattern pattern;

        int count;

        private PatternCounter(final Pattern pattern)
        {
            this.pattern = pattern;
        }

        //
        // Object
        //

        @Override
        public final String toString()
        {
            return ToStringBuilder.reflectionToString(this,
                    ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        }
    }
}
