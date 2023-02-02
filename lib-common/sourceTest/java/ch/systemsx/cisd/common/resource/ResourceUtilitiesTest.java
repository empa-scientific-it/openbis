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

package ch.systemsx.cisd.common.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.utilities.ResourceUtilities;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Test cases for the {@link FileUtilities}.
 * 
 * @author Bernd Rinn
 */
public final class ResourceUtilitiesTest extends AbstractFileSystemTestCase
{

    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testCopyResourceToTempFileIllegalResource()
    {
        ResourceUtilities.copyResourceToTempFile("nonexistent", "pre", "post");
    }

    @Test
    public final void testCopyResourceToTempFile()
    {
        final String resourceName =
                "/" + FileUtilities.class.getCanonicalName().replaceAll("\\.", "/") + ".class";
        final String absoluteTempFileName =
                ResourceUtilities.copyResourceToTempFile(resourceName, "pre", "post");
        assertNotNull(absoluteTempFileName);
        final File tempFile = new File(absoluteTempFileName);
        final String tempFileName = tempFile.getName();
        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
        assertTrue(tempFileName.startsWith("pre"));
        assertTrue(tempFileName.endsWith("post"));
        assertTrue(Arrays.equals(resourceToByteArray(resourceName),
                fileToByteArray(absoluteTempFileName)));

    }

    private byte[] resourceToByteArray(String resourcename)
    {
        final InputStream is = ResourceUtilitiesTest.class.getResourceAsStream(resourcename);
        if (is == null)
        {
            return null;
        }
        try
        {
            return IOUtils.toByteArray(is);
        } catch (IOException ex)
        {
            return null;
        } finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    private byte[] fileToByteArray(String filename)
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(filename);
            return IOUtils.toByteArray(is);
        } catch (IOException ex)
        {
            return null;
        } finally
        {
            IOUtils.closeQuietly(is);
        }

    }

}
