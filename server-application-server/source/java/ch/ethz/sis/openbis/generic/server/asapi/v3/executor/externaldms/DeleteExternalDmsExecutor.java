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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.delete.ExternalDmsDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataManagementSystemDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

@Component
public class DeleteExternalDmsExecutor
        extends AbstractDeleteEntityExecutor<Void, IExternalDmsId, ExternalDataManagementSystemPE, ExternalDmsDeletionOptions>
        implements
        IDeleteExternalDmsExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IExternalDmsAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapExternalDmsByIdExecutor mapExternalDmsByIdExecutor;

    @Override
    protected void checkAccess(IOperationContext context, IExternalDmsId entityId, ExternalDataManagementSystemPE entity)
    {
        authorizationExecutor.canDelete(context);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, ExternalDataManagementSystemPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<ExternalDataManagementSystemPE> externalDms,
            ExternalDmsDeletionOptions deletionOptions)
    {
        IExternalDataManagementSystemDAO edmsDao = daoFactory.getExternalDataManagementSystemDAO();
        edmsDao.delete(externalDms);
        return null;
    }

    @Override
    protected Map<IExternalDmsId, ExternalDataManagementSystemPE> map(IOperationContext context, List<? extends IExternalDmsId> entityIds,
            ExternalDmsDeletionOptions deletionOptions)
    {
        return mapExternalDmsByIdExecutor.map(context, entityIds);
    }

}
