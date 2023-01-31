/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.path;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper class which defines a time interval for a given day.
 * 
 *
 * @author Franz-Josef Elmer
 */
class TimeInterval
{
    private static final int DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    private long endingTimeStamp;

    private long startingTimeStamp;

    public TimeInterval(Date startingTime, Date endingTime, Date now)
    {
        if (endingTime == null)
        {
            throw new IllegalArgumentException("Unspecified ending time");
        }
        endingTimeStamp = getForToday(now, endingTime).getTime();
        startingTimeStamp = getForToday(now, startingTime).getTime();
        if (startingTimeStamp > endingTimeStamp)
        {
            if (now.getTime() > endingTimeStamp)
            {
                endingTimeStamp += DAY_IN_MILLIS;
            } else
            {
                startingTimeStamp -= DAY_IN_MILLIS;
            }
        }
    }

    boolean isInTimeInterval(Date timeStamp)
    {
        return startingTimeStamp <= timeStamp.getTime() && timeStamp.getTime() <= endingTimeStamp;
    }

    private static Date getForToday(Date now, Date timeOrNull)
    {
        Calendar calendarFromTime = getCalendar();
        calendarFromTime.setTime(timeOrNull == null ? now : timeOrNull);
        Calendar calendarWithDate = getCalendar();
        calendarWithDate.setTime(now);
        calendarWithDate.set(Calendar.HOUR_OF_DAY, calendarFromTime.get(Calendar.HOUR_OF_DAY));
        calendarWithDate.set(Calendar.MINUTE, calendarFromTime.get(Calendar.MINUTE));
        calendarWithDate.set(Calendar.SECOND, calendarFromTime.get(Calendar.SECOND));
        calendarWithDate.set(Calendar.MILLISECOND, calendarFromTime.get(Calendar.MILLISECOND));
        return calendarWithDate.getTime();
    }

    private static Calendar getCalendar()
    {
        return GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.US);
    }
}