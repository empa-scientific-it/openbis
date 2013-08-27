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

package ch.systemsx.cisd.openbis.common.controlfile;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.TestResources;

/**
 * @author pkupczyk
 */
public class ControlFileReaderTest
{

    @BeforeMethod
    public void beforeMethod()
    {
        deleteControlFileDirectory();
        getControlFileDirectory().mkdirs();
    }

    @AfterTest
    public void afterTest()
    {
        deleteControlFileDirectory();
    }

    @Test
    public void testSwitchOnAndOff() throws IOException
    {
        ControlFileReader reader = new ControlFileReader(getControlFileDirectory(), -1);
        Assert.assertFalse(reader.isLogServiceCallStartEnabled());

        new File(getControlFileDirectory(), "log-service-call-start-on").createNewFile();
        Assert.assertTrue(reader.isLogServiceCallStartEnabled());

        new File(getControlFileDirectory(), "log-service-call-start-off").createNewFile();
        Assert.assertFalse(reader.isLogServiceCallStartEnabled());
    }

    @Test
    public void testWithNotExistingControlFileDirectory() throws IOException
    {
        ControlFileReader reader = new ControlFileReader(new File(getControlFileDirectory(), "notExisting"), -1);
        Assert.assertFalse(reader.isLogServiceCallStartEnabled());

        new File(getControlFileDirectory(), "log-service-call-start-on").createNewFile();
        Assert.assertFalse(reader.isLogServiceCallStartEnabled());

        new File(getControlFileDirectory(), "log-service-call-start-off").createNewFile();
        Assert.assertFalse(reader.isLogServiceCallStartEnabled());
    }

    private File getControlFileDirectory()
    {
        TestResources resources = new TestResources(getClass());
        return resources.getResourcesDirectory();
    }

    private void deleteControlFileDirectory()
    {
        File directory = getControlFileDirectory();
        if (directory.exists())
        {
            FileUtilities.deleteRecursively(directory);
        }
    }

}
