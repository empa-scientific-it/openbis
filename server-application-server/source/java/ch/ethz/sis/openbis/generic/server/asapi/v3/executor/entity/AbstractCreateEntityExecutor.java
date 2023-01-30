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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICreationIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CreationIdCache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.Batch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CheckAccessProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CheckDataProgress;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * @author pkupczyk
 */
public abstract class AbstractCreateEntityExecutor<CREATION extends ICreation, PE extends IIdHolder, PERM_ID extends IObjectId>
        implements ICreateEntityExecutor<CREATION, PERM_ID>
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public List<PERM_ID> create(IOperationContext context, List<CREATION> creations)
    {
        checkAccess(context);

        if (creations == null || creations.isEmpty())
        {
            return new ArrayList<PERM_ID>();
        }

        try
        {
            List<PERM_ID> permIdsAll = new LinkedList<PERM_ID>();
            Map<CREATION, PE> entitiesAll = new LinkedHashMap<CREATION, PE>();

            for (CollectionBatch<CREATION> batch : Batch.createBatches(creations))
            {
                createEntities(context, batch, permIdsAll, entitiesAll);
            }

            daoFactory.getSessionFactory().getCurrentSession().flush();
            reloadEntities(context, entitiesAll);

            updateAll(context, new MapBatch<CREATION, PE>(0, 0, entitiesAll.size(), entitiesAll, entitiesAll.size()));

            daoFactory.getSessionFactory().getCurrentSession().flush();
            reloadEntities(context, entitiesAll);

            return permIdsAll;
        } catch (DataAccessException e)
        {
            handleException(e);
            return null;
        } catch (PersistenceException e)
        {
            Throwable endOfChain = ch.systemsx.cisd.common.exceptions.ExceptionUtils.getEndOfChain(e);
            throw new UserFailureException(endOfChain.getMessage(), endOfChain);
        }
    }

    private void checkData(final IOperationContext context, CollectionBatch<CREATION> batch)
    {
        new CollectionBatchProcessor<CREATION>(context, batch)
            {
                @Override
                public void process(CREATION object)
                {
                    checkData(context, object);
                }

                @Override
                public IProgress createProgress(CREATION object, int objectIndex, int totalObjectCount)
                {
                    return new CheckDataProgress(object, objectIndex, totalObjectCount);
                }
            };
    }

    private void checkAccess(final IOperationContext context, MapBatch<CREATION, PE> batch)
    {
        new MapBatchProcessor<CREATION, PE>(context, batch)
            {
                @Override
                public void process(CREATION creation, PE entity)
                {
                    try
                    {
                        checkAccess(context, entity);
                    } catch (AuthorizationFailureException ex)
                    {
                        throw new UnauthorizedObjectAccessException(getId(entity));
                    }
                }

                @Override
                public IProgress createProgress(CREATION creation, PE entity, int objectIndex, int totalObjectCount)
                {
                    return new CheckAccessProgress(entity, creation, objectIndex, totalObjectCount);
                }
            };
    }

    private void createEntities(final IOperationContext context, CollectionBatch<CREATION> batch,
            List<PERM_ID> permIdsAll, Map<CREATION, PE> entitiesAll)
    {
        Map<CREATION, PE> creationToEntityMap = new LinkedHashMap<CREATION, PE>();

        daoFactory.setBatchUpdateMode(true);

        checkData(context, batch);

        List<PE> entities = createEntities(context, batch);
        Iterator<CREATION> iterCreations = batch.getObjects().iterator();
        Iterator<PE> iterEntities = entities.iterator();

        while (iterCreations.hasNext() && iterEntities.hasNext())
        {
            CREATION creation = iterCreations.next();
            PE entity = iterEntities.next();
            entitiesAll.put(creation, entity);
            creationToEntityMap.put(creation, entity);
        }

        MapBatch<CREATION, PE> mapBatch = new MapBatch<CREATION, PE>(batch.getBatchIndex(), batch.getFromObjectIndex(), batch.getToObjectIndex(),
                creationToEntityMap, batch.getTotalObjectCount());

        updateBatch(context, mapBatch);
        checkAccess(context, mapBatch);

        save(context, new ArrayList<PE>(creationToEntityMap.values()), false);

        CreationIdCache creationIdCache = CreationIdCache.getInstance(context);

        for (Map.Entry<CREATION, PE> entry : creationToEntityMap.entrySet())
        {
            CREATION creation = entry.getKey();
            PE entity = entry.getValue();

            PERM_ID permId = createPermId(context, entity);
            permIdsAll.add(permId);

            if (creation instanceof ICreationIdHolder)
            {
                ICreationIdHolder creationIdHolder = (ICreationIdHolder) creation;
                if (creationIdHolder.getCreationId() != null)
                {
                    creationIdCache.putIds(creationIdHolder.getCreationId(), permId);
                }
            }
        }

        daoFactory.setBatchUpdateMode(false);
    }

    private void reloadEntities(IOperationContext context, Map<CREATION, PE> creationToEntityMap)
    {
        Collection<Long> ids = new HashSet<Long>();

        for (PE entity : creationToEntityMap.values())
        {
            ids.add(entity.getId());
        }

        List<PE> entities = list(context, ids);

        Map<Long, PE> idToEntityMap = new HashMap<Long, PE>();

        for (PE entity : entities)
        {
            idToEntityMap.put(entity.getId(), entity);
        }

        for (Map.Entry<CREATION, PE> entry : creationToEntityMap.entrySet())
        {
            entry.setValue(idToEntityMap.get(entry.getValue().getId()));
        }
    }

    protected abstract IObjectId getId(PE entity);

    protected abstract void checkData(IOperationContext context, CREATION creation);

    protected abstract void checkAccess(IOperationContext context);

    protected abstract void checkAccess(IOperationContext context, PE entity);

    protected abstract List<PE> createEntities(IOperationContext context, CollectionBatch<CREATION> batch);

    protected abstract PERM_ID createPermId(IOperationContext context, PE entity);

    protected abstract void updateBatch(IOperationContext context, MapBatch<CREATION, PE> batch);

    protected abstract void updateAll(IOperationContext context, MapBatch<CREATION, PE> batch);

    protected abstract List<PE> list(IOperationContext context, Collection<Long> ids);

    protected abstract void save(IOperationContext context, List<PE> entities, boolean clearCache);

    protected abstract void handleException(DataAccessException e);

}
