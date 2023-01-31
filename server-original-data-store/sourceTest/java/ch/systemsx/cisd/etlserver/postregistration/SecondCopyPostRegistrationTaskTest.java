/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogUtils;
import ch.systemsx.cisd.common.test.InvocationRecordingWrapper;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.HierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IContentCache;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IDssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class SecondCopyPostRegistrationTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_STORE_CODE = "DSS";

    private static final String EXAMPLE_CONTENT = "Hello world!";

    private static final String DATA_SET1_LOCATION = "a/b/c/ds1";

    private static final String DATA_SET1_EXAMPLE_FILE_PATH = DATA_SET1_LOCATION
            + "/original/hello.txt";

    private static final String DATA_SET1 = "ds1";

    private static final String SHARE_ID = "1";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IDataStoreServiceInternal dataStoreService;

    private File destination;

    private SecondCopyPostRegistrationTask task;

    private File store;

    private IShareIdManager shareIdManager;

    private IConfigProvider configProvider;

    private IContentCache contentCache;

    private InvocationRecordingWrapper<IHierarchicalContentProvider> contentProviderRecordingWrapper;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO,
                ".*(SecondCopyPostRegistrationTask|AbstractDatastorePlugin|DataSetFileOperationsManager)");
        logRecorder.addFilter(new Filter()
            {
                @Override
                public int decide(LoggingEvent event)
                {
                    String loggerName = event.getLoggerName();
                    return loggerName.contains("RsyncCopier") ? Filter.DENY : Filter.ACCEPT;
                }
            });
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dataStoreService = context.mock(IDataStoreServiceInternal.class);
        shareIdManager = context.mock(IShareIdManager.class);
        configProvider = context.mock(IConfigProvider.class);
        contentCache = context.mock(IContentCache.class);
        IDssServiceRpcGenericFactory dssServiceFactory =
                context.mock(IDssServiceRpcGenericFactory.class);
        store = new File(workingDirectory, "store");
        final IDataSetDirectoryProvider dirProvider =
                new DataSetDirectoryProvider(store, shareIdManager);
        context.checking(new Expectations()
            {
                {
                    allowing(dataStoreService).getDataSetDirectoryProvider();
                    will(returnValue(dirProvider));

                    allowing(configProvider).getStoreRoot();
                    will(returnValue(store));

                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));
                }
            });
        OpenBISSessionHolder sessionHolder = new OpenBISSessionHolder();
        sessionHolder.setDataStoreCode(DATA_STORE_CODE);
        contentProviderRecordingWrapper =
                InvocationRecordingWrapper.<IHierarchicalContentProvider> wrap(
                        new HierarchicalContentProvider(service, shareIdManager, configProvider,
                                contentCache, new DefaultFileBasedHierarchicalContentFactory(),
                                dssServiceFactory, sessionHolder, null), IHierarchicalContentProvider.class, IHierarchicalContent.class,
                        IHierarchicalContentNode.class);
        File exampleFile = new File(store, SHARE_ID + "/" + DATA_SET1_EXAMPLE_FILE_PATH);
        exampleFile.getParentFile().mkdirs();
        FileUtilities.writeToFile(exampleFile, EXAMPLE_CONTENT);
        destination = new File(workingDirectory, "second-copy-destination");
        Properties properties = new Properties();
        properties.setProperty("destination", destination.getAbsolutePath());
        task =
                new SecondCopyPostRegistrationTask(properties, service, dataStoreService,
                        contentProviderRecordingWrapper.getProxy());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod()
    {
        if (logRecorder != null)
        {
            logRecorder.reset();
        }
        if (context != null)
        {
            // The following line of code should also be called at the end of each test method.
            // Otherwise one do not known which test failed.
            context.assertIsSatisfied();
        }
    }

    @Test
    public void testHappyCase()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSetsByCode(Arrays.asList(DATA_SET1));
                    PhysicalDataSet ds1 =
                            new DataSetBuilder(42).code(DATA_SET1)
                                    .store(new DataStoreBuilder(DATA_STORE_CODE).getStore())
                                    .fileFormat("TXT").location(DATA_SET1_LOCATION).getDataSet();
                    will(returnValue(Arrays.asList(ds1)));

                    one(service).tryGetDataSetLocation(DATA_SET1);
                    will(returnValue(new ExternalDataLocationNode(ds1)));

                    allowing(shareIdManager).getShareId(DATA_SET1);
                    will(returnValue(SHARE_ID));

                    one(shareIdManager).lock(DATA_SET1);
                    one(shareIdManager).releaseLock(DATA_SET1);

                    one(service)
                            .updateShareIdAndSize(DATA_SET1, SHARE_ID, EXAMPLE_CONTENT.length());
                }
            });

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET1, false);
        executor.execute();

        assertEquals(
                "INFO  OPERATION.SecondCopyPostRegistrationTask - "
                        + "Archiving data set 'ds1' without updating archiving status.\n"
                        + "INFO  OPERATION.AbstractDatastorePlugin - Archiving of the "
                        + "following datasets has been requested: [Dataset 'ds1']\n"
                        + "INFO  OPERATION.DataSetFileOperationsManager - "
                        + "Copy dataset 'ds1' from '" + store.getPath() + "/1/a/b/c/ds1' to '"
                        + destination.getAbsolutePath() + "/a/b/c", logRecorder.getLogContent());
        assertEquals(
                EXAMPLE_CONTENT,
                FileUtilities.loadToString(
                        new File(store, SHARE_ID + "/" + DATA_SET1_EXAMPLE_FILE_PATH)).trim());
        assertEquals(EXAMPLE_CONTENT,
                FileUtilities.loadToString(new File(destination, DATA_SET1_EXAMPLE_FILE_PATH))
                        .trim());
        assertEquals(
                "asContentWithoutModifyingAccessTimestamp(ds1)\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getRelativePath()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().isDirectory()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getChildNodes()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getChildNodes().get(0).getRelativePath()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getChildNodes().get(0).isDirectory()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getChildNodes().get(0).getChildNodes()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getChildNodes().get(0).getChildNodes().get(0).getRelativePath()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getChildNodes().get(0).getChildNodes().get(0).isDirectory()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getChildNodes().get(0).getChildNodes().get(0).getFileLength()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).getRootNode().getChildNodes().get(0).getChildNodes().get(0).isChecksumCRC32Precalculated()\n"
                        + "asContentWithoutModifyingAccessTimestamp(ds1).close()", contentProviderRecordingWrapper.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testDestinationIsNotADirectory() throws IOException
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSetsByCode(Arrays.asList(DATA_SET1));
                    PhysicalDataSet ds1 =
                            new DataSetBuilder(42).code(DATA_SET1)
                                    .store(new DataStoreBuilder(DATA_STORE_CODE).getStore())
                                    .fileFormat("TXT").location(DATA_SET1_LOCATION).getDataSet();
                    will(returnValue(Arrays.asList(ds1)));

                    allowing(shareIdManager).getShareId(DATA_SET1);
                    will(returnValue(SHARE_ID));

                    one(service)
                            .updateShareIdAndSize(DATA_SET1, SHARE_ID, EXAMPLE_CONTENT.length());
                }
            });

        destination.createNewFile();
        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET1, false);
        try
        {
            executor.execute();
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException e)
        {
            assertEquals("Archiving of data set ds1 failed.",e.getMessage());
        }

        assertEquals("INFO  OPERATION.SecondCopyPostRegistrationTask - "
                + "Archiving data set 'ds1' without updating archiving status.\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Archiving of the "
                + "following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DataSetFileOperationsManager - Copy dataset 'ds1' from '"
                + store.getPath() + "/1/a/b/c/ds1' to '" + destination.getAbsolutePath()
                + "/a/b/c\n" + "ERROR OPERATION.AbstractDatastorePlugin - Archiving failed :path '"
                + destination.getAbsolutePath() + "/a/b/c' does not exist\n"
                + "java.io.IOException: path '" + destination.getAbsolutePath()
                + "/a/b/c' does not exist\n" + "ERROR NOTIFY.AbstractDatastorePlugin - "
                + "Archiving for dataset ds1 finished with the status: "
                + "ERROR: \"Archiving failed :path '" + destination.getAbsolutePath()
                + "/a/b/c' does not exist\".\n" + "ERROR NOTIFY.SecondCopyPostRegistrationTask - "
                + "Creating a second copy of dataset 'ds1' has failed.\n"
                + "Error encountered : Archiving failed :path '" + destination.getAbsolutePath()
                + "/a/b/c' does not exist",
                LogUtils.removeEmbeddedStackTrace(logRecorder.getLogContent()));
        assertEquals(
                EXAMPLE_CONTENT,
                FileUtilities.loadToString(
                        new File(store, SHARE_ID + "/" + DATA_SET1_EXAMPLE_FILE_PATH)).trim());
        assertEquals(false, new File(destination, DATA_SET1_EXAMPLE_FILE_PATH).exists());
        assertEquals("", contentProviderRecordingWrapper.toString());
        context.assertIsSatisfied();
    }

}
