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

import java.util.List;

/**
 * Splits jython code into smaller batches to overcome 64KB script size limitation.
 * 
 * @author pkupczyk
 */
public class JythonScriptSplitter
{

    private static final int DEFAULT_BATCH_SIZE = 100;

    private int batchSize = DEFAULT_BATCH_SIZE;

    private IJythonInterpreter interpreter;

    public JythonScriptSplitter(IJythonInterpreter interpreter)
    {
        this.interpreter = interpreter;
    }

    public List<String> split(String scriptToSplit)
    {
        JythonScript script = new JythonScript(scriptToSplit);
        JythonScriptBatches batches = new JythonScriptBatches();
        JythonScriptBatch batch = new JythonScriptBatch();
        JythonScriptLines command = new JythonScriptLines();

        for (String line : script.getLines())
        {
            if (command.getSize() > 0 && interpreter.isNextCommand(command.getLines()))
            {
                if (batch.getSize() > 0 && batch.getSize() + command.getSize() > getBatchSize())
                {
                    batches.addBatch(batch);
                    batch = new JythonScriptBatch();
                }
                batch.addLines(command);
                command = new JythonScriptLines();
                command.addLine(line);
            } else
            {
                command.addLine(line);
            }
        }

        if (command.getSize() > 0)
        {
            batch.addLines(command);
        }
        if (batch.getSize() > 0)
        {
            batches.addBatch(batch);
        }

        return batches.getLines();
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public void setBatchSize(int batchSize)
    {
        if (batchSize <= 0)
        {
            throw new IllegalArgumentException("Batch size must be > 0");
        }
        this.batchSize = batchSize;
    }

}
