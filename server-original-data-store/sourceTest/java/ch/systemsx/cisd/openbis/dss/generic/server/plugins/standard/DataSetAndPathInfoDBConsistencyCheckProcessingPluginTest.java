/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetAndPathInfoDBConsistencyCheckProcessingPluginTest extends
        AbstractFileSystemTestCase
{
    private static final String SHARE_ID = "42";

    private static final String USER_EMAIL = "a@bc.de";

    private static final String USER_ID = "test-user";

    /**
     * An object for specifying which code paths should be tested.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class TestCaseParameters implements Cloneable
    {
        /**
         * short description of the test. Will be presented in the test results view
         */
        protected String title;

        protected EnumSet<FailurePoint> failurePoints = EnumSet
                .noneOf(TestCaseParameters.FailurePoint.class);

        private TestCaseParameters(String title)
        {
            this.title = title;
        }

        @Override
        public TestCaseParameters clone()
        {
            try
            {
                return (TestCaseParameters) super.clone();
            } catch (CloneNotSupportedException e)
            {
                return null;
            }
        }

        @Override
        public String toString()
        {
            return title;
        }

        // add more when necessary
        public enum FailurePoint
        {
            ROOT_NODE_EXISTS, ROOT_NODE_PATH, FILE_LENGTH, CHECKSUM;
        }
    }

    private static <T> Object[][] asObjectArray(List<T> testCases)
    {
        Object[][] resultsList = new Object[testCases.size()][];

        int index = 0;
        for (T t : testCases)
        {
            resultsList[index++] = new Object[]
            { t };
        }

        return resultsList;
    }

    private DataSetAndPathInfoDBConsistencyCheckProcessingPlugin plugin;

    private Mockery context;

    private IMailClient mailClient;

    private IHierarchicalContentProvider fileProvider;

    private IHierarchicalContent fileContent;

    private IHierarchicalContentNode fileRootNode;

    private IHierarchicalContentNode fileChildNode;

    private IHierarchicalContentProvider pathInfoProvider;

    private IHierarchicalContent pathInfoContent;

    private IHierarchicalContentNode pathInfoRootNode;

    private IHierarchicalContentNode pathInfoChildNode;

    private DataSetProcessingContext processingContext;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
        fileProvider = context.mock(IHierarchicalContentProvider.class, "fileProvider");
        fileContent = context.mock(IHierarchicalContent.class, "fileContent");
        fileRootNode = context.mock(IHierarchicalContentNode.class, "fileRootNode");
        fileChildNode = context.mock(IHierarchicalContentNode.class, "fileChildNode");
        pathInfoProvider = context.mock(IHierarchicalContentProvider.class, "pathInfoProvider");
        pathInfoContent = context.mock(IHierarchicalContent.class, "pathInfoContent");
        pathInfoRootNode = context.mock(IHierarchicalContentNode.class, "pathInfoRootNode");
        pathInfoChildNode = context.mock(IHierarchicalContentNode.class, "pathInfoChildNode");

        plugin =
                new DataSetAndPathInfoDBConsistencyCheckProcessingPlugin(fileProvider,
                        pathInfoProvider);
        processingContext =
                new DataSetProcessingContext(null, new MockDataSetDirectoryProvider(
                        workingDirectory, SHARE_ID), null, mailClient, USER_ID, USER_EMAIL);
    }

    @DataProvider(name = "oneLevelFileHierarchyTestCaseProvider")
    public Object[][] oneLevelFileTestCases()
    {
        List<TestCaseParameters> testCases = oneLevelFileTestCasesList();
        return asObjectArray(testCases);
    }

    private List<TestCaseParameters> oneLevelFileTestCasesList()
    {
        ArrayList<TestCaseParameters> testCases = new ArrayList<TestCaseParameters>();
        TestCaseParameters testCase;

        testCase = new TestCaseParameters("No discrepencies");
        testCases.add(testCase);

        testCase = new TestCaseParameters("Root node exists");
        testCase.failurePoints.add(TestCaseParameters.FailurePoint.ROOT_NODE_EXISTS);
        testCases.add(testCase);

        testCase = new TestCaseParameters("Root node path");
        testCase.failurePoints.add(TestCaseParameters.FailurePoint.ROOT_NODE_PATH);
        testCases.add(testCase);

        testCase = new TestCaseParameters("File length discrepency");
        testCase.failurePoints.add(TestCaseParameters.FailurePoint.FILE_LENGTH);
        testCases.add(testCase);

        testCase = new TestCaseParameters("Checksum discrepency");
        testCase.failurePoints.add(TestCaseParameters.FailurePoint.CHECKSUM);
        testCases.add(testCase);

        return testCases;
    }

    @Test(dataProvider = "oneLevelFileHierarchyTestCaseProvider")
    public void testOneLevelFileHierarchy(final TestCaseParameters parameters)
    {
        final String ds1Code = "ds-1";
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder(ds1Code).location("a").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    // The test consists of parallel calls to the file and path-info structures
                    // In this test, all call return the same thing
                    setUpExpectations();
                }

                protected void setUpExpectations()
                {
                    getContent(ds1Code);
                    getRootNode();
                    rootNodeExists();
                    getRelativePath();

                    if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.ROOT_NODE_EXISTS))
                    {
                        closeContent();
                        sendEmail();
                        return;
                    }

                    rootNodeIsDirectory();
                    getRootChildren();
                    getChildRelativePath();
                    childNodeExists();
                    childIsDirectory();
                    getChildFileLength();
                    childIsChecksumPrecalculated();
                    childGetChecksum();
                    closeContent();
                    sendEmail();
                }

                protected void sendEmail()
                {
                    String subject = "File system and path info DB consistency check report";
                    String body = null;
                    if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.FILE_LENGTH))
                    {
                        body =
                                "Data sets checked:\n\n[ds-1]\n\n"
                                        + "Differences found:\n\n"
                                        + "Data set ds-1:\n"
                                        + "- 'data.txt' size in the file system = 1024 bytes but in the path info database = 2100 bytes.\n\n";
                    } else if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.ROOT_NODE_EXISTS))
                    {
                        body =
                                "Data sets checked:\n\n[ds-1]\n\n"
                                        + "Differences found:\n\n"
                                        + "Data set ds-1:\n"
                                        + "- 'targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetAndPathInfoDBConsistencyCheckProcessingPluginTest' "
                                        + "exists in the path info database but does not exist on the file system\n\n";

                    } else if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.ROOT_NODE_PATH))
                    {
                        body =
                                "Data sets checked:\n\n[ds-1]\n\n"
                                        + "Differences found:\n\n"
                                        + "Data set ds-1:\n"
                                        + "- 'different' is referenced in the path info database but does not exist on the file system\n"
                                        + "- 'targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetAndPathInfoDBConsistencyCheckProcessingPluginTest' is on the file system but is not referenced in the path info database\n\n";
                    } else if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.CHECKSUM))
                    {
                        body =
                                "Data sets checked:\n\n[ds-1]\n\n"
                                        + "Differences found:\n\n"
                                        + "Data set ds-1:\n"
                                        + "- 'data.txt' CRC32 checksum in the file system = 002cc5cb but in the path info database = 000f58fc\n\n";
                    } else
                    {
                        body = "Data sets checked:\n\n[ds-1]\n\nDifferences found:\n\nNone";

                    }
                    oneOf(mailClient).sendEmailMessage(subject, body, null, null,
                            new EMailAddress("a@bc.de"));

                }

                protected void closeContent()
                {
                    oneOf(fileContent).close();
                    oneOf(pathInfoContent).close();
                }

                protected void childGetChecksum()
                {
                    if (parameters.failurePoints.contains(TestCaseParameters.FailurePoint.CHECKSUM))
                    {
                        exactly(2).of(fileChildNode).getChecksumCRC32();
                        will(returnValue(2934219));
                        exactly(2).of(pathInfoChildNode).getChecksumCRC32();
                        will(returnValue(1005820));
                    } else
                    {
                        // In this case, the checksum should not be requested at all
                    }
                }

                protected void childIsChecksumPrecalculated()
                {
                    if (parameters.failurePoints.contains(TestCaseParameters.FailurePoint.CHECKSUM))
                    {
                        allowing(pathInfoChildNode).isChecksumCRC32Precalculated();
                        will(returnValue(true));
                    } else
                    {
                        allowing(pathInfoChildNode).isChecksumCRC32Precalculated();
                        will(returnValue(false));
                    }
                    allowing(fileChildNode).getChecksum();
                    allowing(pathInfoChildNode).getChecksum();
                }

                protected void getChildFileLength()
                {
                    if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.FILE_LENGTH))
                    {
                        exactly(2).of(fileChildNode).getFileLength();
                        will(returnValue(1024L));
                        exactly(2).of(pathInfoChildNode).getFileLength();
                        will(returnValue(2100L));
                    } else
                    {
                        oneOf(fileChildNode).getFileLength();
                        will(returnValue(1024L));
                        oneOf(pathInfoChildNode).getFileLength();
                        will(returnValue(1024L));
                    }
                }

                protected void childIsDirectory()
                {
                    exactly(2).of(fileChildNode).isDirectory();
                    will(returnValue(false));
                    exactly(1).of(pathInfoChildNode).isDirectory();
                    will(returnValue(false));
                }

                protected void childNodeExists()
                {
                    oneOf(fileChildNode).exists();
                    will(returnValue(true));
                    oneOf(pathInfoChildNode).exists();
                    will(returnValue(true));
                }

                protected void getChildRelativePath()
                {
                    if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.FILE_LENGTH)
                            || parameters.failurePoints
                                    .contains(TestCaseParameters.FailurePoint.CHECKSUM))
                    {
                        exactly(4).of(fileChildNode).getRelativePath();
                        will(returnValue("data.txt"));
                    } else
                    {
                        exactly(3).of(fileChildNode).getRelativePath();
                        will(returnValue("data.txt"));
                    }
                    exactly(3).of(pathInfoChildNode).getRelativePath();
                    will(returnValue("data.txt"));
                }

                protected void getRootChildren()
                {

                    oneOf(fileRootNode).getChildNodes();
                    will(returnValue(Arrays.asList(fileChildNode)));
                    oneOf(pathInfoRootNode).getChildNodes();
                    will(returnValue(Arrays.asList(pathInfoChildNode)));
                }

                protected void rootNodeIsDirectory()
                {
                    oneOf(fileRootNode).isDirectory();
                    will(returnValue(true));
                    oneOf(pathInfoRootNode).isDirectory();
                    will(returnValue(true));
                }

                protected void getRelativePath()
                {
                    if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.ROOT_NODE_EXISTS))
                    {
                        oneOf(pathInfoRootNode).getRelativePath();
                        will(returnValue(workingDirectory.getPath()));
                        return;
                    }
                    if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.ROOT_NODE_PATH))
                    {
                        exactly(2).of(fileRootNode).getRelativePath();
                        will(returnValue(workingDirectory.getPath()));
                        exactly(2).of(pathInfoRootNode).getRelativePath();
                        will(returnValue("different"));
                        return;
                    }

                    oneOf(fileRootNode).getRelativePath();
                    will(returnValue(workingDirectory.getPath()));
                    oneOf(pathInfoRootNode).getRelativePath();
                    will(returnValue(workingDirectory.getPath()));

                }

                protected void rootNodeExists()
                {
                    if (parameters.failurePoints
                            .contains(TestCaseParameters.FailurePoint.ROOT_NODE_EXISTS))
                    {
                        oneOf(fileRootNode).exists();
                        will(returnValue(false));
                        oneOf(pathInfoRootNode).exists();
                        will(returnValue(true));
                        return;
                    }
                    oneOf(fileRootNode).exists();
                    will(returnValue(true));
                    oneOf(pathInfoRootNode).exists();
                    will(returnValue(true));
                }

                protected void getRootNode()
                {
                    oneOf(fileContent).getRootNode();
                    will(returnValue(fileRootNode));
                    oneOf(pathInfoContent).getRootNode();
                    will(returnValue(pathInfoRootNode));
                }

                protected void getContent(final String dataSetCode)
                {
                    oneOf(fileProvider).asContentWithoutModifyingAccessTimestamp(dataSetCode);
                    will(returnValue(fileContent));
                    oneOf(pathInfoProvider).asContentWithoutModifyingAccessTimestamp(dataSetCode);
                    will(returnValue(pathInfoContent));
                }
            });
        plugin.process(Arrays.asList(ds1), processingContext);
        context.assertIsSatisfied();
    }
}
