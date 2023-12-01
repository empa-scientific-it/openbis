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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.executor.IExecuteCustomDSSServiceExecutor;

import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.download.DataSetFileDownloadInputStream;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.executor.ICreateUploadedDataSetExecutor;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo.PathInfoFeeder;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.common.pat.IPersonalAccessTokenAware;
import ch.systemsx.cisd.openbis.common.pat.IPersonalAccessTokenInvocation;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IStreamRepository;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Jakub Straszewski
 */
@Component(Constants.INTERNAL_SERVICE_NAME)
public class DataStoreServerApi extends AbstractDssServiceRpc<IDataStoreServerApi>
        implements IDataStoreServerApi, IPersonalAccessTokenAware
{
    @Autowired
    private IConfigProvider configProvider;

    @Autowired
    private IApplicationServerApi as;

    @Autowired
    private ICreateUploadedDataSetExecutor createUploadedDataSetExecutor;

    @Autowired
    private IExecuteCustomDSSServiceExecutor executor;

    /**
     * The designated constructor.
     */
    @Autowired
    public DataStoreServerApi(IEncapsulatedOpenBISService openBISService, IQueryApiServer apiServer,
            IPluginTaskInfoProvider infoProvider)
    {
        // NOTE: IShareIdManager and IHierarchicalContentProvider will be lazily created by spring
        this(openBISService, apiServer, infoProvider, new SimpleFreeSpaceProvider(), null, null);
    }

    DataStoreServerApi(IEncapsulatedOpenBISService openBISService, IQueryApiServer apiServer,
            IPluginTaskInfoProvider infoProvider, IFreeSpaceProvider freeSpaceProvider,
            IShareIdManager shareIdManager, IHierarchicalContentProvider contentProvider)
    {
        this(openBISService, apiServer, infoProvider, null, freeSpaceProvider, shareIdManager,
                contentProvider);
    }

    /**
     * A constructor for testing.
     */
    public DataStoreServerApi(IEncapsulatedOpenBISService openBISService, IQueryApiServer apiServer,
            IPluginTaskInfoProvider infoProvider, IStreamRepository streamRepository,
            IFreeSpaceProvider freeSpaceProvider, IShareIdManager shareIdManager,
            IHierarchicalContentProvider contentProvider)
    {
        super(openBISService, streamRepository, shareIdManager, contentProvider);
        // queryApiServer = apiServer;
        // this.freeSpaceProvider = freeSpaceProvider;
        // putService = service;
        // this.sessionWorkspaceRootDirectory = infoProvider.getSessionWorkspaceRootDir();
        operationLog.info("[rpc] Started DSS API V3 service.");
    }

    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Override
    public SearchResult<DataSetFile> searchFiles(String sessionToken,
            DataSetFileSearchCriteria searchCriteria, DataSetFileFetchOptions fetchOptions)
    {
        getOpenBISService().checkSession(sessionToken);

        List<DataSetFile> result = new ArrayList<>();
        Collection<ISearchCriteria> criteria = searchCriteria.getCriteria();

        Set<String> resultDataSets = null;
        Map<String, DataSet> dataSetMap = new HashMap<>();

        String dataStoreCode = configProvider.getDataStoreCode();

        for (ISearchCriteria iSearchCriterion : criteria)
        {
            if (iSearchCriterion instanceof DataSetSearchCriteria)
            {
                DataSetFetchOptions fo = new DataSetFetchOptions();
                fo.withDataStore();
                SearchResult<DataSet> searchResult =
                        as.searchDataSets(sessionToken, (DataSetSearchCriteria) iSearchCriterion,
                                fo);
                List<DataSet> dataSets = searchResult.getObjects();
                HashSet<String> codes = new HashSet<String>();

                for (DataSet dataSet : dataSets)
                {
                    if (dataStoreCode.equals(dataSet.getDataStore().getCode()))
                    {
                        codes.add(dataSet.getCode());
                        dataSetMap.put(dataSet.getCode(), dataSet);
                    }
                }

                if (resultDataSets == null)
                {
                    resultDataSets = codes;
                } else if (searchCriteria.getOperator().equals(SearchOperator.OR)) // is an or
                {
                    resultDataSets.addAll(codes);
                } else
                { // is an and
                    resultDataSets.retainAll(codes);
                }
            }
        }

        if (resultDataSets != null)
        {
            for (String code : resultDataSets)
            {
                IHierarchicalContent content =
                        getHierarchicalContentProvider(sessionToken).asContent(code);

                try
                {
                    for (IHierarchicalContentNode node : iterate(content.getRootNode()))
                    {
                        DataSet dataSet = dataSetMap.get(code);
                        result.add(Utils.createDataSetFile(code, node, dataSet.getDataStore()));
                    }
                } finally
                {
                    if (content != null)
                    {
                        content.close();
                    }
                }
            }
        }

        return new SearchResult<DataSetFile>(result, result.size());
    }

    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Override
    public FastDownloadSession createFastDownloadSession(String sessionToken,
            List<? extends IDataSetFileId> fileIds, FastDownloadSessionOptions options)
    {
        getOpenBISService().checkSession(sessionToken);
        List<IDataSetFileId> files = new ArrayList<>();
        for (Entry<String, Set<String>> entry : sortFilesByDataSets(fileIds).entrySet())
        {
            String dataSetCode = entry.getKey();
            DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
            Status authorizationStatus = DssSessionAuthorizationHolder.getAuthorizer()
                    .checkDatasetAccess(sessionToken, dataSetCode);
            if (authorizationStatus.isOK())
            {
                files.addAll(entry.getValue().stream().map(p -> new DataSetFilePermId(dataSetId, p))
                        .collect(Collectors.toList()));
            }
        }
        return new FastDownloadSession(getDownloadUrl(), sessionToken, files, options);
    }

    private String getDownloadUrl()
    {
        Properties serviceProperties =
                (Properties) ServiceProvider.getApplicationContext().getBean("configProperties");
        return serviceProperties.getProperty(
                "download-url") + "/" + GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/" + FileTransferServerServlet.SERVLET_NAME;
    }

    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Override
    public InputStream downloadFiles(String sessionToken, List<? extends IDataSetFileId> fileIds,
            DataSetFileDownloadOptions downloadOptions)
    {
        getOpenBISService().checkSession(sessionToken);

        Map<String, Set<String>> filesByDataSet = sortFilesByDataSets(fileIds);

        IHierarchicalContentProvider contentProvider = getHierarchicalContentProvider(sessionToken);
        Map<IHierarchicalContentNode, String> contentNodes =
                new LinkedHashMap<IHierarchicalContentNode, String>();
        boolean recursive = downloadOptions.isRecursive();
        for (Entry<String, Set<String>> entry : filesByDataSet.entrySet())
        {
            String dataSetCode = entry.getKey();
            Status authorizationStatus = DssSessionAuthorizationHolder.getAuthorizer()
                    .checkDatasetAccess(sessionToken, dataSetCode);
            if (authorizationStatus.isOK())
            {
                IHierarchicalContent content = contentProvider.asContent(dataSetCode);
                IHierarchicalContentNode rootNode = content.getRootNode();
                Map<String, IHierarchicalContentNode> nodesByPath = new TreeMap<>();
                Set<String> paths = entry.getValue();
                populate(nodesByPath, rootNode, paths);
                for (String path : paths)
                {
                    IHierarchicalContentNode node = nodesByPath.get(path);
                    if (node.isDirectory() && recursive)
                    {
                        for (IHierarchicalContentNode child : iterate(node))
                        {
                            contentNodes.put(child, dataSetCode);
                        }
                    } else
                    {
                        contentNodes.put(node, dataSetCode);
                    }
                }
            }
        }

        return new DataSetFileDownloadInputStream(contentNodes);
    }

    private void populate(Map<String, IHierarchicalContentNode> nodesByPath,
            IHierarchicalContentNode node, Set<String> paths)
    {
        if (paths.contains(node.getRelativePath()))
        {
            nodesByPath.put(node.getRelativePath(), node);
        }
        if (node.isDirectory())
        {
            List<IHierarchicalContentNode> childNodes = node.getChildNodes();
            for (IHierarchicalContentNode childNode : childNodes)
            {
                populate(nodesByPath, childNode, paths);
            }
        }
    }

    private Map<String, Set<String>> sortFilesByDataSets(List<? extends IDataSetFileId> fileIds)
    {
        Map<String, Set<String>> filesByDataSet = new TreeMap<>();
        for (IDataSetFileId fileId : fileIds)
        {
            if (fileId instanceof DataSetFilePermId == false)
            {
                throw new IllegalArgumentException("Unsupported fileId: " + fileId);
            }
            DataSetFilePermId filePermId = (DataSetFilePermId) fileId;
            if (filePermId.getDataSetId() instanceof DataSetPermId == false)
            {
                throw new IllegalArgumentException(
                        "Unsupported dataSetId: " + filePermId.getDataSetId());
            }
            String dataSetCode = ((DataSetPermId) filePermId.getDataSetId()).getPermId();
            Set<String> ids = filesByDataSet.get(dataSetCode);
            if (ids == null)
            {
                ids = new TreeSet<>();
                filesByDataSet.put(dataSetCode, ids);
            }
            String filePath = filePermId.getFilePath();
            ids.add(filePath == null ? "" : filePath);
        }
        return filesByDataSet;
    }

    private Iterable<IHierarchicalContentNode> iterate(final IHierarchicalContentNode node)
    {
        return new Iterable<IHierarchicalContentNode>()
        {
            @Override
            public Iterator<IHierarchicalContentNode> iterator()
            {
                IteratorChain<IHierarchicalContentNode> chain =
                        new IteratorChain<>(Collections.singleton(node).iterator());

                if (node.isDirectory())
                {
                    for (IHierarchicalContentNode child : node.getChildNodes())
                    {
                        chain.addIterator(iterate(child).iterator());
                    }
                }
                return chain;
            }
        };
    }

    @Override
    public int getMajorVersion()
    {
        return 3;
    }

    @Override
    public int getMinorVersion()
    {
        return 6;
    }

    @Override
    public IDataStoreServerApi createLogger(IInvocationLoggerContext context)
    {
        return new DataStoreServerApiLogger(context);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER })
    public DataSetPermId createUploadedDataSet(String sessionToken,
            UploadedDataSetCreation creation)
    {
        return createUploadedDataSetExecutor.create(sessionToken, creation);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN })
    public List<DataSetPermId> createDataSets(String sessionToken,
            List<FullDataSetCreation> newDataSets)
    {
        if (PathInfoDataSourceProvider.isDataSourceDefined() == false)
        {
            throw new IllegalStateException(
                    "Pathinfo DB not configured - cannot store dataset file information");
        }
        injectDataStoreIdAndCodesIfNeeded(newDataSets);
        IPathsInfoDAO dao =
                QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);

        for (int i = 0; i < newDataSets.size(); i++)
        {
            FullDataSetCreation fullDataSetCreation = newDataSets.get(i);
            List<DataSetFileCreation> files = fullDataSetCreation.getFileMetadata();

            long dataSetId =
                    dao.createDataSet(fullDataSetCreation.getMetadataCreation().getCode(), "");
            String dataSetCode = fullDataSetCreation.getMetadataCreation().getCode();
            PathInfoFeeder feeder = new PathInfoFeeder(dataSetId, dataSetCode, files);

            try
            {
                feeder.storeFilesWith(dao);
            } catch (IllegalArgumentException e)
            {
                dao.rollback();
                dao.close();
                throw new UserFailureException(e.getMessage());
            }
        }
        dao.commit();
        dao.close();

        List<DataSetCreation> metadata = new ArrayList<>();
        for (int i = 0; i < newDataSets.size(); i++)
        {
            DataSetCreation creation = newDataSets.get(i).getMetadataCreation();
            metadata.add(creation);
        }

        return as.createDataSets(sessionToken, metadata);
    }

    @Override
    @Transactional
    public Object executeCustomDSSService(String sessionToken, ICustomDSSServiceId serviceId,
            CustomDSSServiceExecutionOptions options)
    {
        getOpenBISService().checkSession(sessionToken);

        return executor.execute(sessionToken, serviceId, options);
    }

    private void injectDataStoreIdAndCodesIfNeeded(List<FullDataSetCreation> newDataSets)
    {
        String dataStoreCode = configProvider.getDataStoreCode();
        DataStorePermId dataStoreId = new DataStorePermId(dataStoreCode);
        int numberOfPermIdsToGenerate = 0;
        for (int i = 0; i < newDataSets.size(); i++)
        {
            FullDataSetCreation fullDataSetCreation = newDataSets.get(i);
            DataSetCreation metadataCreation = fullDataSetCreation.getMetadataCreation();
            IDataStoreId dsid = metadataCreation.getDataStoreId();
            if (dsid != null && dsid instanceof DataStorePermId)
            {
                String code = ((DataStorePermId) dsid).getPermId();
                if (dataStoreCode.equals(code) == false)
                {
                    throw new UserFailureException(
                            "Data store id specified for creation object with index " + i + " is '" + code + "' instead of '" + dataStoreCode
                                    + "' or undefined.");
                }
            }
            metadataCreation.setDataStoreId(dataStoreId);
            if (metadataCreation.isAutoGeneratedCode())
            {
                numberOfPermIdsToGenerate++;
            } else if (StringUtils.isBlank(metadataCreation.getCode()))
            {
                throw new UserFailureException(
                        "Neither code nor auto generating code specified in creation object with index " + i);
            } else
            {
                metadataCreation.setCode(StringUtils.upperCase(metadataCreation.getCode()));
            }
        }
        if (numberOfPermIdsToGenerate > 0)
        {
            List<String> permIds = getOpenBISService().createPermIds(numberOfPermIdsToGenerate);
            for (FullDataSetCreation newDataSet : newDataSets)
            {
                DataSetCreation metadataCreation = newDataSet.getMetadataCreation();
                if (metadataCreation.isAutoGeneratedCode())
                {
                    metadataCreation.setCode(permIds.remove(0));
                    metadataCreation.setAutoGeneratedCode(false);
                }
            }
        }
    }

    @Override
    public Object createPersonalAccessTokenInvocationHandler(
            final IPersonalAccessTokenInvocation invocation)
    {
        return new DataStoreServerApiPersonalAccessTokenInvocationHandler(invocation);
    }
}
