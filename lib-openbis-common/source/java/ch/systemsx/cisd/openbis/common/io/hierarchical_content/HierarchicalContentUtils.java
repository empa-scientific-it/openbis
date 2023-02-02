/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.hdf5.h5ar.ArchiveEntry;
import ch.systemsx.cisd.openbis.common.hdf5.IHDF5ContainerReader;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Utility methods for working with abstraction of hierarchical content.
 * 
 * @author Piotr Buczek
 */
public class HierarchicalContentUtils
{

    /** Comparator creating order based on node type (directory < file) and node name */
    static final Comparator<IHierarchicalContentNode> DEFAULT_NODE_COMPARATOR =
            new Comparator<IHierarchicalContentNode>()
                {
                    @Override
                    public int compare(IHierarchicalContentNode node1,
                            IHierarchicalContentNode node2)
                    {
                        return createSortableName(node1).compareTo(createSortableName(node2));
                    }

                    private String createSortableName(IHierarchicalContentNode node)
                    {
                        return (node.isDirectory() ? "D" : "F") + node.getName().toUpperCase();
                    }
                };

    /**
     * Sorts the given list of nodes with the default comparator.
     * 
     * @see HierarchicalContentUtils#DEFAULT_NODE_COMPARATOR
     */
    public static void sortNodes(List<IHierarchicalContentNode> nodes)
    {
        Collections.sort(nodes, HierarchicalContentUtils.DEFAULT_NODE_COMPARATOR);
    }

    /**
     * An {@link InputStream} implementation which closes an associated {@link IHierarchicalContent} together with an underlying target
     * {@link InputStream}.
     * 
     * @author Kaloyan Enimanev
     */
    static class HierarchicalContentClosingInputStream extends FilterInputStream
    {
        private final IHierarchicalContent hierarchicalContent;

        public HierarchicalContentClosingInputStream(InputStream target,
                IHierarchicalContent hierarchicalContent)
        {
            super(target);
            this.hierarchicalContent = hierarchicalContent;
        }

        @Override
        public void close() throws IOException
        {
            // no error can be thrown here
            hierarchicalContent.close();

            // can throw IOException
            super.close();
        }

    }

    /**
     * Returns an {@link InputStream} implementation for given node which closes an associated {@link IHierarchicalContent} together when closing the
     * {@link InputStream} itself.
     */
    public static InputStream getInputStreamAutoClosingContent(IHierarchicalContentNode node,
            IHierarchicalContent content) throws IOExceptionUnchecked
    {
        return new HierarchicalContentClosingInputStream(node.getInputStream(), content);
    }

    /**
     * Returns <code>true</code> if the file type (in lower case) is <code>h5</code> and <code>h5Folders == true</code> 
     * or the file type is <code>h5ar</code> and <code>h5arFolders == true</code>. 
     */
    static boolean handleHdf5AsFolder(String filename, boolean h5Folders, boolean h5arFolders)
    {
        return (filename.toLowerCase().endsWith("h5") && h5Folders) || (filename.toLowerCase().endsWith("h5ar") && h5arFolders);
    }

    public static boolean isFileAbstractionOk(IHDF5ContainerReader reader, String groupPath)
    {
        final List<ArchiveEntry> entries = reader.getGroupMembers(groupPath);
        for (ArchiveEntry entry : entries)
        {
            if (entry.isRegularFile() && false == reader.isFileAbstraction(entry))
            {
                return false;
            } else if (entry.isDirectory())
            {
                return isFileAbstractionOk(reader, entry.getPath());
            }
        }
        return true;
    }

}
