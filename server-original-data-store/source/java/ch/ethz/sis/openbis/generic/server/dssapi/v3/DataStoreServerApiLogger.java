/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import java.io.InputStream;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;

public class DataStoreServerApiLogger extends AbstractServerLogger implements
        IDataStoreServerApi
{
    DataStoreServerApiLogger(IInvocationLoggerContext context)
    {
        super(null, context);
    }

    @Override
    public int getMajorVersion()
    {
        return 0;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public SearchResult<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriteria searchCriteria, DataSetFileFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-files", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public InputStream downloadFiles(String sessionToken, List<? extends IDataSetFileId> fileIds,
            DataSetFileDownloadOptions downloadOptions)
    {
        logAccess(sessionToken, "download-files", "FILE_IDS(%s) DOWNLOAD_OPTIONS(%s)", fileIds, downloadOptions);
        return null;
    }

    @Override
    public FastDownloadSession createFastDownloadSession(String sessionToken, List<? extends IDataSetFileId> fileIds,
            FastDownloadSessionOptions options)
    {
        logAccess(sessionToken, "create-fast-download-session", "FILE_IDS(%s) DOWNLOAD_OPTIONS(%s)", fileIds, options);
        return null;
    }

    @Override
    public DataSetPermId createUploadedDataSet(String sessionToken, UploadedDataSetCreation newDataSet)
    {
        logAccess(sessionToken, "create-uploaded-data-sets", "DATA_SETS(%s)", newDataSet);
        return null;
    }

    @Override
    public List<DataSetPermId> createDataSets(String sessionToken, List<FullDataSetCreation> newDataSets)
    {
        logAccess(sessionToken, "create-data-sets", "DATA_SETS(%s)", newDataSets);
        return null;
    }

    @Override
    public Object executeCustomDSSService(String sessionToken, ICustomDSSServiceId serviceId,
            CustomDSSServiceExecutionOptions options)
    {
        logAccess(sessionToken, "execute-custom-dss-service", "SERVICE_ID(%s) SERVICE_OPTIONS(%s)", serviceId, options);
        return null;
    }
}