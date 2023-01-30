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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IMapSpaceByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleSpaceExecutor extends AbstractUpdateEntityToOneRelationExecutor<SampleUpdate, SamplePE, ISpaceId, SpacePE> implements
        IUpdateSampleSpaceExecutor
{

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "sample-space";
    }

    @Override
    protected ISpaceId getRelatedId(SpacePE related)
    {
        return new SpacePermId(related.getCode());
    }

    @Override
    protected SpacePE getCurrentlyRelated(SamplePE entity)
    {
        return entity.getSpace();
    }

    @Override
    protected FieldUpdateValue<ISpaceId> getRelatedUpdate(SampleUpdate update)
    {
        return update.getSpaceId();
    }

    @Override
    protected Map<ISpaceId, SpacePE> map(IOperationContext context, List<ISpaceId> relatedIds)
    {
        return mapSpaceByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, SamplePE entity, ISpaceId relatedId, SpacePE related)
    {
        if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void update(IOperationContext context, SamplePE entity, SpacePE related)
    {
        if (related == null)
        {
            relationshipService.shareSample(context.getSession(), entity);
        } else
        {
            if (entity.getSpace() == null)
            {
                relationshipService.unshareSample(context.getSession(), entity, related);
            } else
            {
                relationshipService.assignSampleToSpace(context.getSession(), entity, related);
            }
        }
    }
}
