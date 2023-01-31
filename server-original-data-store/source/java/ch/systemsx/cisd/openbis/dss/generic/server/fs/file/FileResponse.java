/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.file;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;

public class FileResponse implements IFileSystemViewResponse
{
    private final String fullPath;

    private final IHierarchicalContentNode node;

    private final IHierarchicalContent content;

    public FileResponse(String fullPath, final IHierarchicalContentNode node, final IHierarchicalContent content)
    {
        this.fullPath = fullPath;
        this.node = node;
        this.content = content;
    }

    public String getFullPath()
    {
        return fullPath;
    }

    public IHierarchicalContentNode getNode()
    {
        return node;
    }

    public IHierarchicalContent getContent()
    {
        return content;
    }

}