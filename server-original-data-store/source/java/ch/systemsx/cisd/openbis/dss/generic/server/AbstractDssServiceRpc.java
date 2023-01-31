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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataStoreApiUrlUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * Abstract superclass of DssServiceRpc implementations.
 * <p>
 * Provides methods to check security and access to data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDssServiceRpc<T> extends AbstractServiceWithLogger<T>
{
    private final IEncapsulatedOpenBISService openBISService;

    @Autowired
    private final IStreamRepository streamRepository;

    private String downloadUrl;

    private IHierarchicalContentProvider contentProvider;

    private IShareIdManager shareIdManager;

    private File storeDirectory;

    private DatabaseInstance homeDatabaseInstance;

    public final void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = DataStoreApiUrlUtilities.getDataStoreUrlFromDownloadUrl(downloadUrl);
    }

    /**
     * Configuration method to set the path to the DSS store. Should only be called by the object that configures the RPC services.
     */
    public void setStoreDirectory(File aFile)
    {
        storeDirectory = aFile;
    }

    /**
     * Constructor with required reference to the openBIS service.
     * 
     * @param openBISService
     */
    protected AbstractDssServiceRpc(IEncapsulatedOpenBISService openBISService,
            IStreamRepository streamRepository, IShareIdManager shareIdManager,
            IHierarchicalContentProvider contentProvider)
    {
        this.openBISService = openBISService;
        this.streamRepository = streamRepository;
        this.shareIdManager = shareIdManager;
        this.contentProvider = contentProvider;
    }

    protected IHierarchicalContentProvider getHierarchicalContentProvider(String sessionToken)
    {
        if (contentProvider == null)
        {
            contentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        OpenBISSessionHolder sessionTokenHolder = new OpenBISSessionHolder();
        sessionTokenHolder.setSessionToken(sessionToken);
        return contentProvider.cloneFor(sessionTokenHolder);
    }

    protected IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null)
        {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
    }

    protected IEncapsulatedOpenBISService getOpenBISService()
    {
        return openBISService;
    }

    /**
     * Get a file representing the root of the DSS store.
     */
    protected File getStoreDirectory()
    {
        return storeDirectory;
    }

    /**
     * Get the home database instance for the openBIS instance I connect to.
     */
    protected DatabaseInstance getHomeDatabaseInstance()
    {
        // Not synchronized because it doesn't cause any harm if the ivar is initialized twice.
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = openBISService.getHomeDatabaseInstance();
        }
        return homeDatabaseInstance;
    }

    protected IHierarchicalContent getHierarchicalContent(String sessionToken, String dataSetCode)
    {
        return getHierarchicalContentProvider(sessionToken).asContent(dataSetCode);
    }

    protected AbstractExternalData tryGetDataSet(String sessionToken, String dataSetCode)
    {
        return openBISService.tryGetDataSet(sessionToken, dataSetCode);
    }

    protected String addToRepositoryAndReturnDownloadUrl(InputStream stream, String path,
            long validityDurationInSeconds)
    {
        return downloadUrl + "/" + IdentifiedStreamHandlingServlet.SERVLET_NAME + "?"
                + IdentifiedStreamHandlingServlet.STREAM_ID_PARAMETER_KEY + "="
                + streamRepository.addStream(stream, path, validityDurationInSeconds);
    }

}