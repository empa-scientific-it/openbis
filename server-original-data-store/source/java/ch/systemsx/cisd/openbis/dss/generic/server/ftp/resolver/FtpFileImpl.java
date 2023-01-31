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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpFileFactory;

/**
 * An {@link FtpFile} implementation which lazily creates and uses {@link IHierarchicalContent} when needed.
 * <p>
 * The resources represented by {@link FtpFileImpl} exist in the data store.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpFileImpl extends AbstractFtpFileWithContent
{
    private final String dataSetCode;

    private final String pathInDataSet;

    private final boolean isDirectory;

    private final long size;

    private final IHierarchicalContentNodeFilter childrenFilter;

    private IHierarchicalContent content;

    private final Cache cache;
    
    public FtpFileImpl(String dataSetCode, String path, String pathInDataSet, boolean isDirectory,
            long size, long lastModified, IHierarchicalContent content,
            IHierarchicalContentNodeFilter childrenFilter, Cache cache)
    {
        super(path);
        this.dataSetCode = dataSetCode;
        this.pathInDataSet = pathInDataSet;
        this.isDirectory = isDirectory;
        this.size = size;
        this.cache = cache;
        setLastModified(lastModified);
        this.content = content;
        this.childrenFilter = childrenFilter;
    }

    @Override
    public IRandomAccessFile getFileContent()
    {
        try
        {
            IHierarchicalContentNode contentNode = getContentNodeForThisFile();
            return contentNode.getFileContent();
        } catch (RuntimeException re)
        {
            content.close();
            throw re;
        }
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException
    {
        try
        {
            IHierarchicalContentNode contentNode = getContentNodeForThisFile();
            InputStream result =
                    HierarchicalContentUtils.getInputStreamAutoClosingContent(contentNode, content);

            if (offset > 0)
            {
                result.skip(offset);
            }
            return result;
        } catch (IOException ioex)
        {
            content.close();
            throw ioex;
        } catch (RuntimeException re)
        {
            content.close();
            throw re;
        }
    }

    @Override
    public long getSize()
    {
        if (isFile())
        {
            return size;
        }
        return 0;
    }

    @Override
    public boolean isDirectory()
    {
        return isDirectory;
    }

    @Override
    public boolean isFile()
    {
        return isDirectory() == false;
    }

    @Override
    public List<org.apache.ftpserver.ftplet.FtpFile> unsafeListFiles()
    {
        if (isFile())
        {
            throw new UnsupportedOperationException();
        }

        try
        {
            IHierarchicalContentNode contentNode = getContentNodeForThisFile();
            List<IHierarchicalContentNode> children = contentNode.getChildNodes();
            List<org.apache.ftpserver.ftplet.FtpFile> result =
                    new ArrayList<org.apache.ftpserver.ftplet.FtpFile>();

            for (IHierarchicalContentNode childNode : children)
            {
                if (childrenFilter.accept(childNode))
                {
                    String childPath =
                            absolutePath + FtpConstants.FILE_SEPARATOR + childNode.getName();
                    FtpFile childFile =
                            FtpFileFactory.createFtpFile(dataSetCode, childPath, childNode,
                                    content, childrenFilter, cache);
                    result.add(childFile);
                }
            }
            return result;
        } finally
        {
            content.close();
        }
    }

    private IHierarchicalContentNode getContentNodeForThisFile()
    {
        return content.getNode(pathInDataSet);
    }
}
