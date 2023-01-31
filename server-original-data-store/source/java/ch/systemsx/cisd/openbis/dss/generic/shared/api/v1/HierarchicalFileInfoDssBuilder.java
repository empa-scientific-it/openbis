/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.IOException;
import java.util.ArrayList;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Helper Class for creating FileInfoDss objects based on file system abstraction.
 * 
 * @author Piotr Buczek
 */
public class HierarchicalFileInfoDssBuilder
{

    private final IHierarchicalContentNode listingRootNode;

    /**
     * Constructor for FileInfoDssFactory
     * 
     * @param listingRootNode node in the directory structure from which the recursive traversal starts
     */
    public HierarchicalFileInfoDssBuilder(IHierarchicalContentNode listingRootNode)
    {
        this.listingRootNode = listingRootNode;
    }

    /**
     * Append file info starting from the listing root node. Assumes that the parameters have been verified already.
     * 
     * @param list The list the files infos are appended to
     * @param isRecursive If true, directories will be recursively appended to the list
     */
    public void appendFileInfos(ArrayList<FileInfoDssDTO> list, boolean isRecursive)
            throws IOException
    {
        // at the top level, we should list the contents of directories, but only recurse if the
        // search is recursive; and we should skip listing the top directory
        appendFileInfosForNode(listingRootNode, list, isRecursive ? Integer.MAX_VALUE : 0, true);
    }

    private void appendFileInfosForNode(IHierarchicalContentNode currentNode,
            ArrayList<FileInfoDssDTO> list, int maxDepth, boolean excludeTopLevelIfDirectory)
            throws IOException
    {
        FileInfoDssDTO fileInfo = fileInfoForNode(currentNode);
        //
        if (excludeTopLevelIfDirectory && fileInfo.isDirectory())
        {
            // If specified, skip the top level if it is a directory
        } else
        {
            list.add(fileInfo);
        }

        // If this is a file or we are not recursive, return
        if (fileInfo.isDirectory() == false || maxDepth < 0)
        {
            return;
        }

        for (IHierarchicalContentNode childNode : currentNode.getChildNodes())
        {
            appendFileInfosForNode(childNode, list, maxDepth - 1, false);
        }
    }

    private FileInfoDssDTO fileInfoForNode(IHierarchicalContentNode node) throws IOException
    {
        FileInfoDssDTO fileInfo =
                new FileInfoDssDTO(node.getRelativePath(), pathRelativeToListingRoot(node),
                        node.isDirectory(), (node.isDirectory()) ? -1 : node.getFileLength(),
                        node.isChecksumCRC32Precalculated() ? node.getChecksumCRC32() : null);
        return fileInfo;
    }

    /**
     * Convert the path for node to a path relative to the root of the listing
     */
    private String pathRelativeToListingRoot(IHierarchicalContentNode node) throws IOException
    {
        String path;
        path = node.getRelativePath();
        String listingRootNodeRelativePath = listingRootNode.getRelativePath();
        path = path.substring(listingRootNodeRelativePath.length());
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        return path;
    }
}
