/*
 * Copyright 2014 ETH Zuerich, CISD
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.H5FolderFlags;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.ZipBasedHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.ZipFileIntegrityVerifier;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.ZipDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

import de.schlichtherle.io.rof.SimpleReadOnlyFile;
import de.schlichtherle.util.zip.BasicZipFile;
import de.schlichtherle.util.zip.ZipEntry;

/**
 * @author pkupczyk
 */
public class ZipPackageManager extends AbstractPackageManager
{

    static final String COMPRESS_KEY = "compressing";

    private boolean compress;

    public ZipPackageManager(Properties properties)
    {
        compress = PropertyUtils.getBoolean(properties, COMPRESS_KEY, true);
    }

    @Override
    public String getName(String dataSetCode)
    {
        return dataSetCode + ".zip";
    }

    @Override
    protected AbstractDataSetPackager createPackager(File packageFile, DataSetExistenceChecker existenceChecker)
    {
        return new ZipDataSetPackager(packageFile, compress, getContentProvider(), existenceChecker);
    }

    @Override
    public List<VerificationError> verify(File packageFile)
    {
        return new ZipFileIntegrityVerifier().verify(packageFile);
    }

    @Override
    public Status extract(File packageFile, File toDirectory)
    {
        BasicZipFile zipFile = null;
        FileOutputStream fileOutputStream = null;
        try
        {
            zipFile = new BasicZipFile(new SimpleReadOnlyFile(packageFile), "UTF-8", true, false);
            @SuppressWarnings("unchecked")
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                File outputFile = new File(toDirectory, entry.getName());
                if (entry.isDirectory() == false)
                {
                    if (AbstractDataSetPackager.META_DATA_FILE_NAME.equals(entry.getName()) == false)
                    {
                        outputFile.getParentFile().mkdirs();
                        InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                        fileOutputStream = new FileOutputStream(outputFile);
                        BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream);
                        try
                        {
                            IOUtils.copyLarge(inputStream, outputStream);
                        } finally
                        {
                            IOUtils.closeQuietly(inputStream);
                            IOUtils.closeQuietly(outputStream);
                        }
                    }
                } else
                {
                    if (outputFile.isFile())
                    {
                        throw new EnvironmentFailureException("Could not extract directory '" + outputFile
                                + "' because it exists already as a plain file.");
                    }
                    outputFile.mkdirs();
                }
            }
            return Status.OK;
        } catch (Exception ex)
        {
            return Status.createError(ex.toString());
        } finally
        {
            if (zipFile != null)
            {
                try
                {
                    zipFile.close();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    @Override
    public IHierarchicalContent asHierarchialContent(File packageFile, List<DatasetDescription> dataSets, boolean onlyMetaData)
    {
        List<H5FolderFlags> h5FolderFlags = extractH5FolderFlags(dataSets);
        return new ZipBasedHierarchicalContent(packageFile, h5FolderFlags);
    }

}
