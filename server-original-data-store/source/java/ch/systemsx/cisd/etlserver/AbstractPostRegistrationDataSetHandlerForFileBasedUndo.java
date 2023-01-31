/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;

/**
 * Abstract super class of {@link IPostRegistrationDatasetHandler} classes creating files which have to be deleted in case of undo.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractPostRegistrationDataSetHandlerForFileBasedUndo implements
        IPostRegistrationDatasetHandler
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION,
                    AbstractPostRegistrationDataSetHandlerForFileBasedUndo.class);

    private final List<File> createdFiles = new ArrayList<File>();

    private final IFileOperations fileOperations;

    /**
     * Creates an instance for the specified abstraction of file operations.
     */
    protected AbstractPostRegistrationDataSetHandlerForFileBasedUndo(IFileOperations fileOperations)
    {
        this.fileOperations = fileOperations;
    }

    /**
     * Returns the abstraction of file operations.
     */
    protected IFileOperations getFileOperations()
    {
        return fileOperations;
    }

    /**
     * Adds the specified file. In case of undo it will be deleted.
     */
    protected void addFileForUndo(File file)
    {
        createdFiles.add(file);
    }

    /**
     * Deletes all files added by {@link #addFileForUndo(File)}.
     */
    @Override
    public void undoLastOperation()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Undo last operation by deleting following files: " + createdFiles);
        }

        for (File file : createdFiles)
        {
            if (file.exists())
            {
                fileOperations.deleteRecursively(file);
            }
        }
    }

}
