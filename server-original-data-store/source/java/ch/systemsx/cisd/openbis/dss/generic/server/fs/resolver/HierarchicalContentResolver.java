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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.resolver;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;

class HierarchicalContentResolver implements IResolver
{

    private IHierarchicalContent content;

    public HierarchicalContentResolver(IHierarchicalContent content)
    {
        this.content = content;
    }

    @Override
    public IFileSystemViewResponse resolve(String[] subPath, IResolverContext context)
    {
        IHierarchicalContentNode rootNode;
        if (subPath.length == 0)
        {
            rootNode = content.getRootNode();
        } else
        {
            rootNode = content.tryGetNode(StringUtils.join(subPath, "/"));
        }

        if (rootNode == null)
        {
            return context.createNonExistingFileResponse(null);
        }

        if (false == rootNode.isDirectory())
        {
            return context.createFileResponse(rootNode, content);
        }

        IDirectoryResponse response = context.createDirectoryResponse();
        for (IHierarchicalContentNode node : rootNode.getChildNodes())
        {
            if (node.isDirectory())
            {
                response.addDirectory(node.getName(), node.getLastModified());
            } else
            {
                response.addFile(node.getName(), node);
            }
        }
        return response;
    }

}