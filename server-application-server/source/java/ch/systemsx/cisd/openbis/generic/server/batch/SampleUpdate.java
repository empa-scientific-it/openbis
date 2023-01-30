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
 * {@link IBatchOperation} updating samples. It is like {@link SampleBatchUpdate}, but uses {@link SampleUpdatesDTO} to specify the updates instead of
 * {@link SampleBatchUpdate} and thus has slightly different semantics.
 * <p>
 * Whereas SampleBatchUpdate only makes changes to the sample that are explicitly specified in it the details object of its DTO, SampleUpdate changes
 * the sample to match the DTO.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleUpdate extends AbstractBatchOperation<SampleUpdatesDTO>
{
    private final ISampleTable businessTable;

    private final List<SampleUpdatesDTO> entities;

    public SampleUpdate(ISampleTable businessTable, List<SampleUpdatesDTO> entities)
    {
        this(businessTable, entities, null);
    }

    public SampleUpdate(ISampleTable businessTable, List<SampleUpdatesDTO> entities,
            IBatchOperationDelegate<SampleUpdatesDTO> delegate)
    {
        super(delegate);
        this.businessTable = businessTable;
        this.entities = entities;
    }

    @Override
    public void execute(List<SampleUpdatesDTO> updates)
    {
        businessTable.prepareForUpdateWithSampleUpdates(updates);

        batchOperationWillSave();
        businessTable.save();
        batchOperationDidSave();
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
        return "update";
    }

}