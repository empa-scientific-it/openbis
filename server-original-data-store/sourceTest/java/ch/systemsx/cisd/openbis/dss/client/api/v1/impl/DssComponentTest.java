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
package ch.systemsx.cisd.openbis.dss.client.api.v1.impl;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.api.IRpcServiceFactory;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceDTO;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.DatasetSessionAuthorizer;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.server.api.v1.DssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.AuthorizationGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetAccessGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetFileDTOPredicate;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataStoreApiUrlUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.ShareInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationScriptRunnerTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * A test of the DSS component and {@link IDssServiceRpcGeneric}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class DssComponentTest extends AbstractFileSystemTestCase
{
    private static final String DUMMY_DATA_SET_CODE = "DummyDataSetCode";

    private Mockery context;

    private IGeneralInformationService openBisService;

    private IEncapsulatedOpenBISService etlService;

    private IRpcServiceFactory dssServiceFactory;

    private DssComponent dssComponent;

    private File randomDataFile;

    private static final String DUMMY_SESSION_TOKEN = "DummySessionToken";

    private static final String DUMMY_DSS_DOWNLOAD_URL = "http://localhost/"
            + GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

    private static final String DUMMY_DSS_URL = DataStoreApiUrlUtilities
            .getDataStoreUrlFromDownloadUrl(DUMMY_DSS_DOWNLOAD_URL);

    private IDssServiceRpcGeneric dssServiceV1_0;

    private IDssServiceRpcGeneric dssServiceV1_2;

    private IShareIdManager shareIdManager;

    private StaticListableBeanFactory applicationContext;

    @SuppressWarnings("unchecked")
    private <T extends IRpcService> T getAdvisedDssService(final T service)
    {
        final Advisor advisor = new DssServiceRpcAuthorizationAdvisor(shareIdManager);
        final BeanPostProcessor processor = new AbstractAutoProxyCreator()
            {
                private static final long serialVersionUID = 1L;

                //
                // AbstractAutoProxyCreator
                //
                @Override
                protected final Object[] getAdvicesAndAdvisorsForBean(
                        @SuppressWarnings("rawtypes")
                        final Class beanClass, final String beanName,
                        final TargetSource customTargetSource) throws BeansException
                {
                    return new Object[]
                    { advisor };
                }
            };
        ((AbstractAutoProxyCreator) processor).setBeanFactory(applicationContext);
        final Object proxy =
                processor.postProcessAfterInitialization(service, "proxy of "
                        + service.getClass().getName());
        return (T) proxy;
    }

    public DssComponentTest()
    {

    }

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        DssSessionAuthorizationHolder.setAuthorizer(new DatasetSessionAuthorizer());
        applicationContext = new StaticListableBeanFactory();
        ServiceProviderTestWrapper.setApplicationContext(applicationContext);
        context = new Mockery();
        openBisService = context.mock(IGeneralInformationService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        dssServiceFactory = context.mock(IRpcServiceFactory.class);
        dssComponent = new DssComponent(openBisService, dssServiceFactory, null);
        randomDataFile = getFileWithRandomData(1);
        etlService = context.mock(IEncapsulatedOpenBISService.class);
        applicationContext.addBean("openBIS-service", etlService);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        // Clear the dss services
        dssServiceV1_0 = null;
        dssServiceV1_2 = null;
    }

    @Test
    public void testLogin()
    {
        final SessionContextDTO session = getDummySession();

        context.checking(new Expectations()
            {
                {
                    one(openBisService).tryToAuthenticateForAllServices("foo", "bar");
                    will(returnValue(session.getSessionToken()));
                }
            });

        dssComponent.login("foo", "bar");

        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetFiles() throws IOException
    {
        setupExpectations(true, 1);

        dssComponent.login("foo", "bar");
        IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
        FileInfoDssDTO[] fileInfos = dataSetProxy.listFiles("/", true);
        assertEquals(1, fileInfos.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetFilesNoLogin() throws IOException
    {
        dssComponent = new DssComponent(openBisService, dssServiceFactory, DUMMY_SESSION_TOKEN);
        setupExpectationsNoLogin();
        IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
        FileInfoDssDTO[] fileInfos = dataSetProxy.listFiles("/", true);
        assertEquals(1, fileInfos.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetFilesUnauthorized() throws IOException
    {
        setupExpectations(false, 1);
        dssComponent.login("foo", "bar");
        try
        {
            IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
            dataSetProxy.listFiles("/", true);
            fail("Unauthorized access to data set should have thrown an exception.");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: Not allowed.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testLinkToContents() throws IOException
    {
        setupExpectations(true, false, 3, false);

        dssComponent.login("foo", "bar");
        IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);

        File contents = dataSetProxy.tryLinkToContents(null);
        assertNotNull(contents);
        assertEquals(workingDirectory.getParentFile().getAbsolutePath()
                + "/ch.systemsx.cisd.openbis.dss.client.api.v1.impl.DssComponentTest",
                contents.getPath());

        // Check using an alternative path to the data store server
        String alternativeRoot = workingDirectory.getParentFile().getPath();
        contents = dataSetProxy.tryLinkToContents(alternativeRoot);
        assertNotNull(contents);
        assertEquals(
                "targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.client.api.v1.impl.DssComponentTest",
                contents.getPath());

        // Check using a path which doesn't exist
        contents = dataSetProxy.tryLinkToContents("/foo/bar");
        // Should be null
        assertNull(contents);

        context.assertIsSatisfied();
    }

    @Test
    public void testLinkToContentsEarlierVersion() throws IOException
    {
        setupExpectations(null, true, 1, true);

        dssComponent.login("foo", "bar");
        IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);

        try
        {
            dataSetProxy.tryLinkToContents(null);
        } catch (EnvironmentFailureException e)
        {
            assertEquals("Server does not support this feature.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetValidationScript() throws IOException
    {
        setupExpectations(null, 1);

        dssComponent.login("foo", "bar");
        File dataSetFile = new File(ValidationScriptRunnerTest.INVALID_DATA_SET);
        NewDataSetDTOBuilder builder = new NewDataSetDTOBuilder();
        builder.setFile(dataSetFile);
        List<ValidationError> result =
                dssComponent.validateDataSet(builder.asNewDataSetDTO(), dataSetFile);

        assertEquals(1, result.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testGetValidationScriptEarlierVersion() throws IOException
    {
        setupExpectations(null, true, 1, true);

        dssComponent.login("foo", "bar");
        File dataSetFile = new File(ValidationScriptRunnerTest.INVALID_DATA_SET);
        NewDataSetDTOBuilder builder = new NewDataSetDTOBuilder();
        builder.setFile(dataSetFile);
        List<ValidationError> result =
                dssComponent.validateDataSet(builder.asNewDataSetDTO(), dataSetFile);

        // Against an earlier version of the server, an invalid script will pass validation.
        assertEquals(0, result.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testExtractMetadata() throws IOException
    {
        setupExpectations(null, 1);

        dssComponent.login("foo", "bar");
        File dataSetFile = new File(ValidationScriptRunnerTest.INVALID_DATA_SET);
        NewDataSetDTOBuilder builder = new NewDataSetDTOBuilder();
        builder.setFile(dataSetFile);
        Map<String, Serializable> result =
                dssComponent.extractMetadata(builder.asNewDataSetDTO(), dataSetFile);

        assertEquals(1, result.size());
        assertEquals("test-value", result.get("test-prop"));

        context.assertIsSatisfied();
    }

    @Test
    public void testUnsupportedInterface() throws IOException
    {
        setupExpectations("Some Server Interface", true, null, false, 1, false);

        dssComponent.login("foo", "bar");
        try
        {
            dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e)
        {
            // correct behavior
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetFileContents() throws IOException
    {
        setupExpectations(2);

        dssComponent.login("foo", "bar");
        IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
        FileInfoDssDTO[] fileInfos = dataSetProxy.listFiles("/", true);
        FileInfoDssDTO fileFileInfo = null;
        for (FileInfoDssDTO fid : fileInfos)
        {
            if (fid.isDirectory() == false)
            {
                fileFileInfo = fid;
                break;
            }
        }
        if (fileFileInfo == null)
        {
            fail("Could not find file info for random file");
            return;
        }

        InputStream is = dataSetProxy.getFile(fileFileInfo.getPathInDataSet());
        int byteCount = 0;
        while (is.read() >= 0)
        {
            ++byteCount;
        }
        is.close(); // releases lock on data set

        assertEquals(fileFileInfo.getFileSize(), byteCount);
        context.assertIsSatisfied();
    }

    private void setupExpectations(int lockingCount) throws IOException
    {
        setupExpectations(true, lockingCount);
    }

    private void setupExpectations(Boolean isDataSetAccessible, int lockingCount)
            throws IOException
    {
        setupExpectations(IDssServiceRpcGeneric.DSS_SERVICE_NAME, true, isDataSetAccessible, false,
                lockingCount, true);
    }

    private void setupExpectations(Boolean isDataSetAccessible, boolean returnEarlierVersion,
            int lockingCount, boolean releaseLock) throws IOException
    {
        setupExpectations(IDssServiceRpcGeneric.DSS_SERVICE_NAME, true, isDataSetAccessible,
                returnEarlierVersion, lockingCount, releaseLock);
    }

    private void setupExpectationsNoLogin() throws IOException
    {
        setupExpectations(IDssServiceRpcGeneric.DSS_SERVICE_NAME, false, true, false, 1, true);
    }

    private void setupExpectations(String serviceName, final boolean needsLogin,
            final Boolean isDataSetAccessible, boolean returnEarlierVersion,
            final int lockingCount, final boolean releaseLock) throws IOException
    {
        final SessionContextDTO session = getDummySession();

        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        FileInfoDssBuilder builder =
                new FileInfoDssBuilder(workingDirectory.getCanonicalPath(),
                        workingDirectory.getCanonicalPath());
        builder.appendFileInfosForFile(workingDirectory, list, true);
        final FileInfoDssDTO[] fileInfos = new FileInfoDssDTO[list.size()];
        list.toArray(fileInfos);

        final ArrayList<RpcServiceInterfaceDTO> ifaces = new ArrayList<RpcServiceInterfaceDTO>(1);
        final RpcServiceInterfaceDTO iface = new RpcServiceInterfaceDTO(serviceName);

        final RpcServiceInterfaceVersionDTO ifaceVersionV1_0 =
                new RpcServiceInterfaceVersionDTO(serviceName, "/rpc/v1", 1, 0);

        final RpcServiceInterfaceVersionDTO ifaceVersionV1_2 =
                new RpcServiceInterfaceVersionDTO(serviceName, "/rpc/v1", 1, 2);

        if (returnEarlierVersion)
        {
            iface.addVersion(ifaceVersionV1_0);
        } else
        {
            iface.addVersion(ifaceVersionV1_2);
        }

        ifaces.add(iface);

        if (isDataSetAccessible != null)
        {
            context.checking(new Expectations()
                {
                    {
                        one(etlService).checkDataSetCollectionAccess(DUMMY_SESSION_TOKEN,
                                Arrays.asList(DUMMY_DATA_SET_CODE));
                        if (isDataSetAccessible == false)
                        {
                            will(throwException(new UserFailureException("Not allowed.")));
                        }
                        exactly(lockingCount).of(shareIdManager).lock(
                                Arrays.asList(DUMMY_DATA_SET_CODE));
                        if (releaseLock)
                        {
                            exactly(lockingCount).of(shareIdManager).releaseLocks();
                        }
                    }
                });
        }

        dssServiceV1_0 =
                getAdvisedDssService(new MockDssServiceRpcV1_0(null, shareIdManager, fileInfos,
                        randomDataFile));

        dssServiceV1_2 =
                getAdvisedDssService(new MockDssServiceRpcV1_2(null, shareIdManager, fileInfos,
                        randomDataFile));

        context.checking(new Expectations()
            {
                {
                    final String dataSetCode = DUMMY_DATA_SET_CODE;

                    if (needsLogin)
                    {
                        one(openBisService).tryToAuthenticateForAllServices("foo", "bar");
                        will(returnValue(session.getSessionToken()));
                    }
                    allowing(openBisService).tryGetDataStoreBaseURL(session.getSessionToken(),
                            dataSetCode);
                    will(returnValue(DUMMY_DSS_DOWNLOAD_URL));
                    allowing(dssServiceFactory).getSupportedInterfaces(DUMMY_DSS_URL, false);
                    will(returnValue(ifaces));

                    allowing(dssServiceFactory).getService(ifaceVersionV1_0,
                            IDssServiceRpcGeneric.class, DUMMY_DSS_URL, false);
                    will(returnValue(dssServiceV1_0));

                    allowing(dssServiceFactory).getService(ifaceVersionV1_2,
                            IDssServiceRpcGeneric.class, DUMMY_DSS_URL, false);
                    will(returnValue(dssServiceV1_2));

                    allowing(openBisService).getDefaultPutDataStoreBaseURL(
                            session.getSessionToken());
                    will(returnValue(DUMMY_DSS_DOWNLOAD_URL));
                }
            });
    }

    private SessionContextDTO getDummySession()
    {
        final SessionContextDTO session = new SessionContextDTO();
        session.setSessionToken(DUMMY_SESSION_TOKEN);
        return session;
    }

    private File getFileWithRandomData(long sizeInKB) throws IOException
    {
        File file = new File(workingDirectory, "random.txt");
        Random random = new Random();
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            for (int i = 0; i < sizeInKB; i++)
            {
                random.nextBytes(bytes);
                outputStream.write(bytes);
            }
        } catch (IOException ex)
        {
            throw ex;
        } finally
        {
            if (outputStream != null)
            {
                IOUtils.closeQuietly(outputStream);
            }
        }

        return file;
    }

    private File getStoreRootFile()
    {
        return workingDirectory.getParentFile();
    }

    private File getDataSetFile()
    {
        return workingDirectory;
    }

    private static class MockDssServiceRpcV1_0 extends AbstractDssServiceRpc<IDssServiceRpcGeneric>
            implements IDssServiceRpcGenericInternal
    {
        private final FileInfoDssDTO[] fileInfos;

        private final FileInputStream fileInputStream;

        private URL url;

        /**
         * @param openBISService
         */
        public MockDssServiceRpcV1_0(IEncapsulatedOpenBISService openBISService,
                IShareIdManager shareIdManager, FileInfoDssDTO[] fileInfos, File file)
        {
            super(openBISService, null, shareIdManager, null);
            this.fileInfos = fileInfos;
            try
            {
                this.fileInputStream = new FileInputStream(file);
                url = file.toURI().toURL();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        @Override
        public InputStream getFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            return fileInputStream;
        }

        @Override
        public String getDownloadUrlForFileForDataSet(String sessionToken,
                DataSetFileDTO fileOrFolder) throws IOExceptionUnchecked, IllegalArgumentException
        {
            return url.toString();
        }

        @Override
        public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            return fileInputStream;
        }

        @Override
        public String getDownloadUrlForFileForDataSet(String sessionToken, String dataSetCode,
                String path) throws IOExceptionUnchecked, IllegalArgumentException
        {
            return url.toString();
        }

        @Override
        public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            return fileInfos;
        }

        @Override
        public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
                String path, boolean isRecursive) throws IOExceptionUnchecked,
                IllegalArgumentException
        {
            return fileInfos;
        }

        @Override
        public String putDataSet(String sessionToken, NewDataSetDTO newDataset,
                InputStream inputStream) throws IOExceptionUnchecked, IllegalArgumentException
        {
            return null;
        }

        @Override
        public long putFileToSessionWorkspace(String sessionToken, String filePath,
                InputStream inputStream) throws IOExceptionUnchecked
        {
            return 0;
        }

        @Override
        public File putDirToSessionWorkspace(String sessionToken, String filePath, boolean isEmptyDirectory) throws IOExceptionUnchecked {
            return null;
        }

        @Override
        public long putFileSliceToSessionWorkspace(String sessionToken, String filePath,
                long slicePosition, InputStream sliceInputStream) throws IOExceptionUnchecked
        {
            return 0;
        }

        @Override
        public int getMajorVersion()
        {
            return 1;
        }

        @Override
        public int getMinorVersion()
        {
            return 0;
        }

        @Override
        public String getPathToDataSet(String sessionToken, String dataSetCode,
                String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
                IllegalArgumentException
        {
            throw new IllegalArgumentException("Unimplemented in v1.0");
        }

        @Override
        public String tryGetPathToDataSet(String sessionToken, String dataSetCode, String overrideStoreRootPathOrNull) throws IOExceptionUnchecked
        {
            return getPathToDataSet(sessionToken, dataSetCode, overrideStoreRootPathOrNull);
        }

        @Override
        public List<ShareInfo> listAllShares(String sessionToken)
        {
            return null;
        }

        @Override
        public void shuffleDataSet(String sessionToken, String dataSetCode, String shareId)
        {
        }

        @Override
        public IDssServiceRpcGeneric createLogger(IInvocationLoggerContext context)
        {
            return null;
        }

        @Override
        public String getValidationScript(String sessionToken, String dataSetTypeOrNull)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            throw new IllegalArgumentException("Unimplemented in v1.0");
        }

        @Override
        public boolean deleteSessionWorkspaceFile(String sessionToken, String path)
        {
            return false;
        }

        @Override
        public InputStream getFileFromSessionWorkspace(String sessionToken, String filePath)
                throws IOExceptionUnchecked
        {
            return null;
        }

        @Override
        public QueryTableModel createReportFromAggregationService(String sessionToken,
                String aggregationServiceName, Map<String, Object> parameters)
        {
            return null;
        }

        @Override
        public QueryTableModel createReportFromDataSets(String sessionToken, String serviceKey,
                List<String> dataSetCodes)
        {
            return null;
        }

        @Override
        public List<AggregationServiceDescription> listAggregationServices(String sessionToken)
        {
            return null;
        }

        @Override
        public List<ReportDescription> listTableReportDescriptions(String sessionToken)
        {
            return null;
        }

        @Override
        public String getDownloadUrlForFileForDataSetWithTimeout(String sessionToken,
                String dataSetCode, String path, long validityDuration)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            return url.toString();
        }

        @Override
        @DataSetAccessGuard
        public String getDownloadUrlForFileForDataSetWithTimeout(
                String sessionToken,
                @AuthorizationGuard(guardClass = DataSetFileDTOPredicate.class) DataSetFileDTO fileOrFolder,
                long validityDurationInSeconds) throws IOExceptionUnchecked,
                IllegalArgumentException
        {
            return url.toString();
        }

        @Override
        public void putDataSet(String sessionToken, String dropboxName, String fileName)
        {
        }
    }

    private class MockDssServiceRpcV1_1 extends MockDssServiceRpcV1_0
    {

        /**
         * @param openBISService
         * @param fileInfos
         */
        public MockDssServiceRpcV1_1(IEncapsulatedOpenBISService openBISService,
                IShareIdManager shareIdManager, FileInfoDssDTO[] fileInfos, File file)
        {
            super(openBISService, shareIdManager, fileInfos, file);
        }

        @Override
        public int getMinorVersion()
        {
            return 1;
        }

        @Override
        public String getPathToDataSet(String sessionToken, String dataSetCode,
                String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
                IllegalArgumentException
        {
            return DssServiceRpcGeneric.convertPath(getStoreRootFile(), getDataSetFile(),
                    overrideStoreRootPathOrNull);
        }

        @Override
        public String tryGetPathToDataSet(String sessionToken, String dataSetCode, String overrideStoreRootPathOrNull) throws IOExceptionUnchecked
        {
            return getPathToDataSet(sessionToken, dataSetCode, overrideStoreRootPathOrNull);
        }

    }

    private class MockDssServiceRpcV1_2 extends MockDssServiceRpcV1_1
    {
        /**
         * @param openBISService
         * @param shareIdManager
         * @param fileInfos
         */
        public MockDssServiceRpcV1_2(IEncapsulatedOpenBISService openBISService,
                IShareIdManager shareIdManager, FileInfoDssDTO[] fileInfos, File file)
        {
            super(openBISService, shareIdManager, fileInfos, file);
        }

        @Override
        public int getMinorVersion()
        {
            return 2;
        }

        @Override
        public String getValidationScript(String sessionToken, String dataSetTypeOrNull)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            return FileUtilities.loadToString(new File(
                    ValidationScriptRunnerTest.BASIC_VALIDATION_SCRIPT));
        }
    }

}
