/*
 * Copyright 2010 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.DESTINATION_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataSetCopierForUsers.class)
public class DataSetCopierForUsersTest extends AbstractFileSystemTestCase
{
    private static final String USER_ID = "test-user";

    private static final String USER_EMAIL = "a@bc.de";

    private static final String DS_LOCATION = "ds";

    private Mockery context;

    private IPathCopierFactory pathFactory;

    private ISshCommandExecutorFactory sshFactory;

    private IPathCopier copier;

    private ISshCommandExecutor sshCommandExecutor;

    private IImmutableCopierFactory hardLinkMakerFactory;

    private IMailClient mailClient;

    private ITimeProvider timeProvider;

    private File storeRoot;

    private File sshExecutableDummy;

    private File rsyncExecutableDummy;

    private Properties properties;

    private DatasetDescription ds;

    private File dsData;

    private DataSetProcessingContext dataSetProcessingContext;

    @BeforeMethod
    public void beforeMethod() throws IOException
    {
        context = new Mockery();
        timeProvider = context.mock(ITimeProvider.class);
        mailClient = context.mock(IMailClient.class);
        pathFactory = context.mock(IPathCopierFactory.class);
        sshFactory = context.mock(ISshCommandExecutorFactory.class);
        hardLinkMakerFactory = context.mock(IImmutableCopierFactory.class);
        copier = context.mock(IPathCopier.class);
        sshCommandExecutor = context.mock(ISshCommandExecutor.class);
        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
        sshExecutableDummy = new File(workingDirectory, "my-ssh");
        sshExecutableDummy.createNewFile();
        rsyncExecutableDummy = new File(workingDirectory, "my-rsync");
        rsyncExecutableDummy.createNewFile();
        properties = new Properties();
        properties.setProperty("ssh-executable", sshExecutableDummy.getPath());
        properties.setProperty("rsync-executable", rsyncExecutableDummy.getPath());
        DatasetDescriptionBuilder dsb =
                new DatasetDescriptionBuilder("ds1").type("MY-DATA").location(DS_LOCATION)
                        .sample("s").space("g").project("p").experiment("e");
        ds = dsb.getDatasetDescription();
        ds.setExperimentIdentifier("/g/p/e");
        ds.setExperimentTypeCode("MY_EXPERIMENT");
        File ds1Folder = new File(new File(storeRoot, DEFAULT_SHARE_ID), DS_LOCATION + "/original");
        ds1Folder.mkdirs();
        dsData = new File(ds1Folder, "data.txt");
        dsData.createNewFile();
        Map<String, String> parameterBindings = new HashMap<String, String>();
        parameterBindings.put(Constants.USER_PARAMETER, USER_ID);
        MockDataSetDirectoryProvider directoryProvider =
                new MockDataSetDirectoryProvider(storeRoot, DEFAULT_SHARE_ID);
        dataSetProcessingContext =
                new DataSetProcessingContext(null, directoryProvider, parameterBindings,
                        mailClient, USER_ID, USER_EMAIL);
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(42L));
                }
            });
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCopyWithDestinationTemplate()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/${" + Constants.USER_PARAMETER + "}");
        properties.setProperty(AbstractDropboxProcessingPlugin.SEND_DETAILED_EMAIL_KEY, "true");
        prepareCreateAndCheckCopier();
        final RecordingMatcher<String> subjectRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<EMailAddress[]> recipientsRecorder =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    File canonicalFile = getCanonicalFile("tmp/" + USER_ID);
                    one(copier).copyToRemote(dsData, canonicalFile.getPath(), null, null, null, null, null);
                    will(returnValue(Status.OK));

                    one(mailClient).sendEmailMessage(with(subjectRecorder), with(contentRecorder),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsRecorder));
                }
            });
        DataSetCopier dataSetCopier =
                new DataSetCopierForUsers(properties, storeRoot, pathFactory, sshFactory,
                        hardLinkMakerFactory, timeProvider);

        ProcessingStatus processingStatus =
                dataSetCopier.process(Arrays.asList(ds), dataSetProcessingContext);
        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds);
        assertEquals(USER_EMAIL, recipientsRecorder.recordedObject()[0].tryGetEmailAddress());
        assertEquals("Data set ds1 [MY-DATA] successfully processed",
                subjectRecorder.recordedObject());
        assertEquals("Successfully processed data set ds1 [MY-DATA].\n\n" + "Processing details:\n"
                + "Description: Copy to tmp/test-user\n" + "Experiment: /g/p/e [MY_EXPERIMENT]\n"
                + "Started: 1970-01-01 01:00:00 +0100.\n" + "Finished: 1970-01-01 01:00:00 +0100.",
                contentRecorder.recordedObject());

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyWithConstantDestination()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        prepareCreateAndCheckCopier();
        context.checking(new Expectations()
            {
                {
                    File canonicalFile = getCanonicalFile("tmp/test");
                    one(copier).copyToRemote(dsData, canonicalFile.getPath(), null, null, null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier =
                new DataSetCopierForUsers(properties, storeRoot, pathFactory, sshFactory,
                        hardLinkMakerFactory, timeProvider);

        ProcessingStatus processingStatus =
                dataSetCopier.process(Arrays.asList(ds), dataSetProcessingContext);
        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds);

        context.assertIsSatisfied();
    }

    private void prepareCreateAndCheckCopier()
    {
        context.checking(new Expectations()
            {
                {
                    one(pathFactory).create(rsyncExecutableDummy, sshExecutableDummy,
                            DataSetCopier.SSH_TIMEOUT_MILLIS, RSyncConfig.getInstance().getAdditionalCommandLineOptions());
                    will(returnValue(copier));

                    one(sshFactory).create(sshExecutableDummy, null);
                    will(returnValue(sshCommandExecutor));

                    one(copier).check();
                }
            });
    }

    // asserts for checking status

    private void assertSuccessful(ProcessingStatus processingStatus, DatasetDescription... datasets)
    {
        checkStatus(processingStatus, Status.OK, datasets);
    }

    private void assertNoErrors(ProcessingStatus processingStatus)
    {
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        assertEquals("Unexcpected errors: " + errorStatuses, 0, errorStatuses.size());
    }

    private void checkStatus(ProcessingStatus processingStatus, Status status,
            DatasetDescription... expectedDatasets)
    {
        final List<String> actualDatasets = processingStatus.getDatasetsByStatus(status);
        assertEquals(expectedDatasets.length, actualDatasets.size());
        assertTrue(actualDatasets.containsAll(asCodes(Arrays.asList(expectedDatasets))));
    }

    private static List<String> asCodes(List<DatasetDescription> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (DatasetDescription dataset : datasets)
        {
            codes.add(dataset.getDataSetCode());
        }
        return codes;
    }

    private File getCanonicalFile(String fileName)
    {
        try
        {
            return new File(fileName).getCanonicalFile();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
}
