/*
 * Copyright 2015 ETH Zuerich, SIS
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.IMapProjectByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateSampleProjectExecutor extends AbstractUpdateEntityToOneRelationExecutor<SampleUpdate, SamplePE, IProjectId, ProjectPE>
        implements IUpdateSampleProjectExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "sample-project";
    }

    @Override
    protected IProjectId getRelatedId(ProjectPE related)
    {
        return new ProjectIdentifier(related.getIdentifier());
    }

    @Override
    protected ProjectPE getCurrentlyRelated(SamplePE entity)
    {
        return entity.getProject();
    }

    @Override
    protected FieldUpdateValue<IProjectId> getRelatedUpdate(SampleUpdate update)
    {
        return update.getProjectId();
    }

    @Override
    protected Map<IProjectId, ProjectPE> map(IOperationContext context, List<IProjectId> relatedIds)
    {
        return mapProjectByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, SamplePE entity, IProjectId relatedId, ProjectPE related)
    {
        ProjectByIdentiferValidator validator = new ProjectByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (false == validator.doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void update(IOperationContext context, SamplePE entity, ProjectPE related)
    {
        if (related == null)
        {
            relationshipService.unassignSampleFromProject(context.getSession(), entity);
        } else
        {
            relationshipService.assignSampleToProject(context.getSession(), entity, related);
        }
    }

}
