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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.project;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityFieldUpdateValueRelationExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.person.IMapPersonByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.IPersonId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.PersonPermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateProjectLeaderExecutor extends AbstractUpdateEntityFieldUpdateValueRelationExecutor<ProjectUpdate, ProjectPE, IPersonId, PersonPE>
        implements IUpdateProjectLeaderExecutor
{

    @Autowired
    private IMapPersonByIdExecutor mapPersonByIdExecutor;

    @Override
    protected IPersonId getRelatedId(PersonPE related)
    {
        return new PersonPermId(related.getUserId());
    }

    @Override
    protected PersonPE getCurrentlyRelated(ProjectPE entity)
    {
        return entity.getProjectLeader();
    }

    @Override
    protected FieldUpdateValue<IPersonId> getRelatedUpdate(ProjectUpdate update)
    {
        return update.getLeaderId();
    }

    @Override
    protected Map<IPersonId, PersonPE> map(IOperationContext context, List<IPersonId> relatedIds)
    {
        return mapPersonByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, ProjectPE entity, IPersonId relatedId, PersonPE related)
    {
        // nothing to do
    }

    @Override
    protected void update(IOperationContext context, ProjectPE entity, PersonPE related)
    {
        entity.setProjectLeader(related);
    }

}
