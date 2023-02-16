/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.AuthorizationGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetAccessGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetFileDTOPredicate;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.ShareInfo;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcGenericLogger extends AbstractServerLogger implements
        IDssServiceRpcGenericInternal
{
    DssServiceRpcGenericLogger(IInvocationLoggerContext context)
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
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "list_files_for_data_set", "DATA_SET(%s)", fileOrFolder);
        return null;
    }

    @Override
    public InputStream getFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_file_for_data_set", "DATA_SET(%s)", fileOrFolder);
        return null;
    }

    @Override
    public String getDownloadUrlForFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_download_url_for_file_for_data_set", "DATA_SET(%s)",
                fileOrFolder);
        return null;
    }

    @Override
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String path, boolean isRecursive) throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "list_files_for_data_set", "DATA_SET(%s) PATH(%s) RECURSIVE(%s)",
                dataSetCode, path, isRecursive);
        return null;
    }

    @Override
    public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_file_for_data_set", "DATA_SET(%s) PATH(%s)", dataSetCode, path);
        return null;
    }

    @Override
    public String getDownloadUrlForFileForDataSet(String sessionToken, String dataSetCode,
            String path) throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_download_url_for_file_for_data_set", "DATA_SET(%s) PATH(%s)",
                dataSetCode, path);
        return null;
    }

    @Override
    public String putDataSet(String sessionToken, NewDataSetDTO newDataset, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logTracking(sessionToken, "put_data_set", "DATA_SET(%s)", newDataset);
        return null;
    }

    @Override
    public void putDataSet(String sessionToken, String dropboxName, String fileName)
    {
        logTracking(sessionToken, "put_data_set", "DROP_BOX(%s) FILE(%s)", dropboxName, fileName);
    }

    @Override
    @DataSetAccessGuard
    public long putFileToSessionWorkspace(String sessionToken, String filePath,
            InputStream inputStream) throws IOExceptionUnchecked
    {
        logTracking(sessionToken, "put_file_to_session_workspace", "FILE_PATH(%s)", filePath);
        return 0;
    }

    @Override
    public File putDirToSessionWorkspace(String sessionToken, String filePath, boolean isEmptyDirectory) throws IOExceptionUnchecked {
        logTracking(sessionToken, "putDirToSessionWorkspace", "FILE_PATH(%s) EMPTY_DIRECTORY(%s)", filePath, isEmptyDirectory);
        return null;
    }

    @Override
    public long putFileSliceToSessionWorkspace(String sessionToken, String filePath,
            long slicePosition, InputStream sliceInputStream) throws IOExceptionUnchecked
    {
        logTracking(sessionToken, "put_file_slice_to_session_workspace",
                "FILE_PATH(%s) SLICE_POSITION(%s)", filePath, slicePosition);
        return 0;
    }

    @Override
    @DataSetAccessGuard
    public InputStream getFileFromSessionWorkspace(String sessionToken, String filePath)
            throws IOExceptionUnchecked
    {
        logAccess(sessionToken, "get_file_from_session_workspace", "FILE_PATH(%s)", filePath);
        return null;
    }

    @Override
    public boolean deleteSessionWorkspaceFile(String sessionToken, String path)
    {
        logTracking(sessionToken, "delete_session_workspace_file", "PATH(%s)", path);
        return false;
    }

    @Override
    public String getPathToDataSet(String sessionToken, String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        logAccess(sessionToken, "get_path_to_data_set", "DATA_SET(%s) STORE_ROOT_PATH(%s)",
                dataSetCode, overrideStoreRootPathOrNull);
        return null;
    }

    @Override
    public String tryGetPathToDataSet(String sessionToken, String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        logAccess(sessionToken, "try_get_path_to_data_set", "DATA_SET(%s) STORE_ROOT_PATH(%s)",
                dataSetCode, overrideStoreRootPathOrNull);
        return null;
    }

    @Override
    public List<ShareInfo> listAllShares(String sessionToken)
    {
        logAccess(sessionToken, "list_all_shares");
        return null;
    }

    @Override
    public void shuffleDataSet(String sessionToken, String dataSetCode, String shareID)
    {
        logTracking(sessionToken, "shuffle_data_set", "DATA_SET(%s) SHARE(%s)", dataSetCode,
                shareID);
    }

    @Override
    public void setStoreDirectory(File aFile)
    {
    }

    public void setIncomingDirectory(File aFile)
    {
    }

    @Override
    public String getValidationScript(String sessionToken, String dataSetTypeOrNull)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_validation_script", "DATA_SET_TYPE(%s)", dataSetTypeOrNull);
        return null;
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String sessionToken,
            String aggregationServiceName, Map<String, Object> parameters)
    {
        logAccess(sessionToken, "create_report_from_aggregation_service", "SERVICE_KEY(%s)",
                aggregationServiceName);
        return null;
    }

    @Override
    public QueryTableModel createReportFromDataSets(String sessionToken, String serviceKey,
            List<String> dataSetCodes)
    {
        logAccess(sessionToken, "create_report_from_data_sets", "SERVICE_KEY(%s)", serviceKey);
        return null;
    }

    @Override
    public List<AggregationServiceDescription> listAggregationServices(String sessionToken)
    {
        logAccess(sessionToken, "list-aggregation-services");
        return null;
    }

    @Override
    public List<ReportDescription> listTableReportDescriptions(String sessionToken)
    {
        logAccess(sessionToken, "list-table-report-descriptions");
        return null;
    }

    @Override
    public String getDownloadUrlForFileForDataSetWithTimeout(String sessionToken,
            String dataSetCode, String path, long validityDurationInSeconds)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_download_url_for_file_for_data_set",
                "DATA_SET(%s) PATH(%s) VALIDITY(%s)", dataSetCode, path, validityDurationInSeconds);
        return null;
    }

    @Override
    @DataSetAccessGuard
    public String getDownloadUrlForFileForDataSetWithTimeout(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetFileDTOPredicate.class) DataSetFileDTO fileOrFolder,
            long validityDurationInSeconds) throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_download_url_for_file_for_data_set",
                "DATA_SET(%s) VALIDITY(%s)", fileOrFolder, validityDurationInSeconds);
        return null;
    }
}
