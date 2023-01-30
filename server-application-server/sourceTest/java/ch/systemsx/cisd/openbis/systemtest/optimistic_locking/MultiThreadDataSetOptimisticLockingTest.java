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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class MultiThreadDataSetOptimisticLockingTest extends MultiThreadOptimisticLockingTestCase
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiThreadDataSetOptimisticLockingTest.class);

    @Test
    public void testRegisterChildDataSetsForAParentDataSetInTwoThreads()
    {
        final Experiment experiment = toolBox.createAndLoadExperiment(1);
        final AbstractExternalData parentDataSet =
                toolBox.createAndLoadDataSet(toolBox.dataSet("DS-PARENT", experiment));
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("child data sets main").logger(operationLog)
                        .getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("child data sets second")
                        .logger(operationLog).getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            logger.info(phaseName + " " + numItemsProcessed + "/"
                                    + totalItemsToProcess);
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
                    String sessionToken =
                            genericServer.tryAuthenticate("test", "a").getSessionToken();
                    NewDataSet child3 = toolBox.dataSet("DS-CHILD3", experiment);
                    child3.setParentDataSetCodes(Arrays.asList(parentDataSet.getCode()));
                    AtomicEntityOperationDetailsBuilder builder =
                            new AtomicEntityOperationDetailsBuilder().dataSet(child3);
                    messageChannelMain.assertNextMessage(ToolBox.FIRST_REGISTERED);
                    etlService.performEntityOperations(sessionToken, builder.getDetails());
                    messageChannelSecond.send(ToolBox.REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID).batchSize(1);
        NewDataSet child1 = toolBox.dataSet("DS-CHILD1", experiment);
        child1.setParentDataSetCodes(Arrays.asList(parentDataSet.getCode()));
        NewDataSet child2 = toolBox.dataSet("DS-CHILD2", experiment);
        child2.setParentDataSetCodes(Arrays.asList(parentDataSet.getCode()));
        builder.dataSet(child1).dataSet(child2);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(ToolBox.REGISTERED);

        AbstractExternalData loadedParentDataSet = toolBox.loadDataSet(parentDataSet.getCode());
        assertEquals(experiment.getIdentifier(), loadedParentDataSet.getExperiment()
                .getIdentifier());
        assertEquals("[DS-CHILD1, DS-CHILD2, DS-CHILD3]",
                toolBox.extractCodes(loadedParentDataSet.getChildren()).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedParentDataSet,
                "test");
    }

    @Test
    public void testTrashAndRevertDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        AbstractExternalData parentDataSet =
                toolBox.createAndLoadDataSet(toolBox.dataSet("DS-PARENT", experiment));
        Sample sample = toolBox.createAndLoadSample(1, experiment);
        NewDataSet newDataSet = toolBox.dataSet("DS-1", experiment);
        newDataSet.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample));
        newDataSet.setParentDataSetCodes(Arrays.asList(parentDataSet.getCode()));
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(newDataSet);
        NewContainerDataSet newContainerDataSet = toolBox.containerDataSet("DS-CONT", experiment);
        newContainerDataSet.setContainedDataSetCodes(Arrays.asList(dataSet.getCode()));
        AbstractExternalData containerDataSet = toolBox.createAndLoadDataSet(newContainerDataSet);
        NewDataSet newChilddataSet = toolBox.dataSet("DS-CHILD", experiment);
        newChilddataSet.setParentDataSetCodes(Arrays.asList(newDataSet.getCode()));
        AbstractExternalData childDataSet = toolBox.createAndLoadDataSet(newChilddataSet);
        assertEquals(dataSet.getCode(), childDataSet.getParents().iterator().next().getCode());
        assertEquals(parentDataSet.getCode(), dataSet.getParents().iterator().next().getCode());
        assertEquals(experiment.getIdentifier(), dataSet.getExperiment().getIdentifier());
        assertEquals(sample.getIdentifier(), dataSet.getSample().getIdentifier());
        assertEquals(containerDataSet.getCode(), toolBox.loadDataSet(dataSet.getCode())
                .tryGetContainer().getCode());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        String reason = "test data set deletion";

        commonServer.deleteDataSets(sessionToken, Arrays.asList(dataSet.getCode()), reason,
                DeletionType.TRASH, true);

        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        Sample loadedSample = toolBox.loadSample(sample);
        AbstractExternalData loadedContainerDataSet = toolBox.loadDataSet(containerDataSet.getCode());
        AbstractExternalData loadedParentDataSet = toolBox.loadDataSet(parentDataSet.getCode());
        AbstractExternalData loadedChildDataSet = toolBox.loadDataSet(childDataSet.getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainerDataSet,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedParentDataSet,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedChildDataSet,
                "test");

        // Revert deletion
        sessionToken = commonServer.tryAuthenticate(USER_ID, "a").getSessionToken();
        timeIntervalChecker = new TimeIntervalChecker();
        Deletion deletion = toolBox.findDeletion(reason);

        commonServer.revertDeletions(sessionToken, Arrays.asList(new TechId(deletion.getId())));

        loadedExperiment = toolBox.loadExperiment(experiment);
        loadedSample = toolBox.loadSample(sample);
        loadedContainerDataSet = toolBox.loadDataSet(containerDataSet.getCode());
        AbstractExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        loadedParentDataSet = toolBox.loadDataSet(parentDataSet.getCode());
        loadedChildDataSet = toolBox.loadDataSet(childDataSet.getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                USER_ID);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, USER_ID);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainerDataSet,
                USER_ID);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedParentDataSet,
                USER_ID);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedChildDataSet,
                USER_ID);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, USER_ID);
    }
}
