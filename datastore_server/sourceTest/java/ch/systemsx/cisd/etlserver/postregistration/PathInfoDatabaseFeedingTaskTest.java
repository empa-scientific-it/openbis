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

package ch.systemsx.cisd.etlserver.postregistration;

import static ch.systemsx.cisd.common.utilities.IDelegatedAction.DO_NOTHING;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=PathInfoDatabaseFeedingTask.class)
public class PathInfoDatabaseFeedingTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_CODE = "ds1";
    
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private IDataSetDirectoryProvider directoryProvider;
    private IShareIdManager shareIdManager;
    private IPathsInfoDAO dao;
    private IHierarchicalContentFactory contentFactory;
    private IHierarchicalContentNode node;
    private IHierarchicalContent content;
    private PathInfoDatabaseFeedingTask task;
    private File dataSetFolder;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        shareIdManager = context.mock(IShareIdManager.class);
        context.checking(new Expectations()
            {
                {
                    one(directoryProvider).getShareIdManager();
                    will(returnValue(shareIdManager));
                }
            });
        dao = context.mock(IPathsInfoDAO.class);
        contentFactory = context.mock(IHierarchicalContentFactory.class);
        content = context.mock(IHierarchicalContent.class);
        node = context.mock(IHierarchicalContentNode.class);
        Properties properties = new Properties();
        task = new PathInfoDatabaseFeedingTask(properties, service, directoryProvider, dao, contentFactory);
        dataSetFolder = new File(workingDirectory, "ds1");
        dataSetFolder.mkdirs();
    }
    
    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }
    
    @Test
    public void testHappyCase()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    ExternalData dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));

                    one(shareIdManager).lock(DATA_SET_CODE);

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(dataSetFolder));

                    one(dao).createDataSet(DATA_SET_CODE, "abc");
                    will(returnValue(101L));

                    one(contentFactory).asHierarchicalContent(dataSetFolder, DO_NOTHING);
                    will(returnValue(content));

                    one(contentFactory).asHierarchicalContentNode(content, dataSetFolder);
                    will(returnValue(node));

                    one(node).exists();
                    will(returnValue(true));

                    one(node).getName();
                    will(returnValue("ds1-root"));

                    one(node).getFileLength();
                    will(returnValue(12345L));

                    one(node).isDirectory();
                    will(returnValue(false));

                    one(dao).createDataSetFile(101L, null, "ds1-root", "ds1-root", 12345L, false);
                    will(returnValue(102L));

                    one(dao).commit();
                    one(shareIdManager).releaseLocks();
                }
            });

        task.createExecutor(DATA_SET_CODE).execute();
    }

    @Test
    public void testFailingCase()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    ExternalData dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));

                    one(shareIdManager).lock(DATA_SET_CODE);

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(dataSetFolder));

                    one(dao).createDataSet(DATA_SET_CODE, "abc");
                    will(returnValue(101L));

                    one(contentFactory).asHierarchicalContent(dataSetFolder, DO_NOTHING);
                    will(throwException(new RuntimeException("Oophs!")));

                    one(dao).rollback();
                    one(shareIdManager).releaseLocks();
                }
            });

        task.createExecutor(DATA_SET_CODE).execute();
   }
    
    @Test
    public void testNonExistingDataSetFolder()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    ExternalData dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));

                    one(shareIdManager).lock(DATA_SET_CODE);

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(new File(workingDirectory, "blabla")));

                    one(shareIdManager).releaseLocks();
                }
            });
        
        task.createExecutor(DATA_SET_CODE).execute();
    }
}
