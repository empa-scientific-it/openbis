/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class DirectoryNode implements DataSetContentNode
{
    private final String parentPath;

    private final String name;

    private final String fullPath;

    private final Map<String, DataSetContentNode> children;

    public DirectoryNode(String parentPath, String name, Map<String, DataSetContentNode> children)
    {
        this.parentPath = parentPath;
        this.name = name;
        if (parentPath == null)
        {
            this.fullPath = name;
        } else
        {
            this.fullPath = parentPath + "/" + name;
        }
        this.children = new HashMap<>();
        this.children.putAll(children);
    }

    public DirectoryNode(String parentPath, String name)
    {
        this(parentPath, name, new HashMap<String, DataSetContentNode>());
    }

    public DirectoryNode addFile(Path path, long length, Integer checksumCRC32, String checksum)
    {
        if (path.getParent() != null)
        {
            Path directoryName = path.getName(0);
            Path tail = path.subpath(1, path.getNameCount());
            DirectoryNode node = (DirectoryNode) children.get(directoryName.toString());
            if (node == null)
            {
                node = new DirectoryNode(getFullPath(), directoryName.toString());
            }

            return newNode(directoryName.toString(), node.addFile(tail, length, checksumCRC32, checksum));
        } else
        {
            if (children.containsKey(path.toString()))
            {
                throw new IllegalArgumentException("duplicate file: " + path.toString() + "/" + this.getFullPath());
            }

            return newNode(path.toString(), new FileNode(getFullPath(), path.toString(), length, checksumCRC32, checksum));
        }
    }

    public DirectoryNode addDirectory(Path path)
    {
        if (path.getParent() != null)
        {
            Path directoryName = path.getName(0);
            Path tail = path.subpath(1, path.getNameCount());
            DirectoryNode node = (DirectoryNode) children.get(directoryName.toString());
            if (node == null)
            {
                node = new DirectoryNode(getFullPath(), directoryName.toString());
            }

            return newNode(directoryName.toString(), node.addDirectory(tail));
        } else if (children.containsKey(path.toString()))
        {
            return this;
        } else
        {
            return newNode(path.toString(), new DirectoryNode(getFullPath(), path.toString()));
        }
    }

    private DirectoryNode newNode(String nodeName, DataSetContentNode node)
    {
        Map<String, DataSetContentNode> newContent = new HashMap<String, DataSetContentNode>();
        newContent.putAll(children);
        newContent.put(nodeName, node);
        return new DirectoryNode(parentPath, name, newContent);
    }

    @Override
    public long getLength()
    {
        long result = 0;
        for (DataSetContentNode node : children.values())
        {
            result += node.getLength();
        }
        return result;
    }

    @Override
    public Integer getChecksumCRC32()
    {
        return null;
    }

    @Override
    public String getChecksum()
    {
        return null;
    }

    @Override
    public String getFullPath()
    {
        return fullPath;
    }

    @Override
    public boolean isDirectory()
    {
        return true;
    }

    Map<String, DataSetContentNode> getChildren()
    {
        return children;
    }
}
