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
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;

/**
 * @author anttil
 */
public class FileSystemMonitoringMaintenanceTask implements IMaintenanceTask
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileSystemMonitoringMaintenanceTask.class);

    public static final String READY_TO_IMPORT_MARKER_FILE = "ready-to-import-marker-file";

    public static final String IMPORT_STATE_MARKER_FILE_PREFIX = "import-state-marker-file-prefix";

    public static final String MONITORED_DIRECTORIES = "monitored-directories";

    public static final String DROPBOX_DIRECTORY = "dropbox-directory";

    public static final String DEFAULT_READY_TO_IMPORT_MARKER_FILE = ".ready-to-import";

    public static final String DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX = ".import-";

    private String dropboxDirectory;

    private String readyToImportMarkerFile;

    private String importStateMarkerFilePrefix;

    private Collection<MonitoredDirectory> monitoredDirectories;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        readyToImportMarkerFile = properties.getProperty(READY_TO_IMPORT_MARKER_FILE);
        importStateMarkerFilePrefix = properties.getProperty(IMPORT_STATE_MARKER_FILE_PREFIX);
        dropboxDirectory = properties.getProperty(DROPBOX_DIRECTORY);
        String directories = properties.getProperty(MONITORED_DIRECTORIES);

        if (readyToImportMarkerFile == null || readyToImportMarkerFile.length() == 0)
        {
            readyToImportMarkerFile = DEFAULT_READY_TO_IMPORT_MARKER_FILE;
        }

        if (importStateMarkerFilePrefix == null || importStateMarkerFilePrefix.length() == 0)
        {
            importStateMarkerFilePrefix = DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX;
        }

        monitoredDirectories = new HashSet<MonitoredDirectory>();
        if (directories != null)
        {
            for (String fileName : directories.split("\\,"))
            {
                File monitoredDirectory = new File(fileName.trim());
                try
                {
                    monitoredDirectories.add(new MonitoredDirectory(monitoredDirectory, readyToImportMarkerFile, importStateMarkerFilePrefix));
                } catch (RuntimeException e)
                {
                    operationLog.error("Cannot monitor directory " + monitoredDirectory.getAbsolutePath() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void execute()
    {
        if (monitoredDirectories.isEmpty())
        {
            operationLog.warn("No directories to monitor - doing nothing.");
            return;
        }

        if (dropboxDirectory == null || dropboxDirectory.length() == 0)
        {
            operationLog.warn("Dropbox directory not defined - doing nothing.");
            return;
        }

        for (MonitoredDirectory directory : monitoredDirectories)
        {
            directory.perform(new WriteDirectoryNameToDropboxAction(new File(dropboxDirectory)));
        }
    }
}
