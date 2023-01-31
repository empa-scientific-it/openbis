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

import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TimeIntervalTest
{
    @Test
    public void testIntervalNotOverMidnight()
    {
        TimeInterval interval = createTimeInterval("7:15", "13:45", "2018-01-10 10:15:00");
        
        assertIsNotInInterval(interval, "2018-01-10 7:14:00");
        assertIsInInterval(interval, "2018-01-10 7:15:00");
        assertIsInInterval(interval, "2018-01-10 7:16:00");
        assertIsInInterval(interval, "2018-01-10 12:16:00");
        assertIsInInterval(interval, "2018-01-10 13:45:00");
        assertIsNotInInterval(interval, "2018-01-10 13:45:10");
        assertIsNotInInterval(interval, "2018-01-11 11:15:00");
    }
    
    @Test
    public void testIntervalNotOverMidnightWithNow()
    {
        assertIsNotInInterval("7:15", "13:45", "2018-01-10 7:14:00");
        assertIsInInterval("7:15", "13:45", "2018-01-10 7:15:00");
        assertIsInInterval("7:15", "13:45", "2018-01-10 7:16:00");
        assertIsInInterval("7:15", "13:45", "2018-01-10 12:16:00");
        assertIsInInterval("7:15", "13:45", "2018-01-10 13:45:00");
        assertIsNotInInterval("7:15", "13:45", "2018-01-10 13:45:10");
    }
    
    @Test
    public void testIntervalNotOverMidnightWithDefaultStartingTime()
    {
        TimeInterval interval = createTimeInterval(null, "13:45", "2018-01-10 10:15:00");
        
        assertIsNotInInterval(interval, "2018-01-10 10:14:00");
        assertIsInInterval(interval, "2018-01-10 10:15:00");
        assertIsInInterval(interval, "2018-01-10 10:16:00");
        assertIsInInterval(interval, "2018-01-10 12:16:00");
        assertIsInInterval(interval, "2018-01-10 13:45:00");
        assertIsNotInInterval(interval, "2018-01-10 13:45:10");
        assertIsNotInInterval(interval, "2018-01-11 11:15:00");
    }
    @Test
    public void testIntervalWithDefaultStartingTimeWithNow()
    {
        assertIsInInterval(null, "13:45", "2018-01-10 10:15:00");
        assertIsInInterval(null, "13:45", "2018-01-10 10:16:00");
        assertIsInInterval(null, "13:45", "2018-01-10 12:16:00");
        assertIsInInterval(null, "13:45", "2018-01-10 13:45:00");
        assertIsInInterval(null, "3:45", "2018-01-10 20:45:00");
    }
    
    @Test
    public void testIntervalOverMidnightWithDefaultStartingTime()
    {
        TimeInterval interval = createTimeInterval(null, "3:45", "2018-01-10 22:15:00");
        
        assertIsNotInInterval(interval, "2018-01-10 2:15:00");
        assertIsNotInInterval(interval, "2018-01-10 20:15:00");
        assertIsInInterval(interval, "2018-01-10 22:15:00");
        assertIsInInterval(interval, "2018-01-10 22:16:00");
        assertIsInInterval(interval, "2018-01-11 2:16:00");
        assertIsInInterval(interval, "2018-01-11 3:45:00");
        assertIsNotInInterval(interval, "2018-01-11 3:45:10");
        assertIsNotInInterval(interval, "2018-01-11 17:05:00");
        assertIsNotInInterval(interval, "2018-01-11 23:05:00");
        
    }
    
    @Test
    public void testIntervalOverNextMidnight()
    {
        TimeInterval interval = createTimeInterval("21:44", "3:45", "2018-01-10 12:15:00");
        
        assertIsNotInInterval(interval, "2018-01-10 2:15:00");
        assertIsNotInInterval(interval, "2018-01-10 20:15:00");
        assertIsNotInInterval(interval, "2018-01-10 21:43:59");
        assertIsInInterval(interval, "2018-01-10 21:44:00");
        assertIsInInterval(interval, "2018-01-10 21:45:00");
        assertIsInInterval(interval, "2018-01-11 2:16:00");
        assertIsInInterval(interval, "2018-01-11 3:45:00");
        assertIsNotInInterval(interval, "2018-01-11 3:45:10");
        assertIsNotInInterval(interval, "2018-01-11 17:05:00");
        assertIsNotInInterval(interval, "2018-01-11 23:05:00");
    }
    
    @Test
    public void testIntervalOverPreviousMidnight()
    {
        TimeInterval interval = createTimeInterval("21:44", "3:45", "2018-01-11 1:15:00");
        
        assertIsNotInInterval(interval, "2018-01-10 2:15:00");
        assertIsNotInInterval(interval, "2018-01-10 20:15:00");
        assertIsNotInInterval(interval, "2018-01-10 21:43:59");
        assertIsInInterval(interval, "2018-01-10 21:44:00");
        assertIsInInterval(interval, "2018-01-10 21:45:00");
        assertIsInInterval(interval, "2018-01-11 2:16:00");
        assertIsInInterval(interval, "2018-01-11 3:45:00");
        assertIsNotInInterval(interval, "2018-01-11 3:45:10");
        assertIsNotInInterval(interval, "2018-01-11 17:05:00");
        assertIsNotInInterval(interval, "2018-01-11 23:05:00");
    }
    
    @Test
    public void testIntervalOverMidnightWithNow()
    {
        assertIsNotInInterval("21:44", "3:45", "2018-01-10 20:15:00");
        assertIsNotInInterval("21:44", "3:45", "2018-01-10 21:43:59");
        assertIsInInterval("21:44", "3:45", "2018-01-10 21:44:00");
        assertIsInInterval("21:44", "3:45", "2018-01-10 21:45:00");
        assertIsInInterval("21:44", "3:45", "2018-01-11 2:16:00");
        assertIsInInterval("21:44", "3:45", "2018-01-11 3:45:00");
        assertIsNotInInterval("21:44", "3:45", "2018-01-11 3:45:10");
        assertIsNotInInterval("21:44", "3:45", "2018-01-11 17:05:00");
    }
    
    private void assertIsInInterval(String startingTime, String eindingTime, String nowTimeStamp)
    {
        TimeInterval interval = createTimeInterval(startingTime, eindingTime, nowTimeStamp);
        assertIsInInterval(interval, nowTimeStamp);
    }

    private void assertIsNotInInterval(String startingTime, String eindingTime, String nowTimeStamp)
    {
        TimeInterval interval = createTimeInterval(startingTime, eindingTime, nowTimeStamp);
        assertIsNotInInterval(interval, nowTimeStamp);
    }
    
    private void assertIsInInterval(TimeInterval interval, String timeStamp)
    {
        assertIsInInterval(interval, timeStamp, true);
    }

    private void assertIsNotInInterval(TimeInterval interval, String timeStamp)
    {
        assertIsInInterval(interval, timeStamp, false);
    }
    
    private void assertIsInInterval(TimeInterval interval, String timeStamp, boolean expected)
    {
        assertEquals(interval.isInTimeInterval(parseTimeStamp(timeStamp)), expected);
    }
    
    private TimeInterval createTimeInterval(String startingTimeOrNull, String endingTime, String nowTimeStamp)
    {
        return new TimeInterval(parseTime(startingTimeOrNull), parseTime(endingTime), parseTimeStamp(nowTimeStamp));
    }
    
    private static Date parseTime(String time)
    {
        return time == null ? null : parse("HH:mm", time);
    }

    private static Date parseTimeStamp(String timeStamp)
    {
        return parse("yyyy-MM-dd HH:mm:ss", timeStamp);
    }
    
    private static Date parse(String timeFormat, String string)
    {
        try
        {
            return new SimpleDateFormat(timeFormat).parse(string);
        } catch (ParseException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
}
