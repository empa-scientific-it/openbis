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
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Mock content based on an array of textual content descriptions of the form: <path>:<size>:<checksum>. An empty string for <path> denotes root node.
 * Directory paths end with '/'.
 * 
 * @author Franz-Josef Elmer
 */
public class MockContent implements IHierarchicalContent
{
    private MockNode root;

    private final Map<String, MockNode> nodes = new HashMap<String, MockNode>();

    private boolean closed;

    public MockContent(String... contentDescriptions)
    {
        for (String contentDescription : contentDescriptions)
        {
            String[] splittedDescription = contentDescription.split(":");
            MockNode node = new MockNode();
            String path = splittedDescription[0];
            if (path.length() == 0)
            {
                root = node;
                node.directory = true;
                node.name = "";
                node.relativePath = "";
            } else
            {
                if (path.endsWith("/"))
                {
                    path = path.substring(0, path.length() - 1);
                    node.directory = true;
                } else
                {
                    node.directory = false;
                }
                node.relativePath = path;
                int lastIndexOfDelim = path.lastIndexOf('/');
                MockNode parent = root;
                if (lastIndexOfDelim >= 0)
                {
                    String parentPath = path.substring(0, lastIndexOfDelim);
                    parent = nodes.get(parentPath);
                }
                parent.addNode(node);
                nodes.put(path, node);
                node.name = path.substring(lastIndexOfDelim + 1);
            }
            node.size = Long.parseLong(splittedDescription[1]);
            node.crc32Checksum = (int) Long.parseLong(splittedDescription[2], 16);
            if (splittedDescription.length > 3)
            {
                node.content = splittedDescription[3];
                if (splittedDescription.length == 6)
                {
                    node.checksum = splittedDescription[4] + ":" + splittedDescription[5];
                }
            }
        }
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        return root;
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath)
            throws IllegalArgumentException
    {
        return nodes.get(relativePath);
    }

    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        return nodes.get(relativePath);
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String startingPath,
            String fileNamePattern)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close()
    {
        closed = true;
    }

    public boolean isClosed()
    {
        return closed;
    }

}