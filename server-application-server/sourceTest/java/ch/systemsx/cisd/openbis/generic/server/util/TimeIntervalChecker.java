/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.sql.Timestamp;
import java.util.Date;

import org.testng.AssertJUnit;

/**
 * Helper class to check that a time stamp is between now and a time in the past when an instance of this class has been created.
 * <p>
 * This class is useful in tests where some productive code creates a time stamp which should be checked.
 * 
 * @author Franz-Josef Elmer
 */
public class TimeIntervalChecker extends AssertJUnit
{
    private Timestamp notBeforeDate;

    /**
     * Creates an instance for now.
     */
    public TimeIntervalChecker()
    {
        this(0);
    }

    /**
     * Creates an instance for now minus specified shift in milliseconds.
     */
    public TimeIntervalChecker(long shiftInMillisecond)
    {
        this(new Timestamp(System.currentTimeMillis() - shiftInMillisecond));
    }

    /**
     * Creates an instance for the specified date object.
     */
    public TimeIntervalChecker(Date date)
    {
        notBeforeDate = asTimestamp(date);
    }

    /**
     * Asserts that the specified date is after the time stamp of creation of this instance and before now.
     */
    public void assertDateInInterval(Date date)
    {
        Timestamp timestamp = asTimestamp(date);
        assertFalse("Actual date [" + date + "] is before notBeforeDate [" + notBeforeDate + "].",
                notBeforeDate.after(timestamp));
        Timestamp now = new Timestamp(System.currentTimeMillis());
        assertFalse("Actual date [" + date + "] is after now [" + now + "].",
                now.before(timestamp));
    }

    private Timestamp asTimestamp(Date date)
    {
        return date instanceof Timestamp ? (Timestamp) date : new Timestamp(date.getTime());
    }
}
