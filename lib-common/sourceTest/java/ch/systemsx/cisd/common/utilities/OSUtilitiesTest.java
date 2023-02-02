/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for {@link OSUtilities}.
 * 
 * @author Bernd Rinn
 */
public class OSUtilitiesTest
{

    private static final File workingDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    @BeforeSuite
    public void init()
    {
        LogInitializer.init();
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
    }

    @Test
    public void findExecutableTest() throws IOException
    {
        final File root = new File(workingDirectory, "find-executable-test");
        FileUtilities.deleteRecursively(root);
        root.mkdir();
        assert root.isDirectory();
        final File dir1 = new File(root, "dir1");
        dir1.mkdir();
        assert dir1.isDirectory();
        final File dir2 = new File(root, "dir2");
        dir2.mkdir();
        assert dir2.isDirectory();
        final Set<String> pathSet = new LinkedHashSet<String>();
        pathSet.add(dir1.getPath());
        pathSet.add(dir2.getPath());
        final String executableName = OSUtilities.isUnix() ? "myexecutable" : "myexecutable.exe";
        final File executable1 = new File(dir1, executableName);
        assert executable1.createNewFile();
        final File executable2 = new File(dir2, executableName);
        assert executable2.createNewFile();
        assertEquals(executable1, OSUtilities.findExecutable(executableName, pathSet));
        FileUtilities.deleteRecursively(root);
    }

}
