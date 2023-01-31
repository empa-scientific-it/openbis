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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

/**
 * @author pkupczyk
 */
public abstract class GenericDropboxSystemTest extends GenericSystemTest
{

    private static final FileFilter SVN_FILTER = new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().equals(".svn") == false;
            }
        };

    protected abstract String getDropboxName();

    protected abstract String getDropboxIncomingDirectoryName();

    protected void importData(String dataDirectoryName) throws Exception
    {
        File dataDirectory = new File("resource/test-data/" + getClass().getSimpleName() + "/" + dataDirectoryName);
        File destinationDirectory = new File(getIncomingDirectory(), dataDirectory.getName());
        FileUtils.copyDirectory(dataDirectory, destinationDirectory, SVN_FILTER);
    }

    protected void importDataWithMarker(String dataDirectoryName) throws Exception
    {
        importData(dataDirectoryName);
        File markerFile = new File(getIncomingDirectory(), ".MARKER_is_finished_" + dataDirectoryName);
        markerFile.createNewFile();
    }

    protected void waitUntilDataImported() throws Exception
    {
        waitUntilDataImported(120);
    }

    protected void waitUntilDataImportFails(String errorMessage) throws Exception
    {
        waitUntilDataImportFails(120, errorMessage);
    }

    protected void waitUntilDataImported(int maxWaitDurationInSeconds) throws Exception
    {
        waitUntil(new DropBoxSuccessfullyFinishedCondition(getDropboxName()), maxWaitDurationInSeconds);
    }

    protected void waitUntilDataImportFails(int maxWaitDurationInSeconds, String errorMessage) throws Exception
    {
        waitUntil(new DropBoxFailsCondition(getDropboxName(), errorMessage), maxWaitDurationInSeconds);
    }

    @Override
    protected Level getLogLevel()
    {
        return Level.DEBUG;
    }

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, getDropboxIncomingDirectoryName());
    }

}
