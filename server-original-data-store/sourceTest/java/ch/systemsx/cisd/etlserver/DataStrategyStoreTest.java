/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * Test cases for corresponding {@link DataStrategyStore} class.
 * 
 * @author Christian Ribeaud
 */
public final class DataStrategyStoreTest extends AbstractFileSystemTestCase
{
    private static final String EXPERIMENT_CODE = "E";

    private static final String PROJECT_CODE = "P";

    private static final String SPACE_CODE = "G";

    private Mockery context;

    private IEncapsulatedOpenBISService limsService;

    private IMailClient mailClient;

    private DataStrategyStore dataStrategyStore;

    private BufferedAppender logRecorder;

    private final static Sample createSampleWithExperiment()
    {
        Sample sample = new Sample();
        sample.setCode("sample code");
        sample.setExperiment(createExperiment());
        return sample;
    }

    private final static Experiment createExperiment()
    {
        final Experiment baseExperiment = new Experiment();
        baseExperiment.setCode(EXPERIMENT_CODE);
        final Project project = new Project();
        project.setCode(PROJECT_CODE);
        final Space space = new Space();
        space.setCode(SPACE_CODE);
        project.setSpace(space);
        baseExperiment.setProject(project);
        return baseExperiment;
    }

    private final File createIncomingDataSetPath()
    {
        return new File(workingDirectory, "Twain");
    }

    @BeforeMethod
    public void startup()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public final void tearDown()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @BeforeClass
    public final void beforeClass()
    {
        context = new Mockery();
        limsService = context.mock(IEncapsulatedOpenBISService.class);
        mailClient = context.mock(IMailClient.class);
        dataStrategyStore = new DataStrategyStore(limsService, mailClient);
    }

    @Test
    public final void testEasiestCases()
    {
        boolean exceptionThrown = false;
        try
        {
            dataStrategyStore.getDataStoreStrategy(new DataSetInformation(), null);
        } catch (final AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null incoming data set path not permited", exceptionThrown);
        final File incomingDataSetPath = createIncomingDataSetPath();
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(null, incomingDataSetPath);
        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.UNIDENTIFIED);
    }

    @Test
    public final void testContainerDataSetStrategies()
    {
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        dataSetInfo.setContainedDataSetCodes(Arrays.asList("container-1"));
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        final Sample baseSample = createSampleWithExperiment();
        dataSetInfo.setSampleCode(sampleIdentifier.getSampleCode());
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(baseSample));

