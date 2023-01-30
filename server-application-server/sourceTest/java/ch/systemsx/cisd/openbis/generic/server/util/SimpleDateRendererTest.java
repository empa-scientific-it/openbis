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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleDateRenderer;

/**
 * Tests of {@link SimpleDateRenderer}. The test is in the different package than the tested class to have access to TimeZone class (GWT does not
 * support it).
 * 
 * @author Tomasz Pylak
 */
public class SimpleDateRendererTest extends AssertJUnit
{
    @DataProvider(name = "dates")
    private Object[][] getDates()
    {
        return new Object[][]
        {
                { toDate(1991, 12, 3, 3, 13, 59, "CET"), "1991-12-03 03:13:59 GMT+01:00" },
                { toDate(2008, 12, 6, 15, 35, 17, "CET"), "2008-12-06 15:35:17 GMT+01:00" },
                { toDate(2008, 7, 31, 1, 2, 3, "CET"), "2008-07-31 01:02:03 GMT+02:00" } };
    }

    @Test(dataProvider = "dates")
    public void testDateRenderer(Date date, String formattedDate)
    {
        String renderDate = SimpleDateRenderer.renderDate(date);
        assertEquals(formattedDate, renderDate);
    }

    @Test
    public void testDateRendererForNullArgument()
    {
        assertEquals("", SimpleDateRenderer.renderDate(null));
    }

    private static Date toDate(int year, int month, int day, int h, int min, int sec,
            String timeZoneId)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, h, min, sec);
        calendar.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        Date date = calendar.getTime();
        return date;
    }
}
