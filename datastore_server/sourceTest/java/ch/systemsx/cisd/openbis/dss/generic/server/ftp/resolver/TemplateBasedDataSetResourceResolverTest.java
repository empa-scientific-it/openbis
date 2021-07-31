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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.ftplet.FtpFile;
import org.jmock.Expectations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.test.TrackingMockery;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.ResolverConfigBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Kaloyan Enimanev
 */
@Friend(toClasses = TemplateBasedDataSetResourceResolver.class)
public class TemplateBasedDataSetResourceResolverTest extends AbstractFileSystemTestCase
{
    private static final class SimpleFileContentProvider implements IHierarchicalContentProvider
    {
        private final File root;

        SimpleFileContentProvider(File root)
        {
            this.root = root;
        }

        @Override
        public IHierarchicalContent asContent(AbstractExternalData dataSet)
        {
            return asContent((IDatasetLocation) dataSet.tryGetAsDataSet());
        }

        @Override
        public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(AbstractExternalData dataSet)
        {
            return asContent(dataSet);
        }

        @Override
        public IHierarchicalContent asContent(IDatasetLocation datasetLocation)
        {
            String dataSetCode = datasetLocation.getDataSetCode();
            return asContent(dataSetCode);
        }

        @Override
        public IHierarchicalContent asContent(String dataSetCode)
        {
            return asContent(new File(root, dataSetCode));
        }

        @Override
        public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(String dataSetCode) throws IllegalArgumentException
        {
            return null; // not necessary for this test
        }

        @Override
        public IHierarchicalContent asContent(File datasetDirectory)
        {
            return new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(
                    datasetDirectory, IDelegatedAction.DO_NOTHING);
        }

        @Override
        public IHierarchicalContentProvider cloneFor(ISessionTokenProvider sessionTokenProvider)
        {
            return this;
        }
    }

    private static final Date REGISTRATION_DATE = new Date(42);

    private static final String RENDERED_REGISTRATION_DATE = TemplateBasedDataSetResourceResolver
            .extractDateValue(REGISTRATION_DATE);

    private static final Date MODIFICATION_DATE = new Date(1234567890);

    private static final String SESSION_TOKEN = "token";

    private static final String EXP_ID = "/space/project/experiment";

    private static final long EXP_TECH_ID = 1L;

    private static final String SIMPLE_TEMPLATE = "${dataSetCode}";

    private static final String DS_TYPE1 = "DS_TYPE1";

    private static final String DS_TYPE2 = "DS_TYPE2";

    private static final String DS_TYPE3 = "DS_TYPE3";

    private static final String TEMPLATE_WITH_FILENAMES =
            "DS-${dataSetType}-${fileName}-${disambiguation}";

    private static final String BIG_TEMPLATE =
            "DS-${dataSetType}-${dataSetCode}-${dataSetDate}-${disambiguation}";

    private TrackingMockery context;

    private IServiceForDataStoreServer service;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private FtpPathResolverContext resolverContext;

    private TemplateBasedDataSetResourceResolver resolver;

    private Experiment experiment;

    private IGeneralInformationService generalInfoService;

    private IApplicationServerApi v3api;

    private SimpleFileContentProvider simpleFileContentProvider;

    private PhysicalDataSet ds1;

    private PhysicalDataSet ds2;

    private PhysicalDataSet ds3;

    private ITimeProvider timeProvider = new ITimeProvider()
        {
            @Override
            public long getTimeInMilliseconds()
            {
                return 0;
            }
        };

