/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Test cases for {@link PropertyIOUtils} class.
 *
 * @author Bernd Rinn
 */
public class PropertyIOUtilsTest extends AbstractFileSystemTestCase
{

    @Test
    public void testLoadProperties()
    {
        File propertiesFile = new File(workingDirectory, "p.properties");
        FileUtilities.writeToFile(propertiesFile, "answer = 42\n\n# comment\n  key=4711  ");
        Properties properties = PropertyIOUtils.loadProperties(propertiesFile);

        assertEquals("42", properties.getProperty("answer"));
        assertEquals("4711", properties.getProperty("key"));
    }

    @Test
    public void testLoadInvalidProperties()
    {
        File propertiesFile = new File(workingDirectory, "p.properties");
        FileUtilities.writeToFile(propertiesFile, "answer=42\nquestion");

        try
        {
            PropertyIOUtils.loadProperties(propertiesFile);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Missing '=' in line 2 of properties file '" + propertiesFile
                    + "': question", ex.getMessage());
        }
    }

    @Test
    public void testLoadPropertiesWithEmptyLineWithASpace()
    {
        File propertiesFile = new File(workingDirectory, "p.properties");
        FileUtilities.writeToFile(propertiesFile, "  answer =  42 \n \n");

        Properties properties = PropertyIOUtils.loadProperties(propertiesFile);

        assertEquals("42", properties.getProperty("answer"));
    }

}
