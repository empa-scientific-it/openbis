/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.VerifyProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.utils.EntityUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class VerifyDataSetSampleAndExperimentExecutor implements IVerifyDataSetSampleAndExperimentExecutor
{

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private DataSetTypeWithoutExperimentChecker dataSetTypeChecker;

    @PostConstruct
    public void init() throws Exception
    {
        dataSetTypeChecker = new DataSetTypeWithoutExperimentChecker(configurer == null ? new Properties() : configurer.getResolvedProps());
    }

    @Override
    public void verify(final IOperationContext context, CollectionBatch<DataPE> batch)
    {
        new CollectionBatchProcessor<DataPE>(context, batch)
            {
                @Override
                public void process(DataPE dataSet)
                {
                    verify(context, dataSet);
                }

                @Override
                public IProgress createProgress(DataPE object, int objectIndex, int totalObjectCount)
                {
                    return new VerifyProgress(object, objectIndex, totalObjectCount);
                }
            };
    }

    private void verify(IOperationContext context, DataPE dataSet)
    {
        ExperimentPE experiment = dataSet.getExperiment();
        SamplePE sample = dataSet.tryGetSample();

        verifyExperimentNotInTrash(experiment);
        verifySampleNotInTrash(sample);

        if (sample == null && experiment == null)
        {
            throw new UserFailureException("Data set cannot be registered because it is neither connected to a sample nor to an experiment.");
        } else if (sample == null && experiment != null)
        {
            return;
        } else if (sample != null && experiment == null)
        {
            verifySampleNotShared(sample);
            verifyExperimentNotNeeded(dataSet);
        } else if (sample != null && experiment != null)
        {
            verifySampleNotShared(sample);
            verifySampleInExperiment(sample, experiment);
        }
    }

    private void verifyExperimentNotInTrash(ExperimentPE experiment)
    {
        if (experiment != null && experiment.getDeletion() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + EntityUtils.render(experiment) + "' is in trash.");
        }
    }

    private void verifyExperimentNotNeeded(DataPE dataSet)
    {
        if (false == dataSetTypeChecker.isDataSetTypeWithoutExperiment(dataSet.getDataSetType().getCode()))
        {
            throw new UserFailureException("Data set can not be registered because it is not connected to an experiment.");
        }
    }

    private void verifySampleNotInTrash(SamplePE sample)
    {
        if (sample != null && sample.getDeletion() != null)
        {
            throw new UserFailureException("Data set can not be registered because sample '"
                    + sample.getSampleIdentifier() + "' is in trash.");
        }
    }

    private void verifySampleNotShared(SamplePE sample)
    {
        if (sample.getSpace() == null)
        {
            throw new UserFailureException("Data set can not be registered because sample '"
                    + sample.getSampleIdentifier() + "' is a shared sample.");
        }
    }

    private void verifySampleInExperiment(SamplePE sample, ExperimentPE experiment)
    {
        if (false == experiment.equals(sample.getExperiment()))
        {
            throw new UserFailureException("Data set can not be registered because it connected to a different experiment than its sample.");
        }
    }

}
