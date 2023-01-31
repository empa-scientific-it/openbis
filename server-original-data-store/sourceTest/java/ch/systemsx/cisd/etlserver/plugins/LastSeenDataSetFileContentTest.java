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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.LastSeenDataSetFileContent;

/**
 * @author pkupczyk
 */
public class LastSeenDataSetFileContentTest
{

    private TestResources resources = new TestResources(getClass());

    @Test
    public void testReadFromNotExistingFile()
    {
        LastSeenDataSetFileContent content =
                LastSeenDataSetFileContent.readFromFile(resources.getResourceFile("readLastSeenFileThatDoesNotExist"));
        Assert.assertNull(content);
    }

    @Test
    public void testReadFromFileWithFileCreationAsNumber()
    {
        LastSeenDataSetFileContent content =
                LastSeenDataSetFileContent.readFromFile(resources.getResourceFile("readLastSeenFileWithFileCreationAsNumber"));
        Assert.assertEquals(content.getFileCreationTime(), Long.valueOf(1394722160111L));
        Assert.assertNull(content.getLastSeenDataSetCode());
    }

    @Test
    public void testReadFromFileWithFileCreationAsString()
    {
        LastSeenDataSetFileContent content =
                LastSeenDataSetFileContent.readFromFile(resources.getResourceFile("readLastSeenFileWithFileCreationAsString"));
        Assert.assertEquals(content.getFileCreationTime(), Long.valueOf(1394722160222L));
        Assert.assertNull(content.getLastSeenDataSetCode());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Could not read file-creation-time property")
    public void testReadFromFileWithIncorrectFileCreation()
    {
        LastSeenDataSetFileContent.readFromFile(resources.getResourceFile("readLastSeenFileWithIncorrectFileCreation"));
    }

    @Test
    public void testReadFromFileWithLastSeenDataSetCode()
    {
        LastSeenDataSetFileContent content =
                LastSeenDataSetFileContent.readFromFile(resources.getResourceFile("readLastSeenFileWithLastSeenDataSetCode"));
        Assert.assertNull(content.getFileCreationTime());
        Assert.assertEquals(content.getLastSeenDataSetCode(), "TEST_DATA_SET_CODE");
    }

    @Test
    public void testWriteToNotExistingFile()
    {
        File file = resources.getResourceFile("writeLastSeenFile");

        if (file.exists())
        {
            file.delete();
        }

        Assert.assertFalse(file.exists());

        Long fileCreationTime = System.currentTimeMillis();
        String lastSeenDataSetCode = UUID.randomUUID().toString();

        LastSeenDataSetFileContent writtenContent = new LastSeenDataSetFileContent();
        writtenContent.setFileCreationTime(fileCreationTime);
        writtenContent.setLastSeenDataSetCode(lastSeenDataSetCode);
        writtenContent.writeToFile(file);

        LastSeenDataSetFileContent readContent = LastSeenDataSetFileContent.readFromFile(file);
        Assert.assertEquals(readContent.getFileCreationTime(), fileCreationTime);
        Assert.assertEquals(readContent.getLastSeenDataSetCode(), lastSeenDataSetCode);
    }

    @Test
    public void testWriteToExistingFile() throws IOException
    {
        File file = resources.getResourceFile("writeLastSeenFile");

        Long fileCreationTime1 = System.currentTimeMillis();
        String lastSeenDataSetCode1 = UUID.randomUUID().toString();

        LastSeenDataSetFileContent content1 = new LastSeenDataSetFileContent();
        content1.setFileCreationTime(fileCreationTime1);
        content1.setLastSeenDataSetCode(lastSeenDataSetCode1);
        content1.writeToFile(file);

        LastSeenDataSetFileContent readContent1 = LastSeenDataSetFileContent.readFromFile(file);
        Assert.assertEquals(readContent1.getFileCreationTime(), fileCreationTime1);
        Assert.assertEquals(readContent1.getLastSeenDataSetCode(), lastSeenDataSetCode1);

        Long fileCreationTime2 = System.currentTimeMillis();
        String lastSeenDataSetCode2 = UUID.randomUUID().toString();

        LastSeenDataSetFileContent content2 = new LastSeenDataSetFileContent();
        content2.setFileCreationTime(fileCreationTime2);
        content2.setLastSeenDataSetCode(lastSeenDataSetCode2);
        content2.writeToFile(file);

        LastSeenDataSetFileContent readContent2 = LastSeenDataSetFileContent.readFromFile(file);
        Assert.assertEquals(readContent2.getFileCreationTime(), fileCreationTime2);
        Assert.assertEquals(readContent2.getLastSeenDataSetCode(), lastSeenDataSetCode2);
    }

}
