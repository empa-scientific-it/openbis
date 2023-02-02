/*
 * Copyright 2017 ETH Zuerich, SIS
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class H5FolderChecker
{
    private boolean h5FoldersDefault;
    private boolean h5arFoldersDefault;
    private Map<String, H5FolderFlags> h5FolderFlagsByTreeRoot = new HashMap<>();

    public H5FolderChecker(List<H5FolderFlags> h5FolderFlags)
    {
        if (h5FolderFlags.size() == 1)
        {
            h5FoldersDefault = h5FolderFlags.get(0).isH5Folders();
            h5arFoldersDefault = h5FolderFlags.get(0).isH5arFolders();
        }
        for (H5FolderFlags flags : h5FolderFlags)
        {
            h5FolderFlagsByTreeRoot.put(flags.getTreeRoot(), flags);
        }
    }
    
    public boolean hasOnlyDefaults()
    {
        return h5FolderFlagsByTreeRoot.isEmpty();
    }
    
    public boolean handleHdf5AsFolder(String relativeFilePath)
    {
        if (relativeFilePath != null)
        {
            String treeRoot = relativeFilePath.split("/")[0];
            H5FolderFlags flags = h5FolderFlagsByTreeRoot.get(treeRoot);
            if (flags != null)
            {
                return HierarchicalContentUtils.handleHdf5AsFolder(relativeFilePath, 
                        flags.isH5Folders(), flags.isH5arFolders());
            }
        }
        return handleHdf5AsFolderByDefault(relativeFilePath);
    }

    public boolean handleHdf5AsFolderByDefault(String relativeFilePath)
    {
        return HierarchicalContentUtils.handleHdf5AsFolder(relativeFilePath, 
                h5FoldersDefault, h5arFoldersDefault);
    }
}
