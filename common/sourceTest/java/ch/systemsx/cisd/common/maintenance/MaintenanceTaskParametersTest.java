/*
 * Copyright 2021 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.maintenance;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class MaintenanceTaskParametersTest
{
    private static final String CRON = MaintenanceTaskParameters.CRON_PREFIX;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    @DataProvider(name = "next-next")
    public static Object[][] nextNext()
    {
        return new Object[][] {
                new Object[] { CRON + "* * * * * *" },
                new Object[] { CRON + "3 * * * * *" },
                new Object[] { "21:10" },
                new Object[] { "MO 21:10" },
                new Object[] { "2.SA 1:10" },
                new Object[] { "1.7. 2" },
        };
    }

    @Test(dataProvider = "next-next")
    public void testNextNext(String definition) throws ParseException
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, "dummy");
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, definition);
        MaintenanceTaskParameters parameters = new MaintenanceTaskParameters(properties, "test");
        INextTimestampProvider nextTimestampProvider = parameters.getNextTimestampProvider();

        // When
        Date next = nextTimestampProvider.getNextTimestamp(new Date());
        Date nextNext = nextTimestampProvider.getNextTimestamp(next);

        // Then
        assertTrue(nextNext.after(next),
                "Expection failed: " + DATE_FORMAT.format(next) + " < " + DATE_FORMAT.format(nextNext));
    }

    @DataProvider(name = "run-scheduling")
    public static Object[][] runScheduling()
    {
        return new Object[][] {
                new Object[] { CRON + "   */2   *  * * *   *   ", "2021-03-31 14:15:16.123", "2021-03-31 14:15:18.0" },
                new Object[] { CRON + "2 10 14 * 4 *", "2021-03-31 14:15:16.123", "2021-04-01 14:10:02.0" },
                new Object[] { "21:10", "2021-03-31 14:15:16.123", "2021-03-31 21:10:00.0" },
                new Object[] { "21:10", "2021-03-31 21:15:16.123", "2021-04-01 21:10:00.0" },
                new Object[] { "21:10", "2021-12-31 21:15:16.123", "2022-01-01 21:10:00.0" },
                new Object[] { "21", "2021-12-31 21:15:16.123", "2022-01-01 21:00:00.0" },
                new Object[] { "MO 21:10", "2021-02-01 11:15:16.123", "2021-02-01 21:10:00.0" },
                new Object[] { "MON 21:10", "2021-02-01 21:15:16.123", "2021-02-08 21:10:00.0" },
                new Object[] { "TU  21:10", "2021-02-01 21:15:16.123", "2021-02-02 21:10:00.0" },
                new Object[] { "TUE 21:10", "2021-02-01 21:15:16.123", "2021-02-02 21:10:00.0" },
                new Object[] { "WE  21:10", "2021-02-01 21:15:16.123", "2021-02-03 21:10:00.0" },
                new Object[] { "WED 21:10", "2021-02-01 21:15:16.123", "2021-02-03 21:10:00.0" },
                new Object[] { "TH  21:10", "2021-02-01 21:15:16.123", "2021-02-04 21:10:00.0" },
                new Object[] { "THU 21:10", "2021-02-01 21:15:16.123", "2021-02-04 21:10:00.0" },
                new Object[] { "FR  21:10", "2021-02-01 21:15:16.123", "2021-02-05 21:10:00.0" },
                new Object[] { "FRI 21:10", "2021-02-01 21:15:16.123", "2021-02-05 21:10:00.0" },
                new Object[] { "SA  21:10", "2021-02-01 21:15:16.123", "2021-02-06 21:10:00.0" },
                new Object[] { "SAT 21:10", "2021-02-01 21:15:16.123", "2021-02-06 21:10:00.0" },
                new Object[] { "SU  21:10", "2021-02-01 21:15:16.123", "2021-02-07 21:10:00.0" },
                new Object[] { "SUN 21:10", "2021-02-01 21:15:16.123", "2021-02-07 21:10:00.0" },
                new Object[] { "11. 21:10", "2021-02-11 12:15:16.123", "2021-02-11 21:10:00.0" },
                new Object[] { "11. 21:10", "2021-02-11 22:15:16.123", "2021-03-11 21:10:00.0" },
                new Object[] { "11. 21:10", "2021-02-18 02:15:16.123", "2021-03-11 21:10:00.0" },
                new Object[] { "11. 21:10", "2021-12-31 11:15:16.123", "2022-01-11 21:10:00.0" },
                new Object[] { "3.FR 21:10", "2021-03-19 11:15:16.123", "2021-03-19 21:10:00.0" },
                new Object[] { "3.fr 21:10", "2021-03-19 21:15:16.123", "2021-04-16 21:10:00.0" },
                new Object[] { "3.Fr 21:10", "2021-03-20 11:15:16.123", "2021-04-16 21:10:00.0" },
                new Object[] { "3.Fri 21:10", "2021-03-21 11:15:16.123", "2021-04-16 21:10:00.0" },
                new Object[] { " 3.fri  21:10", "2021-03-22 11:15:16.123", "2021-04-16 21:10:00.0" },
                new Object[] { "3. FR 21:10", "2021-04-01 11:15:16.123", "2021-04-16 21:10:00.0" },
                new Object[] { "3.FR 21:10", "2021-04-02 11:15:16.123", "2021-04-16 21:10:00.0" },
                new Object[] { "3.FR 21:10", "2021-04-08 11:15:16.123", "2021-04-16 21:10:00.0" },
                new Object[] { "3.FR 21:10", "2021-04-09 11:15:16.123", "2021-04-16 21:10:00.0" },
                new Object[] { "3.May 21:10", "2021-05-03 11:15:16.123", "2021-05-03 21:10:00.0" },
                new Object[] { "3.5 21:10", "2021-05-04 11:15:16.123", "2022-05-03 21:10:00.0" },
                new Object[] { "3. 5. 21:10", "2021-11-01 11:15:16.123", "2022-05-03 21:10:00.0" },
                new Object[] { "3. 5. 21:10", "2023-11-01 11:15:16.123", "2024-05-03 21:10:00.0" },
                new Object[] { "3. 5. 21:10", "2024-11-01 11:15:16.123", "2025-05-03 21:10:00.0" },
                new Object[] { "1.MO 1:55, 3. Mo 11:10", "2021-03-02 11:15:16.123", "2021-03-15 11:10:00.0" },
                new Object[] { "1. MO 01:55, 3.Mon 11:10", "2021-03-16 11:15:16.123", "2021-04-05 01:55:00.0" },
        };
    }

    @Test(dataProvider = "run-scheduling")
    public void testRunScheduling(String definition, String currentTimestamp, String expectedNext) throws ParseException
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, "dummy");
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, definition);
        MaintenanceTaskParameters parameters = new MaintenanceTaskParameters(properties, "test");
        INextTimestampProvider nextTimestampProvider = parameters.getNextTimestampProvider();

        // When
        Date next = nextTimestampProvider.getNextTimestamp(DATE_FORMAT.parse(currentTimestamp));

        // Then
        assertEquals(DATE_FORMAT.format(next), expectedNext);
    }

    @DataProvider(name = "invalid-run-scheduling")
    public static Object[][] invalidRunScheduling()
    {
        return new Object[][] {
                new Object[] { "*/2   *  * * * *",
                        "Invalid property 'run-schedule' (Reason: For input string: \"*\"): */2   *  * * * *" },
                new Object[] { "a;b", "Invalid property 'run-schedule' (Reason: For input string: \"a;b\"): a;b" },
                new Object[] { "MAY 2:55", "Invalid property 'run-schedule' (Reason: Invalid description): MAY 2:55" },
                new Object[] { "SO 2:55", "Invalid property 'run-schedule' "
                        + "(Reason: Neither a number nor a 3-letter month nor a 2-letter week day nor "
                        + "a 3-letter week day: SO): SO 2:55" },
                new Object[] { "1.MO 2x:55", "Invalid property 'run-schedule' "
                        + "(Reason: For input string: \"2x\"): 1.MO 2x:55" },
                new Object[] { CRON + "2.MO", "Cron expression must consist of 6 fields (found 1 in \"2.MO\")" },
        };
    }

    @Test(dataProvider = "invalid-run-scheduling")
    public void testInvalidRunScheduling(String definition, String expectedError)
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, "dummy");
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, definition);

        try
        {
            // When
            new MaintenanceTaskParameters(properties, "test");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            // Then
            assertEquals(e.getMessage(), expectedError);
        }
    }

    @Test
    public void testRetryIntervals()
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, "dummy");
        properties.setProperty(MaintenanceTaskParameters.INTERVAL_KEY, "30 m");
        properties.setProperty(MaintenanceTaskParameters.RETRY_INTERVALS_AFTER_FAILURE_KEY, "1 h, 1 min, 40 s");
        
        // When
        MaintenanceTaskParameters parameters = new MaintenanceTaskParameters(properties, "test");
        
        // Then
        assertEquals(parameters.getRetryIntervals().toString(), "[3600000, 60000, 40000]"); 
    }
}
