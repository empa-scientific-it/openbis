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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.CreateTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.CreateTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class CreateTagsOperationExecutor extends CreateObjectsOperationExecutor<TagCreation, TagPermId> implements
        ICreateTagsOperationExecutor
{

    @Autowired
    private ICreateTagExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<TagCreation>> getOperationClass()
    {
        return CreateTagsOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<TagPermId> doExecute(IOperationContext context, CreateObjectsOperation<TagCreation> operation)
    {
        return new CreateTagsOperationResult(executor.create(context, operation.getCreations()));
    }

}
