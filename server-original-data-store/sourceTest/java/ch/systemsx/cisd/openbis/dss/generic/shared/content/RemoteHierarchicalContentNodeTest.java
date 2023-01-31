/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import static ch.systemsx.cisd.common.utilities.SystemTimeProvider.SYSTEM_TIME_PROVIDER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * @author anttil
 */
public class RemoteHierarchicalContentNodeTest
{
    private static final String DATA_STORE_URL = "http://a.b.c";

    private static final String DATA_STORE_CODE = "DSS";

    private static final String SESSION_TOKEN = "token";

    private static final IDatasetLocation CACHED_DATASET_LOCATION = new DatasetLocation(
            "cached_dataset_code", "a/b/c", DATA_STORE_CODE, DATA_STORE_URL);

    private static final IDatasetLocation REMOTE_DATASET_LOCATION = new DatasetLocation(
            "remote_dataset_code", "a/b/c", DATA_STORE_CODE, DATA_STORE_URL);

    private static final File SESSION_WORKSPACE_DIR =
            new File("targets/unit-test/RemoteHierarchicalContentNodeTest/session-workspace/");

    private static final String REMOTE_FILES_DIR =
            "targets/unit-test/RemoteHierarchicalContentNodeTest/remote-files";

    private ContentCache cache;

    Mockery context;

    private IFileOperations fileOperations;

    ISingleDataSetPathInfoProvider provider;

    IDssServiceRpcGeneric remoteDss;

    OpenBISSessionHolder sessionHolder;

    File remoteFile;

    File fileInSessionWorkspace;

    private IDssServiceRpcGenericFactory serviceFactory;

    private IPersistenceManager persistenceManager;

    private HashMap<String, ContentCache.DataSetInfo> dataSetInfos;

    @BeforeMethod
    public void fixture() throws Exception
    {
        context = new Mockery();
        fileOperations = context.mock(IFileOperations.class);

        provider = context.mock(ISingleDataSetPathInfoProvider.class);
        persistenceManager = context.mock(IPersistenceManager.class);

        remoteDss = context.mock(IDssServiceRpcGeneric.class, "remote dss");

        sessionHolder = new OpenBISSessionHolder();
        sessionHolder.setSessionToken("token");

        remoteFile =
                new File(REMOTE_FILES_DIR + "/" + REMOTE_DATASET_LOCATION.getDataSetCode(),
                        "remote-file.txt");

        fileInSessionWorkspace =
                new File(SESSION_WORKSPACE_DIR, ContentCache.CACHE_FOLDER + "/"
                        + CACHED_DATASET_LOCATION.getDataSetCode() + "/already-downloaded-file.txt");
        create(remoteFile);
        create(fileInSessionWorkspace);
        serviceFactory = context.mock(IDssServiceRpcGenericFactory.class);
        dataSetInfos = new HashMap<String, ContentCache.DataSetInfo>();
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).removeRecursivelyQueueing(
                            new File(SESSION_WORKSPACE_DIR, ContentCache.DOWNLOADING_FOLDER));

                    allowing(serviceFactory).getService(DATA_STORE_URL);
                    will(returnValue(remoteDss));

                    one(persistenceManager).load(dataSetInfos);
                    will(returnValue(dataSetInfos));

                    one(persistenceManager).requestPersistence();
                }
            });
        cache =
                new ContentCache(serviceFactory, SESSION_WORKSPACE_DIR, FileUtils.ONE_MB, 100000,
                        fileOperations, SYSTEM_TIME_PROVIDER, persistenceManager);
        cache.afterPropertiesSet();
    }

    @Test
    public void downloadedContentWillBeStoredToSessionWorkspace() throws Exception
    {
        DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setDirectory(false);
        pathInfo.setFileName(remoteFile.getName());
        pathInfo.setRelativePath(remoteFile.getName());

        IHierarchicalContentNode node =
                new RemoteHierarchicalContentNode(REMOTE_DATASET_LOCATION, pathInfo, provider,
                        serviceFactory, sessionHolder, cache);

        context.checking(new Expectations()
            {
                {
                    allowing(remoteDss).getDownloadUrlForFileForDataSet(with(any(String.class)),
                            with(any(String.class)), with(any(String.class)));
                    will(returnValue(remoteFile.toURI().toURL().toString()));

                    one(persistenceManager).requestPersistence();
                }
            });

        node.getFile();

        context.assertIsSatisfied();
    }

    @Test
    public void sessionWorkspaceWillBeUsedToReturnContentThatHasBeenAlreadyDownloaded()
            throws Exception
    {
        DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setDirectory(false);
        pathInfo.setFileName(fileInSessionWorkspace.getName());
        pathInfo.setRelativePath(fileInSessionWorkspace.getName());

        IHierarchicalContentNode node =
                new RemoteHierarchicalContentNode(CACHED_DATASET_LOCATION, pathInfo, provider,
                        serviceFactory, sessionHolder, cache);
        context.checking(new Expectations()
            {
                {
                    one(persistenceManager).requestPersistence();
                }
            });

        File file = node.getFile();
        assertThat(file.getAbsolutePath(), is(fileInSessionWorkspace.getAbsolutePath()));

        context.assertIsSatisfied();
    }

    @Test
    public void childNodeInfoIsRetrievedPrimarilyFromPathInfoDb() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setDirectory(false);
        pathInfo.setFileName(fileInSessionWorkspace.getName());
        pathInfo.setRelativePath(fileInSessionWorkspace.getName());

        IHierarchicalContentNode node =
                new RemoteHierarchicalContentNode(CACHED_DATASET_LOCATION, pathInfo, provider,
                        serviceFactory, sessionHolder, cache);

        context.checking(new Expectations()
            {
                {
                    oneOf(provider).listChildrenPathInfos(pathInfo);
                }
            });

        node.getChildNodes();

        context.assertIsSatisfied();
    }

    @Test
    public void childNodeInfoIsRetrievedFromRemoteDssIfPathInfoDbIsNotAvailable() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setDirectory(false);
        pathInfo.setFileName(fileInSessionWorkspace.getName());
        pathInfo.setRelativePath(fileInSessionWorkspace.getName());

        IHierarchicalContentNode node =
                new RemoteHierarchicalContentNode(CACHED_DATASET_LOCATION, pathInfo, null,
                        serviceFactory, sessionHolder, cache);

        context.checking(new Expectations()
            {
                {
                    oneOf(remoteDss).listFilesForDataSet(SESSION_TOKEN,
                            CACHED_DATASET_LOCATION.getDataSetCode(), pathInfo.getRelativePath(),
                            false);
                    will(returnValue(new FileInfoDssDTO[]
                    { new FileInfoDssDTO("path/to/file", "path/to/file", false, 3) }));
                }
            });

        List<IHierarchicalContentNode> children = node.getChildNodes();

        assertThat(children.size(), is(1));
        assertThat(children.get(0).getRelativePath(), is("path/to/file"));

        context.assertIsSatisfied();
    }

    private static void create(File file)
    {
        try
        {
            if (file.exists() == false)
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

}
