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

package ch.systemsx.cisd.common.jython;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.jython.v27.Jython27InterpreterFactory;

/**
 * @author pkupczyk
 */

@Test(groups =
{ "slow" })
public class JythonScriptSplitterTest
{

    @Test
    public void testSplittingNullScriptShouldReturnNoBatches()
    {
        JythonScriptSplitter splitter = new JythonScriptSplitter(new Jython27InterpreterFactory().createInterpreter());
        List<String> batches = splitter.split(null);
        Assert.assertNotNull(batches);
        Assert.assertEquals(batches, Collections.emptyList());
    }

    @Test
    public void testSplittingEmptyScriptShouldReturnOneEmptyBatch()
    {
        JythonScriptSplitter splitter = new JythonScriptSplitter(new Jython27InterpreterFactory().createInterpreter());
        List<String> batches = splitter.split("");
        Assert.assertNotNull(batches);
        Assert.assertEquals(batches.size(), 1);
        Assert.assertEquals(batches.get(0), "");
    }

    @Test
    public void testSplittingScriptSmallerThanBatchSizeShouldReturnOneBatch()
    {
        List<String> batches = testSplittingScript(1, 100);
        Assert.assertEquals(batches.size(), 1);
    }

    @Test
    public void testSplittingScriptBiggerThanBatchSizeShouldReturnMultipleBatches()
    {
        List<String> batches = testSplittingScript(1, 10);
        Assert.assertEquals(batches.size(), 3);
        Assert.assertEquals(batches.get(0), getTestScriptCodeBatch(1));
        Assert.assertEquals(batches.get(1), getTestScriptCodeBatch(2));
        Assert.assertEquals(batches.get(2), getTestScriptCodeBatch(3));
    }

    @Test
    public void testSplittingScriptBiggerThanJavaLimitShouldReturnMultipleBatches()
    {
        List<String> batches = testSplittingScript(200, 100);
        Assert.assertTrue(batches.size() > 1);
    }

    @Test
    public void testSplittingScriptWithCommandsBiggerThanBatchSizeShouldReturnBatchesThatFitCommands()
    {
        List<String> batches = testSplittingScript(2, 5);
        Assert.assertTrue(batches.size() > 1);
    }

    private List<String> testSplittingScript(int scriptSize, int batchSize)
    {
        String originalScript = getTestScriptCode(scriptSize);

        JythonScriptSplitter splitter = new JythonScriptSplitter(new Jython27InterpreterFactory().createInterpreter());
        splitter.setBatchSize(batchSize);

        List<String> batches = splitter.split(originalScript);
        Assert.assertNotNull(batches);

        StringBuilder scriptFromBatches = new StringBuilder();
        for (String batch : batches)
        {
            scriptFromBatches.append(batch);
        }
        Assert.assertEquals(scriptFromBatches.toString(), originalScript);

        String originalScriptOutput = getTestScriptOutput(scriptSize);

        ByteArrayOutputStream scriptFromBatchesOutput = new ByteArrayOutputStream();
        PythonInterpreter interpreter = PythonInterpreter.createNonIsolatedPythonInterpreter();
        interpreter.setOut(scriptFromBatchesOutput);

        for (String batch : batches)
        {
            interpreter.exec(batch);
        }

        Assert.assertEquals(scriptFromBatchesOutput.toString(), originalScriptOutput);

        return batches;
    }

    private String getTestScriptCode(int scriptSize)
    {
        return getTestFile("testScriptCode.py", scriptSize);
    }

    private String getTestScriptCodeBatch(int index)
    {
        return getTestFile("testScriptCodeBatch" + index + ".py", 1);
    }

    private String getTestScriptOutput(int scriptSize)
    {
        return getTestFile("testScriptOutput.txt", scriptSize);
    }

    private String getTestFile(String fileName, int numberOfDuplications)
    {
        File file = new File("resource/test-data/" + getClass().getSimpleName() + "/" + fileName);
        String content = FileUtilities.loadToString(file);
        StringBuilder duplicatedContent = new StringBuilder();

        for (int i = 0; i < numberOfDuplications; i++)
        {
            duplicatedContent.append(content);
        }
        return duplicatedContent.toString();
    }

}
