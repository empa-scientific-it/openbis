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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;

/**
 * Utility class that rollsback a data set registration.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationRollbacker
{
    private final boolean stopped;

    private final DataSetRegistrationAlgorithm registrationAlgorithm;

    private final File incomingDataSetFile;

    private final Logger notificationLog;

    private final Logger operationLog;

    private final Throwable throwable;

    public DataSetRegistrationRollbacker(boolean stopped,
            DataSetRegistrationAlgorithm registrationAlgorithm, File incomingDataSetFile,
            Logger notificationLog, Logger operationLog, Throwable throwable)
    {
        this.stopped = stopped;
        this.registrationAlgorithm = registrationAlgorithm;
        this.incomingDataSetFile = incomingDataSetFile;
        this.notificationLog = notificationLog;
        this.operationLog = operationLog;
        this.throwable = throwable;
    }

    public void doRollback() throws Error
    {
        if (stopped)
        {
            Thread.interrupted(); // Ensure the thread's interrupted state is cleared.
            getOperationLog().warn(
                    String.format("Requested to stop registration of data set '%s'",
                            registrationAlgorithm.getDataSetInformation()));
        } else
        {
            getNotificationLog().error(
                    String.format(registrationAlgorithm.getErrorMessageTemplate(),
                            registrationAlgorithm.getDataSetInformation()), throwable);
        }
        // Errors which are not AssertionErrors leave the system in a state that we don't
        // know and can't trust. Thus we will not perform any operations any more in this
        // case.
        if (throwable instanceof Error && throwable instanceof AssertionError == false)
        {
            throw (Error) throwable;
        }
        UnstoreDataAction action = registrationAlgorithm.rollbackStorageProcessor(throwable);
        if (stopped == false)
        {
            if (action == UnstoreDataAction.MOVE_TO_ERROR)
            {
                final File baseDirectory =
                        registrationAlgorithm.createBaseDirectory(
                                TransferredDataSetHandler.ERROR_DATA_STRATEGY,
                                registrationAlgorithm.getStoreRoot());
                registrationAlgorithm.setBaseDirectoryHolder(new BaseDirectoryHolder(
                        TransferredDataSetHandler.ERROR_DATA_STRATEGY, baseDirectory,
                        incomingDataSetFile));
                boolean moveInCaseOfErrorOk =
                        FileRenamer.renameAndLog(incomingDataSetFile, registrationAlgorithm
                                .getBaseDirectoryHolder().getTargetFile());
                writeThrowable();
                if (moveInCaseOfErrorOk)
                {
                    registrationAlgorithm.clean();
                }
            } else if (action == UnstoreDataAction.DELETE)
            {
                FileUtilities.deleteRecursively(incomingDataSetFile, new Log4jSimpleLogger(
                        TransferredDataSetHandler.operationLog));
            }
        }
    }

    private Logger getNotificationLog()
    {
        return notificationLog;
    }

    private Logger getOperationLog()
    {
        return operationLog;
    }

    protected final void writeThrowable()
    {
        final String fileName = incomingDataSetFile.getName() + ".exception";
        final File file =
                new File(registrationAlgorithm.getBaseDirectoryHolder().getTargetFile()
                        .getParentFile(), fileName);
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(file);
            throwable.printStackTrace(new PrintWriter(writer));
        } catch (final IOException e)
        {
            TransferredDataSetHandler.operationLog.warn(String.format(
                    "Could not write out the exception '%s' in file '%s'.", fileName,
                    file.getAbsolutePath()), e);
        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }
}
