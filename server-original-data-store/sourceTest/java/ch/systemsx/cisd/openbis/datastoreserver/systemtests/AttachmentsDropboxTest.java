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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author pkupczyk
 */
public class AttachmentsDropboxTest extends SystemTestCase
{
    // for jython script go to
    // sourceTest/core-plugins/generic-test/1/dss/drop-boxes/attachments-test/attachments-test-handler.py

    @BeforeClass
    public void beforeClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.DEBUG);
    }

    @AfterClass
    public void afterClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.INFO);
    }

    @Test
    public void testAttachmentsWithSuccess() throws Exception
    {
        createData("success");
        waitUntilDataSetImported(new LogMonitoringStopConditionBuilder(new DropBoxNameCondition("attachments-test"))
                .and(new ContainsCondition("Successfully committed transaction")).getCondition());
        assertStreamsReleased(3);
    }

    @Test
    public void testAttachmentsWithFailure() throws Exception
    {
        createData("failure");
        waitUntilDataSetImported(new LogMonitoringStopConditionBuilder(ErrorStopCondition.INSTANCE)
                .and(new DropBoxNameCondition("attachments-test"))
                .and(new ContainsCondition("Data set registration failed")).getCondition());
        assertStreamsReleased(3);
    }

    private void createData(String fileName) throws Exception
    {
        File dataDirectory = new File(workingDirectory, "attachments-test-" + fileName);
        dataDirectory.mkdirs();
        FileUtilities.writeToFile(new File(dataDirectory, fileName), "");
        moveFileToIncoming(dataDirectory);
    }

    private void assertStreamsReleased(int expectedCount)
    {
        int count = 0;
        List<ParsedLogEntry> logEntries = getLogEntries();
        for (ParsedLogEntry logEntry : logEntries)
        {
            String logMessage = logEntry.getLogMessage();
            if (logEntry.getLogLevel().equals("DEBUG")
                    && logMessage.contains("OPERATION.Resources - Successfully released a resource")
                    && logMessage.contains("ReleasableStream"))
            {
                count++;
            }
        }
        assertEquals(expectedCount, count);
    }

    @Override
    protected Level getLogLevel()
    {
        return Level.DEBUG;
    }

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-attachments-test");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 120;
    }

}
