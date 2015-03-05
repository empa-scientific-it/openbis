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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.Collection;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityListUpdateValueRelationExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleChildrenExecutor extends AbstractUpdateEntityListUpdateValueRelationExecutor<SampleUpdate, SamplePE, ISampleId, SamplePE>
        implements IUpdateSampleChildrenExecutor
{

    @Override
    protected Collection<SamplePE> getCurrentlyRelated(SamplePE entity)
    {
        return entity.getChildren();
    }

    @Override
    protected IdListUpdateValue<? extends ISampleId> getRelatedUpdate(IOperationContext context, SampleUpdate update)
    {
        return update.getChildIds();
    }

    @Override
    protected void check(IOperationContext context, ISampleId relatedId, SamplePE related)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void add(IOperationContext context, SamplePE entity, SamplePE related)
    {
        relationshipService.addParentToSample(context.getSession(), related, entity);
    }

    @Override
    protected void remove(IOperationContext context, SamplePE entity, SamplePE related)
    {
        relationshipService.removeParentFromSample(context.getSession(), related, entity);
    }

}