    @Override
    @BeforeMethod
    public void setUp()
    {
        experiment = new Experiment();
        experiment.setIdentifier(EXP_ID);
        experiment.setId(EXP_TECH_ID);

        context = new TrackingMockery();
        service = context.mock(IServiceForDataStoreServer.class);
        generalInfoService = context.mock(IGeneralInformationService.class);
        v3api = context.mock(IApplicationServerApi.class);

        hierarchicalContentProvider = context.mock(IHierarchicalContentProvider.class);
        File root = new File(workingDirectory, "data-sets");
        root.mkdirs();
        simpleFileContentProvider = new SimpleFileContentProvider(root);

        Cache cache = new Cache(timeProvider);
        // these are the tests for old style resolvers.
        // they are tested in here in a way that doesn't use the path in the Resolver context.
        // in productive code we always create a new context for each request and we have a
        // requested path for request in the context.
        // As it doesn't affect resolvers tested in here I don't modify the tests accordingly
        resolverContext =
                new FtpPathResolverContext(SESSION_TOKEN, service, generalInfoService, v3api, null, cache, null);
        context.checking(new Expectations()
            {
                {
                    ExperimentIdentifier experimentIdentifier =
                            new ExperimentIdentifierFactory(EXP_ID).createIdentifier();
                    allowing(service).listExperiments(SESSION_TOKEN,
                            Collections.singletonList(experimentIdentifier),
                            new ExperimentFetchOptions());
                    will(returnValue(Collections.singletonList(experiment)));
                }
            });

        ds1 =
                new DataSetBuilder().experiment(experiment).code("ds1").type(DS_TYPE1).modificationDate(MODIFICATION_DATE)
                        .registrationDate(REGISTRATION_DATE).status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        File ds1Root = new File(root, ds1.getCode());
        File ds1Original = new File(ds1Root, "original");
        ds1Original.mkdirs();
        FileUtilities.writeToFile(new File(ds1Original, "abc.txt"), "abcdefghijklmnopqrstuvwxyz");
        FileUtilities.writeToFile(new File(ds1Original, "some.properties"), "a = alpha\nb = bets");
        ds2 =
                new DataSetBuilder().experiment(experiment).code("ds2").type(DS_TYPE2).modificationDate(MODIFICATION_DATE)
                        .registrationDate(REGISTRATION_DATE).status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        File ds2Root = new File(root, ds2.getCode());
        File ds2Original = new File(ds2Root, "original2");
        ds2Original.mkdirs();
        FileUtilities.writeToFile(new File(ds2Original, "hello.txt"), "hello world");
        File dataFolder = new File(ds2Original, "data");
        dataFolder.mkdirs();
        FileUtilities.writeToFile(new File(dataFolder, "a1.tsv"), "t\tlevel\n1.34\t2\n");
        ds3 =
                new DataSetBuilder().experiment(experiment).code("ds3").type(DS_TYPE3).modificationDate(MODIFICATION_DATE)
                        .registrationDate(REGISTRATION_DATE).status(DataSetArchivingStatus.AVAILABLE).getDataSet();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(Method m)
    {
        try
        {
            if (context != null)
            {
                context.assertIsSatisfied();
            }
        } catch (Throwable t)
        {
            throw new Error(m.getName() + "() : ", t);
        }
    }

    @Test
    public void testInvalidConfig()
    {
        FtpPathResolverConfig config =
                new ResolverConfigBuilder().withTemplate(TEMPLATE_WITH_FILENAMES)
                        .showParentsAndChildren().getConfig();
        try
        {
            new TemplateBasedDataSetResourceResolver(config);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Template contains file name variable and the flag "
                    + "to show parents/children data sets is set.", ex.getMessage());
        }
    }

    @Test
    public void testWithParentsTopLevel()
    {
        FtpPathResolverConfig config =
                new ResolverConfigBuilder().withTemplate(BIG_TEMPLATE).showParentsAndChildren()
                        .getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);
        resolver.setContentProvider(simpleFileContentProvider);

        ds1.setParents(Arrays.<AbstractExternalData> asList(ds2));
        final List<AbstractExternalData> dataSets = Arrays.<AbstractExternalData> asList(ds1);

        prepareExperimentListExpectations(dataSets);
        prepareGetDataSetMetaData(ds1);
        prepareListDataSetsByCode(ds2);

