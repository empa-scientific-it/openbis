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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IMapSpaceByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleSpaceExecutor extends AbstractSetEntityToOneRelationExecutor<SampleCreation, SamplePE, ISpaceId, SpacePE> implements
        ISetSampleSpaceExecutor
{

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "sample-space";
    }

    @Override
    protected ISpaceId getRelatedId(SampleCreation creation)
    {
        return creation.getSpaceId();
    }

    @Override
    protected Map<ISpaceId, SpacePE> map(IOperationContext context, List<ISpaceId> relatedIds)
    {
        // See the comment below why we do not check access here.
        return mapSpaceByIdExecutor.map(context, relatedIds, false);
    }

    @Override
    protected void check(IOperationContext context, SamplePE entity, ISpaceId relatedId, SpacePE related)
    {
        // We do not check space access here. With project authorization enabled we might have a situation where we don't have an access to a space
        // itself but we do have an access to some of the projects in that space. In such case, we still want to be able to create a sample in that
        // space as long as this sample is connected with our project or with an experiment in our project. This validation is performed in
        // ISampleAuthorizationExecutor later on.
    }

    @Override
    protected void set(IOperationContext context, SamplePE entity, SpacePE related)
    {
        entity.setSpace(related);
    }
}
