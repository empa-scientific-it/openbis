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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.AVAILABLE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.BACKUP_PENDING;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Kaloyan Enimanev
 */
public class ArchivingPostRegistrationTaskTest extends AssertJUnit
{
    private BufferedAppender logRecorder;

    private String DATASET_CODE = "ds1";

    private String ARCHIVE_ERROR = "rsync: timeout in data send/receive (30)";

    private IEncapsulatedOpenBISService service;

    private IArchiverPlugin archiver;

    private IDataStoreServiceInternal dataStoreService;

    private IDataSetDirectoryProvider directoryProvider;

    private BeanFactory applicationContext;

    private Mockery context;

    private IHierarchicalContentProvider contentProvider;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = LogRecordingUtils.createRecorder(null, Level.INFO);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dataStoreService = context.mock(IDataStoreServiceInternal.class);
        archiver = context.mock(IArchiverPlugin.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        applicationContext = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(applicationContext);
        context.checking(new Expectations()
            {
                {
                    allowing(applicationContext).getBean("data-store-service");
                    will(returnValue(dataStoreService));

                    allowing(applicationContext).getBean("hierarchical-content-provider");
                    will(returnValue(contentProvider));

                    allowing(dataStoreService).getArchiverPlugin();
                    will(returnValue(archiver));

                    allowing(dataStoreService).getDataSetDirectoryProvider();
                    will(returnValue(directoryProvider));
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testEmailSendOnArchiveError()
    {
        prepareListDataSets();
        prepareSetDataSetStatusToPending(true);
        RecordingMatcher<List<DatasetDescription>> recordingMatcher =
                prepareArchive(createFailedProcesingStatus());
        prepareSetDataSetStatusBackToAvailable(false, true);
        ArchivingPostRegistrationTask task =
                new ArchivingPostRegistrationTask(new Properties(), service);

        try
        {
            task.createExecutor(DATASET_CODE, false).execute();
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException e)
        {
            assertEquals("Archiving of data set ds1 failed.",e.getMessage());
        }

        assertEquals("Eager archiving of dataset '" + DATASET_CODE + "' has failed.\n"
                + "Error encountered : " + ARCHIVE_ERROR + "\n\n"
                + "If you wish to archive the dataset in the future, "
                + "you can configure an \'AutoArchiverTask\'.", logRecorder.getLogContent());
        assertEquals("[Dataset '" + DATASET_CODE + "']", recordingMatcher.recordedObject()
                .toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCase()
    {
        prepareListDataSets();
        prepareSetDataSetStatusToPending(true);
        RecordingMatcher<List<DatasetDescription>> recordingMatcher =
                prepareArchive(new ProcessingStatus());
        prepareSetDataSetStatusBackToAvailable(true, true);
        ArchivingPostRegistrationTask task =
                new ArchivingPostRegistrationTask(new Properties(), service);

        task.createExecutor(DATASET_CODE, false).execute();

        assertEquals("", logRecorder.getLogContent());
        assertEquals("[Dataset '" + DATASET_CODE + "']", recordingMatcher.recordedObject()
                .toString());
        context.assertIsSatisfied();
    }

    private RecordingMatcher<List<DatasetDescription>> prepareArchive(final ProcessingStatus status)
    {
        final RecordingMatcher<List<DatasetDescription>> matcher =
                new RecordingMatcher<List<DatasetDescription>>();
        context.checking(new Expectations()
            {
                {
                    one(archiver).archive(with(matcher), with(any(ArchiverTaskContext.class)),
                            with(false));
                    will(returnValue(status));
                }
            });
        return matcher;
    }

    private final void prepareSetDataSetStatusToPending(final boolean updated)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).compareAndSetDataSetStatus(DATASET_CODE, AVAILABLE,
                            BACKUP_PENDING, false);
                    will(returnValue(updated));
                }
            });
    }

    private final void prepareSetDataSetStatusBackToAvailable(boolean presentInArchive, boolean updated)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).compareAndSetDataSetStatus(DATASET_CODE, BACKUP_PENDING,
                            AVAILABLE, presentInArchive);
                    will(returnValue(updated));
                }
            });
    }

    private final void prepareListDataSets()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSetsByCode(Arrays.asList(DATASET_CODE));
                    List<AbstractExternalData> externalDatas = Arrays.asList(createDataSet());
                    will(returnValue(externalDatas));
                }
            });
    }

    private ProcessingStatus createFailedProcesingStatus()
    {
        return createProcessingStatus(Status.createError(true, ARCHIVE_ERROR));
    }

    private ProcessingStatus createProcessingStatus(Status status)
    {
        ProcessingStatus processingStatus = new ProcessingStatus();
        processingStatus.addDatasetStatus(DATASET_CODE, status);
        return processingStatus;
    }

    private AbstractExternalData createDataSet()
    {
        DataStore dataStore = new DataStore();
        dataStore.setCode("STANDARD");

        PhysicalDataSet dataSet = new PhysicalDataSet();
        dataSet.setCode(DATASET_CODE);
        dataSet.setFileFormatType(new FileFormatType("DATA"));
        dataSet.setDataStore(dataStore);
        return dataSet;
    }
}
