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

import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class MutableV2Test extends MutableTest
{
    // for jython script go to
    // sourceTest/core-plugins/generic-test/1/dss/drop-boxes/mutable-v2-test/mutable-v2-test.py

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-mutable-v2-test");
    }

    @Override
    @Test
    public void testMutable() throws Exception
    {
        super.testMutable();
    }

    @Override
    protected void assertAfter()
    {
        super.assertAfter();
        List<ParsedLogEntry> logEntries = getLogEntries();
        for (ParsedLogEntry logEntry : logEntries)
        {
            if (logEntry.getLogMessage().contains("Projects updated: 1\nMaterials updated: 1\n"
                    + "Experiments updated: 1\nSamples updated: 1\nData sets updated: 1"))
            {
                return;
            }
        }
        fail("Missing Projects update log message.");
    }

}
