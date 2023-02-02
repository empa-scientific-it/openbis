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

package ch.systemsx.cisd.common.converter;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link DateConverter}.
 * 
 * @author Christian Ribeaud
 */
public final class DateConverterTest
{

    @Test(expectedExceptions =
    { AssertionError.class })
    public final void testConstructor()
    {
        new DateConverter(null);
    }

    @Test(expectedExceptions =
    { IllegalArgumentException.class })
    public final void testConstructorWithBadFormat()
    {
        new DateConverter("badFormat");
    }

    @Test
    public final void testConvert()
    {
        DateConverter converter = new DateConverter("dd.MM.yyyy");
        Date date = converter.convert("08.11.1971");
        Calendar calendar = Calendar.getInstance();
        calendar.set(1971, Calendar.NOVEMBER, 8);
        // Calendar saves the current time as well.
        assert date.equals(calendar.getTime()) == false;
        Calendar birthday = Calendar.getInstance();
        birthday.setTime(date);
        assert DateUtils.isSameDay(calendar, birthday);
        assertNull(converter.convert("notParsableDate"));
        assertNull(converter.convert(null));
        assertNull(converter.convert(""));
    }

    @Test
    public final void testGetDefault()
    {
        // Explicit call to <code>getDefaultValue()</code>
        Calendar today = Calendar.getInstance();
        DateConverter converter = new DateConverter("dd.MM.yyyy");
        Calendar result = Calendar.getInstance();
        result.setTime(converter.getDefaultValue());
        assert DateUtils.isSameDay(today, result);
    }
}