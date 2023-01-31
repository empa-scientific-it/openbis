/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class MultiDataSetArchiveCleanerTest extends AbstractFileSystemTestCase
{
    private BufferedAppender logRecorder;

    private Mockery context;

    private IDataStoreServiceInternal dssService;

    private Properties properties;

    private IMailClient mailClient;

    private File deletionRequestDir;

    private File dataFolder1;

    private File dataFolder2;

    private File dataFolder3;

    private MessageChannel deleterChannel;

    private MessageChannel testrunnerChannel;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
        BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        dssService = ServiceProviderTestWrapper.mock(context, IDataStoreServiceInternal.class);
        context.checking(new Expectations()
            {
                {
                    allowing(dssService).createEMailClient();
                    will(returnValue(mailClient));
                }
            });
        properties = new Properties();
        deleterChannel = new MessageChannelBuilder().name("deleter").getChannel();
        testrunnerChannel = new MessageChannelBuilder().name("testrunner").getChannel();

        deletionRequestDir = new File(workingDirectory, "deletion-requests");
        deletionRequestDir.mkdirs();
        dataFolder1 = new File(workingDirectory, "data-folder1");
        dataFolder1.mkdirs();
        dataFolder2 = new File(workingDirectory, "data-folder2");
        dataFolder2.mkdirs();
        dataFolder3 = new File(workingDirectory, "data-folder3");
        dataFolder3.mkdirs();

    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        if (result.getStatus() == ITestResult.FAILURE)
        {
            fail(result.getName() + " failed. Log content:\n" + logRecorder.getLogContent());
        }
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testFailingDeleteForNoPrefixesForAsyncDeletion()
    {
        MultiDataSetArchiveCleaner cleaner = createCleaner(0);
        File file = new File(dataFolder1, "hi.txt");
        FileUtilities.writeToFile(file, "hello world!");
        assertEquals(true, file.exists());

        cleaner.delete(dataFolder1);

        assertEquals(true, dataFolder1.exists());
        assertEquals(true, file.exists());
        assertEquals("WARN  OPERATION.MultiDataSetArchiveCleaner - Failed to delete file immediately: " + dataFolder1,
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteForNoPrefixesForAsyncDeletion()
    {
        MultiDataSetArchiveCleaner cleaner = createCleaner(0);
        File file = new File(dataFolder1, "hi.txt");
        FileUtilities.writeToFile(file, "hello world!");
        assertEquals(true, file.exists());

        cleaner.delete(file);

        assertEquals(false, file.exists());
        assertEquals("INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " + file,
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testMissingPropertyDeletionRequestsDir()
    {
        properties.setProperty(MultiDataSetArchiveCleaner.FILE_PATH_PREFIXES_FOR_ASYNC_DELETION_KEY,
                dataFolder2.getAbsolutePath());

        try
        {
            createCleaner(0);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key 'deletion-requests-dir' not found in properties '["
                    + MultiDataSetArchiveCleaner.FILE_PATH_PREFIXES_FOR_ASYNC_DELETION_KEY + "]'", ex.getMessage());
        }

        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testInvalidProperty()
    {
        properties.setProperty(MultiDataSetArchiveCleaner.FILE_PATH_PREFIXES_FOR_ASYNC_DELETION_KEY,
                dataFolder2.getAbsolutePath());
        properties.setProperty(MultiDataSetArchiveCleaner.DELETION_REQUESTS_DIR_KEY, deletionRequestDir.getPath());
        properties.setProperty(FileDeleter.DELETION_POLLING_TIME_KEY, "abc");

        try
        {
            createCleaner(0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("'abc' is not a valid duration", ex.getMessage());
        }

        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testSyncDeleteWithDefinedPrefixes()
    {
        properties.setProperty(MultiDataSetArchiveCleaner.FILE_PATH_PREFIXES_FOR_ASYNC_DELETION_KEY,
                dataFolder2.getAbsolutePath() + ", " + dataFolder3.getAbsolutePath());
        properties.setProperty(MultiDataSetArchiveCleaner.DELETION_REQUESTS_DIR_KEY, deletionRequestDir.getPath());
        MultiDataSetArchiveCleaner cleaner = createCleaner(0);
        File file = new File(dataFolder1, "hi.txt");
        FileUtilities.writeToFile(file, "hello world!");
        assertEquals(true, file.exists());

        cleaner.delete(file);

        assertEquals(false, file.exists());
        assertEquals("INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " + file,
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testAsyncDeleteWithDefinedPrefixes()
    {
        properties.setProperty(MultiDataSetArchiveCleaner.FILE_PATH_PREFIXES_FOR_ASYNC_DELETION_KEY,
                dataFolder2.getAbsolutePath() + ", " + dataFolder3.getAbsolutePath());
        properties.setProperty(MultiDataSetArchiveCleaner.DELETION_REQUESTS_DIR_KEY, deletionRequestDir.getPath());
        MultiDataSetArchiveCleaner cleaner = createCleaner(2);
        File file = new File(dataFolder3, "hi.txt");
        FileUtilities.writeToFile(file, "hello world!");
        assertEquals(true, file.exists());

        cleaner.delete(file);
        deleterChannel.assertNextMessage("1 polls");
        testrunnerChannel.send(TimeProviderWithMessageChannelInteraction.CONTINUE_MESSAGE);
        deleterChannel.assertNextMessage("0 polls");
        testrunnerChannel.send(TimeProviderWithMessageChannelInteraction.CONTINUE_MESSAGE);

        assertEquals("INFO  OPERATION.FileDeleter - Schedule for deletion: " + file + "\n"
                + "INFO  OPERATION.FileDeleter - Deletion request file for '" + file
                + "': " + deletionRequestDir + "/19700101-01?000_1.deletionrequest\n"
                + "INFO  OPERATION.FileDeleter - Successfully deleted: " + file.getAbsolutePath(),
                logRecorder.getLogContent().replaceAll("01.000", "01?000"));
        assertEquals(false, file.exists());
        assertEquals("[]", Arrays.asList(deletionRequestDir.list()).toString());
        context.assertIsSatisfied();
    }

    private MultiDataSetArchiveCleaner createCleaner(int numberOfPolls)
    {
        HashMap<File, FileDeleter> deleters = new HashMap<File, FileDeleter>();
        TimeProviderWithMessageChannelInteraction timeProvider =
                new TimeProviderWithMessageChannelInteraction(deleterChannel, testrunnerChannel, numberOfPolls);
        MultiDataSetArchiveCleaner cleaner = new MultiDataSetArchiveCleaner(properties,
                timeProvider, deleters);
        Collection<FileDeleter> values = deleters.values();
        for (FileDeleter fileDeleter : values)
        {
            timeProvider.setDeleter(fileDeleter);
        }

        return cleaner;
    }

}
