/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.batch;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;

/**
 * {@link IBatchOperation} updating samples.
 * 
 * @author Izabela Adamczyk
 */
public class SampleBatchUpdate implements IBatchOperation<SampleBatchUpdatesDTO>, IProgressAware
{
    private final ISampleTable businessTable;

    private final List<SampleBatchUpdatesDTO> entities;

    private int endIndex;

    private int maxIndex;

    int limit;

    public SampleBatchUpdate(ISampleTable businessTable, List<SampleBatchUpdatesDTO> entities,
            int limit)
    {
        this.businessTable = businessTable;
        this.entities = entities;
        this.limit = limit;
    }

    @Override
    public void execute(List<SampleBatchUpdatesDTO> updates)
    {
        businessTable.prepareForUpdate(updates);
        businessTable.save(maxIndex - endIndex > limit);
    }

    @Override
    public List<SampleBatchUpdatesDTO> getAllEntities()
    {
        return entities;
    }

    @Override
    public String getEntityName()
    {
        return "sample";
    }

    @Override
    public String getOperationName()
    {
        return "update";
    }

    @Override
    public void setNextChunk(int startIndex, int endIndex, int maxIndex)
    {
        this.endIndex = endIndex;
        this.maxIndex = maxIndex;
    }

}