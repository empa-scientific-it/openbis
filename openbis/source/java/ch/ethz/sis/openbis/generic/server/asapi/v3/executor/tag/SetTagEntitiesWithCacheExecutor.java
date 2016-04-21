/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IReindexObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToManyRelationExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public abstract class SetTagEntitiesWithCacheExecutor<RELATED_ID extends IObjectId, RELATED_PE extends IEntityWithMetaprojects>
        extends AbstractSetEntityToManyRelationExecutor<TagCreation, MetaprojectPE, RELATED_ID, RELATED_PE>
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IReindexObjectExecutor reindexObjectExecutor;

    protected abstract Class<RELATED_PE> getRelatedClass();

    @Override
    public void set(IOperationContext context, Map<TagCreation, MetaprojectPE> creationsMap, Map<RELATED_ID, RELATED_PE> relatedMap)
    {
        super.set(context, creationsMap, relatedMap);

        daoFactory.getSessionFactory().getCurrentSession().flush();

        reindexObjectExecutor.reindex(context, getRelatedClass(), relatedMap.values());
    }

    @Override
    protected void setRelated(IOperationContext context, MetaprojectPE entity, Collection<RELATED_PE> related)
    {
        for (RELATED_PE aRelated : related)
        {
            aRelated.addMetaproject(entity);
        }
    }

}