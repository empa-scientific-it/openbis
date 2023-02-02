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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Simple {@link IHierarchicalContent} implementation for virtual data sets with dynamic behavior (almost no caching).
 * 
 * @author Piotr Buczek
 */
public class VirtualHierarchicalContent implements IHierarchicalContent
{

    final static IVirtualNodeMergerFactory DEFAULT_MERGER_FACTORY = new IVirtualNodeMergerFactory()
        {
            @Override
            public IVirtualNodeMerger createNodeMerger()
            {
                return new VirtualNodeMerger(this);
            }

            @Override
            public IVirtualNodeListMerger createNodeListMerger()
            {
                return new VirtualNodeListMerger(this);
            }
        };

    private final IVirtualNodeMergerFactory mergerFactory;

    private final List<IHierarchicalContent> components;

    private IHierarchicalContentNode rootNode; // cached

    // for tests
    VirtualHierarchicalContent(IVirtualNodeMergerFactory mergerFactory,
            List<IHierarchicalContent> components)
    {
        if (components == null || components.isEmpty())
        {
            throw new IllegalArgumentException("Undefined contents");
        }
        this.components = components;
        this.mergerFactory = mergerFactory;
    }

    public VirtualHierarchicalContent(List<IHierarchicalContent> components)
    {
        this(DEFAULT_MERGER_FACTORY, components);
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        if (rootNode == null)
        {
            rootNode = tryMergeNodes(new INodeProvider()
                {
                    @Override
                    public IHierarchicalContentNode tryGetNode(IHierarchicalContent content)
                    {
                        return content.getRootNode();
                    }
                });
        }
        return rootNode;
    }

    @Override
    public IHierarchicalContentNode getNode(final String relativePath)
            throws IllegalArgumentException
    {
        final IHierarchicalContentNode nodeOrNull = tryGetNode(relativePath);

        if (nodeOrNull == null)
        {
            throw new IllegalArgumentException("Resource '" + relativePath + "' doesn't exist.");
        }
        return nodeOrNull;
    }

