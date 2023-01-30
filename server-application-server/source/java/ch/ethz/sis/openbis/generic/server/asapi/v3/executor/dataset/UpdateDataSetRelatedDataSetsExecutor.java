/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetRelatedDataSetsExecutor extends AbstractUpdateEntityMultipleRelationsExecutor<DataSetUpdate, DataPE, IDataSetId, DataPE>
        implements IUpdateDataSetRelatedDataSetsExecutor
{

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IUpdateDataSetContainersExecutor updateDataSetContainersExecutor;

    @Autowired
    private IUpdateDataSetComponentsExecutor updateDataSetComponentsExecutor;

    @Autowired
    private IUpdateDataSetParentsExecutor updateDataSetParentsExecutor;

    @Autowired
    private IUpdateDataSetChildrenExecutor updateDataSetChildrenExecutor;

    @Override
    protected void addRelatedIds(Set<IDataSetId> relatedIds, DataSetUpdate update)
    {
        addRelatedIds(relatedIds, update.getContainerIds());
        addRelatedIds(relatedIds, update.getComponentIds());
        addRelatedIds(relatedIds, update.getParentIds());
        addRelatedIds(relatedIds, update.getChildIds());
    }

    @Override
    protected Map<IDataSetId, DataPE> map(IOperationContext context, Collection<IDataSetId> relatedIds)
    {
        return mapDataSetByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void update(IOperationContext context, MapBatch<DataSetUpdate, DataPE> batch, Map<IDataSetId, DataPE> relatedMap)
    {
        updateDataSetContainersExecutor.update(context, batch, relatedMap);
        updateDataSetComponentsExecutor.update(context, batch, relatedMap);
        updateDataSetParentsExecutor.update(context, batch, relatedMap);
        updateDataSetChildrenExecutor.update(context, batch, relatedMap);
    }

}
