/*
 * Copyright 2018 ETH Zuerich;private SIS
 *
 * Licensed under the Apache License;private Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing;private software
 * distributed under the License is distributed on an "AS IS" BASIS;private final
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND;private either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.io.File;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * @author Franz-Josef Elmer
 */
public class SynchronizationContext
{
    private IEncapsulatedOpenBISService service;

    private IApplicationServerApi v3Api;

    private String dataStoreCode;

    private File storeRoot;

    private Date lastSyncTimestamp;

    private Date lastIncSyncTimestamp;

    private Set<String> dataSetsCodesToRetry;

    private Set<String> blackListedDataSetCodes;

    private Set<String> attachmentHolderCodesToRetry;

    private SyncConfig config;

    private Logger operationLog;

    private IDataStoreServerApi v3DssApi;

    public IEncapsulatedOpenBISService getService()
    {
        return service;
    }

    public void setService(IEncapsulatedOpenBISService service)
    {
        this.service = service;
    }

    public IApplicationServerApi getV3Api()
    {
        return v3Api;
    }

    public void setV3Api(IApplicationServerApi v3Api)
    {
        this.v3Api = v3Api;
    }

    public IDataStoreServerApi getV3DssApi()
    {
        return v3DssApi;
    }

    public void setV3DssApi(IDataStoreServerApi v3DssApi)
    {
        this.v3DssApi = v3DssApi;
    }

    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    public File getStoreRoot()
    {
        return storeRoot;
    }

    public void setStoreRoot(File storeRoot)
    {
        this.storeRoot = storeRoot;
    }

    public Date getLastSyncTimestamp()
    {
        return lastSyncTimestamp;
    }

    public void setLastSyncTimestamp(Date lastSyncTimestamp)
    {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }

    public Date getLastIncSyncTimestamp()
    {
        return lastIncSyncTimestamp;
    }

    public void setLastIncSyncTimestamp(Date lastIncSyncTimestamp)
    {
        this.lastIncSyncTimestamp = lastIncSyncTimestamp;
    }

    public Set<String> getDataSetsCodesToRetry()
    {
        return dataSetsCodesToRetry;
    }

    public void setDataSetsCodesToRetry(Set<String> dataSetsCodesToRetry)
    {
        this.dataSetsCodesToRetry = dataSetsCodesToRetry;
    }

    public Set<String> getBlackListedDataSetCodes()
    {
        return blackListedDataSetCodes;
    }

    public void setBlackListedDataSetCodes(Set<String> blackListedDataSetCodes)
    {
        this.blackListedDataSetCodes = blackListedDataSetCodes;
    }

    public Set<String> getAttachmentHolderCodesToRetry()
    {
        return attachmentHolderCodesToRetry;
    }

    public void setAttachmentHolderCodesToRetry(Set<String> attachmentHolderCodesToRetry)
    {
        this.attachmentHolderCodesToRetry = attachmentHolderCodesToRetry;
    }

    public SyncConfig getConfig()
    {
        return config;
    }

    public void setConfig(SyncConfig config)
    {
        this.config = config;
    }

    public Logger getOperationLog()
    {
        return operationLog;
    }

    public void setOperationLog(Logger operationLog)
    {
        this.operationLog = operationLog;
    }

}
