/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.etlserver.IPreRegistrationAction;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class PreRegistrationExecutorTest extends AbstractFileSystemTestCase
{
    IPreRegistrationAction action;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        action =
                PreRegistrationExecutor
                        .create("sourceTest/java/ch/systemsx/cisd/etlserver/utils/test-script.sh");
    }

    @Test
    public void testScriptExecution()
    {
        action.execute("data-set-code", workingDirectory.getAbsolutePath());

        // The test script should have created a file called "data-set-code" in the working
        // directory
        String[] contents = workingDirectory.list(new FilenameFilter()
            {

                @Override
                public boolean accept(File dir, String name)
                {
                    return "data-set-code".equals(name);
                }
            });
        assertEquals(Arrays.toString(workingDirectory.list()), 1, contents.length);
    }
}
