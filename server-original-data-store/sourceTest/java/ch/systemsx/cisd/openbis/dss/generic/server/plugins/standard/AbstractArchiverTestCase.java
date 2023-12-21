/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IUnarchivingPreparation;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractArchiverTestCase extends AbstractFileSystemTestCase
{
    protected static final String DATA_STORE_CODE = "dss1";

    public static final class ShareFinder implements IShareFinder
    {
        static Properties properties;

        static SimpleDataSetInformationDTO recordedDataSet;

        static List<Share> recordedShares;

        private boolean alwaysReturnNull = false;

        public ShareFinder(Properties properties)
        {
            ShareFinder.properties = properties;
            if (properties.containsKey("alwaysReturnNull"))
            {
                this.alwaysReturnNull = true;
            }
        }

        @Override
        public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
        {
            ShareFinder.recordedDataSet = dataSet;
            ShareFinder.recordedShares = shares;
            if (shares.isEmpty() || alwaysReturnNull)
            {
                return null;
            } else
            {
                return shares.get(0);
            }
        }
    }

    protected BufferedAppender logRecorder;

    protected Mockery context;

    protected IDataSetDirectoryProvider dataSetDirectoryProvider;

    protected ArchiverTaskContext archiverTaskContext;

    protected IDataSetStatusUpdater statusUpdater;

    protected Properties properties;

    private BeanFactory beanFactory;

    protected IConfigProvider configProvider;

    protected IEncapsulatedOpenBISService service;

    protected IShareIdManager shareIdManager;

    protected File store;

    protected File share1;

    private IDataStoreServiceInternal dataStoreService;

    protected IDataSetDeleter deleter;

    protected IHierarchicalContentProvider contentProvider;

    protected IDataSetFileOperationsManager fileOperationsManager;

    protected IDataSetFileOperationsManagerFactory fileOperationsManagerFactory;

    protected IUnarchivingPreparation unarchivingPreparation;

    public AbstractArchiverTestCase()
    {
    }

    public AbstractArchiverTestCase(boolean cleanAfterMethod)
    {
        super(cleanAfterMethod);
    }

    @BeforeMethod
    public void beforeMethod(Method method)
    {
        System.out.println(">>>>>> set up for " + method.getName() + " " + Arrays.asList(workingDirectory.list()));
        LogInitializer.init();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        fileOperationsManager = context.mock(IDataSetFileOperationsManager.class);
        dataSetDirectoryProvider = context.mock(IDataSetDirectoryProvider.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        unarchivingPreparation = context.mock(IUnarchivingPreparation.class);
        statusUpdater = context.mock(IDataSetStatusUpdater.class);
        configProvider = context.mock(IConfigProvider.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        deleter = context.mock(IDataSetDeleter.class);
        final Advised adviced = context.mock(Advised.class);
        final TargetSource targetSource = context.mock(TargetSource.class);
        dataStoreService = context.mock(IDataStoreServiceInternal.class);
        beanFactory = context.mock(BeanFactory.class);
        fileOperationsManagerFactory = context.mock(IDataSetFileOperationsManagerFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        context.checking(new Expectations()
            {
                {
                    allowing(beanFactory).getBean("config-provider");
                    will(returnValue(configProvider));

                    allowing(beanFactory).getBean("hierarchical-content-provider");
                    will(returnValue(contentProvider));

                    allowing(beanFactory).getBean("openBIS-service");
                    will(returnValue(service));

                    allowing(beanFactory).getBean("share-id-manager");
                    will(returnValue(shareIdManager));

                    allowing(beanFactory).getBean("data-store-service");
                    will(returnValue(adviced));

                    allowing(adviced).getTargetSource();
                    will(returnValue(targetSource));

                    try
                    {
                        allowing(targetSource).getTarget();
                        will(returnValue(dataStoreService));
                    } catch (Exception ex)
                    {
                        // ignored
                    }

                    allowing(dataSetDirectoryProvider).getStoreRoot();
                    will(returnValue(store));

                    allowing(dataStoreService).getDataSetDeleter();
                    will(returnValue(deleter));

                    allowing(dataStoreService).getDataSetDirectoryProvider();
                    will(returnValue(dataSetDirectoryProvider));

                    allowing(dataSetDirectoryProvider).getShareIdManager();
                    will(returnValue(shareIdManager));

                    allowing(fileOperationsManagerFactory).create();
                    will(returnValue(fileOperationsManager));

                    allowing(shareIdManager).cleanupLocks();
                }
            });

        IncomingShareIdProviderTestWrapper.setShareIds(Arrays.asList("1"));
        store = new File(workingDirectory, "store");
        store.mkdirs();
        share1 = new File(store, "1");
        share1.mkdir();
        archiverTaskContext = new ArchiverTaskContext(dataSetDirectoryProvider, contentProvider);
        properties = new Properties();
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        System.out.println("======= Log content for " + method.getName() + "():");
        System.out.println(logRecorder.getLogContent());
        System.out.println("======================");
        logRecorder.reset();
        ServiceProviderTestWrapper.restoreApplicationContext();
        IncomingShareIdProviderTestWrapper.restoreOriginalShareIds();
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

}