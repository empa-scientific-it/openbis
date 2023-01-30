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
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateTagDataSetsExecutor extends AbstractUpdateEntityMultipleRelationsExecutor<TagUpdate, MetaprojectPE, IDataSetId, DataPE>
        implements IUpdateTagDataSetsExecutor
{

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IUpdateTagDataSetsWithCacheExecutor updateTagDataSetsWithCacheExecutor;

    @Override
    protected void addRelatedIds(Set<IDataSetId> relatedIds, TagUpdate update)
    {
        addRelatedIds(relatedIds, update.getDataSetIds());
    }

    @Override
    protected Map<IDataSetId, DataPE> map(IOperationContext context, Collection<IDataSetId> relatedIds)
    {
        return mapDataSetByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void update(IOperationContext context, MapBatch<TagUpdate, MetaprojectPE> batch, Map<IDataSetId, DataPE> relatedMap)
    {
        updateTagDataSetsWithCacheExecutor.update(context, batch, relatedMap);
    }

}