    @Override
    public IHierarchicalContentNode tryGetNode(final String relativePath)
    {
        return tryMergeNodes(new INodeProvider()
            {
                @Override
                public IHierarchicalContentNode tryGetNode(IHierarchicalContent content)
                {
                    return content.tryGetNode(relativePath);
                }
            });
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(final String relativePathPattern)
    {
        return mergeNodeLists(new INodeListProvider()
            {
                @Override
                public List<IHierarchicalContentNode> getNodeList(IHierarchicalContent content)
                {
                    return content.listMatchingNodes(relativePathPattern);
                }
            });
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(final String startingPath,
            final String fileNamePattern)
    {
        return mergeNodeLists(new INodeListProvider()
            {
                @Override
                public List<IHierarchicalContentNode> getNodeList(IHierarchicalContent content)
                {
                    return content.listMatchingNodes(startingPath, fileNamePattern);
                }
            });
    }

    @Override
    public void close()
    {
        for (IHierarchicalContent component : components)
        {
            component.close();
        }
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "VirtualHierarchicalContent [components="
                + Arrays.toString(components.toArray(new IHierarchicalContent[0])) + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        for (IHierarchicalContent component : components)
        {
            result = prime * result + component.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof VirtualHierarchicalContent))
        {
            return false;
        }
        VirtualHierarchicalContent other = (VirtualHierarchicalContent) obj;
        return components.equals(other.components);
    }

    private IHierarchicalContentNode tryMergeNodes(INodeProvider provider)
    {
        IVirtualNodeMerger merger = mergerFactory.createNodeMerger();
        for (IHierarchicalContent component : components)
        {
            IHierarchicalContentNode componentNode = provider.tryGetNode(component);
            if (componentNode != null)
            {
                merger.addNode(componentNode);
            }
        }
        return merger.tryCreateMergedNode();
    }

    private List<IHierarchicalContentNode> mergeNodeLists(INodeListProvider listProvider)
    {
        IVirtualNodeListMerger listMerger = mergerFactory.createNodeListMerger();
        for (IHierarchicalContent component : components)
        {
            List<IHierarchicalContentNode> componentNodes = listProvider.getNodeList(component);
            listMerger.addNodes(componentNodes);
        }
        return listMerger.createMergedNodeList();
    }

    private interface INodeProvider
    {
        IHierarchicalContentNode tryGetNode(IHierarchicalContent content);
    }

    private interface INodeListProvider
    {
        List<IHierarchicalContentNode> getNodeList(IHierarchicalContent content);
    }

    //
    // NOTE: following interfaces and classes are exposed (package protected) only for testing
    //

    interface IVirtualNodeMergerFactory
    {
        IVirtualNodeMerger createNodeMerger();

        IVirtualNodeListMerger createNodeListMerger();
    }

    interface IVirtualNodeMerger
    {
        void addNode(IHierarchicalContentNode node);

        IHierarchicalContentNode tryCreateMergedNode();
    }

    interface IVirtualNodeListMerger
    {
        void addNodes(List<IHierarchicalContentNode> nodes);

        List<IHierarchicalContentNode> createMergedNodeList();
    }

    /**
     * Merges nodes with the same relative paths:
     * <ul>
     * <li>For directories merges the internal nodes.
     * <li>For normal files uses the 'last' node's file.
     * </ul>
     */
    static class VirtualNodeMerger implements IVirtualNodeMerger
    {
        private final IVirtualNodeMergerFactory factory;

        // For convenience in iteration the order of these nodes is reversed.
        // It is the first node, not the last one, which is overriding all files of other nodes.
        private final LinkedList<IHierarchicalContentNode> nodes =
                new LinkedList<IHierarchicalContentNode>();

        public VirtualNodeMerger(IVirtualNodeMergerFactory factory)
        {
            this.factory = factory;
        }

        @Override
        public void addNode(IHierarchicalContentNode node)
        {
            nodes.addFirst(node);
        }

        @Override
        public IHierarchicalContentNode tryCreateMergedNode()
        {
            if (nodes.isEmpty())
            {
                return null;
            } else
            {
                return new VirtualNode(factory, nodes);
            }
        }
    }

    /**
     * Merges lists of nodes into a list with nodes merging nodes with the same relative paths.
     */
    static class VirtualNodeListMerger implements IVirtualNodeListMerger
    {
        private final IVirtualNodeMergerFactory nodeMergerFactory;

        // relative path -> merger (with preserved order)
        private final Map<String, IVirtualNodeMerger> mergers =
                new LinkedHashMap<String, IVirtualNodeMerger>();

        public VirtualNodeListMerger(IVirtualNodeMergerFactory nodeMergerFactory)
        {
            this.nodeMergerFactory = nodeMergerFactory;
        }

        @Override
        public void addNodes(List<IHierarchicalContentNode> nodes)
        {
            for (IHierarchicalContentNode node : nodes)
            {
                String relativePath = node.getRelativePath();
                IVirtualNodeMerger merger = mergers.get(relativePath);
                if (merger == null)
                {
                    merger = nodeMergerFactory.createNodeMerger();
                    mergers.put(relativePath, merger);
                }
                merger.addNode(node);
            }
        }

        @Override
        public List<IHierarchicalContentNode> createMergedNodeList()
        {
            List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            for (IVirtualNodeMerger merger : mergers.values())
            {
                final IHierarchicalContentNode nodeOrNull = merger.tryCreateMergedNode();
                if (nodeOrNull != null)
                {
                    result.add(nodeOrNull);
                }
            }
            return result;
        }
    }

    /**
     * {@link IHierarchicalContentNode} implementation merging nodes with the same relative paths:
     * <ul>
     * <li>For directories merges the internal nodes.
     * <li>For normal files uses the 'last' available node's file.
     * </ul>
     */
    public static class VirtualNode implements IHierarchicalContentNode
    {

        private final IVirtualNodeMergerFactory nodeMergerFactory;

        private final List<IHierarchicalContentNode> nodes;

        public VirtualNode(IVirtualNodeMergerFactory factory, List<IHierarchicalContentNode> nodes)
        {
            assert nodes != null : "Undefined nodes.";
            if (nodes.isEmpty())
            {
                throw new IllegalArgumentException("Resource doesn't exist.");
            }
            this.nodeMergerFactory = factory;
            this.nodes = nodes;
        }

        public IHierarchicalContentNode lastNode()
        {
            return nodes.get(0);
        }

        private IHierarchicalContentNode lastExistingNode()
        {
            for (IHierarchicalContentNode node : nodes)
            {
                if (node.exists()) // archived node doesn't exist and will be omitted
                {
                    return node;
                }
            }
            throw new IllegalStateException(
                    "Resource is currently unavailable. It might be in an archive.");
        }

        @Override
        public String getName()
        {
            return lastNode().getName();
        }

        @Override
        public String getRelativePath()
        {
            return lastNode().getRelativePath();
        }

        @Override
        public String getParentRelativePath()
        {
            return lastNode().getParentRelativePath();
        }

        @Override
        public boolean exists()
        {
            // the node exists if at least one node exists
            for (IHierarchicalContentNode node : nodes)
            {
                if (node.exists())
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isDirectory()
        {
            // NOTE: we don't support files and directories with the same name
            return lastNode().isDirectory();
        }

        @Override
        public long getLastModified()
        {
            return lastNode().getLastModified();
        }

        @Override
        public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
        {
            IVirtualNodeListMerger listMerger = nodeMergerFactory.createNodeListMerger();
            // Returns the nodes in the order they have been added
            for (int i = nodes.size() - 1; i >= 0; i--)
            {
                listMerger.addNodes(nodes.get(i).getChildNodes());
            }
            return listMerger.createMergedNodeList();
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            return lastExistingNode().getFile();
        }

        @Override
        public File tryGetFile()
        {
            return lastExistingNode().tryGetFile();
        }

        @Override
        public long getFileLength() throws UnsupportedOperationException
        {
            if (isDirectory())
            {
                // For directory we return an estimated length - sum of lengths of all nodes.
                // NOTE: This code is here for future. Currently all implementations for real data
                // set directories throw an exception when asked for file length.
                long estimatedLength = 0;
                for (IHierarchicalContentNode node : nodes)
                {
                    estimatedLength += node.getFileLength();
                }
                return estimatedLength;
            } else
            {
                return lastExistingNode().getFileLength();
            }
        }

        @Override
        public String getChecksum() throws UnsupportedOperationException
        {
            return lastExistingNode().getChecksum();
        }

        @Override
        public int getChecksumCRC32() throws UnsupportedOperationException
        {
            return lastExistingNode().getChecksumCRC32();
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return lastExistingNode().isChecksumCRC32Precalculated();
        }

        @Override
        public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
                IOExceptionUnchecked
        {
            return lastExistingNode().getFileContent();
        }

        @Override
        public InputStream getInputStream() throws UnsupportedOperationException,
                IOExceptionUnchecked
        {
            return lastExistingNode().getInputStream();
        }

        //
        // Object
        //

        @Override
        public String toString()
        {
            return "VirtualNode [nodes="
                    + Arrays.toString(nodes.toArray(new IHierarchicalContentNode[0])) + "]";
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (!(obj instanceof VirtualNode))
            {
                return false;
            }
            VirtualNode other = (VirtualNode) obj;
            return nodes.equals(other.nodes);
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            for (IHierarchicalContentNode node : nodes)
            {
                result = prime * result + node.hashCode();
            }
            return result;
        }

    }

}
