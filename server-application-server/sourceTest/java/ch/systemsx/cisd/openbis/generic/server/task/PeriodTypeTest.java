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

import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 *
 */
public class PeriodTypeTest
{
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    @Test
    public void testGetBestType()
    {
        assertEquals(PeriodType.getBestType(0), PeriodType.DAYLY);
        assertEquals(PeriodType.getBestType(DateUtils.MILLIS_PER_DAY - 1), PeriodType.DAYLY);
        assertEquals(PeriodType.getBestType(DateUtils.MILLIS_PER_DAY), PeriodType.DAYLY);
        assertEquals(PeriodType.getBestType(DateUtils.MILLIS_PER_DAY + 1), PeriodType.WEEKLY);
        assertEquals(PeriodType.getBestType(7 * DateUtils.MILLIS_PER_DAY - 1), PeriodType.WEEKLY);
        assertEquals(PeriodType.getBestType(7 * DateUtils.MILLIS_PER_DAY), PeriodType.WEEKLY);
        assertEquals(PeriodType.getBestType(7 * DateUtils.MILLIS_PER_DAY + 1), PeriodType.MONTHLY);
        assertEquals(PeriodType.getBestType(28 * DateUtils.MILLIS_PER_DAY), PeriodType.MONTHLY);
        assertEquals(PeriodType.getBestType(30 * DateUtils.MILLIS_PER_DAY), PeriodType.MONTHLY);
        assertEquals(PeriodType.getBestType(31 * DateUtils.MILLIS_PER_DAY), PeriodType.MONTHLY);
        assertEquals(PeriodType.getBestType(32 * DateUtils.MILLIS_PER_DAY), PeriodType.MONTHLY);
    }
    
    @Test
    public void testGetPeriod() throws ParseException
    {
        check(PeriodType.DAYLY, "2018-06-04 23:14:59", "2018-06-03 00:00:00", "2018-06-04 00:00:00");
        check(PeriodType.DAYLY, "2018-06-01 00:00:00", "2018-05-31 00:00:00", "2018-06-01 00:00:00");
        check(PeriodType.DAYLY, "2018-06-01 00:00:01", "2018-05-31 00:00:00", "2018-06-01 00:00:00");
        check(PeriodType.DAYLY, "2018-06-01 23:59:59", "2018-05-31 00:00:00", "2018-06-01 00:00:00");
        
        check(PeriodType.WEEKLY, "2018-06-03 00:00:00", "2018-05-27 00:00:00", "2018-06-03 00:00:00");
        check(PeriodType.WEEKLY, "2018-06-03 00:00:01", "2018-05-27 00:00:00", "2018-06-03 00:00:00");
        check(PeriodType.WEEKLY, "2018-06-03 23:59:59", "2018-05-27 00:00:00", "2018-06-03 00:00:00");
        check(PeriodType.WEEKLY, "2018-06-04 14:47:27", "2018-05-27 00:00:00", "2018-06-03 00:00:00");
        check(PeriodType.WEEKLY, "2018-06-05 03:54:53", "2018-05-27 00:00:00", "2018-06-03 00:00:00");
        check(PeriodType.WEEKLY, "2018-06-06 23:34:13", "2018-05-27 00:00:00", "2018-06-03 00:00:00");
        check(PeriodType.WEEKLY, "2018-06-09 23:59:59", "2018-05-27 00:00:00", "2018-06-03 00:00:00");
        check(PeriodType.WEEKLY, "2018-06-10 00:00:00", "2018-06-03 00:00:00", "2018-06-10 00:00:00");
        
        check(PeriodType.MONTHLY, "2018-06-01 00:00:00", "2018-05-01 00:00:00", "2018-06-01 00:00:00");
        check(PeriodType.MONTHLY, "2018-06-12 13:14:15", "2018-05-01 00:00:00", "2018-06-01 00:00:00");
        check(PeriodType.MONTHLY, "2018-01-01 15:34:57", "2017-12-01 00:00:00", "2018-01-01 00:00:00");
        check(PeriodType.MONTHLY, "2018-01-31 23:59:59", "2017-12-01 00:00:00", "2018-01-01 00:00:00");
    }
    
    private void check(PeriodType type, String date, String expectedFrom, String expectedUntil) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Period period = type.getPeriod(format.parse(date));
        assertEquals(format.format(period.getFrom()), expectedFrom, "From date");
        assertEquals(format.format(period.getUntil()), expectedUntil, "Until date");
    }

}
