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
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.UpdateFieldWithListUpdateValueExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;

/**
 * @author pkupczyk
 */
@Component
public class UpdateTagForEntityExecutor extends UpdateFieldWithListUpdateValueExecutor<IEntityWithMetaprojects, ITagId> implements
        IUpdateTagForEntityExecutor
{

    @Autowired
    private ISetTagForEntityExecutor setTagForEntityExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagForEntityExecutor;

    @Autowired
    private IRemoveTagFromEntityExecutor removeTagFromEntityExecutor;

    @SuppressWarnings("unused")
    private UpdateTagForEntityExecutor()
    {
    }

    public UpdateTagForEntityExecutor(ISetTagForEntityExecutor setTagForEntityExecutor, IAddTagToEntityExecutor addTagForEntityExecutor,
            IRemoveTagFromEntityExecutor removeTagFromEntityExecutor)
    {
        this.setTagForEntityExecutor = setTagForEntityExecutor;
        this.addTagForEntityExecutor = addTagForEntityExecutor;
        this.removeTagFromEntityExecutor = removeTagFromEntityExecutor;
    }

    @Override
    public void update(IOperationContext context, IEntityWithMetaprojects entity,
            IdListUpdateValue<ITagId> updates)
    {
        update(context, Collections.singletonMap(entity, updates));
    }

    @Override
    protected void setValues(IOperationContext context, IEntityWithMetaprojects entity, Collection<? extends ITagId> tagIds)
    {
        setTagForEntityExecutor.setTags(context, entity, tagIds);
    }

    @Override
    protected void addValues(IOperationContext context, IEntityWithMetaprojects entity, Collection<? extends ITagId> tagIds)
    {
        addTagForEntityExecutor.add(context, Collections.<IEntityWithMetaprojects, Collection<? extends ITagId>> singletonMap(entity, tagIds));
    }

    @Override
    protected void removeValues(IOperationContext context, IEntityWithMetaprojects entity, Collection<? extends ITagId> tagIds)
    {
        removeTagFromEntityExecutor.removeTag(context, entity, tagIds);
    }

}
