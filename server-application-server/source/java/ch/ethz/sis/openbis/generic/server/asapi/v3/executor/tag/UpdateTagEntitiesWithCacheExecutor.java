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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToManyRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IReindexEntityExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
public abstract class UpdateTagEntitiesWithCacheExecutor<RELATED_ID, RELATED_PE extends IEntityWithMetaprojects & IEntityInformationWithPropertiesHolder>
        extends AbstractUpdateEntityToManyRelationExecutor<TagUpdate, MetaprojectPE, RELATED_ID, RELATED_PE>
{

    @Autowired
    private IReindexEntityExecutor reindexObjectExecutor;

    protected abstract Class<RELATED_PE> getRelatedClass();

    protected abstract RELATED_PE getCurrentlyRelated(MetaprojectAssignmentPE entity);

    @Override
    protected void postUpdate(IOperationContext context, Collection<RELATED_PE> allAdded, Collection<RELATED_PE> allRemoved)
    {
        Collection<RELATED_PE> entitiesToReindex = new HashSet<RELATED_PE>();
        entitiesToReindex.addAll(allAdded);
        entitiesToReindex.addAll(allRemoved);

        reindexObjectExecutor.reindex(context, getRelatedClass(), entitiesToReindex);
    }

    @Override
    protected Collection<RELATED_PE> getCurrentlyRelated(MetaprojectPE entity)
    {
        List<RELATED_PE> relatedList = new ArrayList<RELATED_PE>();

        for (MetaprojectAssignmentPE assignment : entity.getAssignments())
        {
            RELATED_PE related = getCurrentlyRelated(assignment);
            if (related != null)
            {
                relatedList.add(related);
            }
        }

        return relatedList;
    }

    @Override
    protected void add(IOperationContext context, MetaprojectPE entity, RELATED_PE related)
    {
        related.addMetaproject(entity);
    }

    @Override
    protected void remove(IOperationContext context, MetaprojectPE entity, RELATED_PE related)
    {
        related.removeMetaproject(entity);
    }

}
