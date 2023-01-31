/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.v1.impl;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.common.api.retry.Retry;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetDss implements IDataSetDss
{
    private final String code;

    private final IDssServiceRpcGeneric service;

    private final AuthenticatedState parent;

    public DataSetDss(String code, IDssServiceRpcGeneric service, AuthenticatedState parent)
    {
        this.code = code;
        this.service = service;
        this.parent = parent;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public InputStream getFile(String path) throws IllegalArgumentException,
            InvalidSessionException
    {
        return parent.getFile(this, path);
    }

    @Override
    public FileInfoDssDTO[] listFiles(String startPath, boolean isRecursive)
            throws IllegalArgumentException, InvalidSessionException
    {
        return parent.listFiles(this, startPath, isRecursive);
    }

    @Override
    public File tryLinkToContents(String overrideStoreRootPathOrNull)
            throws IllegalArgumentException, InvalidSessionException
    {
        return parent.tryLinkToContents(this, overrideStoreRootPathOrNull);
    }

    @Override
    public String tryGetInternalPathInDataStore() throws InvalidSessionException,
            EnvironmentFailureException
    {
        return parent.tryGetInternalPathInDataStore(this, null);
    }

    @Override
    public File getLinkOrCopyOfContents(String overrideStoreRootPathOrNull, File downloadDir)
            throws IllegalArgumentException, InvalidSessionException
    {
        return parent.getLinkOrCopyOfContents(this, overrideStoreRootPathOrNull, downloadDir, null);
    }

    @Override
    public File getLinkOrCopyOfContent(String overrideStoreRootPathOrNull, File downloadDir,
            String pathInDataSet) throws IllegalArgumentException, InvalidSessionException
    {
        return parent.getLinkOrCopyOfContents(this, overrideStoreRootPathOrNull, downloadDir,
                pathInDataSet);
    }

    public AuthenticatedState getParent()
    {
        return parent;
    }

    public IDssServiceRpcGeneric getService()
    {
        return service;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("code", code);
        return builder.toString();
    }

    @Override
    @Retry
    public String getSessionURLForFile(String path) throws IllegalArgumentException,
            InvalidSessionException
    {
        return parent.getSessionURLForFile(this, path);
    }

    @Override
    @Retry
    public String getURLForFileWithTimeout(String path, long validityDurationInSeconds)
            throws IllegalArgumentException, InvalidSessionException
    {
        return parent.getURLForFileWithTimeout(this, path, validityDurationInSeconds);
    }
}
