/*
 * Copyright 2014 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.etlserver.plugins.FileSystemMonitoringMaintenanceTask.DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX;
import static ch.systemsx.cisd.etlserver.plugins.FileSystemMonitoringMaintenanceTask.DEFAULT_READY_TO_IMPORT_MARKER_FILE;
import static ch.systemsx.cisd.etlserver.plugins.FileSystemMonitoringMaintenanceTask.DROPBOX_DIRECTORY;
import static ch.systemsx.cisd.etlserver.plugins.FileSystemMonitoringMaintenanceTask.IMPORT_STATE_MARKER_FILE_PREFIX;
import static ch.systemsx.cisd.etlserver.plugins.FileSystemMonitoringMaintenanceTask.MONITORED_DIRECTORIES;
import static ch.systemsx.cisd.etlserver.plugins.FileSystemMonitoringMaintenanceTask.READY_TO_IMPORT_MARKER_FILE;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author pkupczyk
 */
public class FileSystemMonitoringMaintenanceTaskTest
{

    private File monitoredDirectory1;

    private File monitoredDirectory2;

    private File dropboxDirectory;

    @BeforeMethod
    private void beforeMethod()
    {
        monitoredDirectory1 = createDirectory("monitored-directory-1");
        monitoredDirectory2 = createDirectory("monitored-directory-2");
        dropboxDirectory = createDirectory("dropbox-directory");
    }

    @AfterMethod
    private void afterMethod()
    {
        deleteDirectory(monitoredDirectory1);
        deleteDirectory(monitoredDirectory2);
        deleteDirectory(dropboxDirectory);
    }

    @Test
    public void testExecuteWithoutReadyToImportMarkerFile() throws IOException
    {
        File experimentDirectory = new File(monitoredDirectory1, "TEST-EXPERIMENT");

        experimentDirectory.mkdirs();

        execute();

        assertDirectoryEmpty(experimentDirectory);
        assertDirectoryEmpty(dropboxDirectory);
    }

    @Test
    public void testExecuteWithExistingMarkerFileForCorrectState() throws IOException
    {
        File experimentDirectory = new File(monitoredDirectory1, "TEST-EXPERIMENT");
        File readyMarkerFile = new File(experimentDirectory, DEFAULT_READY_TO_IMPORT_MARKER_FILE);
        File stateMarkerFile = new File(experimentDirectory, DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX + "running");

        experimentDirectory.mkdirs();
        readyMarkerFile.createNewFile();
        stateMarkerFile.createNewFile();

        execute();

        assertDirectoryFileNames(experimentDirectory, DEFAULT_READY_TO_IMPORT_MARKER_FILE, DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX + "running");
        assertDirectoryEmpty(dropboxDirectory);
    }

    @Test
    public void testExecuteWithExistingMarkerFileForIncorrectState() throws IOException
    {
        File experimentDirectory = new File(monitoredDirectory1, "TEST-EXPERIMENT");
        File readyMarkerFile = new File(experimentDirectory, DEFAULT_READY_TO_IMPORT_MARKER_FILE);
        File stateMarkerFile = new File(experimentDirectory, DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX + "some-incorrect-state");

        experimentDirectory.mkdirs();
        readyMarkerFile.createNewFile();
        stateMarkerFile.createNewFile();

        execute();

        assertDirectoryFileNames(experimentDirectory, DEFAULT_READY_TO_IMPORT_MARKER_FILE, DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX
                + "queued");
        assertDirectoryFileCount(dropboxDirectory, 1);
        assertDirectoryContainsFileWithContent(dropboxDirectory, experimentDirectory.getAbsolutePath());
    }

    @Test
    public void testExecuteWithDefaultMarkerFileNames() throws IOException
    {
        File experimentDirectory = new File(monitoredDirectory1, "TEST-EXPERIMENT");
        File readyMarkerFile = new File(experimentDirectory, DEFAULT_READY_TO_IMPORT_MARKER_FILE);

        experimentDirectory.mkdirs();
        readyMarkerFile.createNewFile();

        execute();

        assertDirectoryFileNames(experimentDirectory, DEFAULT_READY_TO_IMPORT_MARKER_FILE, DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX + "queued");
        assertDirectoryFileCount(dropboxDirectory, 1);
        assertDirectoryContainsFileWithContent(dropboxDirectory, experimentDirectory.getAbsolutePath());
    }

