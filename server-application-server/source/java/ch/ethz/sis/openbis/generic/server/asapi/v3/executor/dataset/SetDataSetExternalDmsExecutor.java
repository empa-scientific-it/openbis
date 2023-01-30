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
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms.IMapExternalDmsByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocationType;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetExternalDmsExecutor extends
        AbstractSetEntityToOneRelationExecutor<DataSetCreation, DataPE, IExternalDmsId, ExternalDataManagementSystemPE>
        implements ISetDataSetExternalDmsExecutor
{

    @Autowired
    private IMapExternalDmsByIdExecutor mapExternalDmsByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "dataset-externaldms";
    }

    @Override
    protected IExternalDmsId getRelatedId(DataSetCreation creation)
    {
        return creation.getLinkedData() != null ? creation.getLinkedData().getExternalDmsId() : null;
    }

    @Override
    protected Map<IExternalDmsId, ExternalDataManagementSystemPE> map(IOperationContext context, List<IExternalDmsId> relatedIds)
    {
        return mapExternalDmsByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, IExternalDmsId relatedId, ExternalDataManagementSystemPE related)
    {
        if (entity instanceof LinkDataPE && relatedId == null)
        {
            throw new UserFailureException("External data management system id cannot be null for a link data set.");
        }
    }

    @Override
    protected void set(IOperationContext context, DataPE entity, ExternalDataManagementSystemPE related)
    {
        if (entity instanceof LinkDataPE)
        {
            LinkDataPE dataSet = ((LinkDataPE) entity);
            Set<ContentCopyPE> contentCopies = dataSet.getContentCopies();

            if (contentCopies.size() == 1)
            {
                ContentCopyPE current = contentCopies.iterator().next();

                ContentCopyPE newCopy = new ContentCopyPE();
                newCopy.setExternalCode(current.getExternalCode());
                newCopy.setDataSet(current.getDataSet());
                newCopy.setRegistrator(context.getSession().tryGetPerson());

                switch (related.getAddressType())
                {
                    case OPENBIS:
                        newCopy.setLocationType(LocationType.OPENBIS);
                        break;
                    case URL:
                        newCopy.setLocationType(LocationType.URL);
                        break;
                    default:
                        throw new UserFailureException("Cannot set external data management system of dataset to be of type "
                                + related.getAddressType() + " using legacy methods");
                }

                newCopy.setExternalDataManagementSystem(related);

                contentCopies.remove(current);
                contentCopies.add(newCopy);
            } else
            {
                throw new UserFailureException("Cannot set external data management system to linked dataset with multiple or zero copies");
            }
        }
    }

}