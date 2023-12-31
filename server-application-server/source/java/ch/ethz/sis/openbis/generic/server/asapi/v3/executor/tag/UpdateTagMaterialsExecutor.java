/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IMapMaterialByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateTagMaterialsExecutor extends AbstractUpdateEntityMultipleRelationsExecutor<TagUpdate, MetaprojectPE, IMaterialId, MaterialPE>
        implements IUpdateTagMaterialsExecutor
{

    @Autowired
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Autowired
    private IUpdateTagMaterialsWithCacheExecutor updateTagMaterialsWithCacheExecutor;

    @Override
    protected void addRelatedIds(Set<IMaterialId> relatedIds, TagUpdate update)
    {
        addRelatedIds(relatedIds, update.getMaterialIds());
    }

    @Override
    protected Map<IMaterialId, MaterialPE> map(IOperationContext context, Collection<IMaterialId> relatedIds)
    {
        return mapMaterialByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void update(IOperationContext context, MapBatch<TagUpdate, MetaprojectPE> batch, Map<IMaterialId, MaterialPE> relatedMap)
    {
        updateTagMaterialsWithCacheExecutor.update(context, batch, relatedMap);
    }

}
