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

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class MultiThreadProjectOptimisticLockingTest extends MultiThreadOptimisticLockingTestCase
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiThreadProjectOptimisticLockingTest.class);

    private static final String CREATE_EXPERIMENTS_PHASE = "createExperiments";

    /*
     * This test registers three experiments for the same project. Two of them are registered by the main thread using performEntityOperations(). A
     * second thread registers an experiment between the registration of the two other experiments. This is done by using a
     * IServiceConversationProgressListener together with two message channels to coordinate the order of actions in both threads.
     */
    @Test
    public void testRegisterExperimentsInTwoThreads()
    {
        assertEquals("system", toolBox.project1.getModifier().getUserId());
        final StringBuilder stringBuilder = new StringBuilder();
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("main").logger(operationLog).getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("second").logger(operationLog).getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            stringBuilder.append(phaseName).append(" ").append(numItemsProcessed)
                                    .append("/").append(totalItemsToProcess).append("\n");
                            if (phaseName.equals(CREATE_EXPERIMENTS_PHASE)
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
                    NewExperiment experiment3 = toolBox.experiment(3);
                    String sessionToken =
                            genericServer.tryAuthenticate("test", "a").getSessionToken();
                    messageChannelMain.assertNextMessage(ToolBox.FIRST_REGISTERED);
                    genericServer.registerExperiment(sessionToken, experiment3,
                            Collections.<NewAttachment> emptyList());
                    messageChannelSecond.send(ToolBox.REGISTERED);
                }
            }).start();

        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID);
        builder.experiment(toolBox.experiment(1)).experiment(toolBox.experiment(2));
        AtomicEntityOperationDetails details = builder.getDetails();

        etlService.performEntityOperations(systemSessionToken, details);

        messageChannelSecond.assertNextMessage(ToolBox.REGISTERED);

        List<Experiment> experiments =
                commonServer.listExperiments(systemSessionToken,
                        new ExperimentTypeBuilder().code(ToolBox.EXPERIMENT_TYPE_CODE)
                                .getExperimentType(),
                        toolBox
                                .createProjectIdentifier(toolBox.project1.getIdentifier()));
        assertEquals("[OLT-E1, OLT-E2, OLT-E3]", toolBox.extractCodes(experiments).toString());
        toolBox.checkModifierAndModificationDateOfProject1(timeIntervalChecker);
        assertEquals("authorize 1/2\n" + "authorize 2/2\n" + "createExperiments 1/2\n"
                + "createExperiments 2/2\n", stringBuilder.toString());
    }
}
