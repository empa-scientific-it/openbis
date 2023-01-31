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
import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * This test tests little functionality, but focuses on benchmarking the sample registration process during development. Run this test on "before" and
 * "after" versions of your code to measure the performance changes.
 * <p>
 * It starts a dropbox that registers the given big number of samples with certain type. Edit the constants in the dropbox script to change the
 * number, and the type. The default values are small, to avoid having a long-running test, that doesn't do much. You might also want to update the
 * timeout value in method <code>dataSetImportWaitDurationInSeconds()</code> if your benchmark takes too much time.
 * <p>
 * After the test completes it prints to the console how much time took the jython script part and the registration in application server.
 * 
 * @author Jakub Straszewski
 */
@Test(groups = "slow")
public class SampleBatchImportBenchmarkSystemTest extends SystemTestCase
{
    // for jython script go to
    // sourceTest/core-plugins/generic-test/1/dss/drop-boxes/sample-benchmark/sample-benchmark-data-set-handler.py

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-sample-benchmark");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 120;
    }

    @Test
    public void testSampleBenchmark() throws Exception
    {
        dropSomeFileInADropbox();

        waitUntilDataSetImported();

        assertEmailHasBeenSentFromHook();
    }

    private void assertEmailHasBeenSentFromHook()
    {
        File emailDirectory = new File(new File(workingDirectory, "dss-root"), "email");

        for (File f : FileUtilities.listFiles(emailDirectory))
        {
            String content = FileUtilities.loadExactToString(f);
            if (content.contains("sample_benchmark_test"))
            {
                System.out.println(content);
                // with the persistent map
                return; // assert ok
            }
        }
        fail("No email found!");
    }

    private void dropSomeFileInADropbox() throws IOException
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "set1.txt"), "hello world");
        moveFileToIncoming(exampleDataSet);
    }

}
