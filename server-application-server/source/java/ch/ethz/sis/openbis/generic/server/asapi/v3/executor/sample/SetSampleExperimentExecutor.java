/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityExperimentRelationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleExperimentExecutor extends AbstractSetEntityExperimentRelationExecutor<SampleCreation, SamplePE> implements
        ISetSampleExperimentExecutor
{

    @Override
    protected String getRelationName()
    {
        return "sample-experiment";
    }

    @Override
    protected IExperimentId getRelatedId(SampleCreation creation)
    {
        return creation.getExperimentId();
    }

    @Override
    protected void set(IOperationContext context, SamplePE entity, ExperimentPE related)
    {
        if (related != null)
        {
            // Set experiment first. Without it the predicate at the relationship service method won't let us in
            // if the user does not have access to the space but only to the experiment's project.
            entity.setExperiment(related);

            relationshipService.assignSampleToExperiment(context.getSession(), entity, related);
        }
    }

}
