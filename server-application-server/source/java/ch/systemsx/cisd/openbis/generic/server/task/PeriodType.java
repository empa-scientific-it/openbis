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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;

/**
 * Defines a period type.
 * 
 * @author Franz-Josef Elmer
 */
enum PeriodType
{
    DAYLY(DateUtils.MILLIS_PER_DAY)
    {
        @Override
        protected Period getPeriod(GregorianCalendar calendar)
        {
            long until = calendar.getTimeInMillis();
            return new Period(new Date(until - DateUtils.MILLIS_PER_DAY), new Date(until));
        }
    },
    WEEKLY(7 * DateUtils.MILLIS_PER_DAY)
    {
        @Override
        protected Period getPeriod(GregorianCalendar calendar)
        {
            calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SUNDAY);
            long until = calendar.getTimeInMillis();
            return new Period(new Date(until - 7 * DateUtils.MILLIS_PER_DAY), new Date(until));
        }
    },
    MONTHLY(31 * DateUtils.MILLIS_PER_DAY)
    {
        @Override
        protected Period getPeriod(GregorianCalendar calendar)
        {
            calendar.set(GregorianCalendar.DAY_OF_MONTH, 1);
            Date until = calendar.getTime();
            int currentMonth = calendar.get(GregorianCalendar.MONTH);
            if (currentMonth == GregorianCalendar.JANUARY)
            {
                calendar.set(GregorianCalendar.MONTH, GregorianCalendar.DECEMBER);
                calendar.set(GregorianCalendar.YEAR, calendar.get(GregorianCalendar.YEAR) - 1);
            } else
            {
                calendar.set(GregorianCalendar.MONTH, currentMonth - 1);
            }
            Date from = calendar.getTime();
            return new Period(from, until);
        }
    };

    private long periodLength;

    static PeriodType getBestType(long interval)
    {
        PeriodType[] values = PeriodType.values();
        for (PeriodType periodType : values)
        {
            if (interval <= periodType.periodLength)
            {
                return periodType;
            }
        }
        return values[values.length - 1];
    }

    PeriodType(long periodLength)
    {
        this.periodLength = periodLength;
    }
    
    Period getPeriod(Date date)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
        calendar.set(GregorianCalendar.MINUTE, 0);
        calendar.set(GregorianCalendar.SECOND, 0);
        calendar.set(GregorianCalendar.MILLISECOND, 0);
        return getPeriod(calendar);
    }

    protected abstract Period getPeriod(GregorianCalendar calendar);
}