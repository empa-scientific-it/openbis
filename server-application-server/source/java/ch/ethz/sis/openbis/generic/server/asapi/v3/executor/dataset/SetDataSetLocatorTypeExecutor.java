/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ILocatorTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetLocatorTypeExecutor extends
        AbstractSetEntityToOneRelationExecutor<DataSetCreation, DataPE, ILocatorTypeId, LocatorTypePE>
        implements ISetDataSetLocatorTypeExecutor
{

    @Autowired
    private IMapLocatorTypeByIdExecutor mapLocatorTypeByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "dataset-locatortype";
    }

    @Override
    protected ILocatorTypeId getRelatedId(DataSetCreation creation)
    {
        if (creation.getPhysicalData() != null)
        {
            return creation.getPhysicalData().getLocatorTypeId() != null ? creation.getPhysicalData().getLocatorTypeId()
                    : new RelativeLocationLocatorTypePermId();
        } else
        {
            return null;
        }
    }

    @Override
    protected Map<ILocatorTypeId, LocatorTypePE> map(IOperationContext context, List<ILocatorTypeId> relatedIds)
    {
        return mapLocatorTypeByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, ILocatorTypeId relatedId, LocatorTypePE related)
    {
        if (entity instanceof ExternalDataPE && relatedId == null)
        {
            throw new UserFailureException("Locator type id cannot be null for a physical data set.");
        }
    }

    @Override
    protected void set(IOperationContext context, DataPE entity, LocatorTypePE related)
    {
        if (entity instanceof ExternalDataPE)
        {
            ((ExternalDataPE) entity).setLocatorType(related);
        }
    }

}