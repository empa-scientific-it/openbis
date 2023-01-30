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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToManyRelationExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetComponentsExecutor extends AbstractUpdateEntityToManyRelationExecutor<DataSetUpdate, DataPE, IDataSetId, DataPE>
        implements IUpdateDataSetComponentsExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected String getRelationName()
    {
        return "dataset-components";
    }

    @Override
    protected Collection<DataPE> getCurrentlyRelated(DataPE entity)
    {
        return entity.getContainedDataSets();
    }

    @Override
    protected IdListUpdateValue<? extends IDataSetId> getRelatedUpdate(IOperationContext context, DataSetUpdate update)
    {
        return update.getComponentIds();
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, IDataSetId relatedId, DataPE related)
    {
        DataSetPEByExperimentOrSampleIdentifierValidator validator = new DataSetPEByExperimentOrSampleIdentifierValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (false == validator.doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
        if (false == entity.isContainer())
        {
            throw new UserFailureException("Data set " + entity.getCode()
                    + " is not of a container type therefore cannot have component data sets.");
        }
    }

    @Override
    protected void add(IOperationContext context, DataPE entity, DataPE related)
    {
        relationshipService.assignDataSetToContainer(context.getSession(), related, entity);
    }

    @Override
    protected void remove(IOperationContext context, DataPE entity, DataPE related)
    {
        relationshipService.removeDataSetFromContainer(context.getSession(), related, entity);
    }

}
