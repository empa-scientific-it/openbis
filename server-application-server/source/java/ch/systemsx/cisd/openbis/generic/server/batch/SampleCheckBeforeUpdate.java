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
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

/**
 * {@link IBatchOperation} that checks samples before update.
 * 
 * @author Piotr Kupczyk
 */
public class SampleCheckBeforeUpdate extends AbstractBatchOperation<SampleUpdatesDTO>
{
    private final ISampleTable businessTable;

    private final List<SampleUpdatesDTO> entities;

    public SampleCheckBeforeUpdate(ISampleTable businessTable, List<SampleUpdatesDTO> entities)
    {
        super(null);
        this.businessTable = businessTable;
        this.entities = entities;
    }

    @Override
    public void execute(List<SampleUpdatesDTO> updates)
    {
        businessTable.checkBeforeUpdate(updates);
    }

    @Override
    public List<SampleUpdatesDTO> getAllEntities()
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
        return "checkBeforeUpdate";
    }

}