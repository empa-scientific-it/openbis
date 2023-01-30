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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag.TagAuthorization;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class CreateMissingTagExecutor implements ICreateMissingTagExecutor
{

    @Autowired
    private IGetTagIdentifierExecutor getTagIdentifierExecutor;

    @Autowired
    private ICreateTagExecutor createTagExecutor;

    @Autowired
    private IMapTagByIdExecutor mapTagByIdExecutor;

    @Override
    public void create(IOperationContext context, Collection<? extends ITagId> neededTagIds, Map<ITagId, MetaprojectPE> existingTagMap)
    {
        TagAuthorization authorization = new TagAuthorization(context);

        List<TagCreation> creations = new ArrayList<TagCreation>();
        Map<TagCreation, ITagId> neededTagIdMap = new IdentityHashMap<TagCreation, ITagId>();

        for (ITagId neededTagId : neededTagIds)
        {
            MetaprojectPE tag = existingTagMap.get(neededTagId);
            if (tag == null)
            {
                MetaprojectIdentifier tagIdentifier = getTagIdentifierExecutor.getIdentifier(context, neededTagId);

                authorization.checkAccess(tagIdentifier);

                TagCreation creation = new TagCreation();
                creation.setCode(tagIdentifier.getMetaprojectName());

                creations.add(creation);
                neededTagIdMap.put(creation, neededTagId);
            }
        }

        List<TagPermId> createdIds = createTagExecutor.create(context, creations);
        Map<ITagId, MetaprojectPE> createdMap = mapTagByIdExecutor.map(context, createdIds);

        for (int i = 0; i < creations.size(); i++)
        {
            TagCreation creation = creations.get(i);
            TagPermId createdId = createdIds.get(i);
            MetaprojectPE created = createdMap.get(createdId);
            ITagId neededTagId = neededTagIdMap.get(creation);
            existingTagMap.put(neededTagId, created);
        }
    }

}
