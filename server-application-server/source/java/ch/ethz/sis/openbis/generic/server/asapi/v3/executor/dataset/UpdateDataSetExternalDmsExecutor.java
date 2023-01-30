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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms.IMapExternalDmsByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocationType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetExternalDmsExecutor extends
        AbstractUpdateEntityToOneRelationExecutor<DataSetUpdate, DataPE, IExternalDmsId, ExternalDataManagementSystemPE> implements
        IUpdateDataSetExternalDmsExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapExternalDmsByIdExecutor mapExternalDmsByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "dataset-externaldms";
    }

    @Override
    protected IExternalDmsId getRelatedId(ExternalDataManagementSystemPE related)
    {
        return new ExternalDmsPermId(related.getCode());
    }

    @Override
    protected ExternalDataManagementSystemPE getCurrentlyRelated(DataPE entity)
    {
        if (entity instanceof LinkDataPE)
        {
            LinkDataPE linkData = (LinkDataPE) entity;
            if (linkData.getContentCopies().size() > 0)
            {
                return ((LinkDataPE) entity).getContentCopies().iterator().next().getExternalDataManagementSystem();
            } else
            {
                return null;
            }
        } else
        {
            return null;
        }
    }

    @Override
    protected FieldUpdateValue<IExternalDmsId> getRelatedUpdate(DataSetUpdate update)
    {
        if (update.getLinkedData() != null && update.getLinkedData().getValue() != null)
        {
            return update.getLinkedData().getValue().getExternalDmsId();
        } else
        {
            return null;
        }
    }

    @Override
    protected Map<IExternalDmsId, ExternalDataManagementSystemPE> map(IOperationContext context, List<IExternalDmsId> relatedIds)
    {
        return mapExternalDmsByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, IExternalDmsId relatedId, ExternalDataManagementSystemPE related)
    {
    }

    @Override
    protected void update(IOperationContext context, DataPE entity, ExternalDataManagementSystemPE related)
    {
        if (entity instanceof LinkDataPE)
        {
            LinkDataPE dataSet = ((LinkDataPE) entity);
            Set<ContentCopyPE> contentCopies = dataSet.getContentCopies();
            if (contentCopies.size() == 1)
            {
                PersonPE person = context.getSession().tryGetPerson();

                ContentCopyPE current = contentCopies.iterator().next();

                ContentCopyPE newCopy = new ContentCopyPE();
                newCopy.setExternalCode(current.getExternalCode());
                newCopy.setDataSet(current.getDataSet());
                newCopy.setRegistrator(person);

                if (current.getExternalCode() != null)
                {
                    switch (related.getAddressType())
                    {
                        case OPENBIS:
                            newCopy.setLocationType(LocationType.OPENBIS);
                            break;
                        case URL:
                            newCopy.setLocationType(LocationType.URL);
                            break;
                        default:
                            throw new UserFailureException("Cannot set extenal data management system of dataset to be of type "
                                    + related.getAddressType() + " using legacy methods");
                    }

                    newCopy.setExternalDataManagementSystem(related);

                    contentCopies.add(newCopy);
                    contentCopies.remove(current);

                    Date timeStamp = daoFactory.getTransactionTimestamp();
                    RelationshipUtils.updateModificationDateAndModifier(entity, person, timeStamp);
                } else
                {
                    throw new UserFailureException("Cannot set external data management system to content copy of type " + current.getLocationType());
                }
            } else
            {
                throw new UserFailureException("Cannot set external data management system to linked dataset with multiple or zero copies");
            }
        }
    }

}
