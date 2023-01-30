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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag.TagAuthorization;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class AddTagToEntityExecutor implements IAddTagToEntityExecutor
{

    @Autowired
    private IMapTagByIdExecutor mapTagByIdExecutor;

    @Autowired
    private ICreateMissingTagExecutor createMissingTagExecutor;

    @SuppressWarnings("unused")
    private AddTagToEntityExecutor()
    {
    }

    public AddTagToEntityExecutor(IMapTagByIdExecutor mapTagByIdExecutor, ICreateMissingTagExecutor createMissingTagExecutor)
    {
        this.mapTagByIdExecutor = mapTagByIdExecutor;
        this.createMissingTagExecutor = createMissingTagExecutor;
    }

    @Override
    public void add(IOperationContext context, Map<IEntityWithMetaprojects, Collection<? extends ITagId>> entityToTagIdsMap)
    {
        if (entityToTagIdsMap == null || entityToTagIdsMap.isEmpty())
        {
            return;
        }

        Set<ITagId> allTagIds = new HashSet<ITagId>();

        for (Collection<? extends ITagId> tagIds : entityToTagIdsMap.values())
        {
            if (tagIds != null && false == tagIds.isEmpty())
            {
                allTagIds.addAll(tagIds);
            }
        }

        TagAuthorization authorization = new TagAuthorization(context);
        Map<ITagId, MetaprojectPE> allTagsMap = mapTagByIdExecutor.map(context, allTagIds);

        createMissingTagExecutor.create(context, allTagIds, allTagsMap);

        for (Map.Entry<IEntityWithMetaprojects, Collection<? extends ITagId>> entry : entityToTagIdsMap.entrySet())
        {
            IEntityWithMetaprojects entity = entry.getKey();
            Collection<? extends ITagId> tagIds = entry.getValue();

            if (tagIds != null && false == tagIds.isEmpty())
            {
                for (ITagId tagId : tagIds)
                {
                    MetaprojectPE tag = allTagsMap.get(tagId);
                    authorization.checkAccess(tag);
                    entity.addMetaproject(tag);
                }
            }
        }
    }

}
