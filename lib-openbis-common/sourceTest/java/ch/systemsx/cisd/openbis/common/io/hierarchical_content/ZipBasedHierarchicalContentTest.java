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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipOutputStream;

/**
 * @author Franz-Josef Elmer
 */
public class ZipBasedHierarchicalContentTest extends AbstractPackageBasedHierarchicalContentTest
{

    @Override
    protected void init()
    {
        removeUnzippedFiles();
    }

    @Override
    protected IHierarchicalContent createPackage(File packageFile, File dataDir)
    {
        zip(packageFile, dataDir);
        List<H5FolderFlags> h5FolderFlags = Arrays.asList(new H5FolderFlags("", true, true));
        return new ZipBasedHierarchicalContent(packageFile, h5FolderFlags);
    }

    @Override
    protected void assertDataInPackage(IHierarchicalContent content) throws Exception
    {
        super.assertDataInPackage(content);

        IHierarchicalContentNode hdf5Node = content.getNode("my-container.h5");

        File[] tempFiles = getUnzippedFiles();
        for (File tempFile : tempFiles)
        {
            assertEquals(0, tempFile.length());
        }
        assertEquals(1, tempFiles.length);

        // now the file is lazy created
        assertEquals(hdf5Node.getFileLength(), hdf5Node.getFile().length());
        for (File tempFile : tempFiles)
        {
            assertEquals(hdf5Node.getFile(), tempFile);
            assertEquals(537641, tempFile.length());
        }

        HDF5ContainerBasedHierarchicalContentNodeTest.assertH5ExampleContent(hdf5Node);

        content.close();
        assertEquals("[]", Arrays.asList(getUnzippedFiles()).toString());
    }

    private void zip(File zipFile, File folder)
    {
        OutputStream outputStream = null;
        ZipOutputStream zipOutputStream = null;
        try
        {
            outputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(outputStream);
            zip(zipOutputStream, folder, folder);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (zipOutputStream != null)
            {
                try
                {
                    zipOutputStream.close();
                } catch (Exception ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            } else
            {
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    private void zip(ZipOutputStream zipOutputStream, File rootFile, File file)
    {
        if (file.isFile())
        {
            zipTo(zipOutputStream, rootFile, file);
        } else if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files.length == 0)
            {
                try
                {
                    String path = FileUtilities.getRelativeFilePath(rootFile, file).replace('\\', '/');
                    if (path.endsWith("/") == false)
                    {
                        path += "/";
                    }
                    ZipEntry entry = new ZipEntry(path);
                    entry.setTime(file.lastModified());
                    zipOutputStream.putNextEntry(entry);
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                } finally
                {
                    try
                    {
                        zipOutputStream.closeEntry();
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            } else
            {
                Arrays.sort(files);
                for (File childFile : files)
                {
                    zip(zipOutputStream, rootFile, childFile);
                }
            }
        }
    }

    private void zipTo(ZipOutputStream zipOutputStream, File rootFile, File file)
    {
        long lastModified = file.lastModified();
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(FileUtilities.getRelativeFilePath(rootFile, file).replace('\\', '/'));
            zipEntry.setTime(lastModified);
            zipEntry.setMethod(ZipEntry.DEFLATED);
            zipOutputStream.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0)
            {
                zipOutputStream.write(buffer, 0, len);
            }
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(in);
            try
            {
                zipOutputStream.closeEntry();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    public static File[] getUnzippedFiles()
    {
        File[] tempFiles = ZipBasedHierarchicalContent.TEMP_FOLDER.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File file, String name)
                {
                    return name.startsWith(ZipBasedHierarchicalContent.TEMP_FILE_PREFIX);
                }
            });
        return tempFiles;
    }

    public static void removeUnzippedFiles()
    {
        File[] unzippedFiles = getUnzippedFiles();
        for (File unzippedFile : unzippedFiles)
        {
            FileUtilities.delete(unzippedFile);
        }
    }

}
