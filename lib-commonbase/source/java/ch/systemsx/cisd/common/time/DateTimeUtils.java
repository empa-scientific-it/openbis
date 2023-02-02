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

package ch.systemsx.cisd.common.time;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;

/**
 * A suite of utilities surrounding the use of the {@link java.util.Calendar} and {@link java.util.Date} object.
 * 
 * @author Christian Ribeaud
 */
public final class DateTimeUtils
{
    private static final Pattern secPattern = Pattern.compile("([0-9]+)\\s*(|s|sec)");

    private static final Pattern minPattern = Pattern.compile("([0-9]+) *(m|min)");

    private static final Pattern hourPattern = Pattern.compile("([0-9]+) *(h|hours)");

    private static final Pattern dayPattern = Pattern.compile("([0-9]+) *(d|days)");

    private static final Pattern milliPattern = Pattern.compile("([0-9]+) *(ms|msec)");

    /**
     * Returns the time zone in the following form: <code>GMT+01:00</code> (could not be easily performed using {@link DateFormat}).
     */
    public final static String getTimeZone(final Date date)
    {
        assert date != null : "Unspecified date.";
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final StringBuffer zoneString = new StringBuffer();
        int value = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
        if (value < 0)
        {
            zoneString.append('-');
            // Suppress the '-' sign for text display.
            value = -value;
        } else
        {
            zoneString.append('+');
        }
        int num = value / (int) DateUtils.MILLIS_PER_HOUR;
        zoneString.append(prependZeroIfNeeded(num));
        zoneString.append(":");
        num = (value % (int) DateUtils.MILLIS_PER_HOUR) / (int) DateUtils.MILLIS_PER_MINUTE;
        zoneString.append(prependZeroIfNeeded(num));
        return "GMT" + zoneString.toString();
    }

    private final static String prependZeroIfNeeded(final int num)
    {
        return num < 10 ? "0" + num : "" + num;
    }

    /**
     * Renders the specified duration. If time is negative it is rendered as "?".
     */
    public static String renderDuration(long durationInMillisecondsOrNeg)
    {
        if (durationInMillisecondsOrNeg < 0)
        {
            return "?";
        }
        if (durationInMillisecondsOrNeg < 1000)
        {
            return "< " + render(1, "sec");
        }
        long durationInSeconds = (durationInMillisecondsOrNeg + 500) / 1000;
        if (durationInSeconds < 100)
        {
            return render(durationInSeconds, "sec");
        }
        long durationInMinutes = (durationInSeconds + 30) / 60;
        if (durationInMinutes < 60)
        {
            return render(durationInMinutes, "min");
        }
        long minutes = durationInMinutes % 60;
        long hours = durationInMinutes / 60;
        if (minutes > 0)
        {
            return render(hours, "h") + " " + render(minutes, "min");
        } else
        {
            return render(hours, "h");
        }
    }

    private static String render(long value, String unit)
    {
        return value + unit;
    }

    /**
     * Gets from specified properties the specified property as a duration time in milliseconds. The duration can be specified with time unit as
     * explained in the method {@link #parseDurationToMillis(String)}.
     * 
     * @return <code>defaultValue</code> if property doesn't exist
     */
    public static long getDurationInMillis(Properties properties, String key, long defaultValue)
    {
        String value = properties.getProperty(key);
        return value == null ? defaultValue : parseDurationToMillis(value);
    }

    /**
     * Parses a time duration to milli-seconds. The string will be trimmed and white spaces in between number and unit are ignored. Accepted numbers
     * are:
     * <ul>
     * <li>ms, msec: milli-seconds</li>
     * <li>s, sec or nothing: seconds</li>
     * <li>m, min: minutes</li>
     * <li>h, hours: hours</li>
     * <li>d, days: days</li>
     * </ul>
     */
    public static long parseDurationToMillis(String durationStr)
    {
        final String durationStrTrimmed = durationStr.trim();
        Matcher m;
        m = secPattern.matcher(durationStrTrimmed);
        if (m.matches())
        {
            return Long.parseLong(m.group(1)) * 1000L;
        }
        m = minPattern.matcher(durationStrTrimmed);
        if (m.matches())
        {
            return Long.parseLong(m.group(1)) * 60 * 1000L;
        }
        m = hourPattern.matcher(durationStrTrimmed);
        if (m.matches())
        {
            return Long.parseLong(m.group(1)) * 3600 * 1000L;
        }
        m = dayPattern.matcher(durationStrTrimmed);
        if (m.matches())
        {
            return Long.parseLong(m.group(1)) * 24 * 3600 * 1000L;
        }
        m = dayPattern.matcher(durationStrTrimmed);
        if (m.matches())
        {
            return Long.parseLong(m.group(1)) * 24 * 3600 * 1000L;
        }
        m = milliPattern.matcher(durationStrTrimmed);
        if (m.matches())
        {
            return Long.parseLong(m.group(1));
        }

        throw new IllegalArgumentException(String.format("'%s' is not a valid duration",
                durationStrTrimmed));
    }

    /**
     * Extends the given <var>date</var> until the end of the day.
     */
    public static Date extendUntilEndOfDay(Date date)
    {
        return DateUtils.addMilliseconds(DateUtils.addDays(DateUtils.truncate(date,
                Calendar.DAY_OF_MONTH), 1), -1);
    }

}
