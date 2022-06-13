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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.AuthorizationGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetAccessGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetCodeStringPredicate;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetFileDTOPredicate;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.NewDataSetPredicate;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.PrivilegeLevel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * Generic functionality for interacting with the DSS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssServiceRpcGeneric extends IRpcService
{
    public String DSS_SERVICE_NAME = "DSS Service";

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     * 
     * @param sessionToken The session token
     * @param fileOrFolder The file or folder to get information on
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     */
    @DataSetAccessGuard
    public FileInfoDssDTO[] listFilesForDataSet(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetFileDTOPredicate.class) DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     * 
     * @param sessionToken The session token
     * @param fileOrFolder The file or folder to retrieve
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     * @deprecated use {@link #getDownloadUrlForFileForDataSet(String, DataSetFileDTO)}.
     */
    @DataSetAccessGuard
    @Deprecated
    public InputStream getFileForDataSet(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetFileDTOPredicate.class) DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Returns an URL from which the requested file. The URL is valid only for a short time.
     * 
     * @param sessionToken The session token
     * @param fileOrFolder The file or folder to retrieve
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     * @since 1.4
     */
    @DataSetAccessGuard
    public String getDownloadUrlForFileForDataSet(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetFileDTOPredicate.class) DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Returns an URL from which the requested file. The URL is valid for a caller-specified amount of time.
     * 
     * @param sessionToken The session token
     * @param fileOrFolder The file or folder to retrieve
     * @param validityDurationInSeconds The number of seconds for which the download URL should be valid. The validity is clipped to the durations
     *            defined in the properties <i>data-stream-timeout</i> and <i>data-stream-max-timeout</i>, which default to 5 seconds and 4 hours,
     *            respectively.
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     * @since 1.7
     */
    @DataSetAccessGuard
    public String getDownloadUrlForFileForDataSetWithTimeout(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetFileDTOPredicate.class) DataSetFileDTO fileOrFolder,
            long validityDurationInSeconds) throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     * 
     * @param sessionToken The session token
     * @param dataSetCode The data set to retrieve file information about
     * @param path The path within the data set to retrieve file information about
     * @param isRecursive Should the result include information for sub folders?
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     */
    @DataSetAccessGuard
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeStringPredicate.class) String dataSetCode,
            String path, boolean isRecursive) throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     * 
     * @param sessionToken The session token
     * @param dataSetCode The data set to retrieve file from
     * @param path The path within the data set to retrieve file information about
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     * @deprecated use {@link #getDownloadUrlForFileForDataSet(String, String, String)}.
     */
    @DataSetAccessGuard
    @Deprecated
    public InputStream getFileForDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeStringPredicate.class) String dataSetCode,
            String path) throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Returns an URL from which the requested file of the specified data set can be downloaded. The URL is valid only for a short time.
     * 
     * @param sessionToken The session token
     * @param dataSetCode The data set to retrieve file from
     * @param path The path within the data set to retrieve file information about
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     * @since 1.4
     */
    @DataSetAccessGuard
    public String getDownloadUrlForFileForDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeStringPredicate.class) String dataSetCode,
            String path) throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Returns an URL from which the requested file of the specified data set can be downloaded. The URL is valid for a caller-specified amount of
     * time.
     * 
     * @param sessionToken The session token
     * @param dataSetCode The data set to retrieve file from
     * @param path The path within the data set to retrieve file information about
     * @param validityDurationInSeconds The number of seconds for which the download URL should be valid.
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     * @since 1.7
     */
    @DataSetAccessGuard
    public String getDownloadUrlForFileForDataSetWithTimeout(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeStringPredicate.class) String dataSetCode,
            String path, long validityDurationInSeconds) throws IOExceptionUnchecked,
            IllegalArgumentException;

    /**
     * Upload a new data set to the DSS.
     * 
     * @param sessionToken The session token
     * @param newDataset The new data set that should be registered
     * @param inputStream An input stream on the file or folder to register
     * @return The code of the newly-added data set
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     */
    @DataSetAccessGuard
    public String putDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = NewDataSetPredicate.class) NewDataSetDTO newDataset,
            InputStream inputStream) throws IOExceptionUnchecked, IllegalArgumentException;

    public void putDataSet(String sessionToken, String dropboxName, String fileName);

    /**
     * Upload a new file to the user's session workspace.
     * 
     * @param sessionToken The session token.
     * @param filePath The file path (including the sub-directory) to upload the file to.
     * @param inputStream An input stream on the file to upload.
     * @returns The number of bytes written.
     * @throws IOExceptionUnchecked Thrown if because <var>filePath</var> does not exist.
     */
    public long putFileToSessionWorkspace(String sessionToken, String filePath,
            InputStream inputStream) throws IOExceptionUnchecked;

    /**
     * Upload a file slice to the user's session workspace. If the file does not exist then it will created.
     * 
     * @param sessionToken The session token.
     * @param filePath The file path (including the sub-directory) to upload the slice to.
     * @param slicePosition The position the slice should be inserted at.
     * @param sliceInputStream An input stream of the slice to be uploaded.
     * @return The number of bytes written.
     * @throws IOExceptionUnchecked Thrown if IOException occurs.
     */
    public long putFileSliceToSessionWorkspace(String sessionToken, String filePath,
            long slicePosition, InputStream sliceInputStream) throws IOExceptionUnchecked;

    /**
     * Download a file from the user's session workspace.
     * 
     * @param sessionToken The session token.
     * @param filePath The file path (including the sub-directory) to download the file from.
     * @return The input stream containing the file content.
     * @throws IOExceptionUnchecked Thrown if an IOException occurs.
     */
    public InputStream getFileFromSessionWorkspace(String sessionToken, String filePath)
            throws IOExceptionUnchecked;

    /**
     * Delete a file or directory in the session workspace.
     * 
     * @return <code>true</code> if the <var>path</var> doesn't exist anymore.
     */
    public boolean deleteSessionWorkspaceFile(String sessionToken, String path);

    /**
     * Get a path to the data set. This can be used by clients that run on the same machine as the DSS for more efficient access to a data set.
     * <p>
     * NOTE: This method shouldn't be called for a container data set. No file would exist with the returned path.
     * </p>
     * 
     * @param sessionToken The session token
     * @param dataSetCode The data set to retrieve file from
     * @param overrideStoreRootPathOrNull The path to replace the store path (see return comment).
     * @return An absolute path to the data set. If overrideStorePathOrNull is specified, it replaces the DSS's notion of the store path. Otherwise
     *         the return value will begin with the DSS's storeRootPath.
     * @throws IOExceptionUnchecked if an IOException occurs when listing the files.
     * @throws IllegalArgumentException if <var>dataSetCode</var> is a container dataset.
     * @see IDssServiceRpcGeneric#tryGetPathToDataSet(String, String, String)
     * @since 1.1
     */
    @DataSetAccessGuard(releaseDataSetLocks = false)
    public String getPathToDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeStringPredicate.class) String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
            IllegalArgumentException;

    /**
     * Get a path to the data set. This can be used by clients that run on the same machine as the DSS for more efficient access to a data set.
     * <p>
     * NOTE: This method returns null for a container data set.
     * </p>
     * 
     * @param sessionToken The session token
     * @param dataSetCode The data set to retrieve file from
     * @param overrideStoreRootPathOrNull The path to replace the store path (see return comment).
     * @return An absolute path to the data set. If overrideStorePathOrNull is specified, it replaces the DSS's notion of the store path. Otherwise
     *         the return value will begin with the DSS's storeRootPath. For a container data set returns null.
     * @throws IOExceptionUnchecked if an IOException occurs when listing the files.
     * @see IDssServiceRpcGeneric#getPathToDataSet(String, String, String)
     * @since 1.8
     */
    @DataSetAccessGuard(releaseDataSetLocks = false)
    public String tryGetPathToDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeStringPredicate.class) String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked;

    /**
     * Lists all shares.
     * 
     * @since 1.7
     */
    @DataSetAccessGuard(privilegeLevel = PrivilegeLevel.INSTANCE_ADMIN)
    public List<ShareInfo> listAllShares(String sessionToken);

    /**
     * Moves specified data set to specified share.
     * 
     * @throws IllegalArgumentException if data set does not exit or is a container data set or share does not exist.
     * @since 1.7
     */
    @DataSetAccessGuard(privilegeLevel = PrivilegeLevel.INSTANCE_ADMIN)
    public void shuffleDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeStringPredicate.class) String dataSetCode, String shareId);

    /**
     * Get the validation script for the specified data set type.
     * 
     * @param sessionToken The session token
     * @param dataSetTypeOrNull The data set type the script should validate, or null to request the generic validation script.
     * @return The string of the python (jython) script for the validation or null if there is no applicable validation script.
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when accessing the script
     * @throws IllegalArgumentException Thrown if the data set type or startPath are not valid
     * @since 1.2
     */
    public String getValidationScript(String sessionToken, String dataSetTypeOrNull)
            throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Returns metadata for all aggregation services. See {@link IQueryApiServer#listAggregationServices(String)}
     * 
     * @since 1.6
     */
    public List<AggregationServiceDescription> listAggregationServices(String sessionToken);

    /**
     * Create the report from the specified aggregation service. See
     * {@link IQueryApiServer#createReportFromAggregationService(String, String, String, Map)}
     * 
     * @since 1.6
     */
    public QueryTableModel createReportFromAggregationService(String sessionToken,
            String aggregationServiceName, Map<String, Object> parameters);

    /**
     * Returns meta data for all reporting plugins which deliver a table. See {@link IQueryApiServer#listTableReportDescriptions(String)}
     * 
     * @since 1.6
     */
    public List<ReportDescription> listTableReportDescriptions(String sessionToken);

    /**
     * Creates for the specified data sets a report. See {@link IQueryApiServer#createReportFromDataSets(String, String, String, List)}
     * 
     * @since 1.6
     */
    public QueryTableModel createReportFromDataSets(String sessionToken, String serviceKey,
            List<String> dataSetCodes);

}
