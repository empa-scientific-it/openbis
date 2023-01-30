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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IReindexEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateProjectSpaceExecutor extends AbstractUpdateEntityToOneRelationExecutor<ProjectUpdate, ProjectPE, ISpaceId, SpacePE>
        implements IUpdateProjectSpaceExecutor
{

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Autowired
    private IReindexEntityExecutor reindexObjectExecutor;

    @Override
    protected String getRelationName()
    {
        return "project-space";
    }

    @Override
    public void update(IOperationContext context, MapBatch<ProjectUpdate, ProjectPE> batch)
    {
        super.update(context, batch);

        reindex(context, batch.getObjects().values());
    }

    @Override
    public void update(IOperationContext context, MapBatch<ProjectUpdate, ProjectPE> batch, Map<ISpaceId, SpacePE> relatedMap)
    {
        super.update(context, batch, relatedMap);

        reindex(context, batch.getObjects().values());
    }

    @Override
    protected ISpaceId getRelatedId(SpacePE related)
    {
        return new SpacePermId(related.getCode());
    }

    @Override
    protected SpacePE getCurrentlyRelated(ProjectPE entity)
    {
        return entity.getSpace();
    }

    @Override
    protected FieldUpdateValue<ISpaceId> getRelatedUpdate(ProjectUpdate update)
    {
        return update.getSpaceId();
    }

    @Override
    protected Map<ISpaceId, SpacePE> map(IOperationContext context, List<ISpaceId> relatedIds)
    {
        return mapSpaceByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, ProjectPE entity, ISpaceId relatedId, SpacePE related)
    {
        if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void update(IOperationContext context, ProjectPE entity, SpacePE related)
    {
        if (related == null)
        {
            throw new UserFailureException("Space id cannot be null");
        } else
        {
            relationshipService.assignProjectToSpace(context.getSession(), entity, related);
        }
    }

    private void reindex(IOperationContext context, Collection<ProjectPE> projects)
    {
        Set<ExperimentPE> experiments = new HashSet<ExperimentPE>();
        for (ProjectPE project : projects)
        {
            experiments.addAll(project.getExperiments());
        }
        reindexObjectExecutor.reindex(context, ExperimentPE.class, experiments);
    }

}
