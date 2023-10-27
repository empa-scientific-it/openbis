/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.common.pat.IPersonalAccessTokenInvocation;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConverter;

public class DataStoreServerApiPersonalAccessTokenInvocationHandler implements IDataStoreServerApi
{
    private final IPersonalAccessTokenInvocation invocation;

    public DataStoreServerApiPersonalAccessTokenInvocationHandler(final IPersonalAccessTokenInvocation invocation)
    {
        this.invocation = invocation;
    }

    @Override public int getMajorVersion()
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public int getMinorVersion()
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public SearchResult<DataSetFile> searchFiles(final String sessionToken, final DataSetFileSearchCriteria searchCriteria,
            final DataSetFileFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(getConverter().convert(sessionToken));
    }

    @Override public InputStream downloadFiles(final String sessionToken, final List<? extends IDataSetFileId> fileIds,
            final DataSetFileDownloadOptions downloadOptions)
    {
        return invocation.proceedWithNewFirstArgument(getConverter().convert(sessionToken));
    }

    @Override public FastDownloadSession createFastDownloadSession(final String sessionToken, final List<? extends IDataSetFileId> fileIds,
            final FastDownloadSessionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(getConverter().convert(sessionToken));
    }

    @Override public DataSetPermId createUploadedDataSet(final String sessionToken, final UploadedDataSetCreation newDataSet)
    {
        return invocation.proceedWithNewFirstArgument(getConverter().convert(sessionToken));
    }

    @Override public List<DataSetPermId> createDataSets(final String sessionToken, final List<FullDataSetCreation> newDataSets)
    {
        return invocation.proceedWithNewFirstArgument(getConverter().convert(sessionToken));
    }

    @Override
    public Object executeCustomDSSService(String sessionToken, ICustomDSSServiceId serviceId,
            CustomDSSServiceExecutionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(getConverter().convert(sessionToken));
    }

    public IPersonalAccessTokenConverter getConverter()
    {
        return ServiceProvider.getPersonalAccessTokenConverter();
    }
}
