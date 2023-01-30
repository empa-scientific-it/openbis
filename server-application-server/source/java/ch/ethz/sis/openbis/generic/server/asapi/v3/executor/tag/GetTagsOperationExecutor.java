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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.get.GetTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.get.GetTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.tag.ITagTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class GetTagsOperationExecutor extends GetObjectsPEOperationExecutor<ITagId, MetaprojectPE, Tag, TagFetchOptions>
        implements IGetTagsOperationExecutor
{

    @Autowired
    private IMapTagByIdExecutor mapExecutor;

    @Autowired
    private ITagTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<ITagId, TagFetchOptions>> getOperationClass()
    {
        return GetTagsOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<ITagId, MetaprojectPE> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Tag, TagFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<ITagId, Tag> getOperationResult(Map<ITagId, Tag> objectMap)
    {
        return new GetTagsOperationResult(objectMap);
    }

}
