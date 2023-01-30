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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * {@link IBatchOperation} registering samples.
 * 
 * @author Izabela Adamczyk
 */
public class SampleBatchRegistration implements IBatchOperation<NewSample>, IProgressAware
{
    private final ISampleTable businessTable;

    private final List<NewSample> entities;

    private final PersonPE registratorOrNull;

    private int endIndex;

    private int maxIndex;

    private int limit;

    public SampleBatchRegistration(ISampleTable businessTable, List<NewSample> entities,
            PersonPE registratorOrNull)
    {
        this(businessTable, entities, registratorOrNull, 50000);
    }

    public SampleBatchRegistration(ISampleTable businessTable, List<NewSample> entities,
            PersonPE registratorOrNull, int limit)
    {
        this.businessTable = businessTable;
        this.entities = entities;
        this.registratorOrNull = registratorOrNull;
        this.limit = limit;
    }

    @Override
    public void execute(List<NewSample> batch)
    {
        businessTable.prepareForRegistration(batch, registratorOrNull);
        businessTable.save(maxIndex - endIndex > limit);
    }

    @Override
    public List<NewSample> getAllEntities()
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
        return "registration";
    }

    @Override
    public void setNextChunk(int startIndex, int endIndex, int maxIndex)
    {
        this.endIndex = endIndex;
        this.maxIndex = maxIndex;
    }

}