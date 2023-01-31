/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ShareIdManagerTest extends AssertJUnit
{
    private static final String DS1 = "ds1";

    private static final String DS2 = "ds2";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private ShareIdManager manager;

    private Level level;

    @BeforeMethod
    public void setUp()
    {
        level = Logger.getRootLogger().getLevel();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG,
                ".*" + ShareIdManager.class.getSimpleName());
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        context.checking(new Expectations()
            {
                {
                    allowing(service).listDataSetShareIds();
                    DataSetShareId ds1 = new DataSetShareId();
                    ds1.setDataSetCode(DS1);
                    DataSetShareId ds2 = new DataSetShareId();
                    ds2.setDataSetCode(DS2);
                    ds2.setShareId("2");
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    allowing(service).tryGetLocalDataSet("ds?");
                    will(returnValue(null));
                }
            });
        manager = new ShareIdManager(service, 1);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
        Logger.getRootLogger().setLevel(level);
    }

    @Test
    public void testUnlockedGetShareId()
    {
        assertEquals(Constants.DEFAULT_SHARE_ID, manager.getShareId(DS1));
        assertEquals("2", manager.getShareId(DS2));
    }

    @Test
    public void testGetShareIdOfUnknownDataSet()
    {
        try
        {
            manager.getShareId("ds?");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unknown data set: ds?", ex.getMessage());
        }
    }

    @Test
    public void testLockOfUnknownDataSet()
    {
        try
        {
            manager.lock("ds?");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unknown data set: ds?", ex.getMessage());
        }
    }

    @Test
    public void testReleaseLockOfUnknownDataSet()
    {
        manager.releaseLock("ds?");
    }

    @Test
    public void testUnlockedSetShareId()
    {
        manager.setShareId(DS1, "1");
        assertEquals("1", manager.getShareId(DS1));
        assertEquals("2", manager.getShareId(DS2));
    }

    @Test
    public void testSetShareIdForNewDataSet()
    {
        manager.setShareId("new data set", "42");
        assertEquals("42", manager.getShareId("new data set"));
    }

    @Test(groups = "slow")
    public void testLockingTimeOut()
    {
        final MessageChannel ch = new MessageChannel(2000);
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    manager.lock(DS1);
                    try
                    {
                        manager.await(DS1);
                    } catch (EnvironmentFailureException ex)
                    {
                        System.out.println(ex);
                        ch.send(ex.getMessage());
                    }
                }
            }, "T1").start();
        ch.assertNextMessage("Lock for data set ds1 hasn't been released after "
                + "time out of 1 seconds.");

        String logContent = logRecorder.getLogContent();

        assertTrue(logContent.contains("INFO  OPERATION.ShareIdManager"
                + " - Share id manager initialized with 2 data sets.\n"
                + "DEBUG OPERATION.ShareIdManager - Data set ds1 has been locked.\n"));

        assertTrue(logContent.contains("DEBUG OPERATION.ShareIdManager"
                + " - Data set ds1 is locked by the following threads: T1\n"
                + "ERROR OPERATION.ShareIdManager - Timeout: Lock for data set ds1 is held "
                + "by threads 'T1' for 1 seconds."));

        ch.assertEmpty();
    }

    @Test
    public void testLocking()
    {
        final MessageChannel ch1 = new MessageChannel();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    manager.lock(DS1);
                    ch1.send("locked");
                    try
                    {
                        Thread.sleep(200);
                    } catch (InterruptedException ex)
                    {
                        // ignored
                    }
                    manager.releaseLock(DS1);
                    ch1.send("unlocked");
                }
            }, "T1").start();
        ch1.assertNextMessage("locked"); // wait until data set is really locked.

        manager.await(DS1);

        ch1.assertNextMessage("unlocked"); // wait until thread is finished

        String logContent = logRecorder.getLogContent();

        assertTrue(logContent.contains("INFO  OPERATION.ShareIdManager"
                + " - Share id manager initialized with 2 data sets.\n"
                + "DEBUG OPERATION.ShareIdManager - Data set ds1 has been locked.\n"));

        assertTrue(logContent.contains("DEBUG OPERATION.ShareIdManager"
                + " - Data set ds1 is locked by the following threads: T1\n"
                + "DEBUG OPERATION.ShareIdManager - Unlock data set ds1"));
    }

    @Test(groups = "slow")
    public void testMultipleLocking()
    {
        final MessageChannel ch1 = new MessageChannel();
        final MessageChannel ch3 = new MessageChannel();
        final MessageChannel ch4 = new MessageChannel();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    manager.lock(DS1);
                    ch3.send("locked");
                    ch4.assertNextMessage("locked");
                    manager.releaseLock(DS1);
                    ch1.send("unlocked");
                }
            }, "T1").start();
        final MessageChannel ch2 = new MessageChannel();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    ch3.assertNextMessage("locked");
                    manager.lock(DS1);
                    ch2.send("locked");
                    ch4.send("locked");
                }
            }, "T2").start();
        ch1.assertNextMessage("unlocked");
        ch2.assertNextMessage("locked");

        try
        {
            manager.await(DS1);
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Lock for data set ds1 hasn't been released after time out of 1 seconds.",
                    ex.getMessage());
        }

        String logContent = logRecorder.getLogContent();

        assertTrue(logContent.contains("INFO  OPERATION.ShareIdManager"
                + " - Share id manager initialized with 2 data sets.\n"
                + "DEBUG OPERATION.ShareIdManager - Data set ds1 has been locked.\n"));

        assertTrue(logContent.contains("DEBUG OPERATION.ShareIdManager"
                + " - Data set ds1 is locked by the following threads: T1\n"
                + "DEBUG OPERATION.ShareIdManager"
                + " - Data set ds1 is locked by the following threads: T1, T2\n"
                + "DEBUG OPERATION.ShareIdManager"
                + " - Data set ds1 is locked by the following threads: T2\n"
                + "ERROR OPERATION.ShareIdManager - Timeout: Lock for data set ds1 is held by "
                + "threads 'T2' for 1 seconds."));

    }
}
