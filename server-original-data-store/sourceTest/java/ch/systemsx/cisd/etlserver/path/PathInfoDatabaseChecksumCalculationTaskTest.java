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

package ch.systemsx.cisd.etlserver.path;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class PathInfoDatabaseChecksumCalculationTaskTest extends AbstractFileSystemTestCase
{

    private static final String LOGGER_NAME = "OPERATION." + PathInfoDatabaseChecksumCalculationTask.class.getSimpleName();

    private static final String LOG_INFO_PREFIX = "INFO  " + LOGGER_NAME + " - ";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IPathsInfoDAO dao;

    private IHierarchicalContentProvider contentProvider;

    private IHierarchicalContent content1;

    private IHierarchicalContent content2;

    private IHierarchicalContentNode node1;

    private IHierarchicalContentNode node2;

    private IHierarchicalContentNode node3;

    @BeforeMethod
    public void setUpMocks()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, LOGGER_NAME);
        context = new Mockery();
        dao = context.mock(IPathsInfoDAO.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        content1 = context.mock(IHierarchicalContent.class, "content 1");
        content2 = context.mock(IHierarchicalContent.class, "content 2");
        node1 = context.mock(IHierarchicalContentNode.class, "node 1");
        node2 = context.mock(IHierarchicalContentNode.class, "node 2");
        node3 = context.mock(IHierarchicalContentNode.class, "node 3");
    }

    @AfterMethod
    public void checkContext()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        final PathEntryDTO e1 = pathEntry("1", "f1.txt");
        final PathEntryDTO e2 = pathEntry("1", "f2.txt");
        final PathEntryDTO e3 = pathEntry("2", "f3.txt");
        final PathEntryDTO e4 = pathEntry("3", "f4.txt");
        context.checking(new Expectations()
            {
                {
                    one(dao).listDataSetFilesWithUnkownChecksum();
                    will(returnValue(Arrays.asList(e4, e2, e3, e1)));

                    one(dao).commit();
                    
                    one(contentProvider).asContentWithoutModifyingAccessTimestamp("1");
                    will(returnValue(content1));

                    one(contentProvider).asContentWithoutModifyingAccessTimestamp("2");
                    will(returnValue(content2));

                    one(contentProvider).asContentWithoutModifyingAccessTimestamp("3");
                    will(throwException(new IllegalArgumentException("unkown data set 3")));

                    one(content1).getNode("f1.txt");
                    will(returnValue(node1));

                    one(node1).getInputStream();
                    will(returnValue(new ByteArrayInputStream("a".getBytes())));

                    one(dao).updateChecksum(e1.getId(), 1908338681, null);

                    one(content1).getNode("f2.txt");
                    will(returnValue(node2));

                    one(node2).getInputStream();
                    will(returnValue(new ByteArrayInputStream("b".getBytes())));

                    one(dao).updateChecksum(e2.getId(), -390611389, null);

                    one(dao).commit();

                    one(content2).getNode("f3.txt");
                    will(returnValue(node3));

                    one(node3).getInputStream();
                    will(returnValue(new ByteArrayInputStream("c".getBytes())));

                    one(dao).updateChecksum(e3.getId(), 112844655, null);

                    one(dao).commit();
                }
            });
        PathInfoDatabaseChecksumCalculationTask task 
                = new PathInfoDatabaseChecksumCalculationTask(dao, contentProvider, timeProvider(), null);
        System.out.println(task);

        task.execute();

        assertEquals(LOG_INFO_PREFIX + "Start calculating checksums of 4 files in 3 data sets.\n"
                + LOG_INFO_PREFIX
                + "10 seconds needed to update checksums of 2 files of data set 1.\n"
                + LOG_INFO_PREFIX
                + "20 seconds needed to update checksums of 1 files of data set 2.\n"
                + LOG_INFO_PREFIX + "Checksums of 3 files in 2 data sets have been calculated.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithSHA256Checksum()
    {
        final PathEntryDTO e1 = pathEntry("1", "f1.txt");
        context.checking(new Expectations()
            {
                {
                    one(dao).listDataSetFilesWithUnkownChecksum();
                    will(returnValue(Arrays.asList(e1)));

                    one(dao).commit();

                    one(contentProvider).asContentWithoutModifyingAccessTimestamp("1");
                    will(returnValue(content1));

                    one(content1).getNode("f1.txt");
                    will(returnValue(node1));

                    one(node1).getInputStream();
                    will(returnValue(new ByteArrayInputStream("a".getBytes())));

                    one(dao).updateChecksum(e1.getId(), -390611389, "SHA-256:ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb");

                    one(dao).commit();
                }
            });
        PathInfoDatabaseChecksumCalculationTask task 
                = new PathInfoDatabaseChecksumCalculationTask(dao, contentProvider, timeProvider(), "SHA-256");
        System.out.println(task);
        
        task.execute();
        
        assertEquals(LOG_INFO_PREFIX + "Start calculating checksums of 1 files in 1 data sets.\n"
                + LOG_INFO_PREFIX
                + "10 seconds needed to update checksums of 1 files of data set 1.\n"
                + LOG_INFO_PREFIX + "Checksums of 1 files in 1 data sets have been calculated.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }
    
    private PathEntryDTO pathEntry(String dataSetCode, String relativePath)
    {
        PathEntryDTO pathEntry =
                new PathEntryDTO(dataSetCode.hashCode(), null, relativePath, relativePath, 0, null,
                        null, false, new Date(0));
        pathEntry.setId(Long.parseLong(dataSetCode));
        pathEntry.setDataSetCode(dataSetCode);
        return pathEntry;
    }
    
    private ITimeProvider timeProvider()
    {
        return new ITimeProvider()
            {
                private long previousTime;

                private long currentTime = 10000;

                @Override
                public long getTimeInMilliseconds()
                {
                    System.err.println(this);
                    long nextTime = previousTime + currentTime;
                    previousTime = currentTime;
                    currentTime = nextTime;
                    return currentTime;
                }
            };
    }

}
