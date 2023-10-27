/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.dssapi.v3;

import java.io.InputStream;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * V3 data store server API. Detailed documentation on how to use the API together code examples in both Java and Javascript can be found at "openBIS
 * V3 API" openBIS WIKI page.
 * 
 * @author Jakub Straszewski
 */
public interface IDataStoreServerApi extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "data-store-server";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v3";

    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    /**
     * Searches for metadata of data set files basing on the provided {@code DataSetFileSearchCriteria}.
     * <p>
     * By default the returned data set files metdata contains only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code DataSetFileFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     * 
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriteria searchCriteria, DataSetFileFetchOptions fetchOptions);

    /**
     * Downloads files with the provided {@code IDataSetFileId} ids. The requested files are returned as a single {@code InputStream}.
     * {@code DataSetFileDownloadReader} can be used to easily read individual files from that stream. Additional download options can be set via
     * {@code DataSetFileDownloadOptions}.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     * 
     * @throws UserFailureException in case of any problems
     */
    public InputStream downloadFiles(String sessionToken, List<? extends IDataSetFileId> fileIds,
            DataSetFileDownloadOptions downloadOptions);
    
    /**
     * Creates a download session for fast download of the requested files.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     * 
     * @throws UserFailureException in case of any problems
     */
    public FastDownloadSession createFastDownloadSession(String sessionToken, List<? extends IDataSetFileId> fileIds, 
            FastDownloadSessionOptions options);

    /**
     * Creates a data set which files have been already uploaded to data store server /store_share_file_upload servlet. Uploaded files and a data set
     * to be created have to share the same upload id (the id can be set via {@code uploadID} request parameter during the upload and via
     * {@code UploadedDataSetCreation} during a data set creation).
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     * 
     * @throws UserFailureException in case of any problems
     */
    public DataSetPermId createUploadedDataSet(String sessionToken, UploadedDataSetCreation newDataSet);

    /**
     * Creates full data sets (i.e. both data set metadata as well as data set files metadata).
     * <p>
     * Required access rights: {@code SPACE_ETL_SERVER} or stronger
     * </p>
     * 
     * @throws UserFailureException in case of any problems
     */
    public List<DataSetPermId> createDataSets(String sessionToken, List<FullDataSetCreation> newDataSets);


    /**
     * Executes a custom data store server service with the provided {@code ICustomDSSServiceId} id. Additional execution options (e.g. parameters)
     * can be set via {@code CustomDSSServiceExecutionOptions}.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Object executeCustomDSSService(String sessionToken, ICustomDSSServiceId serviceId, CustomDSSServiceExecutionOptions options);

}
