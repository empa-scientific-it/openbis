/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;

/**
 * Executing engine of {@link IPostRegistrationTask} instances. Manages persistent {@link ICleanupTask} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class TaskExecutor
{
    @Private
    static final String CLEANUP_TASKS_FOLDER_PROPERTY = "cleanup-tasks-folder";

    private static final String DEFAULT_CLEANUP_TASKS_FOLDER = "clean-up-tasks";

    private static final String FILE_TYPE = ".ser";

    private static final FilenameFilter FILTER = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(FILE_TYPE);
            }
        };

    private final Logger operationLog;

    private final File cleanupTasksFolder;

    /**
     * Creates an instance for specified folder which will store persistent {@link ICleanupTask} instances.
     */
    public TaskExecutor(Properties properties, Logger operationLog)
    {
        this.operationLog = operationLog;
        cleanupTasksFolder =
                new File(properties.getProperty(CLEANUP_TASKS_FOLDER_PROPERTY,
                        DEFAULT_CLEANUP_TASKS_FOLDER));
        if (cleanupTasksFolder.isFile())
        {
            throw new EnvironmentFailureException("Cleanup tasks folder is a file: "
                    + cleanupTasksFolder.getAbsolutePath());
        }
        cleanupTasksFolder.mkdirs();
    }

    public void execute(IPostRegistrationTask task, String taskName, String dataSetCode,
            boolean container) throws Throwable
    {
        ICleanupTask cleanupTask = null;
        File savedCleanupTask = null;
        try
        {
            IPostRegistrationTaskExecutor executor = task.createExecutor(dataSetCode, container);
            cleanupTask = executor.createCleanupTask();
            savedCleanupTask = saveCleanupTask(dataSetCode, taskName, cleanupTask);
            executor.execute();
        } catch (Throwable t)
        {
            cleanUpAndLog(t, cleanupTask, dataSetCode, taskName);
            throw t;
        } finally
        {
            if (savedCleanupTask != null)
            {
                savedCleanupTask.delete();
            }
        }
    }

    private File saveCleanupTask(String code, String taskName, ICleanupTask cleanupTask)
    {
        try
        {
            File file = new File(cleanupTasksFolder, code + "_" + taskName + FILE_TYPE);
            FileUtils.writeByteArrayToFile(file, SerializationUtils.serialize(cleanupTask));
            return file;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private void cleanUpAndLog(Throwable throwable, ICleanupTask cleanupTaskOrNull,
            String dataSetCode, String taskName)
    {
        operationLog.error("Task '" + taskName + "' for data set " + dataSetCode + " failed.",
                throwable);
        if (cleanupTaskOrNull != null)
        {
            try
            {
                cleanupTaskOrNull.cleanup(new Log4jSimpleLogger(operationLog));
            } catch (Throwable t)
            {
                operationLog.error("Clean up of failed task '" + taskName + "' for data set "
                        + dataSetCode + " failed, too.", t);
            }
        }
    }

    /**
     * Performs all cleanup tasks in cleanup-tasks folder.
     */
    public void cleanup()
    {
        Log4jSimpleLogger logger = new Log4jSimpleLogger(operationLog);
        File[] files = cleanupTasksFolder.listFiles(FILTER);
        if (files != null && files.length > 0)
        {
            operationLog.info("Perform " + files.length + " clean up task.");
            for (File file : files)
            {
                try
                {
                    ICleanupTask cleanupTask = deserializeFromFile(file);
                    cleanupTask.cleanup(logger);
                } catch (Exception ex)
                {
                    operationLog.error("Couldn't performed clean up task " + file, ex);
                }
                file.delete();
            }
        }
    }

    private ICleanupTask deserializeFromFile(File file) throws IOException
    {
        byte[] bytes = FileUtils.readFileToByteArray(file);
        return (ICleanupTask) SerializationUtils.deserialize(bytes);
    }

}
