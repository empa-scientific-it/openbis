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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.AbstractObjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author pkupczyk
 */
public abstract class AbstractDeleteEntityExecutor<DELETION_ID, ENTITY_ID, ENTITY_PE, DELETION_OPTIONS extends AbstractObjectDeletionOptions<?>>
        implements IDeleteEntityExecutor<DELETION_ID, ENTITY_ID, DELETION_OPTIONS>
{

    @Autowired
    protected IDAOFactory daoFactory;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Override
    public DELETION_ID delete(IOperationContext context, List<? extends ENTITY_ID> entityIds, DELETION_OPTIONS deletionOptions)
    {
        if (context == null)
        {
            throw new UserFailureException("Context cannot be null");
        }
        if (entityIds == null)
        {
            throw new UserFailureException("Entity ids cannot be null");
        }
        if (deletionOptions == null)
        {
            throw new UserFailureException("Deletion options cannot be null");
        }
        if (deletionOptions.getReason() == null)
        {
            throw new UserFailureException("Deletion reason cannot be null");
        }

        Map<ENTITY_ID, ENTITY_PE> entityMap = map(context, entityIds, deletionOptions);

        if (entityMap.isEmpty())
        {
            return null;
        }

        for (Map.Entry<ENTITY_ID, ENTITY_PE> entry : entityMap.entrySet())
        {
            ENTITY_ID entityId = entry.getKey();
            ENTITY_PE entity = entry.getValue();

            try
            {
                checkAccess(context, entityId, entity);
            } catch (AuthorizationFailureException ex)
            {
                throw new UnauthorizedObjectAccessException((IObjectId) entityId);
            }

            updateModificationDateAndModifier(context, entity);
        }

        return delete(context, entityMap.values(), deletionOptions);
    }

    protected List<TechId> asTechIds(Collection<? extends IIdHolder> entities)
    {
        List<TechId> techIds = new ArrayList<TechId>();
        for (IIdHolder entity : entities)
        {
            techIds.add(new TechId(HibernateUtils.getId(entity)));
        }
        return techIds;
    }

    protected abstract Map<ENTITY_ID, ENTITY_PE> map(IOperationContext context, List<? extends ENTITY_ID> entityIds,
            DELETION_OPTIONS deletionOptions);

    protected abstract void checkAccess(IOperationContext context, ENTITY_ID entityId, ENTITY_PE entity);

    protected abstract void updateModificationDateAndModifier(IOperationContext context, ENTITY_PE entity);

    protected abstract DELETION_ID delete(IOperationContext context, Collection<ENTITY_PE> entities, DELETION_OPTIONS deletionOptions);

}
