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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractVerifyEntityCyclesExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor.RelationshipType;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public abstract class AbstractVerifyDataSetCyclesExecutor extends AbstractVerifyEntityCyclesExecutor<DataPE>
{

    @Autowired
    private IGetRelationshipIdExecutor getRelationshipIdExecutor;

    @Override
    protected Long getId(DataPE entity)
    {
        return entity.getId();
    }

    @Override
    protected String getIdentifier(Long entityId)
    {
        DataPE dataSet = daoFactory.getDataDAO().getByTechId(new TechId(entityId));
        return dataSet.getCode();
    }

    @Override
    protected Map<Long, Set<Long>> getRelatedIdsMap(IOperationContext context, Set<Long> entityIds)
    {
        Long relationshipId = getRelationshipIdExecutor.get(context, getRelationshipType());
        return daoFactory.getDataDAO().mapDataSetIdsByChildrenIds(entityIds, relationshipId);
    }

    protected abstract RelationshipType getRelationshipType();

}
