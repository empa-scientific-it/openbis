/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * HDF5 aware implementation of {@link IHierarchicalContentFactory} using file system as source of information.
 * 
 * @author anttil
 */
public class Hdf5AwareHierarchicalContentFactory implements IHierarchicalContentFactory
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            Hdf5AwareHierarchicalContentFactory.class);

    private H5FolderChecker folderChecker;

    public Hdf5AwareHierarchicalContentFactory(boolean h5Folders, boolean h5arFolders)
    {
        this(Arrays.asList(new H5FolderFlags("", h5Folders, h5arFolders)));
    }
    
    public Hdf5AwareHierarchicalContentFactory(List<H5FolderFlags> h5FolderFlags)
    {
        folderChecker = new H5FolderChecker(h5FolderFlags);
    }
    
    @Override
    public IHierarchicalContent asVirtualHierarchicalContent(
            List<IHierarchicalContent> components)
    {
        return new VirtualHierarchicalContent(components);
    }

    @Override
    public IHierarchicalContent asHierarchicalContent(File file, IDelegatedAction onCloseAction)
    {
        return new DefaultFileBasedHierarchicalContent(this, file, onCloseAction);
    }

    @Override
    public IHierarchicalContentNode asHierarchicalContentNode(IHierarchicalContent rootContent,
            File file)
    {
        if (handleHdf5AsFolder(rootContent, file))
        {
            try
            {
                HDF5ContainerBasedHierarchicalContentNode node = new HDF5ContainerBasedHierarchicalContentNode(rootContent, file);
                if (node.isFileAbstractionOk())
                {
                    return node;
                }
            } catch (Exception e)
            {
                operationLog.warn("File " + file + " can not be opened as HDF5 container: " + e);
            }
        }
        return new DefaultFileBasedHierarchicalContentNode(this, rootContent, file);
    }
    
    private boolean handleHdf5AsFolder(IHierarchicalContent rootContent, File file)
    {
        if (FileUtilities.isHDF5ContainerFile(file) == false)
        {
            return false;
        }
        String filename = file.getName();
        if (folderChecker.hasOnlyDefaults() == false)
        {
            IHierarchicalContentNode rootNode = rootContent.getRootNode();
            if (rootNode != null)
            {
                File rootFile = rootNode.tryGetFile();
                if (rootFile != null)
                {
                    String relativeFilePath = FileUtilities.getRelativeFilePath(rootFile, file);
                    return folderChecker.handleHdf5AsFolder(relativeFilePath);
                }
            }
        }
        return folderChecker.handleHdf5AsFolderByDefault(filename);
    }
}
