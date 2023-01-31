/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author anttil
 */
public class WriteDirectoryNameToDropboxAction implements DirectoryAction
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, WriteDirectoryNameToDropboxAction.class);

    private final File dropboxDirectory;

    public WriteDirectoryNameToDropboxAction(File dropboxDirectory)
    {
        this.dropboxDirectory = dropboxDirectory;

    }

    @Override
    public void performOn(File subdir)
    {
        File file = new File(dropboxDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
        PrintWriter out = null;

        try
        {
            out = new PrintWriter(file);
            out.print(subdir.getAbsolutePath());
            operationLog.info("Written a directory path: " + subdir.getAbsolutePath() + " to a file: " + file.getAbsolutePath());
        } catch (FileNotFoundException ex)
        {
            operationLog.error("Could not write a directory path: " + subdir.getAbsolutePath() + " to a file: " + file.getAbsolutePath());
        } finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }
}
