/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
public class SetTagForEntityExecutor implements ISetTagForEntityExecutor
{

    @Autowired
    private IMapTagByIdExecutor mapTagByIdExecutor;

    @Autowired
    private ICreateMissingTagExecutor createMissingTagExecutor;

    @SuppressWarnings("unused")
    private SetTagForEntityExecutor()
    {
    }

    public SetTagForEntityExecutor(IMapTagByIdExecutor mapTagByIdExecutor, ICreateMissingTagExecutor createMissingTagExecutor)
    {
        this.mapTagByIdExecutor = mapTagByIdExecutor;
        this.createMissingTagExecutor = createMissingTagExecutor;
    }

    @Override
    public void setTags(IOperationContext context, IEntityWithMetaprojects entity, Collection<? extends ITagId> tagIds)
    {
        TagAuthorization authorization = new TagAuthorization(context);
        Map<ITagId, MetaprojectPE> tagMap = mapTagByIdExecutor.map(context, tagIds);
        Set<MetaprojectPE> tags = new HashSet<MetaprojectPE>(tagMap.values());

        for (MetaprojectPE existingTag : entity.getMetaprojects())
        {
            if (false == tags.contains(existingTag) && authorization.canAccess(existingTag))
            {
                entity.removeMetaproject(existingTag);
            }
        }

        if (tagIds != null)
        {
            createMissingTagExecutor.create(context, tagIds, tagMap);

            for (ITagId tagId : tagIds)
            {
                MetaprojectPE tag = tagMap.get(tagId);
                authorization.checkAccess(tag);
                entity.addMetaproject(tag);
            }
        }
    }

}
