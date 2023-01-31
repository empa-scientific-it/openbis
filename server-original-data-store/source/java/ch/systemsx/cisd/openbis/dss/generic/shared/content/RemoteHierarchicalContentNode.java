/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.common.resource.IReleasable;
import ch.systemsx.cisd.common.resource.Resources;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * A node of hierarchical content that stored on a remote datastore server. If file content is queried, it is downloaded to session workspace and
 * cached there.
 * 
 * @author anttil
 */
public class RemoteHierarchicalContentNode implements IHierarchicalContentNode
{

    private ISingleDataSetPathInfoProvider provider;

    private ISessionTokenProvider sessionTokenProvider;

    private DataSetPathInfo path;

    private String parentRelativePath;

    private final IContentCache cache;

    private final IDatasetLocation dataSetLocation;

    private final IDssServiceRpcGenericFactory serviceFactory;

    private final Resources resources;

    public RemoteHierarchicalContentNode(IDatasetLocation dataSetetLocation, DataSetPathInfo path,
            ISingleDataSetPathInfoProvider provider, IDssServiceRpcGenericFactory serviceFactory,
            ISessionTokenProvider sessionTokenProvider, IContentCache contentCache)
    {
        this(dataSetetLocation, path, provider, serviceFactory, sessionTokenProvider, contentCache,
                null);
    }

    private RemoteHierarchicalContentNode(IDatasetLocation dataSetetLocation, DataSetPathInfo path,
            ISingleDataSetPathInfoProvider provider, IDssServiceRpcGenericFactory serviceFactory,
            ISessionTokenProvider sessionTokenProvider, IContentCache contentCache,
            String parentRelativePath)
    {
        this.dataSetLocation = dataSetetLocation;
        this.path = path;
        this.provider = provider;
        this.sessionTokenProvider = sessionTokenProvider;
        this.serviceFactory = serviceFactory;
        this.cache = contentCache;
        this.parentRelativePath = parentRelativePath;
        this.resources = new Resources();
    }

    @Override
    public String getName()
    {
        return path.getFileName();
    }

    @Override
    public String getRelativePath()
    {
        return path.getRelativePath();
    }

    @Override
    public String getParentRelativePath()
    {
        return parentRelativePath;
    }

    @Override
    public boolean exists()
    {
        return true;
    }

    @Override
    public boolean isDirectory()
    {
        return path.isDirectory();
    }

    @Override
    public long getLastModified()
    {
        if (path.getLastModified() != null)
        {
            return path.getLastModified().getTime();
        } else
        {
            return 0;
        }
    }

    @Override
    public long getFileLength() throws UnsupportedOperationException
    {
        return path.getSizeInBytes();
    }

    @Override
    public String getChecksum() throws UnsupportedOperationException
    {
        return path.getChecksum();
    }

    @Override
    public int getChecksumCRC32() throws UnsupportedOperationException
    {
        return path.getChecksumCRC32();
    }

    @Override
    public boolean isChecksumCRC32Precalculated()
    {
        return false;
    }

    @Override
    public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
    {
        List<IHierarchicalContentNode> children = new ArrayList<IHierarchicalContentNode>();
        String relativePath = path.getRelativePath();
        if (provider != null)
        {
            for (DataSetPathInfo childPath : provider.listChildrenPathInfos(path))
            {
                children.add(new RemoteHierarchicalContentNode(dataSetLocation, childPath,
                        provider, serviceFactory, sessionTokenProvider, cache, relativePath));
            }
        } else
        {
            IDssServiceRpcGeneric service =
                    serviceFactory.getService(dataSetLocation.getDataStoreUrl());
            String sessionToken = sessionTokenProvider.getSessionToken();
            String dataSetCode = dataSetLocation.getDataSetCode();
            FileInfoDssDTO[] files =
                    service.listFilesForDataSet(sessionToken, dataSetCode, relativePath, false);
            for (FileInfoDssDTO file : files)
            {
                DataSetPathInfo info = new DataSetPathInfo();
                info.setChecksumCRC32(file.tryGetCrc32Checksum());
                info.setDirectory(file.isDirectory());
                info.setFileName(file.getPathInListing());
                info.setRelativePath(file.getPathInDataSet());
                info.setSizeInBytes(file.getFileSize());
                info.setLastModified(new Date(0L));
                children.add(new RemoteHierarchicalContentNode(dataSetLocation, info, provider,
                        serviceFactory, sessionTokenProvider, cache, relativePath));
            }
        }
        return children;
    }

    @Override
    public File getFile() throws UnsupportedOperationException
    {
        return cache.getFile(sessionTokenProvider.getSessionToken(), dataSetLocation, path);
    }

    @Override
    public File tryGetFile()
    {
        try
        {
            return getFile();
        } catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
            IOExceptionUnchecked
    {
        return new RandomAccessFileImpl(getFile(), "r");
    }

    @Override
    public InputStream getInputStream() throws UnsupportedOperationException, IOExceptionUnchecked
    {
        final InputStream stream =
                cache.getInputStream(sessionTokenProvider.getSessionToken(), dataSetLocation, path);
        resources.add(new IReleasable()
            {
                @Override
                public void release()
                {
                    try
                    {
                        stream.close();
                    } catch (IOException e)
                    {
                        CheckedExceptionTunnel.wrapIfNecessary(e);
                    }
                }
            });
        return stream;
    }

    public void close()
    {
        resources.release();
    }
}