    @Test
    public void testExecuteWithCustomMarkerFileNames() throws IOException
    {
        File experimentDirectory = new File(monitoredDirectory1, "TEST-EXPERIMENT");
        File readyMarkerFile = new File(experimentDirectory, ".i-am-ready-please-import-me");

        experimentDirectory.mkdirs();
        readyMarkerFile.createNewFile();

        execute(".i-am-ready-please-import-me", ".the-import-is-now-", monitoredDirectory1.getAbsolutePath(), dropboxDirectory.getAbsolutePath());

        assertDirectoryFileNames(experimentDirectory, ".i-am-ready-please-import-me", ".the-import-is-now-queued");
        assertDirectoryFileCount(dropboxDirectory, 1);
        assertDirectoryContainsFileWithContent(dropboxDirectory, experimentDirectory.getAbsolutePath());
    }

    @Test
    public void testExecuteWithMultipleMonitoredDirectories() throws IOException
    {
        File experimentDirectory1 = new File(monitoredDirectory1, "TEST-EXPERIMENT-1");
        File experimentDirectory2 = new File(monitoredDirectory2, "TEST-EXPERIMENT-2");
        File experimentDirectory3 = new File(monitoredDirectory2, "TEST-EXPERIMENT-3");

        experimentDirectory1.mkdirs();
        experimentDirectory2.mkdirs();
        experimentDirectory3.mkdirs();

        new File(experimentDirectory1, DEFAULT_READY_TO_IMPORT_MARKER_FILE).createNewFile();
        new File(experimentDirectory3, DEFAULT_READY_TO_IMPORT_MARKER_FILE).createNewFile();

        execute();

        assertDirectoryFileNames(experimentDirectory1, DEFAULT_READY_TO_IMPORT_MARKER_FILE, DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX + "queued");
        assertDirectoryEmpty(experimentDirectory2);
        assertDirectoryFileNames(experimentDirectory3, DEFAULT_READY_TO_IMPORT_MARKER_FILE, DEFAULT_IMPORT_STATE_MARKER_FILE_PREFIX + "queued");
        assertDirectoryFileCount(dropboxDirectory, 2);
        assertDirectoryContainsFileWithContent(dropboxDirectory, experimentDirectory1.getAbsolutePath());
        assertDirectoryContainsFileWithContent(dropboxDirectory, experimentDirectory3.getAbsolutePath());
    }

    private void execute()
    {
        execute(null, null, monitoredDirectory1.getAbsolutePath() + "," + monitoredDirectory2.getAbsolutePath(), dropboxDirectory.getAbsolutePath());
    }

    private void execute(String readyToImportMarkerFile, String importStateMarkerFilePrefix, String monitoredDirectoriesPaths,
            String dropboxDirectoryPath)
    {
        FileSystemMonitoringMaintenanceTask task = new FileSystemMonitoringMaintenanceTask();

        Properties properties = new Properties();

        if (readyToImportMarkerFile != null)
        {
            properties.setProperty(READY_TO_IMPORT_MARKER_FILE, readyToImportMarkerFile);
        }
        if (importStateMarkerFilePrefix != null)
        {
            properties.setProperty(IMPORT_STATE_MARKER_FILE_PREFIX, importStateMarkerFilePrefix);
        }

        properties.setProperty(MONITORED_DIRECTORIES, monitoredDirectoriesPaths);
        properties.setProperty(DROPBOX_DIRECTORY, dropboxDirectoryPath);

        task.setUp("file-system-monitoring-test", properties);
        task.execute();
    }

    private File createDirectory(String directoryName)
    {
        File testDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + getClass().getName());
        File directory = new File(testDirectory, directoryName);

        if (directory.exists())
        {
            FileUtilities.deleteRecursively(directory);
        }

        directory.mkdirs();
        return directory;
    }

    private void deleteDirectory(File directory)
    {
        FileUtilities.deleteRecursively(directory);
    }

    private static void assertDirectoryEmpty(final File directory)
    {
        assertDirectoryFileCount(directory, 0);
    }

    private static void assertDirectoryFileCount(final File directory, final int expectedFileCount)
    {
        Assert.assertEquals(directory.listFiles().length, expectedFileCount);
    }

    private static void assertDirectoryFileNames(final File directory, String... expectedFileNames)
    {
        String[] fileNames = directory.list();

        Arrays.sort(fileNames);
        Arrays.sort(expectedFileNames);

        Assert.assertEquals(fileNames, expectedFileNames);
    }

    private static void assertDirectoryContainsFileWithContent(final File directory, String expectedContent) throws IOException
    {
        for (File file : directory.listFiles())
        {
            String content = FileUtils.readFileToString(file);
            if (content.equals(expectedContent))
            {
                return;
            }
        }
        Assert.fail();
    }
}
