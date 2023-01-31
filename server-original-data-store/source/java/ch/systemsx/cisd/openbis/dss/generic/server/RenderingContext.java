/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

final class RenderingContext
{

    private final IHierarchicalContent rootContent;

    private final String relativePath;

    private final IHierarchicalContentNode fileNode;

    private final String urlPrefix;

    private String relativeParentPath;

    private final String sessionIdOrNull;

    RenderingContext(IHierarchicalContent rootContent, String urlPrefix, String relativePath,
            String sessionIdOrNull)
    {
        this.rootContent = rootContent;
        this.relativePath = relativePath;
        this.urlPrefix = urlPrefix;
        this.sessionIdOrNull = sessionIdOrNull;
        if (relativePath.length() > 0)
        {
            fileNode = rootContent.getNode(relativePath);
            relativeParentPath = fileNode.getParentRelativePath();
            if (relativeParentPath == null)
            {
                relativeParentPath = "";
            }
        } else
        {
            fileNode = rootContent.getRootNode();
        }
    }

    RenderingContext(RenderingContext oldContext, String newRelativePathOrNull)
    {
        this(oldContext.rootContent, oldContext.urlPrefix, newRelativePathOrNull,
                oldContext.sessionIdOrNull);
    }

    public final IHierarchicalContent getRootContent()
    {
        return rootContent;
    }

    public final IHierarchicalContentNode getRootNode()
    {
        return rootContent.getRootNode();
    }

    public final IHierarchicalContentNode getContentNode()
    {
        return fileNode;
    }

    public final String getRelativePath()
    {
        return relativePath;
    }

    public final String getUrlPrefix()
    {
        return urlPrefix;
    }

    public final String getRelativeParentPath()
    {
        return relativeParentPath;
    }

    public String getSessionIdOrNull()
    {
        return sessionIdOrNull;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}