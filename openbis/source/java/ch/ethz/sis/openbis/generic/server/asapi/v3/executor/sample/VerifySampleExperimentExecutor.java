/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.VerifyProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.utils.EntityUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class VerifySampleExperimentExecutor implements IVerifySampleExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IVerifySampleDataSetsExecutor verifySampleDataSetsExecutor;

    @SuppressWarnings("unused")
    private VerifySampleExperimentExecutor()
    {
    }

    public VerifySampleExperimentExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public void verify(final IOperationContext context, final CollectionBatch<SamplePE> batch)
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();

        final Map<SamplePE, Boolean> haveDatasetsMap = dataDAO.haveDataSets(batch.getObjects());

        new CollectionBatchProcessor<SamplePE>(context, batch)
            {
                @Override
                public void process(SamplePE sample)
                {
                    boolean hasDatasets = haveDatasetsMap.get(sample);
                    ExperimentPE experiment = sample.getExperiment();

                    if (experiment == null)
                    {
                        verifySampleDataSetsExecutor.checkDataSetsDoNotNeedAnExperiment(context, sample);
                    }

                    if (hasDatasets && sample.getSpace() == null)
                    {
                        throw UserFailureException.fromTemplate("Cannot detach the sample %s from the space "
                                + "because there are already datasets attached to the sample.",
                                EntityUtils.render(sample));
                    }

                    if (experiment != null && sample.getSpace() == null)
                    {
                        throw new UserFailureException("Shared samples cannot be attached to experiments. Sample: "
                                + EntityUtils.render(sample) + ", Experiment: " + EntityUtils.render(experiment));
                    }

                    if (experiment != null && experiment.getProject().getSpace().equals(sample.getSpace()) == false)
                    {
                        sample.setProject(null); // needed to render into the original identifier
                        throw new UserFailureException("Sample space must be the same as experiment space. "
                                + "Sample: " + EntityUtils.render(sample) + ", Experiment: " + EntityUtils.render(experiment));
                    }
                    if (experiment != null && sample.getProject() != null
                            && experiment.getProject().equals(sample.getProject()) == false)
                    {
                        throw new UserFailureException("Sample project must be the same as experiment project. "
                                + "Sample: " + EntityUtils.render(sample) + ", Project: " + EntityUtils.render(sample.getProject())
                                + ", Experiment: " + EntityUtils.render(experiment));
                    }
                }

                @Override
                public IProgress createProgress(SamplePE object, int objectIndex, int totalObjectCount)
                {
                    return new VerifyProgress(object, objectIndex, totalObjectCount);
                }
            };
    }

}
