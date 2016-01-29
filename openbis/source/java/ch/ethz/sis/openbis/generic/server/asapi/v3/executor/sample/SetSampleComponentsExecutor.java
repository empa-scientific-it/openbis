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

import java.util.Collection;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Progress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToManyRelationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleComponentsExecutor extends AbstractSetEntityToManyRelationExecutor<SampleCreation, SamplePE, ISampleId> implements
        ISetSampleComponentsExecutor
{

    @Override
    protected Collection<? extends ISampleId> getRelatedIds(IOperationContext context, SampleCreation creation)
    {
        return creation.getComponentIds();
    }

    @Override
    protected void setRelated(IOperationContext context, SamplePE container, Collection<SamplePE> components)
    {
        context.pushProgress(new Progress("set components for sample " + container.getCode()));

        for (SamplePE component : components)
        {
            relationshipService.assignSampleToContainer(context.getSession(), component, container);
        }

        context.popProgress();
    }

}
