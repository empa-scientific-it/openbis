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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DeletionCommand.class)
public class DeletionCommandTest extends AbstractFileSystemTestCase
{
    private static final String SHARE_ID = "1";

    private static final class DeletionCommandWithMockLogger extends DeletionCommand
    {
        private static final long serialVersionUID = 1L;

        private final ISimpleLogger logger;

        DeletionCommandWithMockLogger(ISimpleLogger logger, List<DatasetDescription> dataSets)
        {
            super(dataSets, 0, 0);
            this.logger = logger;
        }

        @Override
        ISimpleLogger createLogger()
        {
            return logger;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IShareIdManager shareIdManager;

    private IHierarchicalContentProvider contentProvider;

    private MockLogger log;

    private DataSetDirectoryProvider dataSetDirectoryProvider;

    private File store;

    private File share1;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        shareIdManager = context.mock(IShareIdManager.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        store = new File(workingDirectory, "store");
        store.mkdirs();
        share1 = new File(store, SHARE_ID);
        share1.mkdir();
        dataSetDirectoryProvider = new DataSetDirectoryProvider(store, shareIdManager);
        log = new MockLogger();
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testDeletionError() throws Exception
    {
        DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds-1").location("a").getDatasetDescription();
        new File(share1, "a").mkdirs();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId("ds-1");
                    will(returnValue(SHARE_ID));

                    one(shareIdManager).await("ds-1");
                }
            });

        DeletionCommand deletionCommand =
                new DeletionCommandWithMockLogger(log, Arrays.asList(ds1));

        deletionCommand.execute(contentProvider, dataSetDirectoryProvider);

        log.assertNextLogMessage("Await for data set ds-1 to be unlocked.");
        log.assertNextLogMessage("Couldn't delete Dataset 'ds-1', reason: unexpected invocation: "
                + "iShareIdManager.getShareId(\"ds-1\")\n"
                + "expectations:\n"
                + "  expected once, already invoked 1 time: iShareIdManager.getShareId(\"ds-1\"); returns \"1\"\n"
                + "  expected once, already invoked 1 time: iShareIdManager.await(\"ds-1\"); returns a default value\n"
                + "what happened before this:\n" + "  iShareIdManager.getShareId(\"ds-1\")\n"
                + "  iShareIdManager.await(\"ds-1\")\n");

        context.assertIsSatisfied();
    }

    @Test
    public void testDeletion() throws Exception
    {
        DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds-1").location("a").getDatasetDescription();
        File f1 = new File(share1, "a");
        FileUtilities.writeToFile(f1, "hello ds-1");
        assertEquals(true, f1.exists());
        DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds-2").location("b").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(shareIdManager).getShareId("ds-1");
                    will(returnValue(SHARE_ID));

                    one(shareIdManager).await("ds-1");

                    one(shareIdManager).getShareId("ds-2");
                    will(returnValue(SHARE_ID));
                }
            });
        IDataSetCommand command = new DeletionCommandWithMockLogger(log, Arrays.asList(ds1, ds2));

        command.execute(contentProvider, dataSetDirectoryProvider);

        log.assertNextLogMessage("Await for data set ds-1 to be unlocked.");
        log.assertNextLogMessage("Start deleting data set ds-1 at " + f1);
        log.assertNextLogMessage("Data set ds-1 at " + f1 + " has been successfully deleted.");
        log.assertNoMoreLogMessages();
        assertEquals("WARN  OPERATION.DataSetExistenceChecker - "
                + "Data set 'ds-2' no longer exists.", logRecorder.getLogContent());
        assertEquals(false, f1.exists());

        context.assertIsSatisfied();
    }
}
