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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.FtpFileImpl;

/**
 * A factory constructing {@link FtpFile} objects.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpFileFactory
{

    /**
     * This is just a convenience method that retrieves all necessary data from an {@link IHierarchicalContentNode} and constructs an {@link FtpFile}.
     * <p>
     * The reference to {@link IHierarchicalContentNode} is *not* kept by the returned instance. Thus, callers are responsible for closing all
     * resources associated with the {@link IHierarchicalContentNode} on their own.
     */
    public static FtpFile createFtpFile(String dataSetCode, String path,
            IHierarchicalContentNode contentNode, IHierarchicalContent content,
            IHierarchicalContentNodeFilter childrenFilter, Cache cache)
    {
        return createFtpFile(dataSetCode, path, contentNode, content, childrenFilter, getLastModified(contentNode), cache);

    }

    public static FtpFile createFtpFile(String dataSetCode, String path, IHierarchicalContentNode contentNode, 
            IHierarchicalContent content, IHierarchicalContentNodeFilter childrenFilter, 
            long lastModified, Cache cache)
    {
        FtpFile file = cache.getFile(path);
        if (file == null)
        {
            file = new FtpFileImpl(dataSetCode, path, contentNode.getRelativePath(),
                    contentNode.isDirectory(), getSize(contentNode), lastModified,
                    content, childrenFilter, cache);
            cache.putFile(file, path);
        }
        return file;
    }

    private static long getSize(IHierarchicalContentNode contentNode)
    {
        if (contentNode.isDirectory())
        {
            return 0;
        } else
        {
            return contentNode.getFileLength();
        }
    }

    private static long getLastModified(IHierarchicalContentNode contentNode)
    {
        try
        {
            return contentNode.getLastModified();
        } catch (UnsupportedOperationException uoe)
        {
            // ignore
        }
        return 0;
    }
}
