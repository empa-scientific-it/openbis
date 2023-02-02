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

package ch.systemsx.cisd.common.properties;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.IExitHandler;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = PropertiesFileMerger.class)
public class PropertiesFileMergerTest extends AbstractFileSystemTestCase
{
    private static final class MockExitHandler implements IExitHandler
    {
        Integer exitCode;

        @Override
        public void exit(int code)
        {
            this.exitCode = code;
        }
    }

    @Test
    public void testNoOverloadingPropertyFiles()
    {
        File propertiesFile = new File(workingDirectory, "prop.properties");
        String content = "# hello world\n"
                + "\n"
                + "a= alpha\n"
                + "b =beta\n";
        FileUtilities.writeToFile(propertiesFile, content);
        MockExitHandler exitHandler = new MockExitHandler();

        PropertiesFileMerger.main(new String[]
        { propertiesFile.getPath() }, exitHandler);

        assertEquals(null, exitHandler.exitCode);
        assertEquals(content, FileUtilities.loadToString(propertiesFile));
    }

    @Test
    public void testMergingWithTwoOverloadingPropertyFiles()
    {
        File propertiesFile = new File(workingDirectory, "prop.properties");
        FileUtilities.writeToFile(propertiesFile, "# hello world\n"
                + "\n"
                + "a= alpha\n"
                + "b =beta");
        File prop1 = new File(workingDirectory, "prop1.properties");
        FileUtilities.writeToFile(prop1, "  a=a  \n"
                + "# new section\n"
                + "\n"
                + "c=gamma");
        File prop2 = new File(workingDirectory, "prop2.properties");
        FileUtilities.writeToFile(prop2, "b   =  b\n"
                + "# 2. new section\n"
                + "a = A\n"
                + "  d =    delta  ");
        MockExitHandler exitHandler = new MockExitHandler();

        PropertiesFileMerger.main(new String[]
        { propertiesFile.getPath(), prop1.getPath(), prop2.getPath() }, exitHandler);

        assertEquals(null, exitHandler.exitCode);
        assertEquals("# hello world\n"
                + "\n"
                + "a = A\n"
                + "b = b\n"
                + "# new section\n"
                + "\n"
                + "c = gamma\n"
                + "# 2. new section\nd = delta\n",
                FileUtilities.loadToString(propertiesFile));
    }

    @Test
    public void testMergingWithTwoOverloadingPropertyFilesWithMultilineProperty() throws Exception
    {
        File propertiesFile = new File(workingDirectory, "prop.properties");
        String content = "# hello world\n"
                + "\n"
                + "a= alpha\n"
                + "b =beta";
        FileUtilities.writeToFile(propertiesFile, content);
        File prop1 = new File(workingDirectory, "prop1.properties");
        String content1 = "  a=a  \n"
                + "# new section\n"
                + "\n"
                + "c=gamma";
        FileUtilities.writeToFile(prop1, content1);
        File prop2 = new File(workingDirectory, "prop2.properties");
        String content2 = "b   =  b\n"
                + "# 2. new section\n"
                + "a = A\n"
                + "  d =    \\\n"
                + "  delta\n";
        FileUtilities.writeToFile(prop2, content2);
        MockExitHandler exitHandler = new MockExitHandler();

        PropertiesFileMerger.main(new String[]
        { propertiesFile.getPath(), prop1.getPath(), prop2.getPath() }, exitHandler);

        assertEquals(null, exitHandler.exitCode);
        assertEquals(content + "\n" + content1 + "\n" + content2,
                FileUtilities.loadToString(propertiesFile));
        FileInputStream stream = null;
        try
        {
            stream = new FileInputStream(propertiesFile);
            Properties properties = new Properties();
            properties.load(stream);
            assertEquals("A", properties.getProperty("a"));
            assertEquals("b", properties.getProperty("b"));
            assertEquals("gamma", properties.getProperty("c"));
            assertEquals("delta", properties.getProperty("d"));
        } finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

}
