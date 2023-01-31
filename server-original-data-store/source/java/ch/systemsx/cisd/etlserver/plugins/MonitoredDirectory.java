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
import java.io.FileFilter;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author anttil
 */
public class MonitoredDirectory
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MonitoredDirectory.class);

    private final File directory;

    private final String readyToImportMarkerFile;

    private final String importStateMarkerFilePrefix;

    public MonitoredDirectory(File directory, String readyToImportMarkerFile, String importStateMarkerFilePrefix)
    {
        if (directory.isFile())
        {
            throw new IllegalArgumentException("Monitored directory " + directory.getAbsolutePath() + " is a file.");
        }
        if (directory.exists() == false)
        {
            throw new IllegalArgumentException("Monitored directory " + directory.getAbsolutePath() + " does not exist.");
        }
        if (directory.canWrite() == false)
        {
            throw new IllegalArgumentException("Cannot write to monitored directory " + directory.getAbsolutePath());
        }
        if (directory.canRead() == false)
        {
            throw new IllegalArgumentException("Cannot read from monitored directory " + directory.getAbsolutePath());
        }

        this.directory = directory;
        this.readyToImportMarkerFile = readyToImportMarkerFile;
        this.importStateMarkerFilePrefix = importStateMarkerFilePrefix;
    }

    public void perform(DirectoryAction action)
    {
        for (File subdir : listSubDirectoriesOf(directory))
        {
            File readyFile = new File(subdir, readyToImportMarkerFile);
            File[] stateFiles = ImportStateMarkerFile.listMarkerFiles(importStateMarkerFilePrefix, subdir);

            if ((readyFile.exists() == true) && (stateFiles.length == 0))
            {
                try
                {
                    action.performOn(subdir);
                    ImportStateMarkerFile.setMarkerFile(importStateMarkerFilePrefix, subdir, ImportState.QUEUED);
                } catch (Exception ex)
                {
                    operationLog.error("Could not perform an action: " + action + " on a monitored directory: " + subdir, ex);
                    continue;
                }
            }
        }
    }

    private File[] listSubDirectoriesOf(File dir)
    {
        File[] subdirectories = dir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isDirectory();
                }
            });
        return subdirectories;
    }

}
