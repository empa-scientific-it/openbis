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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.ZipBasedHierarchicalContentTest;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = { AbstractArchiverProcessingPlugin.class, RsyncArchiver.class })
@Test
public class ZipArchiverTest extends AbstractPackageArchiverTest
{

    @Override
    protected void init()
    {
        ZipBasedHierarchicalContentTest.removeUnzippedFiles();
    }

    @Override
    protected IArchiverPlugin createArchiver()
    {
        ZipArchiver archiver = new ZipArchiver(properties, store);
        archiver.statusUpdater = statusUpdater;
        return archiver;
    }

    @Override
    protected String getPackageExtension()
    {
        return ".zip";
    }

    @Override
    protected void assertPackageFileContent(File expectedContent, File file, String path, boolean compressed)
    {
        try
        {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry(path);
            assertNotNull("No entry for " + path, entry);
            if (entry.isDirectory())
            {
                fail("Directory path: " + path);
            }
            assertEquals(compressed ? ZipEntry.DEFLATED : ZipEntry.STORED, entry.getMethod());
            assertEquals(IOUtils.toByteArray(new FileInputStream(expectedContent)),
                    IOUtils.toByteArray(zipFile.getInputStream(entry)));
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    protected void assertPackageFileContent(String expectedContent, File packageFile, String path, boolean compressed)
    {
        try
        {
            ZipFile zipFile = new ZipFile(packageFile);
            ZipEntry entry = zipFile.getEntry(path);
            assertNotNull("No entry for " + path, entry);
            if (entry.isDirectory())
            {
                fail("Directory path: " + path);
            }
            assertEquals(compressed ? ZipEntry.DEFLATED : ZipEntry.STORED, entry.getMethod());
            assertEquals(expectedContent, IOUtils.toString(zipFile.getInputStream(entry)));
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    protected void assertPackageDirectory(File file, String path)
    {
        try
        {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry(path);
            assertNotNull("No entry for " + path, entry);
            assertTrue("Not a directory entry: " + path, entry.isDirectory());
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
