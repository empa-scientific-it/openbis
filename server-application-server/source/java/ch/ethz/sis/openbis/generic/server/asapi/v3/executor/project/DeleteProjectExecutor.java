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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteProjectExecutor extends AbstractDeleteEntityExecutor<Void, IProjectId, ProjectPE, ProjectDeletionOptions> implements
        IDeleteProjectExecutor
{

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Autowired
    private IProjectAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<IProjectId, ProjectPE> map(IOperationContext context, List<? extends IProjectId> entityIds, ProjectDeletionOptions deletionOptions)
    {
        return mapProjectByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IProjectId entityId, ProjectPE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, ProjectPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<ProjectPE> projects, ProjectDeletionOptions deletionOptions)
    {
        IProjectBO projectBO = businessObjectFactory.createProjectBO(context.getSession());
        for (ProjectPE project : projects)
        {
            projectBO.deleteByTechId(new TechId(project.getId()), deletionOptions.getReason());
        }
        return null;
    }

}
