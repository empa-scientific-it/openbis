/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import java.io.File;

import org.testng.AssertJUnit;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RegressionTestCase extends AssertJUnit
{
    private static final File SOURCE_FOLDER = new File("source/java");
    private static final File SOURCE_TEST_FOLDER = new File("sourceTest/java");
    
    private static final class DefaultNormalizer implements INormalizer
    {
        static final INormalizer INSTANCE = new DefaultNormalizer();
        
        public String normalize(String notNormalizedText)
        {
            return notNormalizedText;
        }
    }
    
    protected void assertNormalizedSourceFilesAreEqual(Class<?> clazz)
    {
        assertNormalizedSourceFilesAreEqual(clazz, SOURCE_TEST_FOLDER, SOURCE_FOLDER,
                DefaultNormalizer.INSTANCE);
    }
    
    protected void assertNormalizedSourceFilesAreEqual(Class<?> clazz, File rootFolderOfExpected,
            File rootFolderOfActual)
    {
        assertNormalizedSourceFilesAreEqual(clazz, rootFolderOfExpected, rootFolderOfActual,
                DefaultNormalizer.INSTANCE);
    }
    
    protected void assertNormalizedSourceFilesAreEqual(Class<?> clazz, File rootFolderOfExpected,
            File rootFolderOfActual, INormalizer normalizer)
    {
        String fileName = clazz.getName().replace('.', '/') + ".java";
        String expectedContent = readContent(normalizer, rootFolderOfExpected, fileName + ".expected");
        String actualContent = readContent(normalizer, rootFolderOfActual, fileName);
        assertEquals(expectedContent, actualContent);
    }
    
    private String readContent(INormalizer normalizer, File rootFolder, String fileName)
    {
        return normalizer.normalize(FileUtilities.loadToString(new File(rootFolder, fileName))); 
    }
}
