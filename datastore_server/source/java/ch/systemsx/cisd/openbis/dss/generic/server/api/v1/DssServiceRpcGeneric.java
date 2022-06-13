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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.etlserver.plugins.DataSetMover;
import ch.systemsx.cisd.etlserver.plugins.IDataSetMover;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IStreamRepository;
import ch.systemsx.cisd.openbis.dss.generic.server.SessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.HierarchicalFileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.ShareInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils.FilterOptions;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.translator.QueryTableModelTranslator;

/**
 * Implementation of the generic RPC interface.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcGeneric extends AbstractDssServiceRpc<IDssServiceRpcGenericInternal>
        implements IDssServiceRpcGenericInternal
{
    /**
     * Logger with {@link LogCategory#OPERATION} with name of the concrete class, needs to be static for our purpose.
     */
    @SuppressWarnings("hiding")
    protected static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DssServiceRpcGeneric.class);

    private final PutDataSetService putService;

    private final IQueryApiServer queryApiServer;

    private final File sessionWorkspaceRootDirectory;

    private final IFreeSpaceProvider freeSpaceProvider;

    private IDataSetMover dataSetMover;

    private String dataStoreCode;

    /**
     * The designated constructor.
     */
    public DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService,
            IQueryApiServer apiServer, IPluginTaskInfoProvider infoProvider)
    {
        // NOTE: IShareIdManager and IHierarchicalContentProvider will be lazily created by spring
        this(openBISService, apiServer, infoProvider, new SimpleFreeSpaceProvider(), null, null);
    }

    DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService, IQueryApiServer apiServer,
            IPluginTaskInfoProvider infoProvider, IFreeSpaceProvider freeSpaceProvider,
            IShareIdManager shareIdManager, IHierarchicalContentProvider contentProvider)
    {
        this(openBISService, apiServer, infoProvider, null, freeSpaceProvider, shareIdManager,
                contentProvider, new PutDataSetService(openBISService, operationLog));
    }

    /**
     * A constructor for testing.
     */
    public DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService,
            IQueryApiServer apiServer, IPluginTaskInfoProvider infoProvider,
            IStreamRepository streamRepository, IFreeSpaceProvider freeSpaceProvider,
            IShareIdManager shareIdManager, IHierarchicalContentProvider contentProvider,
            PutDataSetService service)
    {
        super(openBISService, streamRepository, shareIdManager, contentProvider);
        queryApiServer = apiServer;
        this.freeSpaceProvider = freeSpaceProvider;
        putService = service;
        this.sessionWorkspaceRootDirectory = infoProvider.getSessionWorkspaceRootDir();
        operationLog.info("[rpc] Started DSS API V1 service.");
    }

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    void setDataSetMover(IDataSetMover dataSetMover)
    {
        this.dataSetMover = dataSetMover;
    }

    @Override
    public IDssServiceRpcGenericInternal createLogger(IInvocationLoggerContext context)
    {
        return new DssServiceRpcGenericLogger(context);
    }

    @Override
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String startPath, boolean isRecursive) throws IllegalArgumentException
    {
        IHierarchicalContent content = null;
        try
        {
            content = getHierarchicalContent(sessionToken, dataSetCode);
            IHierarchicalContentNode startPathNode = getContentNode(content, startPath);
            ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
            if (startPathNode.isDirectory())
            {
                appendFileInfosForFile(startPathNode, list, isRecursive);
            } else
            {
                list.add(new FileInfoDssDTO(startPathNode.getRelativePath(), startPathNode
                        .getName(), false, startPathNode.getFileLength(), startPathNode
                        .isChecksumCRC32Precalculated() ? startPathNode.getChecksumCRC32() : null));
            }
            FileInfoDssDTO[] fileInfos = new FileInfoDssDTO[list.size()];
            return list.toArray(fileInfos);
        } catch (IOException ex)
        {
            operationLog.info("listFiles: " + startPath + " caused an exception", ex);
            throw new IOExceptionUnchecked(ex);
        } catch (RuntimeException ex)
        {
            operationLog.info("listFiles: " + startPath + " caused an exception", ex);
            throw ex;
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }
    }

    @Override
    public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        IHierarchicalContent content = null;
        try
        {
            content = getHierarchicalContent(sessionToken, dataSetCode);
            IHierarchicalContentNode contentNode = getContentNode(content, path);
            return HierarchicalContentUtils.getInputStreamAutoClosingContent(contentNode, content);
        } catch (RuntimeException ex)
        {
            operationLog.info("getFile: " + path + " caused an exception", ex);
            if (content != null)
            {
                // close content only on exception, otherwise stream close should close the content
                content.close();
            }
            throw ex;
        }
    }

    @Override
    public String getDownloadUrlForFileForDataSet(String sessionToken, String dataSetCode,
            String path) throws IOExceptionUnchecked, IllegalArgumentException
    {
        InputStream stream = getFileForDataSet(sessionToken, dataSetCode, path);
        return addToRepositoryAndReturnDownloadUrl(stream, path, 0);
    }

    @Override
    public String getDownloadUrlForFileForDataSetWithTimeout(String sessionToken,
            String dataSetCode, String path, long validityDurationInSeconds)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        InputStream stream = getFileForDataSet(sessionToken, dataSetCode, path);
        return addToRepositoryAndReturnDownloadUrl(stream, path, validityDurationInSeconds);
    }

    private IHierarchicalContentNode getContentNode(IHierarchicalContent content, String startPath)
    {
        // handle both relative and absolute paths for backward compatibility
        return content.getNode(startPath.startsWith("/") ? startPath.substring(1) : startPath);
    }

    @Override
    public String putDataSet(String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return putService.putDataSet(sessionToken, newDataSet, inputStream);
    }

    @Override
    public void putDataSet(String sessionToken, String dropboxName, String fileName)
    {
        File workspaceDir = new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                .getSessionWorkspace();
        File file = new File(workspaceDir, FilenameUtils.getName(fileName));
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copyLarge(inputStream, outputStream);
            CustomImportFile customImportFile = new CustomImportFile(fileName, outputStream.toByteArray());
            putService.putDataSet(sessionToken, dropboxName, customImportFile);
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public long putFileToSessionWorkspace(String sessionToken, String filePath,
            InputStream inputStream) throws IOExceptionUnchecked
    {
        getOpenBISService().checkSession(sessionToken);
        if (filePath.contains("../"))
        {
            throw new IOExceptionUnchecked("filePath must not contain '../'");
        }
        final String subDir = FilenameUtils.getFullPath(filePath);
        final String filename = FilenameUtils.getName(filePath);
        final File workspaceDir =
                new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                        .getSessionWorkspace();
        final File dir = new File(workspaceDir, subDir);
        dir.mkdirs();
        final File file = new File(dir, filename);
        OutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(file);
            long size = IOUtils.copyLarge(inputStream, ostream);
            ostream.close();
            return size;
        } catch (IOException ex)
        {
            file.delete();
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(ostream);
        }
    }

    @Override
    public long putFileSliceToSessionWorkspace(String sessionToken, String filePath,
            long slicePosition, InputStream sliceInputStream) throws IOExceptionUnchecked
    {
        getOpenBISService().checkSession(sessionToken);
        if (filePath.contains("../"))
        {
            throw new IOExceptionUnchecked("filePath must not contain '../'");
        }
        final String subDir = FilenameUtils.getFullPath(filePath);
        final String filename = FilenameUtils.getName(filePath);
        final File workspaceDir =
                new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                        .getSessionWorkspace();
        final File dir = new File(workspaceDir, subDir);
        dir.mkdirs();
        final File file = new File(dir, filename);

        return FileUtilities.writeToFile(file, slicePosition, sliceInputStream);
    }

    @Override
    public InputStream getFileFromSessionWorkspace(String sessionToken, String filePath)
            throws IOExceptionUnchecked
    {
        getOpenBISService().checkSession(sessionToken);
        if (filePath.contains("../"))
        {
            throw new IOExceptionUnchecked("filePath must not contain '../'");
        }
        final File workspaceDir =
                new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                        .getSessionWorkspace();
        final File file = new File(workspaceDir, filePath);
        try
        {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public boolean deleteSessionWorkspaceFile(String sessionToken, String path)
    {
        getOpenBISService().checkSession(sessionToken);
        if (path.contains("../"))
        {
            throw new IOExceptionUnchecked("path must not contain '../'");
        }
        final File workspaceDir =
                new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                        .getSessionWorkspace();
        final File file = new File(workspaceDir, path);
        FileUtilities.deleteRecursively(file);
        return file.exists() == false;
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 8;
    }

    /**
     * Append file info for the requested node of a file or file hierarchy. Assumes that the parameters have been verified already.
     * 
     * @param requestedFile A file known to be accessible by the user
     * @param dataSetRoot The root of the file hierarchy; used to determine the absolute path of the file
     * @param listingRootNode The node which is a root of the list hierarchy; used to determine the relative path of the file
     * @param list The list the files infos are appended to
     * @param isRecursive If true, directories will be recursively appended to the list
     */
    private void appendFileInfosForFile(IHierarchicalContentNode listingRootNode,
            ArrayList<FileInfoDssDTO> list, boolean isRecursive) throws IOException
    {
        HierarchicalFileInfoDssBuilder factory =
                new HierarchicalFileInfoDssBuilder(listingRootNode);
        factory.appendFileInfos(list, isRecursive);
    }

    @Override
    public InputStream getFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return this.getFileForDataSet(sessionToken, fileOrFolder.getDataSetCode(),
                fileOrFolder.getPath());
    }

    @Override
    public String getDownloadUrlForFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        InputStream stream = getFileForDataSet(sessionToken, fileOrFolder);
        return addToRepositoryAndReturnDownloadUrl(stream, fileOrFolder.getPath(), 0);
    }

    @Override
    public String getDownloadUrlForFileForDataSetWithTimeout(String sessionToken,
            DataSetFileDTO fileOrFolder, long validityDurationInSeconds)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        InputStream stream = getFileForDataSet(sessionToken, fileOrFolder);
        return addToRepositoryAndReturnDownloadUrl(stream, fileOrFolder.getPath(),
                validityDurationInSeconds);
    }

    @Override
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return this.listFilesForDataSet(sessionToken, fileOrFolder.getDataSetCode(),
                fileOrFolder.getPath(), fileOrFolder.isRecursive());
    }

    @Override
    public void setStoreDirectory(File aFile)
    {
        super.setStoreDirectory(aFile);
        putService.setStoreDirectory(aFile);
    }

    @Override
    public String getPathToDataSet(String sessionToken, String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        final File dataSetRootDirectory = tryGetDataSetRootDirectory(dataSetCode);

        // if container data set - see NOTE in interface documentation
        if (dataSetRootDirectory.exists() == false)
        {
            throw new IllegalArgumentException("Path to dataset '" + dataSetCode
                    + "' not available: this is a container dataset.");
        }

        return convertPath(getStoreDirectory(), dataSetRootDirectory, overrideStoreRootPathOrNull);
    }

    @Override
    public String tryGetPathToDataSet(String sessionToken, String dataSetCode, String overrideStoreRootPathOrNull) throws IOExceptionUnchecked
    {
        final File dataSetRootDirectory = tryGetDataSetRootDirectory(dataSetCode);

        // if container data set - see NOTE in interface documentation
        if (dataSetRootDirectory.exists() == false)
        {
            return null;
        }

        return convertPath(getStoreDirectory(), dataSetRootDirectory, overrideStoreRootPathOrNull);
    }

    private File tryGetDataSetRootDirectory(String dataSetCode)
    {
        return DatasetLocationUtil.getDatasetLocationPath(getStoreDirectory(), dataSetCode,
                getShareIdManager().getShareId(dataSetCode), getHomeDatabaseInstance()
                        .getUuid());
    }

    @Override
    public List<ShareInfo> listAllShares(String sessionToken)
    {
        getOpenBISService().checkSession(sessionToken);
        List<Share> shares =
                SegmentedStoreUtils.getSharesWithDataSets(getStoreDirectory(), dataStoreCode,
                        FilterOptions.ALL, Collections.<String> emptySet(), freeSpaceProvider,
                        getOpenBISService(), new Log4jSimpleLogger(operationLog));
        List<ShareInfo> result = new ArrayList<ShareInfo>();
        for (Share share : shares)
        {
            ShareInfo shareInfo = new ShareInfo(share.getShareId(), share.calculateFreeSpace());
            shareInfo.setIgnoredForShuffling(share.isIgnoredForShuffling());
            shareInfo.setIncoming(share.isIncoming());
            shareInfo.setWithdrawShare(share.isWithdrawShare());
            result.add(shareInfo);
        }
        return result;
    }

    @Override
    public void shuffleDataSet(String sessionToken, String dataSetCode, String shareId)
    {
        getOpenBISService().checkSession(sessionToken);
        AbstractExternalData dataSet = getOpenBISService().tryGetDataSet(dataSetCode);
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set: " + dataSetCode);
        }
        if (dataSet.isContainer())
        {
            throw new UserFailureException("Container data set: " + dataSetCode);
        }
        PhysicalDataSet realDataSet = dataSet.tryGetAsDataSet();
        String dataSetLocation = realDataSet.getDataSetLocation();
        if (realDataSet.getShareId().equals(shareId))
        {
            throw new UserFailureException("Data set " + dataSetCode + " is already in share "
                    + shareId + ".");
        }
        File share = new File(getStoreDirectory(), getShareIdManager().getShareId(dataSetCode));
        File newShare = new File(getStoreDirectory(), shareId);
        if (newShare.exists() == false)
        {
            throw new UserFailureException("Share does not exists: " + newShare.getAbsolutePath());
        }
        if (newShare.isDirectory() == false)
        {
            throw new UserFailureException("Share is not a directory: "
                    + newShare.getAbsolutePath());
        }
        getDataSetMover().moveDataSetToAnotherShare(new File(share, dataSetLocation), newShare,
                null, new Log4jSimpleLogger(operationLog));
    }

    private IDataSetMover getDataSetMover()
    {
        if (dataSetMover == null)
        {
            dataSetMover = new DataSetMover(getOpenBISService(), getShareIdManager());
        }
        return dataSetMover;
    }

    public static String convertPath(File storeRoot, File dataSetRoot,
            String overrideStoreRootPathOrNull)
    {
        String dataStoreRootPath = storeRoot.getAbsolutePath();
        String dataSetPath = dataSetRoot.getAbsolutePath();

        // No override specified; give the user the path as we understand it.
        if (null == overrideStoreRootPathOrNull
                || false == dataSetPath.startsWith(dataStoreRootPath))
        {
            return dataSetPath;
        }

        // Make the path begin with the user's store root override
        File usersPath =
                new File(overrideStoreRootPathOrNull, dataSetPath.substring(dataStoreRootPath
                        .length()));
        return usersPath.getPath();
    }

    @Override
    public String getValidationScript(String sessionToken, String dataSetTypeOrNull)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return putService.getValidationScript(dataSetTypeOrNull);
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String sessionToken,
            String aggregationServiceName, Map<String, Object> parameters)
    {
        IEncapsulatedOpenBISService openBisService = getOpenBISService();
        IDataStoreServiceInternal service = ServiceProvider.getDataStoreService();
        SessionContextDTO sessionContext = openBisService.tryGetSession(sessionToken);
        TableModel tableModel =
                service.internalCreateReportFromAggregationService(sessionToken,
                        aggregationServiceName, parameters, sessionContext.getUserName(),
                        sessionContext.getUserEmail());
        return new QueryTableModelTranslator(tableModel).translate();
    }

    @Override
    public QueryTableModel createReportFromDataSets(String sessionToken, String serviceKey,
            List<String> dataSetCodes)
    {
        IEncapsulatedOpenBISService openBisService = getOpenBISService();
        IDataStoreServiceInternal service = ServiceProvider.getDataStoreService();
        SessionContextDTO sessionContext = openBisService.tryGetSession(sessionToken);
        List<DatasetDescription> dataSetDescriptions = new ArrayList<DatasetDescription>();
        for (String dataSetCode : dataSetCodes)
        {
            AbstractExternalData dataSet = getOpenBISService().tryGetDataSet(dataSetCode);
            dataSetDescriptions.add(translateToDescription(dataSet));
        }
        TableModel tableModel =
                service.internalCreateReportFromDatasets(sessionToken, serviceKey,
                        dataSetDescriptions, sessionContext.getUserName(),
                        sessionContext.getUserEmail());
        return new QueryTableModelTranslator(tableModel).translate();
    }

    private static DatasetDescription translateToDescription(AbstractExternalData dataSet)
    {
        return DataSetTranslator.translateToDescription(dataSet);
    }

    @Override
    public List<AggregationServiceDescription> listAggregationServices(String sessionToken)
    {
        return queryApiServer.listAggregationServices(sessionToken);
    }

    @Override
    public List<ReportDescription> listTableReportDescriptions(String sessionToken)
    {
        return queryApiServer.listTableReportDescriptions(sessionToken);
    }
}
