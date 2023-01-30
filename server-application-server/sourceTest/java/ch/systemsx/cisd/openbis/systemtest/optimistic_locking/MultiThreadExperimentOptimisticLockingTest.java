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

import static ch.systemsx.cisd.openbis.systemtest.optimistic_locking.ToolBox.USER_ID;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class MultiThreadExperimentOptimisticLockingTest extends
        MultiThreadOptimisticLockingTestCase
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiThreadExperimentOptimisticLockingTest.class);

    @Test
    public void testRegisterSamplesForSameExperimentInTwoThreads()
    {
        final NewExperiment experiment = toolBox.experiment(1);
        genericServer.registerExperiment(systemSessionToken, experiment, ToolBox.NO_ATTACHMENTS);
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("samples main").logger(operationLog)
                        .getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("samples second").logger(operationLog)
                        .getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            logger.info(phaseName + " " + numItemsProcessed + "/"
                                    + totalItemsToProcess);
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
                    NewSample sample3 = toolBox.sample(3, experiment);

                    String sessionToken =
                            genericServer.tryAuthenticate("test", "a").getSessionToken();
                    messageChannelMain.assertNextMessage(ToolBox.FIRST_REGISTERED);
                    genericServer.registerSample(sessionToken, sample3, ToolBox.NO_ATTACHMENTS);
                    messageChannelSecond.send(ToolBox.REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID).batchSize(1);
        NewSample sample1 = toolBox.sample(1, experiment);
        NewSample sample2 = toolBox.sample(2, experiment);
        builder.sample(sample1).sample(sample2);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(ToolBox.REGISTERED);

        Experiment experimentInfo = toolBox.loadExperiment(experiment);
        List<Sample> samples =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(experimentInfo)));
        assertEquals("[OLT-S1, OLT-S2, OLT-S3]", toolBox.extractCodes(samples).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, experimentInfo, "test");
    }

    @Test
    public void testRegisterDataSetsForSameExperimentInTwoThreads()
    {
        final Experiment experiment = toolBox.createAndLoadExperiment(1);
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("data sets main").logger(operationLog)
                        .getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("data sets second").logger(operationLog)
                        .getChannel();
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
                                    .dataSet(toolBox.dataSet("DS3", experiment)).getDetails();
                    etlService.performEntityOperations(systemSessionToken, details);
                    messageChannelSecond.send(ToolBox.REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID).batchSize(1);
        builder.dataSet(toolBox.dataSet("DS1", experiment)).dataSet(
                toolBox.dataSet("DS2", experiment));

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(ToolBox.REGISTERED);

        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        List<AbstractExternalData> dataSets =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment));
        assertEquals("[DS1, DS2, DS3]", toolBox.extractCodes(dataSets).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                "test");
    }

    @Test
    public void testTrashAndRevertExperiment()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        String reason = "test deletion";

        commonServer.deleteExperiments(sessionToken, Arrays.asList(new TechId(experiment)), reason,
                DeletionType.TRASH);

        Project loadedProject =
                commonServer.getProjectInfo(systemSessionToken,
                        ExperimentIdentifierFactory.parse(experiment.getIdentifier()));
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedProject, "test");

        // Revert deletion
        sessionToken = commonServer.tryAuthenticate(USER_ID, "a").getSessionToken();
        timeIntervalChecker = new TimeIntervalChecker();
        Deletion deletion = toolBox.findDeletion(reason);

        commonServer.revertDeletions(sessionToken, Arrays.asList(new TechId(deletion.getId())));

        loadedProject =
                commonServer.getProjectInfo(systemSessionToken,
                        ExperimentIdentifierFactory.parse(experiment.getIdentifier()));
        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedProject, USER_ID);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                USER_ID);
    }
}
