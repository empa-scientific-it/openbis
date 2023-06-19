/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.zip.CRC32;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.MetaDataBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;

/**
 * Abstract super class of packagers which package all files of a data set together with a meta data file.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataSetPackager
{
    public static final String META_DATA_FILE_NAME = "meta-data.tsv";

    private final IHierarchicalContentProvider contentProvider;

    private final DataSetExistenceChecker dataSetExistenceChecker;

    protected AbstractDataSetPackager(IHierarchicalContentProvider contentProvider,
            DataSetExistenceChecker dataSetExistenceChecker)
    {
        this.contentProvider = contentProvider;
        this.dataSetExistenceChecker = dataSetExistenceChecker;
    }

    /**
     * Adds an entry with specified entry path and last modification date filled with data from specified input stream.
     * 
     * @param size Number of bytes.
     * @param checksum Checksum. Can be 0 if {@link #isChecksumNeeded()} return <code>false</code>.
     */
    public abstract void addEntry(String entryPath, long lastModified, long size, long checksum, InputStream in);

    public abstract void addDirectoryEntry(String entryPath);

    /**
     * Returns <code>true</code> if the checksum is needed.
     */
    protected abstract boolean isChecksumNeeded();

    /**
     * Closes the package.
     */
    public abstract void close();

    public void addDataSetTo(String rootPath, AbstractExternalData externalData)
    {
        try
        {
            byte[] bytes = MetaDataBuilder.createMetaData(externalData).getBytes();
            CRC32 checksumCalculator = new CRC32();
            for (byte b : bytes)
            {
                checksumCalculator.update(0xff & b);
            }
            Long checksum = checksumCalculator.getValue();
            addEntry(rootPath + META_DATA_FILE_NAME, System.currentTimeMillis(), Long.valueOf(bytes.length), checksum,
                    new ByteArrayInputStream(bytes));
        } catch (Exception ex)
        {
            throw new RuntimeException(
                    "Couldn't package meta data for data set '" + externalData.getCode() + "'.", ex);
        }
        if (externalData.isContainer() == false
                && dataSetExistenceChecker.dataSetExists(DataSetTranslator.translateToDescription(externalData)) == false)
        {
            throw handleNonExistingDataSet(externalData, null);
        }
        IHierarchicalContent root = null;
        try
        {
            root = contentProvider.asContentWithoutModifyingAccessTimestamp(externalData.getCode());
        } catch (Exception ex)
        {
            throw handleNonExistingDataSet(externalData, ex);
        }
        try
        {
            addTo(rootPath, root.getRootNode());
        } catch (Exception ex)
        {
            throw new RuntimeException("Couldn't package data set '" + externalData.getCode() + "'.", ex);
        } finally
        {
            if (root != null)
            {
                root.close();
            }
        }
    }

    private RuntimeException handleNonExistingDataSet(AbstractExternalData externalData, Exception ex)
    {
        return new RuntimeException("Data set '" + externalData.getCode() + "' does not exist.", ex);
    }

    private void addTo(String newRootPath, IHierarchicalContentNode node)
    {
        File file = node.tryGetFile();
        if (file == null)
        {
            return;
        }
        if (file.exists() == false)
        {
            throw new IllegalStateException("File '" + file + "' doesn't exist.");
        }
        String entryPath = newRootPath + node.getRelativePath();
        if (node.isDirectory() && file.isDirectory())
        {
            List<IHierarchicalContentNode> childNodes = node.getChildNodes();
            if (childNodes.isEmpty())
            {
                addDirectoryEntry(entryPath);
            } else
            {
                for (IHierarchicalContentNode childNode : childNodes)
                {
                    addTo(newRootPath, childNode);
                }
            }
        } else if (file.isFile())
        {
            long size = file.length();
            long checksum = isChecksumNeeded() ? node.getChecksumCRC32() : 0;
            try (InputStream nodeInputStream = node.getInputStream())
            {
                addEntry(entryPath, node.getLastModified(), size, checksum, nodeInputStream);
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        } else
        {
            throw new IllegalStateException("Node '" + node.getRelativePath() + "' is a real folder");
        }
    }
}