        String dataSetPathElement = "DS-DS_TYPE1-ds1-" + RENDERED_REGISTRATION_DATE + "-A";
        String path = EXP_ID + FtpConstants.FILE_SEPARATOR + dataSetPathElement;
        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertEquals(dataSetPathElement, ftpFile.getName());
        assertEquals(path, ftpFile.getAbsolutePath());
        assertEquals(true, ftpFile.isDirectory());
        List<FtpFile> files = ftpFile.listFiles();
        assertEquals("PARENT-DS-DS_TYPE2-ds2-" + RENDERED_REGISTRATION_DATE + "-A", files.get(0)
                .getName());
        assertEquals(true, files.get(0).isDirectory());
        assertEquals("original", files.get(1).getName());
        assertEquals(true, files.get(1).isDirectory());
        assertEquals(2, files.size());
    }

    @Test
    public void testChildOfParent()
    {
        FtpPathResolverConfig config =
                new ResolverConfigBuilder().withTemplate(BIG_TEMPLATE).showParentsAndChildren()
                        .getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);
        resolver.setContentProvider(simpleFileContentProvider);

        ds1.setParents(Arrays.<AbstractExternalData> asList(ds2));
        ds2.setChildren(Arrays.<AbstractExternalData> asList(ds1, ds3));
        final List<AbstractExternalData> dataSets = Arrays.<AbstractExternalData> asList(ds1);

        prepareExperimentListExpectations(dataSets);
        prepareGetDataSetMetaData(ds1);
        prepareListDataSetsByCode(ds2);
        prepareGetDataSetMetaData(ds2);
        prepareListDataSetsByCode(ds1, ds3);

        String dataSetPathElement = "DS-DS_TYPE1-ds1-" + RENDERED_REGISTRATION_DATE + "-A";
        String ds2AsParent = "PARENT-DS-DS_TYPE2-ds2-" + RENDERED_REGISTRATION_DATE + "-A";

        String path =
                EXP_ID + FtpConstants.FILE_SEPARATOR + dataSetPathElement
                        + FtpConstants.FILE_SEPARATOR + ds2AsParent;
        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertEquals(ds2AsParent, ftpFile.getName());
        assertEquals(path, ftpFile.getAbsolutePath());
        assertEquals(true, ftpFile.isDirectory());
        List<FtpFile> files = ftpFile.listFiles();
        assertEquals("CHILD-DS-DS_TYPE3-ds3-" + RENDERED_REGISTRATION_DATE + "-B", files.get(0)
                .getName());
        assertEquals(true, files.get(0).isDirectory());
        assertEquals("original2", files.get(1).getName());
        assertEquals(true, files.get(1).isDirectory());
        assertEquals(2, files.size());
    }

    @Test
    public void testAvoidInfiniteParentChildChains()
    {
        FtpPathResolverConfig config =
                new ResolverConfigBuilder().withTemplate(BIG_TEMPLATE).showParentsAndChildren()
                        .getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);
        resolver.setContentProvider(simpleFileContentProvider);

        ds1.setParents(Arrays.<AbstractExternalData> asList(ds2));
        ds2.setChildren(Arrays.<AbstractExternalData> asList(ds1));
        final List<AbstractExternalData> dataSets = Arrays.<AbstractExternalData> asList(ds1);

        prepareExperimentListExpectations(dataSets);
        prepareGetDataSetMetaData(ds1);
        prepareListDataSetsByCode(ds2);
        prepareGetDataSetMetaData(ds2);
        prepareListDataSetsByCode(ds1);

        String dataSetPathElement = "DS-DS_TYPE1-ds1-" + RENDERED_REGISTRATION_DATE + "-A";
        String ds2AsParent = "PARENT-DS-DS_TYPE2-ds2-" + RENDERED_REGISTRATION_DATE + "-A";

        String path =
                EXP_ID + FtpConstants.FILE_SEPARATOR + dataSetPathElement
                        + FtpConstants.FILE_SEPARATOR + ds2AsParent;
        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertEquals(ds2AsParent, ftpFile.getName());
        assertEquals(path, ftpFile.getAbsolutePath());
        assertEquals(true, ftpFile.isDirectory());
        List<FtpFile> files = ftpFile.listFiles();
        assertEquals("original2", files.get(0).getName());
        assertEquals(true, files.get(0).isDirectory());
        assertEquals(1, files.size());
    }

    @Test
    public void testResolveNestedFilesWithSimpleTemplate() throws IOException
    {
        FtpPathResolverConfig config =
                new ResolverConfigBuilder().withTemplate(SIMPLE_TEMPLATE).showParentsAndChildren()
                        .getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);
        resolver.setContentProvider(simpleFileContentProvider);

        ds1.setParents(Arrays.<AbstractExternalData> asList(ds2));
        ds2.setChildren(Arrays.<AbstractExternalData> asList(ds1));
        final List<AbstractExternalData> dataSets = Arrays.<AbstractExternalData> asList(ds1);

        prepareExperimentListExpectations(dataSets);
        prepareGetDataSetMetaData(ds1);
        prepareListDataSetsByCode(ds2);
        prepareGetDataSetMetaData(ds2);
        prepareListDataSetsByCode(ds1);

        String path =
                EXP_ID + FtpConstants.FILE_SEPARATOR + "ds1" + FtpConstants.FILE_SEPARATOR
                        + "PARENT-ds2" + FtpConstants.FILE_SEPARATOR + "original2"
                        + FtpConstants.FILE_SEPARATOR + "data" + FtpConstants.FILE_SEPARATOR
                        + "a1.tsv";
        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertEquals("a1.tsv", ftpFile.getName());
        assertEquals(path, ftpFile.getAbsolutePath());
        assertEquals(true, ftpFile.isFile());
        InputStream fileContent = ftpFile.createInputStream(0);
        assertEquals("[t\tlevel, 1.34\t2]", IOUtils.readLines(fileContent).toString());
        fileContent.close();
    }

    @Test
    public void testHierarchicalContentClosed() throws IOException
    {
        FtpPathResolverConfig config =
                new ResolverConfigBuilder().withTemplate(SIMPLE_TEMPLATE).getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);
        resolver.setContentProvider(hierarchicalContentProvider);

        final String subPath = "fileName.txt";

        String path =
                EXP_ID + FtpConstants.FILE_SEPARATOR + ds1.getCode() + FtpConstants.FILE_SEPARATOR
                        + subPath;

        List<AbstractExternalData> dataSets = Arrays.<AbstractExternalData> asList(ds1);

        prepareExperimentListExpectations(dataSets);

        final RecordingMatcher<ISessionTokenProvider> sessionTokeProviderMatcher = new RecordingMatcher<ISessionTokenProvider>();
        context.checking(new Expectations()
            {
                {
                    IHierarchicalContent content =
                            context.mock(IHierarchicalContent.class, ds1.getCode());

                    one(hierarchicalContentProvider).asContent((AbstractExternalData) ds1);
                    will(returnValue(content));

                    one(hierarchicalContentProvider).cloneFor(with(sessionTokeProviderMatcher));
                    will(returnValue(hierarchicalContentProvider));

                    IHierarchicalContentNode rootNode =
                            context.mock(IHierarchicalContentNode.class, "root");
                    IHierarchicalContentNode fileNode =
                            context.mock(IHierarchicalContentNode.class, "file");

                    one(content).getRootNode();
                    will(returnValue(rootNode));

                    one(rootNode).getChildNodes();
                    will(returnValue(Arrays.asList(fileNode)));

                    one(fileNode).getName();
                    will(returnValue(subPath));

                    one(fileNode).getRelativePath();
                    will(returnValue(subPath));

                    exactly(2).of(fileNode).isDirectory();
                    will(returnValue(false));

                    one(fileNode).getFileLength();
                    will(returnValue(2L));

                    one(fileNode).getInputStream();
                    ByteArrayInputStream is = new ByteArrayInputStream(new byte[] {});
                    will(returnValue(is));

                    allowing(content).getNode(subPath);
                    will(returnValue(fileNode));

                    oneOf(fileNode).getLastModified();
                    will(returnValue(0L));

                    atLeast(1).of(content).close();
                }
            });

        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertEquals(SESSION_TOKEN, sessionTokeProviderMatcher.recordedObject().getSessionToken());
        assertNotNull(ftpFile);
        assertEquals(subPath, ftpFile.getName());
        assertTrue(ftpFile.isFile());
        InputStream fileContent = ftpFile.createInputStream(0);
        // this call will also close the IHierarchicalContent
        fileContent.close();
    }

    @Test
    public void testSubPathAndFileFilters()
    {
        FtpPathResolverConfig config =
                new ResolverConfigBuilder().withTemplate(TEMPLATE_WITH_FILENAMES)
                        .withFileListSubPath(DS_TYPE1, "orig[^/]*")
                        .withFileListFilter(DS_TYPE1, "[^.]*\\.txt").getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);
        resolver.setContentProvider(simpleFileContentProvider);

        List<AbstractExternalData> dataSets = Arrays.<AbstractExternalData> asList(ds1, ds2);

        prepareExperimentListExpectations(dataSets);

        List<FtpFile> files =
                resolver.listExperimentChildrenPaths(experiment, EXP_ID, resolverContext);

        assertNotNull(files);
        assertEquals("DS-DS_TYPE1-abc.txt-A", files.get(0).getName());
        assertTrue(files.get(0).isFile());
        assertEquals(26, files.get(0).getSize());
        assertEquals("DS-DS_TYPE2-original2-B", files.get(1).getName());
        assertTrue(files.get(1).isDirectory());
        assertEquals(2, files.size());
    }

    private void prepareExperimentListExpectations(final List<AbstractExternalData> dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSetsByExperimentID(SESSION_TOKEN, new TechId(experiment));
                    will(returnValue(dataSets));
                }
            });
    }

    private void prepareGetDataSetMetaData(final AbstractExternalData... dataSets)
    {
        final List<String> codes = extractCodes(dataSets);
        final List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> translateDataSets =
                Translator.translate(Arrays.asList(dataSets),
                        EnumSet.of(Connections.PARENTS, Connections.CHILDREN));
        context.checking(new Expectations()
            {
                {
                    one(generalInfoService).getDataSetMetaData(
                            SESSION_TOKEN,
                            codes,
                            EnumSet.of(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS,
                                    DataSetFetchOption.CHILDREN));
                    will(returnValue(translateDataSets));
                }
            });
    }

    private void prepareListDataSetsByCode(final AbstractExternalData... dataSets)
    {
        final List<String> codes = extractCodes(dataSets);
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSetsByCode(SESSION_TOKEN, codes);
                    will(returnValue(Arrays.asList(dataSets)));
                }
            });

    }

    private List<String> extractCodes(final AbstractExternalData... dataSets)
    {
        final List<String> codes = new ArrayList<String>();
        for (AbstractExternalData dataSet : dataSets)
        {
            codes.add(dataSet.getCode());
        }
        return codes;
    }

    private String getHierarchicalContentMockName(String dataSetCode)
    {
        return dataSetCode;
    }

    protected IHierarchicalContent getHierarchicalContentMock(String dataSetCode)
    {
        String mockName = getHierarchicalContentMockName(dataSetCode);
        return context.getMock(mockName, IHierarchicalContent.class);
    }

}
