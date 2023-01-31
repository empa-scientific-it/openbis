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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipOutputStream;

/**
 * Packager based on a {@link java.util.zip.ZipOutputStream}.
 *
 * @author Franz-Josef Elmer
 */
public class ZipDataSetPackager extends AbstractDataSetPackager
{
    private final File zipFile;

    private final boolean compress;

    private ZipOutputStream zipOutputStream;

    public ZipDataSetPackager(File zipFile, boolean compress,
            IHierarchicalContentProvider contentProvider, DataSetExistenceChecker dataSetExistenceChecker)
    {
        super(contentProvider, dataSetExistenceChecker);
        this.zipFile = zipFile;
        this.compress = compress;
    }

    @Override
    protected boolean isChecksumNeeded()
    {
        return compress == false;
    }

    @Override
    public void addEntry(String entryPath, long lastModified, long size, long checksum, InputStream in)
    {
        ZipOutputStream zos = getZipOutputStream();
        try
        {
            ZipEntry zipEntry = new ZipEntry(entryPath.replace('\\', '/'));
            zipEntry.setTime(lastModified);
            zipEntry.setMethod(compress ? ZipEntry.DEFLATED : ZipEntry.STORED);
            if (compress == false)
            {
                zipEntry.setSize(size);
                zipEntry.setCompressedSize(size);
                zipEntry.setCrc(0xffffffffL & checksum);
            }
            zos.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0)
            {
                zos.write(buffer, 0, len);
            }
        } catch (IOException ex)
        {
            throw new RuntimeException("Error while adding entry " + entryPath, ex);
        } finally
        {
            IOUtils.closeQuietly(in);
            try
            {
                zos.closeEntry();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public void addDirectoryEntry(String entryPath)
    {
        ZipOutputStream zos = getZipOutputStream();
        try
        {
            String path = entryPath.replace('\\', '/');
            if (path.endsWith("/") == false)
            {
                path += "/";
            }
            ZipEntry zipEntry = new ZipEntry(path);
            zos.putNextEntry(zipEntry);
        } catch (IOException ex)
        {
            throw new RuntimeException("Error while adding entry " + entryPath, ex);
        } finally
        {
            try
            {
                zos.closeEntry();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public void close()
    {
        if (zipOutputStream != null)
        {
            try
            {
                zipOutputStream.close();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    private ZipOutputStream getZipOutputStream()
    {
        if (zipOutputStream == null)
        {
            FileOutputStream outputStream = null;
            try
            {
                outputStream = new FileOutputStream(zipFile);
                zipOutputStream = new ZipOutputStream(outputStream);
            } catch (Exception ex)
            {
                IOUtils.closeQuietly(outputStream);
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        return zipOutputStream;
    }

}
