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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Test of {@link HierarchicalContentProvider}
 * 
 * @author Piotr Buczek
 */
@Friend(toClasses = HierarchicalContentProvider.class)
public class HierarchicalContentProviderTest extends AssertJUnit
{

    private IDataSetDirectoryProvider directoryProvider;

    private IHierarchicalContentFactory hierarchicalContentFactory;

    private IEncapsulatedOpenBISService openbisService;

    private IShareIdManager shareIdManager;

    private Mockery context;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        shareIdManager = context.mock(IShareIdManager.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        hierarchicalContentFactory = context.mock(IHierarchicalContentFactory.class);
        context.checking(new Expectations()
            {
                {
                    allowing(directoryProvider).getShareIdManager();
                    will(returnValue(shareIdManager));
                }
            });
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        hierarchicalContentProvider =
                new HierarchicalContentProvider(openbisService, directoryProvider,
                        hierarchicalContentFactory, null, null, null, "STANDARD", null);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    void testAsContentFromDataSetCode()
    {
        final String dataSetCode = "DS_CODE";

        final DatasetLocation dataLocation =
                new DatasetLocation(dataSetCode, "LOCATION", "STANDARD", null);
        final DatasetLocationNode data = new DatasetLocationNode(dataLocation);
        final File dataRootFile = new File("DS_FILE");
        final RecordingMatcher<IDelegatedAction> actionMatcher = RecordingMatcher.create();
        context.checking(new Expectations()
            {
                {

                    one(openbisService).tryGetDataSetLocation(dataSetCode);
                    will(returnValue(data));

                    one(shareIdManager).lock(dataSetCode);
                    one(directoryProvider).getDataSetDirectory(data.getLocation());
                    will(returnValue(dataRootFile));

                    one(hierarchicalContentFactory).asHierarchicalContent(with(same(dataRootFile)),
                            with(actionMatcher));

                }
            });

        hierarchicalContentProvider.asContent(dataSetCode);
        context.assertIsSatisfied();

        // check that lock is released on recorded action execution
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).releaseLock(dataSetCode);
                }
            });
        actionMatcher.recordedObject().execute();
        context.assertIsSatisfied();
    }

    @Test
    void testAsContentFromContainerCode()
    {
        final String dataSet1Code = "DS_CODE_1";
        final String dataSet2Code = "DS_CODE_2";
        final String dataSet3Code = "DS_CODE_3";

        final File dataSetRootFile1 = new File("DS_FILE_1");
        final File dataSetRootFile2 = new File("DS_FILE_2");
        final File dataSetRootFile3 = new File("DS_FILE_3");

        final DatasetLocation dataSet1Location =
                new DatasetLocation(dataSet1Code, "LOCATION_1", "STANDARD", null, 3);
        final DatasetLocation dataSet2Location =
                new DatasetLocation(dataSet2Code, "LOCATION_2", "STANDARD", null, 1);
        final DatasetLocation dataSet3Location =
                new DatasetLocation(dataSet3Code, "LOCATION_3", "STANDARD", null, 2);

        final DatasetLocationNode dataSet1 = new DatasetLocationNode(dataSet1Location);
        final DatasetLocationNode dataSet2 = new DatasetLocationNode(dataSet2Location);
        final DatasetLocationNode dataSet3 = new DatasetLocationNode(dataSet3Location);

        final String containerCode = "CONTAINER_CODE";
        final DatasetLocation containerLocation =
                new DatasetLocation(containerCode, null, "STANDARD", null);
        containerLocation.setDataStoreCode("STANDARD");
        final DatasetLocationNode container = new DatasetLocationNode(containerLocation);
        container.addContained(dataSet1);
        container.addContained(dataSet2);
        container.addContained(dataSet3);

        final RecordingMatcher<IDelegatedAction> actionMatcher = RecordingMatcher.create();
        final RecordingMatcher<List<IHierarchicalContent>> contentMatcher = new RecordingMatcher<List<IHierarchicalContent>>();
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetDataSetLocation(containerCode);
                    will(returnValue(container));

                    one(shareIdManager).lock(dataSet1Code);
                    one(directoryProvider).getDataSetDirectory(dataSet1.getLocation());
                    will(returnValue(dataSetRootFile1));

                    one(shareIdManager).lock(dataSet2Code);
                    one(directoryProvider).getDataSetDirectory(dataSet2.getLocation());
                    will(returnValue(dataSetRootFile2));

                    one(shareIdManager).lock(dataSet3Code);
                    one(directoryProvider).getDataSetDirectory(dataSet3.getLocation());
                    will(returnValue(dataSetRootFile3));

                    IHierarchicalContent content1 = new DummyHierarchicalContent("content 1");
                    IHierarchicalContent content2 = new DummyHierarchicalContent("content 2");
                    one(hierarchicalContentFactory).asHierarchicalContent(
                            with(same(dataSetRootFile1)), with(actionMatcher));
                    will(returnValue(content1));
                    one(hierarchicalContentFactory).asHierarchicalContent(
                            with(same(dataSetRootFile2)), with(actionMatcher));
                    will(returnValue(content2));

                    // creation of content for dataSet3 fails:
                    // - its lock should be automatically released
                    // - it should not be a component of the virtual content
                    one(hierarchicalContentFactory).asHierarchicalContent(
                            with(same(dataSetRootFile3)), with(actionMatcher));
                    will(throwException(new IllegalArgumentException("")));
                    one(shareIdManager).releaseLock(dataSet3Code);

                    one(hierarchicalContentFactory).asVirtualHierarchicalContent(with(
                            contentMatcher));
                }
            });

        hierarchicalContentProvider.asContent(containerCode);
        context.assertIsSatisfied();

        // check that locks are released on execution of recorded actions
        assertEquals(3, actionMatcher.getRecordedObjects().size());
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).releaseLock(dataSet2Code);
                }
            });
        List<IHierarchicalContent> contents = contentMatcher.recordedObject();
        assertEquals(2, contents.size());
        actionMatcher.getRecordedObjects().get(0).execute();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).releaseLock(dataSet3Code);
                }
            });
        actionMatcher.getRecordedObjects().get(1).execute();
        // context.checking(new Expectations()
        // {
        // {
        // one(shareIdManager).releaseLock(componentCode3);
        // }
        // });
        // actionMatcher.getRecordedObjects().get(2).execute();

        context.assertIsSatisfied();
    }

    @Test
    void testAsContentFromFakeDataSetCodeFails()
    {
        final String dataSetCode = "FAKE_DS_CODE";

        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetDataSetLocation(dataSetCode);
                    will(returnValue(null));
                }
            });

        try
        {
            hierarchicalContentProvider.asContent(dataSetCode);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unknown data set: " + dataSetCode, ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testAsContentFromDataSetLocation()
    {
        final String code = "DS_CODE";
        final String location = "DS_LOCATION";
        final IDatasetLocation dataSetLocation = new IDatasetLocation()
            {

                @Override
                public String getDataSetCode()
                {
                    return code;
                }

                @Override
                public String getDataSetLocation()
                {
                    return location;
                }

                @Override
                public String getDataStoreUrl()
                {
                    return null;
                }

                @Override
                public String getDataStoreCode()
                {
                    return null;
                }

                @Override
                public Integer getOrderInContainer(String containerDataSetCode)
                {
                    return null;
                }

                @Override
                public Long getDataSetSize()
                {
                    // TODO Auto-generated method stub
                    return null;
                }

            };

        final File dataRootFile = new File("DS_FILE");
        final RecordingMatcher<IDelegatedAction> actionMatcher = RecordingMatcher.create();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(code);
                    one(directoryProvider).getDataSetDirectory(dataSetLocation);
                    will(returnValue(dataRootFile));

                    one(hierarchicalContentFactory).asHierarchicalContent(with(same(dataRootFile)),
                            with(actionMatcher));
                }
            });

        hierarchicalContentProvider.asContent(dataSetLocation);
        context.assertIsSatisfied();

        // check that lock is released on recorded action execution
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).releaseLock(code);
                }
            });
        actionMatcher.recordedObject().execute();
        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testAsContentFromDataSetFile()
    {
        final File dataRootFile = new File("DS_FILE");

        context.checking(new Expectations()
            {
                {
                    one(hierarchicalContentFactory).asHierarchicalContent(dataRootFile,
                            IDelegatedAction.DO_NOTHING);
                }
            });

        hierarchicalContentProvider.asContent(dataRootFile);

        context.assertIsSatisfied();

    }

    private static class DummyHierarchicalContent implements IHierarchicalContent
    {
        private String name;

        DummyHierarchicalContent(String name)
        {
            this.name = name;
        }

        @Override
        public IHierarchicalContentNode getRootNode()
        {
            return null;
        }

        @Override
        public IHierarchicalContentNode getNode(String relativePath)
                throws IllegalArgumentException
        {
            return null;
        }

        @Override
        public IHierarchicalContentNode tryGetNode(String relativePath)
        {
            return null;
        }

        @Override
        public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
        {
            return null;
        }

        @Override
        public List<IHierarchicalContentNode> listMatchingNodes(String startingPath,
                String fileNamePattern)
        {
            return null;
        }

        @Override
        public void close()
        {
        }

        @Override
        public String toString()
        {
            return name;
        }

    }

}
