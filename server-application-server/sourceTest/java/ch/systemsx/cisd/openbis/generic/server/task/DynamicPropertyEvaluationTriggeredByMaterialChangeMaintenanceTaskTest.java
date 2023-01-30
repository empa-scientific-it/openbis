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

package ch.systemsx.cisd.openbis.generic.server.task;

import static ch.systemsx.cisd.openbis.generic.server.task.DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask.INITIAL_TIMESTAMP_KEY;
import static ch.systemsx.cisd.openbis.generic.server.task.DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask.TIMESTAMP_FILE_KEY;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationSchedulerWithQueue;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTaskTest extends AbstractFileSystemTestCase
{
    private static final String SESSION_TOKEN = "my-session";

    private BufferedAppender logRecorder;

    private Mockery context;

    private ICommonServerForInternalUse server;

    private IDynamicPropertyEvaluationSchedulerWithQueue scheduler;

    private ITimeProvider timeProvider;

    private DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask task;

    private Properties properties;

    private File timestampFile;

    @BeforeMethod
    public void setUpMocksAndProperties()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %m%n", Level.DEBUG);
        context = new Mockery();
        server = context.mock(ICommonServerForInternalUse.class);
        scheduler = context.mock(IDynamicPropertyEvaluationSchedulerWithQueue.class);
        timeProvider = new MockTimeProvider(7000, 1000);
        task = new DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask(server, scheduler, timeProvider);
        properties = new Properties();
        timestampFile = new File(workingDirectory, "timestamp.txt");
        properties.setProperty(TIMESTAMP_FILE_KEY, timestampFile.getPath());
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        logRecorder.reset();
        context.assertIsSatisfied();
    }

    @Test
    public void testMissingInitialTimestamp()
    {
        try
        {
            task.setUp("test", properties);
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key 'initial-timestamp' not found in properties '[timestamp-file]'", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticationAsSystemFailed()
    {
        properties.setProperty(DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask.INITIAL_TIMESTAMP_KEY, "2011-01-01");
        task.setUp("test", properties);
        prepareLogInAndOut(null);

        task.execute();

        assertEquals("WARN  Couldn't authenticate as system.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForFoundSamplesSinceInitialTimestamp()
    {
        properties.setProperty(INITIAL_TIMESTAMP_KEY, "2011-01-01");
        task.setUp("test", properties);
        prepareLogInAndOut(SESSION_TOKEN);
        Material m1 = new MaterialBuilder().id(101L).code("M101").type("T").getMaterial();
        RecordingMatcher<DetailedSearchCriteria> criteriaRecorder = prepareSearchForMaterials(m1);
        prepareListMaterialIds(Arrays.asList(new TechId(101L)), 102L, 103L);
        prepareListMaterialIds(Arrays.asList(new TechId(102L), new TechId(103L)));
        Sample s1 = new SampleBuilder("/S/1").id(1).getSample();
        Sample s2 = new SampleBuilder("/S/2").id(2).getSample();
        RecordingMatcher<Collection<TechId>> materialIdsRecoder = prepareListSamples(s1, s2);
        RecordingMatcher<DynamicPropertyEvaluationOperation> scheduledOperationsRecorder =
                prepareScheduleDynamicPropertyEvaluation();

        task.execute();

        assertEquals("ATTRIBUTE MODIFICATION_DATE: 2011-01-01 (without wildcards)",
                criteriaRecorder.recordedObject().toString());
        List<Long> materialIds = TechId.asLongs(materialIdsRecoder.recordedObject());
        Collections.sort(materialIds);
        assertEquals("[101, 102, 103]", materialIds.toString());
        assertEquals("ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE: [1, 2]",
                scheduledOperationsRecorder.recordedObject().toString());
        assertEquals("INFO  1 materials changed since [2011-01-01].\n" +
                "INFO  3 materials in total changed.\n" +
                "INFO  2 samples found for changed materials.\n" +
                "INFO  Timestamp [1970-01-01 01:00:07 +0100] saved in '" + timestampFile + "'.",
                logRecorder.getLogContent());
        assertEquals("1970-01-01 01:00:07 +0100", FileUtilities.loadToString(timestampFile).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForNoSamplesSinceTimestampFromFile()
    {
        FileUtilities.writeToFile(timestampFile, "2013-09-05 09:19:54 +0200");
        properties.setProperty(INITIAL_TIMESTAMP_KEY, "2011-01-01");
        task.setUp("test", properties);
        prepareLogInAndOut(SESSION_TOKEN);
        Material m1 = new MaterialBuilder().id(101L).code("M101").type("T").getMaterial();
        RecordingMatcher<DetailedSearchCriteria> criteriaRecorder = prepareSearchForMaterials(m1);
        prepareListMaterialIds(Arrays.asList(new TechId(101L)));
        RecordingMatcher<Collection<TechId>> materialIdsRecoder = prepareListSamples();

        task.execute();

        assertEquals("ATTRIBUTE MODIFICATION_DATE: 2013-09-05 09:19:54 +0200 (without wildcards)",
                criteriaRecorder.recordedObject().toString());
        List<Long> materialIds = TechId.asLongs(materialIdsRecoder.recordedObject());
        Collections.sort(materialIds);
        assertEquals("[101]", materialIds.toString());
        assertEquals("INFO  1 materials changed since [2013-09-05 09:19:54 +0200].\n" +
                "INFO  0 samples found for changed materials.\n" +
                "INFO  Timestamp [1970-01-01 01:00:07 +0100] saved in '" + timestampFile + "'.",
                logRecorder.getLogContent());
        assertEquals("1970-01-01 01:00:07 +0100", FileUtilities.loadToString(timestampFile).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForNoMaterialChangedSinceTimestampFromFile()
    {
        FileUtilities.writeToFile(timestampFile, "2013-09-05 09:19:54 +0200");
        properties.setProperty(INITIAL_TIMESTAMP_KEY, "2011-01-01");
        task.setUp("test", properties);
        prepareLogInAndOut(SESSION_TOKEN);
        RecordingMatcher<DetailedSearchCriteria> criteriaRecorder = prepareSearchForMaterials();

        task.execute();

        assertEquals("ATTRIBUTE MODIFICATION_DATE: 2013-09-05 09:19:54 +0200 (without wildcards)",
                criteriaRecorder.recordedObject().toString());
        assertEquals("INFO  0 materials changed since [2013-09-05 09:19:54 +0200].\n" +
                "INFO  Timestamp [1970-01-01 01:00:07 +0100] saved in '" + timestampFile + "'.",
                logRecorder.getLogContent());
        assertEquals("1970-01-01 01:00:07 +0100", FileUtilities.loadToString(timestampFile).trim());
        context.assertIsSatisfied();
    }

    private void prepareLogInAndOut(final String sessionTokenOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(server).tryToAuthenticateAsSystem();
                    SessionContextDTO sessionContext = new SessionContextDTO();
                    sessionContext.setSessionToken(sessionTokenOrNull);
                    will(returnValue(sessionTokenOrNull == null ? null : sessionContext));

                    if (sessionTokenOrNull != null)
                    {
                        one(server).logout(sessionTokenOrNull);
                    }
                }
            });
    }

    private RecordingMatcher<DetailedSearchCriteria> prepareSearchForMaterials(final Material... materials)
    {
        final RecordingMatcher<DetailedSearchCriteria> criteriaRecorder = new RecordingMatcher<DetailedSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(server).searchForMaterials(with(SESSION_TOKEN), with(criteriaRecorder));
                    will(returnValue(Arrays.asList(materials)));
                }
            });
        return criteriaRecorder;
    }

    private void prepareListMaterialIds(final Collection<TechId> propertiesMaterialIds, final Long... materialIds)
    {
        context.checking(new Expectations()
            {
                {
                    one(server).listMaterialIdsByMaterialProperties(SESSION_TOKEN, propertiesMaterialIds);
                    will(returnValue(TechId.createList(Arrays.asList(materialIds))));
                }
            });
    }

    private RecordingMatcher<Collection<TechId>> prepareListSamples(final Sample... samples)
    {
        final RecordingMatcher<Collection<TechId>> matcher = new RecordingMatcher<Collection<TechId>>();
        context.checking(new Expectations()
            {
                {
                    one(server).listSamplesByMaterialProperties(with(SESSION_TOKEN), with(matcher));
                    will(returnValue(Arrays.asList(samples)));
                }
            });
        return matcher;
    }

    private RecordingMatcher<DynamicPropertyEvaluationOperation> prepareScheduleDynamicPropertyEvaluation()
    {
        final RecordingMatcher<DynamicPropertyEvaluationOperation> matcher = new RecordingMatcher<DynamicPropertyEvaluationOperation>();
        context.checking(new Expectations()
            {
                {
                    one(scheduler).scheduleUpdate(with(matcher));
                    one(scheduler).synchronizeThreadQueue();
                }
            });
        return matcher;
    }
}
