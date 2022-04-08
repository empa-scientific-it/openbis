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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IEventExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingEvent;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingFlags;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSpaceExecutor extends AbstractUpdateEntityExecutor<SpaceUpdate, SpacePE, ISpaceId, SpacePermId> implements
        IUpdateSpaceExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Autowired
    private ISpaceAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IEventExecutor eventExecutor;

    @Override
    protected ISpaceId getId(SpaceUpdate update)
    {
        return update.getSpaceId();
    }

    @Override
    protected SpacePermId getPermId(SpacePE entity)
    {
        return new SpacePermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, SpaceUpdate update)
    {
        if (update.getSpaceId() == null)
        {
            throw new UserFailureException("Space id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, ISpaceId id, SpacePE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<SpaceUpdate, SpacePE> batch)
    {
        List<FreezingEvent> freezingEvents = new ArrayList<>();
        for (Map.Entry<SpaceUpdate, SpacePE> entry : batch.getObjects().entrySet())
        {
            SpaceUpdate update = entry.getKey();
            FreezingFlags freezingFlags = new FreezingFlags();
            SpacePE space = entry.getValue();

            if (update.getDescription() != null && update.getDescription().isModified())
            {
                space.setDescription(update.getDescription().getValue());
            }
            if (update.shouldBeFrozen())
            {
                authorizationExecutor.canFreeze(context, space);
                space.setFrozen(true);
                freezingFlags.freeze();
            }
            if (update.shouldBeFrozenForProjects())
            {
                authorizationExecutor.canFreeze(context, space);
                space.setFrozenForProject(true);
                freezingFlags.freezeForProjects();
            }
            if (update.shouldBeFrozenForSamples())
            {
                authorizationExecutor.canFreeze(context, space);
                assertionOfNoDeletedEntityExecutor.assertSpaceHasNoDeletedSamples(space.getCode());
                space.setFrozenForSample(true);
                freezingFlags.freezeForSamples();
            }
            if (freezingFlags.noFlags() == false)
            {
                freezingEvents.add(new FreezingEvent(space.getCode(), EventPE.EntityType.SPACE, freezingFlags));
            }
        }
        if (freezingEvents.isEmpty() == false)
        {
            eventExecutor.persist(context, freezingEvents);
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<SpaceUpdate, SpacePE> batch)
    {
        // nothing to do
    }

    @Override
    protected Map<ISpaceId, SpacePE> map(IOperationContext context, Collection<ISpaceId> ids)
    {
        return mapSpaceByIdExecutor.map(context, ids);
    }

    @Override
    protected List<SpacePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getSpaceDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<SpacePE> entities, boolean clearCache)
    {
        for (SpacePE entity : entities)
        {
            daoFactory.getSpaceDAO().validateAndSaveUpdatedEntity(entity);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "space", null);
    }

}