                    one(limsService).tryGetPropertiesOfSample(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));
                }
            });
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, null);
        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.IDENTIFIED);
        context.assertIsSatisfied();

        boolean exceptionThrown = false;
        try
        {
            final File incomingDataSetPath = createIncomingDataSetPath();
            dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);
        } catch (final AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null incoming data set path required for container data sets", exceptionThrown);
    }

    @Test
    public final void testWithFullDataSetInfo()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        final Sample baseSample = createSampleWithExperiment();
        dataSetInfo.setSampleCode(sampleIdentifier.getSampleCode());
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(baseSample));

                    one(limsService).tryGetPropertiesOfSample(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));
                }
            });
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);
        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.IDENTIFIED);
        context.assertIsSatisfied();
    }

    @Test
    public final void testWithoutExperimentIdentifier()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        dataSetInfo.setExperimentIdentifier(null);
        final Sample baseSample = createSampleWithExperiment();
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(baseSample));

                    one(limsService).tryGetPropertiesOfSample(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));
                }
            });
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);
        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.IDENTIFIED);
        context.assertIsSatisfied();
    }

    @Test
    public final void testWithNullBaseExperiment()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(dataSetInfo.getSampleIdentifier());
                    will(returnValue(null));
                }
            });

        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);

        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.UNIDENTIFIED);
        final String logContent = logRecorder.getLogContent();
        assertEquals("Unexpected log content: " + logContent, true,
                logContent.startsWith("ERROR NOTIFY.DataStrategyStore"));

        context.assertIsSatisfied();
    }

    @Test
    public final void testWithNoSampleButIdentifierOfExistingExperiment()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        dataSetInfo.setSampleCode(null);
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetExperiment(dataSetInfo.getExperimentIdentifier());
                    will(returnValue(new Experiment()));
                }
            });
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);
        assertEquals(DataStoreStrategyKey.IDENTIFIED, dataStoreStrategy.getKey());
        final String logContent = logRecorder.getLogContent();
        assertEquals("INFO  OPERATION.DataStrategyStore - "
                + "Identified that database knows experiment '/G/P/E'.", logContent);

        context.assertIsSatisfied();
    }

    @Test
    public final void testWithOnlySampleIdentifier()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        final Sample baseSample = createSampleWithExperiment();
        dataSetInfo.setExperimentIdentifier(null);
        final Person person = new Person();
        final String email = "john.doe@freemail.org";
        person.setEmail(email);
        baseSample.getExperiment().setRegistrator(person);
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(dataSetInfo.getSampleIdentifier());
                    will(returnValue(baseSample));

                    one(limsService).tryGetPropertiesOfSample(
                            dataSetInfo.getSampleIdentifier());
                    will(returnValue(null));

                    String replyTo = null;
                    From nullFrom = null;
                    one(mailClient).sendMessage(
                            with(equal(String.format(DataStrategyStore.SUBJECT_FORMAT,
                                    new ExperimentIdentifier(baseSample.getExperiment())))),
                            with(any(String.class)), with(equal(replyTo)), with(equal(nullFrom)),
                            with(equal(new String[]
                            { email })));
                }
            });

        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);

        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.INVALID);
        final String logContent = logRecorder.getLogContent();
        assertEquals("Unexpected log content: " + logContent, true,
                logContent.startsWith("ERROR OPERATION.DataStrategyStore"));

        context.assertIsSatisfied();
    }

    @Test
    public final void testWithOnlySampleIdentifierAndNoExperiment()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        dataSetInfo.setExperimentIdentifier(null);
        final Person person = new Person();
        final String email = "john.doe@freemail.org";
        person.setEmail(email);
        final Sample baseSample = new SampleBuilder("/S/S").registrator(person).getSample();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(dataSetInfo.getSampleIdentifier());
                    will(returnValue(baseSample));

                    one(limsService).tryGetPropertiesOfSample(
                            dataSetInfo.getSampleIdentifier());
                    will(returnValue(null));

                    String replyTo = null;
                    From nullFrom = null;
                    one(mailClient).sendMessage(
                            with(equal(String.format(DataStrategyStore.SUBJECT_SAMPLE_FORMAT,
                                    new SampleIdentifier(baseSample.getCode())))),
                            with(any(String.class)), with(equal(replyTo)), with(equal(nullFrom)),
                            with(equal(new String[]
                            { email })));
                }
            });

        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);

        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.INVALID);
        final String logContent = logRecorder.getLogContent();
        assertEquals("Unexpected log content: " + logContent, true,
                logContent.startsWith("ERROR OPERATION.DataStrategyStore"));

        context.assertIsSatisfied();
    }

    @Test
    public final void testSampleIsNotRegistered()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        final Sample baseSample = createSampleWithExperiment();
        dataSetInfo.setExperimentIdentifier(null);
        dataSetInfo.setSample(baseSample);
        final Person person = new Person();
        final String email = "john.doe@freemail.org";
        person.setEmail(email);
        baseSample.getExperiment().setRegistrator(person);
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetPropertiesOfSample(
                            dataSetInfo.getSampleIdentifier());
                    will(returnValue(null));

                    String replyTo = null;
                    From nullFrom = null;
                    one(mailClient).sendMessage(
                            with(equal(String.format(DataStrategyStore.SUBJECT_FORMAT,
                                    new ExperimentIdentifier(baseSample.getExperiment())))),
                            with(any(String.class)), with(equal(replyTo)), with(equal(nullFrom)),
                            with(equal(new String[]
                            { email })));
                }
            });

        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);

        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.INVALID);
        final String logContent = logRecorder.getLogContent();
        assertEquals("Unexpected log content: " + logContent, true,
                logContent.startsWith("ERROR OPERATION.DataStrategyStore"));

        context.assertIsSatisfied();
    }
}
