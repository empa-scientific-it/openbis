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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateExternalDmsExecutor
        extends AbstractUpdateEntityExecutor<ExternalDmsUpdate, ExternalDataManagementSystemPE, IExternalDmsId, ExternalDmsPermId> implements
        IUpdateExternalDmsExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapExternalDmsByIdExecutor mapExternalDmsByIdExecutor;

    @Autowired
    private IExternalDmsAuthorizationExecutor authorizationExecutor;

    @Override
    protected IExternalDmsId getId(ExternalDmsUpdate update)
    {
        return update.getExternalDmsId();
    }

    @Override
    protected ExternalDmsPermId getPermId(ExternalDataManagementSystemPE entity)
    {
        return new ExternalDmsPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, ExternalDmsUpdate update)
    {
        if (update.getAddress().isModified() && (update.getAddress().getValue() == null || update.getAddress().getValue().isEmpty()))
        {
            throw new UserFailureException("Address is required");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IExternalDmsId id, ExternalDataManagementSystemPE entity)
    {
        authorizationExecutor.canUpdate(context);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<ExternalDmsUpdate, ExternalDataManagementSystemPE> batch)
    {
        for (Map.Entry<ExternalDmsUpdate, ExternalDataManagementSystemPE> entry : batch.getObjects().entrySet())
        {
            ExternalDmsUpdate update = entry.getKey();
            ExternalDataManagementSystemPE edms = entry.getValue();

            if (update.getLabel().isModified())
            {
                edms.setLabel(update.getLabel().getValue());
            }

            if (update.getAddress().isModified())
            {
                String address = update.getAddress().getValue();
                if (ExternalDataManagementSystemType.FILE_SYSTEM.equals(edms.getAddressType()))
                {
                    String pattern = "^[^:]+:[^:]+$";
                    if (address.matches(pattern) == false)
                    {
                        throw new UserFailureException("Invalid address: " + address);
                    }
                }

                edms.setAddress(address);
            }
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<ExternalDmsUpdate, ExternalDataManagementSystemPE> batch)
    {
    }

    @Override
    protected Map<IExternalDmsId, ExternalDataManagementSystemPE> map(IOperationContext context, Collection<IExternalDmsId> ids)
    {
        return mapExternalDmsByIdExecutor.map(context, ids);
    }

    @Override
    protected List<ExternalDataManagementSystemPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getExternalDataManagementSystemDAO().listExternalDataManagementSystems(ids);
    }

    @Override
    protected void save(IOperationContext context, List<ExternalDataManagementSystemPE> entities, boolean clearCache)
    {
        for (ExternalDataManagementSystemPE entity : entities)
        {
            daoFactory.getExternalDataManagementSystemDAO().createOrUpdateExternalDataManagementSystem(entity);
        }
        daoFactory.getSessionFactory().getCurrentSession().flush();
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "externaldms", null);
    }
}
