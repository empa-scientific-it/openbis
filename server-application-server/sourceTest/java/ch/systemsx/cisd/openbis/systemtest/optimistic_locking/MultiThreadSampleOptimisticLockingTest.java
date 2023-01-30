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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.VerifyProgress;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class MultiThreadSampleOptimisticLockingTest extends MultiThreadOptimisticLockingTestCase
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiThreadSampleOptimisticLockingTest.class);

    @Test
    public void testRegisterChildSamplesForSameSampleInTwoThreads()
    {
        final Sample sample = toolBox.createAndLoadSample(1, null);
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("child samples for samples main")
                        .logger(operationLog).getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("child samples for samples second")
                        .logger(operationLog).getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            if (phaseName.equals("createContainerSamples")
                                    && numItemsProcessed == 1 && totalItemsToProcess == 2)
                            {
                                messageChannelMain.send(ToolBox.FIRST_REGISTERED);
                            }
                        }
                    };
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    messageChannelMain.assertNextMessage(ToolBox.FIRST_REGISTERED);
                    AtomicEntityOperationDetails details =
                            new AtomicEntityOperationDetailsBuilder().user("test")
                                    .sample(toolBox.sampleWithParent(13, sample)).getDetails();
                    etlService.performEntityOperations(systemSessionToken, details);
                    messageChannelSecond.send(ToolBox.REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID).batchSize(1);
        builder.sample(toolBox.sampleWithParent(11, sample));
        builder.sample(toolBox.sampleWithParent(12, sample));

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(ToolBox.REGISTERED);

        Sample loadedSample = toolBox.loadSample(sample);
        List<Sample> samples =
                etlService.listSamples(systemSessionToken,
                        ListSampleCriteria.createForParent(new TechId(loadedSample)));
        assertEquals("[OLT-S11, OLT-S12, OLT-S13]", toolBox.extractCodes(samples).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");

    }

    @Test
    public void testRegisterContainedSamplesForSameSampleInTwoThreads()
    {
        final Sample sample = toolBox.createAndLoadSample(1, null);
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("contained samples for samples main")
                        .logger(operationLog).getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("contained samples for samples second")
                        .logger(operationLog).getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            if (phaseName.equals("createContainerSamples")
                                    && numItemsProcessed == 1 && totalItemsToProcess == 2)
                            {
                                messageChannelMain.send(ToolBox.FIRST_REGISTERED);
                            }
                        }
                    };
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    messageChannelMain.assertNextMessage(ToolBox.FIRST_REGISTERED);
                    AtomicEntityOperationDetails details =
                            new AtomicEntityOperationDetailsBuilder().user("test")
                                    .sample(toolBox.sampleComponent(13, sample)).getDetails();
                    etlService.performEntityOperations(systemSessionToken, details);
                    messageChannelSecond.send(ToolBox.REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID).batchSize(1);
        builder.sample(toolBox.sampleComponent(11, sample));
        builder.sample(toolBox.sampleComponent(12, sample));

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(ToolBox.REGISTERED);

        Sample loadedSample = toolBox.loadSample(sample);
        List<Sample> samples =
                etlService.listSamples(systemSessionToken,
                        ListSampleCriteria.createForContainer(new TechId(loadedSample)));
        assertEquals("[OLT-S1:OLT-S11, OLT-S1:OLT-S12, OLT-S1:OLT-S13]",
                toolBox.extractCodes(samples).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");

    }

    @Test
    public void testRegisterDataSetsForSameSampleInTwoThreads()
    {
        final Experiment experiment = toolBox.createAndLoadExperiment(1);
        final Sample sample = toolBox.createAndLoadSample(1, experiment);
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("data sets for samples main")
                        .logger(operationLog).getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("data sets for samples second")
                        .logger(operationLog).getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            if (phaseName.equals(VerifyProgress.VERIFYING) && numItemsProcessed == 2
                                    && totalItemsToProcess == 2)
                            {
                                messageChannelMain.send(ToolBox.FIRST_REGISTERED);
                            }
                        }
                    };
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    messageChannelMain.assertNextMessage(ToolBox.FIRST_REGISTERED);
                    AtomicEntityOperationDetails details =
                            new AtomicEntityOperationDetailsBuilder().user("test")
                                    .dataSet(toolBox.dataSet("DS3", sample)).getDetails();
                    etlService.performEntityOperations(systemSessionToken, details);
                    messageChannelSecond.send(ToolBox.REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID).batchSize(1);
        builder.dataSet(toolBox.dataSet("DS1", sample)).dataSet(toolBox.dataSet("DS2", sample));

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(ToolBox.REGISTERED);

        Sample loadedSample = toolBox.loadSample(sample);
        List<AbstractExternalData> dataSets =
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample), true);
        assertEquals("[DS1, DS2, DS3]", toolBox.extractCodes(dataSets).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
    }

    @Test
    public void testTrashAndRevertSample()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        Sample containerSample = toolBox.createAndLoadSample(1, null);
        Sample parentSample = toolBox.createAndLoadSample(2, null);
        NewSample newContainedSample = toolBox.sample(3, experiment);
        newContainedSample.setContainerIdentifier(containerSample.getIdentifier());
        newContainedSample.setParents(parentSample.getCode());
        Sample containedSample = toolBox.createAndLoadSample(newContainedSample);
        assertEquals(containerSample.getIdentifier(), containedSample.getContainer()
                .getIdentifier());
        assertChildren("[OLT-S1:OLT-S3]", parentSample);
        Sample childSample =
                toolBox.createAndLoadSample(toolBox.sampleWithParent(4, containedSample));
        assertChildren("[OLT-S4]", containedSample);
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        String reason = "test sample deletion";

        commonServer.deleteSamples(sessionToken, Arrays.asList(new TechId(containedSample)),
                reason, DeletionType.TRASH);

        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        Sample loadedContainerSample = toolBox.loadSample(containerSample);
        Sample loadedParentSample = toolBox.loadSample(parentSample);
        Sample loadedChildSample = toolBox.loadSample(childSample);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainerSample,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedParentSample,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedChildSample,
                "test");

        // Revert deletion
        timeIntervalChecker = new TimeIntervalChecker();
        Deletion deletion = toolBox.findDeletion(reason);

        commonServer.revertDeletions(systemSessionToken,
                Arrays.asList(new TechId(deletion.getId())));

        loadedExperiment = toolBox.loadExperiment(experiment);
        loadedContainerSample = toolBox.loadSample(containerSample);
        loadedParentSample = toolBox.loadSample(parentSample);
        loadedChildSample = toolBox.loadSample(childSample);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                "system");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainerSample,
                "system");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedParentSample,
                "system");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedChildSample,
                "system");
    }

    private void assertChildren(String expectedChildren, Sample sample)
    {
        ListSampleCriteria criteria = ListSampleCriteria.createForParent(new TechId(sample));
        assertEquals(expectedChildren,
                toolBox.extractCodes(etlService.listSamples(systemSessionToken, criteria))
                        .toString());
    }
}
