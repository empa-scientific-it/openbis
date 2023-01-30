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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IMapSpaceByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SetProjectSpaceExecutor extends AbstractSetEntityToOneRelationExecutor<ProjectCreation, ProjectPE, ISpaceId, SpacePE> implements
        ISetProjectSpaceExecutor
{

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "project-space";
    }

    @Override
    protected ISpaceId getRelatedId(ProjectCreation creation)
    {
        return creation.getSpaceId();
    }

    @Override
    protected Map<ISpaceId, SpacePE> map(IOperationContext context, List<ISpaceId> relatedIds)
    {
        return mapSpaceByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, ProjectPE entity, ISpaceId relatedId, SpacePE related)
    {
        if (relatedId == null)
        {
            throw new UserFailureException("Space id cannot be null.");
        }

        if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void set(IOperationContext context, ProjectPE entity, SpacePE related)
    {
        entity.setSpace(related);
    }

}
