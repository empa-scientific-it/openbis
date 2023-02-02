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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.tar.Untar;
import ch.systemsx.cisd.common.io.MonitoredIOStreamCopier;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.openbis.common.io.HierarchicalContentNodeBasedHierarchicalContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author pkupczyk
 */
public class TarBasedHierarchicalContent extends AbstractHierarchicalContent
{

    private final File packageFile;

    private File extractTo;

    private final File tempFolder;

    private final ISimpleLogger ioSpeedLoggerOrNull;

    private final int bufferSize;

    private final List<H5FolderFlags> h5FolderFlags;

    public TarBasedHierarchicalContent(File packageFile, List<H5FolderFlags> h5FolderFlags, 
            File tempFolder, int bufferSize, ISimpleLogger ioSpeedLoggerOrNull)
    {
        this.packageFile = packageFile;
        this.h5FolderFlags = h5FolderFlags;
        this.tempFolder = tempFolder;
        this.bufferSize = bufferSize;
        this.ioSpeedLoggerOrNull = ioSpeedLoggerOrNull;
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        maybeExtract();

        IHierarchicalContentNode root = getFileBasedHierarchicalContent().getRootNode();
        return new HierarchicalContentNodeBasedHierarchicalContentNode(root)
            {
                @Override
                public String getName()
                {
                    return "";
                }

                @Override
                public String getParentRelativePath()
                {
                    return null;
                }
            };
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {
        maybeExtract();
        return getFileBasedHierarchicalContent().getNode(relativePath);
    }

    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        maybeExtract();
        return getFileBasedHierarchicalContent().tryGetNode(relativePath);
    }

    @Override
    public void close()
    {
        if (extractTo != null && extractTo.exists())
        {
            FileUtilities.deleteRecursively(extractTo);
        }
    }

    private File getTempFolder()
    {

        if (tempFolder != null)
        {
            return tempFolder;
        } else
        {
            return new File(System.getProperty("java.io.tmpdir"));
        }
    }

    private void maybeExtract()
    {
        if (extractTo == null)
        {
            Untar untar = null;

            try
            {
                File temp = getTempFolder();
                extractTo = new File(temp, UUID.randomUUID().toString());
                extractTo.mkdirs();

                MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier(bufferSize);
                copier.setLogger(ioSpeedLoggerOrNull);
                untar = new Untar(packageFile, copier);
                untar.extract(extractTo);
            } catch (Exception e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
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
            }
        }
    }

    private IHierarchicalContent getFileBasedHierarchicalContent()
    {
        return new Hdf5AwareHierarchicalContentFactory(h5FolderFlags).asHierarchicalContent(extractTo, null);
    }

}
