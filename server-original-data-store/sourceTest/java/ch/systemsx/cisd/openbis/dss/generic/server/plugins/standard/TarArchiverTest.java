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
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.tar.Untar;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = { AbstractArchiverProcessingPlugin.class, RsyncArchiver.class })
@Test
public class TarArchiverTest extends AbstractPackageArchiverTest
{

    @Override
    protected IArchiverPlugin createArchiver()
    {
        TarArchiver archiver = new TarArchiver(properties, store);
        archiver.statusUpdater = statusUpdater;
        return archiver;
    }

    @Override
    protected String getPackageExtension()
    {
        return ".tar";
    }

    @Override
    protected void assertPackageFileContent(final File expectedContent, final File packageFile, String path, boolean compressed)
    {
        try
        {
            assertPackageFileContent(FileUtils.readFileToString(expectedContent), packageFile, path, compressed);
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    @Override
    protected void assertPackageFileContent(final String expectedContent, File packageFile, final String path, boolean compressed)
    {
        assertFileFromPackage(packageFile, path, new IAssertAction()
            {

                @Override
                public void assertFileFromPackage(File fileFromPackage) throws Exception
                {
                    assertTrue(fileFromPackage.exists());
                    if (fileFromPackage.isDirectory())
                    {
                        fail("Directory path: " + path);
                    }
                    assertEquals(expectedContent, FileUtils.readFileToString(fileFromPackage));
                }
            });
    }

    @Override
    protected void assertPackageDirectory(File packageFile, final String path)
    {
        assertFileFromPackage(packageFile, path, new IAssertAction()
            {
                @Override
                public void assertFileFromPackage(File fileFromPackage)
                {
                    assertTrue("Not a directory entry: " + path, fileFromPackage.isDirectory());
                }
            });
    }

    private void assertFileFromPackage(File packageFile, String path, IAssertAction assertAction)
    {
        Untar untar = null;
        File extractTo = null;

        try
        {
            File temp = new File(System.getProperty("java.io.tmpdir"));
            extractTo = new File(temp, UUID.randomUUID().toString());

            untar = new Untar(packageFile);
            untar.extract(extractTo);

            File fileFromPackage = new File(extractTo, path);

            assertAction.assertFileFromPackage(fileFromPackage);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (untar != null)
            {
                try
                {
                    untar.close();
                } catch (IOException e)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(e);
                }
            }
            if (extractTo != null)
            {
                FileUtilities.deleteRecursively(extractTo);
            }
        }
    }

    private static interface IAssertAction
    {
        public void assertFileFromPackage(File fileFromPackage) throws Exception;
    }
}
