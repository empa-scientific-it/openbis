/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionProgress;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.IOperationExecutionConfig;

/**
 * @author pkupczyk
 */
@Component
public class OperationExecutionFSStore implements IOperationExecutionFSStore
{

    public static final String OPERATIONS_FILE_NAME = "operations";

    public static final String PROGRESS_FILE_NAME = "progress";

    public static final String ERROR_FILE_NAME = "error";

    public static final String RESULTS_FILE_NAME = "results";

    @Autowired
    private IOperationExecutionConfig config;

    public OperationExecutionFSStore()
    {
    }

    OperationExecutionFSStore(IOperationExecutionConfig config)
    {
        this.config = config;
    }

    @Override
    public void executionNew(String code, List<? extends IOperation> operations)
    {
        File dir = getExecutionDirectory(code).getFile();
        dir.mkdirs();
        File file = new File(dir, OPERATIONS_FILE_NAME);
        writeToFile(file, operations, true);
    }

    @Override
    public void executionProgressed(String code, OperationExecutionProgress progress)
    {
        // This method may be called when the execution has been already finished and the execution directory has been already removed by the cleanup
        // maintenance task or an explicit cleanup request (progress is reported with some delay by a different thread - other than the execution
        // thread). Therefore we should not fail when the execution directory no longer exists.

        File dir = getExecutionDirectory(code).getFile();
        File file = new File(dir, PROGRESS_FILE_NAME);

        writeToFile(file, progress, false);
    }

    @Override
    public void executionFailed(String code, IOperationExecutionError error)
    {
        File dir = getExecutionDirectory(code).getFile();
        File file = new File(dir, ERROR_FILE_NAME);
        writeToFile(file, error, false);
    }

    @Override
    public void executionFinished(String code, List<? extends IOperationResult> results)
    {
        File dir = getExecutionDirectory(code).getFile();
        File file = new File(dir, RESULTS_FILE_NAME);
        writeToFile(file, results, true);
    }

    @Override
    public void executionAvailability(String code, OperationExecutionAvailability availability)
    {
        if (OperationExecutionAvailability.DELETED.equals(availability) || OperationExecutionAvailability.TIMED_OUT.equals(availability))
        {
            File dir = getExecutionDirectory(code).getFile();

            if (dir.exists())
            {
                try
                {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void executionSummaryAvailability(String code, OperationExecutionAvailability summaryAvailability)
    {
        // nothing do nothing
    }

    @Override
    public void executionDetailsAvailability(String code, OperationExecutionAvailability detailsAvailability)
    {
        executionAvailability(code, detailsAvailability);
    }

    @SuppressWarnings("unchecked")
    @Override
    public OperationExecutionFS getExecution(String code, OperationExecutionFSFetchOptions fo)
    {
        // The execution directory might have been already removed by the cleanup maintenance task or an explicit cleanup request. Therefore we should
        // not fail when the execution directory no longer exists.

        OperationExecutionFS execution = new OperationExecutionFS();
        OperationExecutionDirectory executionDir = getExecutionDirectory(code);

        execution.setRelativePath(executionDir.getRelativePath());

        if (fo.hasOperations())
        {
            File file = new File(executionDir.getFile(), OPERATIONS_FILE_NAME);
            execution.setOperations((List<? extends IOperation>) readFromFile(file, false));
        }

        if (fo.hasProgress())
        {
            File file = new File(executionDir.getFile(), PROGRESS_FILE_NAME);
            execution.setProgress((IOperationExecutionProgress) readFromFile(file, false));
        }

        if (fo.hasError())
        {
            File file = new File(executionDir.getFile(), ERROR_FILE_NAME);
            execution.setError((IOperationExecutionError) readFromFile(file, false));
        }

        if (fo.hasResults())
        {
            File file = new File(executionDir.getFile(), RESULTS_FILE_NAME);
            execution.setResults((List<? extends IOperationResult>) readFromFile(file, false));
        }

        return execution;
    }

    private OperationExecutionDirectory getExecutionDirectory(String code)
    {
        return new OperationExecutionDirectory(config.getStorePath(), code);
    }

    private void writeToFile(File file, Object object, boolean failOnFileNotFound)
    {
        ObjectOutputStream out = null;

        try
        {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeObject(object);
        } catch (Exception e)
        {
            if (e instanceof FileNotFoundException && false == failOnFileNotFound)
            {
                return;
            } else
            {
                throw new RuntimeException("Couldn't write operation execution details to file " + file.getAbsolutePath(), e);
            }
        } finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                } catch (IOException e)
                {
                }
            }
        }
    }

    private Object readFromFile(File file, boolean failOnFileNotFound)
    {
        ObjectInputStream in = null;

        try
        {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return in.readObject();
        } catch (Exception e)
        {
            if (e instanceof FileNotFoundException && false == failOnFileNotFound)
            {
                return null;
            } else
            {
                throw new RuntimeException("Couldn't read operation execution details from file " + file.getAbsolutePath(), e);
            }
        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (IOException e)
                {
                }
            }
        }
    }

}
