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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateTagExperimentsExecutor extends
        AbstractUpdateEntityMultipleRelationsExecutor<TagUpdate, MetaprojectPE, IExperimentId, ExperimentPE>
        implements IUpdateTagExperimentsExecutor
{

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IUpdateTagExperimentsWithCacheExecutor updateTagExperimentsWithCacheExecutor;

    @Override
    protected void addRelatedIds(Set<IExperimentId> relatedIds, TagUpdate update)
    {
        addRelatedIds(relatedIds, update.getExperimentIds());
    }

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context, Collection<IExperimentId> relatedIds)
    {
        return mapExperimentByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void update(IOperationContext context, MapBatch<TagUpdate, MetaprojectPE> batch, Map<IExperimentId, ExperimentPE> relatedMap)
    {
        updateTagExperimentsWithCacheExecutor.update(context, batch, relatedMap);
    }

}
