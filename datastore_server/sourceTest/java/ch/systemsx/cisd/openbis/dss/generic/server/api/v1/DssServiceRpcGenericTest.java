/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.Collections;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.DatasetSessionAuthorizer;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;

/**
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcGenericTest extends AssertJUnit
{

    private static final String SESSION_TOKEN = "SESSION";

    private IEncapsulatedOpenBISService service;

    private Mockery context;

    private IDssServiceRpcGeneric dssService;

    private IShareIdManager shareIdManager;

    private IFreeSpaceProvider freeSpaceProvider;
    
    private IPluginTaskInfoProvider infoProvider;

    private IQueryApiServer apiService;

    private IHierarchicalContentProvider contentProvider;

    private IHierarchicalContent content;

    @BeforeMethod
    public void beforeMethod()
    {
        DssSessionAuthorizationHolder.setAuthorizer(new DatasetSessionAuthorizer());
        final StaticListableBeanFactory applicationContext = new StaticListableBeanFactory();
        ServiceProviderTestWrapper.setApplicationContext(applicationContext);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        apiService = context.mock(IQueryApiServer.class);
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        shareIdManager = context.mock(IShareIdManager.class);
        infoProvider = context.mock(IPluginTaskInfoProvider.class);
        context.checking(new Expectations()
            {
                {
                    one(infoProvider).getSessionWorkspaceRootDir();
                    will(returnValue(new File("sessionWorkspaceRoot")));
                }
            });
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        content = context.mock(IHierarchicalContent.class);
        applicationContext.addBean("openBIS-service", service);
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setInterfaces(new Class[]
            { IDssServiceRpcGeneric.class });
        DssServiceRpcGeneric nakedDssService =
                new DssServiceRpcGeneric(service, apiService, infoProvider, freeSpaceProvider,
                        shareIdManager, contentProvider);
        proxyFactoryBean.setTarget(nakedDssService);
        proxyFactoryBean.addAdvisor(new DssServiceRpcAuthorizationAdvisor(shareIdManager));
        dssService = (IDssServiceRpcGeneric) proxyFactoryBean.getObject();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        if (context != null)
        {
            context.assertIsSatisfied();
        }
    }

    @Test
    public void testListSingleFileForData()
    {
        final String dataSetCode = "ds-1";
        final String path = "abc/de";
        prepareLockDataSet(dataSetCode);
        prepareAuthorizationCheck(dataSetCode);
        RecordingMatcher<ISessionTokenProvider> matcher = prepareGetContent(dataSetCode);
        context.checking(new Expectations()
            {
                {
                    one(content).getNode(path);
                    IHierarchicalContentNode mainNode =
                            context.mock(IHierarchicalContentNode.class, "mainNode");
                    will(returnValue(mainNode));

                    allowing(mainNode).getName();
                    will(returnValue("main-node"));

                    allowing(mainNode).isDirectory();
                    will(returnValue(false));

                    allowing(mainNode).isChecksumCRC32Precalculated();
                    will(returnValue(false));

                    allowing(mainNode).getRelativePath();
                    will(returnValue(path));

                    one(mainNode).getFileLength();
                    will(returnValue(42L));
                }

            });

        FileInfoDssDTO[] files =
                dssService.listFilesForDataSet(SESSION_TOKEN, dataSetCode, path, true);

        assertEquals(SESSION_TOKEN, matcher.recordedObject().getSessionToken());
        assertEquals(path, files[0].getPathInDataSet());
        assertEquals("main-node", files[0].getPathInListing());
        assertEquals(42L, files[0].getFileSize());
        assertEquals(1, files.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testFilesForData()
    {
        final String dataSetCode = "ds-1";
        final String path = "abc/de";
        prepareLockDataSet(dataSetCode);
        prepareAuthorizationCheck(dataSetCode);
        RecordingMatcher<ISessionTokenProvider> matcher = prepareGetContent(dataSetCode);
        context.checking(new Expectations()
            {
                {
                    IHierarchicalContentNode mainNode = createNodeMock("mainNode");
                    one(content).getNode(path);
                    will(returnValue(mainNode));
                    
                    IHierarchicalContentNode childNode1 = createNodeMock("childNode1");
                    IHierarchicalContentNode childNode1Child1 = createNodeMock("childNode1Child1");
                    IHierarchicalContentNode childNode1Child2 = createNodeMock("childNode1Child2");
                    IHierarchicalContentNode childNode2 = createNodeMock("childNode2");
                    IHierarchicalContentNode childNode2Child1 = createNodeMock("childNode2Child1");
                    IHierarchicalContentNode childNode3 = createNodeMock("childNode3");

                    prepareDirectoryNode(mainNode, path, childNode1, childNode2, childNode3);
                    // child1
                    prepareDirectoryNode(childNode1, path + "/child1", childNode1Child1,
                            childNode1Child2);
                    prepareFileNode(childNode1Child1, path + "/child1/child1", 11, 123);
                    prepareFileNode(childNode1Child2, path + "/child1/child2", 12, -17);
                    // child2
                    prepareDirectoryNode(childNode2, path + "/child2", childNode2Child1);
                    prepareFileNode(childNode2Child1, path + "/child2/child1", 21, 42);
                    // child3
                    prepareFileNode(childNode3, path + "/child3", 3, 1111);
                }

                private IHierarchicalContentNode createNodeMock(String mockName)
                {
                    return context.mock(IHierarchicalContentNode.class, mockName);
                }

                private void prepareFileNode(IHierarchicalContentNode node,
                        final String relativePath, long length, int crc32Checksum)
                {
                    allowing(node).isDirectory();
                    will(returnValue(false));
                    allowing(node).getRelativePath();
                    will(returnValue(relativePath));
                    one(node).getFileLength();
                    will(returnValue(length));
                    one(node).isChecksumCRC32Precalculated();
                    will(returnValue(true));
                    one(node).getChecksumCRC32();
                    will(returnValue(crc32Checksum));
                }

                private void prepareDirectoryNode(IHierarchicalContentNode node,
                        final String relativePath, IHierarchicalContentNode... childNodes)
                {
                    allowing(node).isDirectory();
                    will(returnValue(true));
                    one(node).isChecksumCRC32Precalculated();
                    will(returnValue(false));
                    allowing(node).getRelativePath();
                    will(returnValue(relativePath));
                    one(node).getChildNodes();
                    will(returnValue(Arrays.asList(childNodes)));
                }
            });

        FileInfoDssDTO[] dataSets =
                dssService.listFilesForDataSet(SESSION_TOKEN, dataSetCode, path, true);

        assertEquals(SESSION_TOKEN, matcher.recordedObject().getSessionToken());
        assertEquals(6, dataSets.length);
        assertEquals(fileInfoString(path, "child1", -1, null), dataSets[0].toString());
        assertEquals(fileInfoString(path, "child1/child1", 11, 123), dataSets[1].toString());
        assertEquals(fileInfoString(path, "child1/child2", 12, -17), dataSets[2].toString());
        assertEquals(fileInfoString(path, "child2", -1, null), dataSets[3].toString());
        assertEquals(fileInfoString(path, "child2/child1", 21, 42), dataSets[4].toString());
        assertEquals(fileInfoString(path, "child3", 3, 1111), dataSets[5].toString());
        context.assertIsSatisfied();
    }

    private static String fileInfoString(String startPath, String pathInListing, long length,
            Integer checksum)
    {
        if (checksum != null)
        {
            return String.format("FileInfoDssDTO[%s/%s,%s,%d,%s]", startPath, pathInListing,
                    pathInListing, length, IOUtilities.crc32ToString(checksum));
        } else
        {
            return String.format("FileInfoDssDTO[%s/%s,%s,%d]", startPath, pathInListing,
                    pathInListing, length);
        }
    }

    private void prepareLockDataSet(final String dataSetCode)
    {
        // NOTE: this is done by the DssServiceRpcAuthorizationAdvisor
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(Arrays.asList(dataSetCode));
                    one(shareIdManager).releaseLocks();
                }
            });
    }

    private void prepareAuthorizationCheck(final String dataSetCode)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).checkDataSetCollectionAccess(SESSION_TOKEN,
                            Collections.singletonList(dataSetCode));
                }
            });

    }

    private RecordingMatcher<ISessionTokenProvider> prepareGetContent(final String dataSetCode)
    {
        final RecordingMatcher<ISessionTokenProvider> sessionTokenProviderMatcher =
                new RecordingMatcher<ISessionTokenProvider>();
        context.checking(new Expectations()
            {
                {
                    one(contentProvider).cloneFor(with(sessionTokenProviderMatcher));
                    will(returnValue(contentProvider));

                    one(contentProvider).asContent(dataSetCode);
                    will(returnValue(content));

                    one(content).close(); // content should be always closed
                }
            });
        return sessionTokenProviderMatcher;
    }
}
