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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.IAssertionOfNoDeletedEntityExecutor;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;

/**
 * @author pkupczyk
 */
public abstract class AbstractUpdateEntityExecutor<UPDATE extends IUpdate, PE extends IIdentityHolder, ID extends IObjectId, PERM_ID>
        implements IUpdateEntityExecutor<UPDATE, PERM_ID>
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    protected IAssertionOfNoDeletedEntityExecutor assertionOfNoDeletedEntityExecutor;

    @Override
    public List<PERM_ID> update(IOperationContext context, List<UPDATE> updates)
    {

        if (updates == null || updates.isEmpty())
        {
            return Collections.emptyList();
        }

        try
        {
            Map<UPDATE, PE> entitiesAll = new LinkedHashMap<UPDATE, PE>();

            for (CollectionBatch<UPDATE> batch : Batch.createBatches(updates))
            {
                updateEntities(context, batch, entitiesAll);
            }

            reloadEntities(context, entitiesAll);

            updateAll(context, new MapBatch<UPDATE, PE>(0, 0, entitiesAll.size(), entitiesAll, entitiesAll.size()));

            reloadEntities(context, entitiesAll);

            List<PERM_ID> permIds = new ArrayList<PERM_ID>();
            for (PE entity : entitiesAll.values())
            {
                permIds.add(getPermId(entity));
            }
            return permIds;

        } catch (DataAccessException e)
        {
            handleException(e);
        } catch (PersistenceException e)
        {
            Throwable endOfChain = ch.systemsx.cisd.common.exceptions.ExceptionUtils.getEndOfChain(e);
            throw new UserFailureException(endOfChain.getMessage(), endOfChain);
        }

        return Collections.emptyList();
    }

    private void checkData(final IOperationContext context, CollectionBatch<UPDATE> batch)
    {
        new CollectionBatchProcessor<UPDATE>(context, batch)
            {
                @Override
                public void process(UPDATE object)
                {
                    checkData(context, object);
                }

                @Override
                public IProgress createProgress(UPDATE object, int objectIndex, int totalObjectCount)
                {
                    return new CheckDataProgress(object, objectIndex, totalObjectCount);
                }
            };
    }

    private void checkAccess(final IOperationContext context, MapBatch<UPDATE, PE> batch)
    {
        new MapBatchProcessor<UPDATE, PE>(context, batch)
            {
                @Override
                public void process(UPDATE update, PE entity)
                {
                    ID id = getId(update);
                    try
                    {
                        checkAccess(context, id, entity);
                    } catch (AuthorizationFailureException ex)
                    {
                        throw new UnauthorizedObjectAccessException(id);
                    }
                }

                @Override
                public IProgress createProgress(UPDATE update, PE entity, int objectIndex, int totalObjectCount)
                {
                    return new CheckAccessProgress(entity, update, objectIndex, totalObjectCount);
                }
            };
    }

    private Map<UPDATE, PE> getEntitiesMap(IOperationContext context, CollectionBatch<UPDATE> batch)
    {
        Collection<ID> entityIds = CollectionUtils.collect(batch.getObjects(), new Transformer<UPDATE, ID>()
            {
                @Override
                public ID transform(UPDATE update)
                {
                    return getId(update);
                }
            });

        Map<ID, PE> entityMap = map(context, entityIds);

        for (ID entityId : entityIds)
        {
            PE entity = entityMap.get(entityId);

            if (entity == null)
            {
                throw new ObjectNotFoundException(entityId);
            }
        }

        Map<UPDATE, PE> result = new HashMap<UPDATE, PE>();

        for (UPDATE update : batch.getObjects())
        {
            ID id = getId(update);
            result.put(update, entityMap.get(id));
        }

        return result;
    }

    private void updateEntities(IOperationContext context, CollectionBatch<UPDATE> batch, Map<UPDATE, PE> entitiesAll)
    {
        checkData(context, batch);

        Map<UPDATE, PE> updateToEntityMap = getEntitiesMap(context, batch);
        entitiesAll.putAll(updateToEntityMap);

        daoFactory.setBatchUpdateMode(true);

        MapBatch<UPDATE, PE> mapBatch = new MapBatch<UPDATE, PE>(batch.getBatchIndex(), batch.getFromObjectIndex(), batch.getToObjectIndex(),
                updateToEntityMap, batch.getTotalObjectCount());

        checkAccess(context, mapBatch);
        updateBatch(context, mapBatch);

        save(context, new ArrayList<PE>(updateToEntityMap.values()), false);

        daoFactory.setBatchUpdateMode(false);
    }

    private void reloadEntities(IOperationContext context, Map<UPDATE, PE> updateToEntityMap)
    {
        Collection<Long> ids = new HashSet<Long>();

        for (PE entity : updateToEntityMap.values())
        {
            ids.add(entity.getId());
        }

        List<PE> entities = list(context, ids);

        Map<Long, PE> idToEntityMap = new HashMap<Long, PE>();

        for (PE entity : entities)
        {
            idToEntityMap.put(entity.getId(), entity);
        }

        for (Map.Entry<UPDATE, PE> entry : updateToEntityMap.entrySet())
        {
            entry.setValue(idToEntityMap.get(entry.getValue().getId()));
        }
    }
    
    protected <T> T getNewValue(FieldUpdateValue<T> fieldUpdateValue, T currentValue)
    {
        return fieldUpdateValue != null && fieldUpdateValue.isModified() ? fieldUpdateValue.getValue() : currentValue;
    }
    

    protected abstract ID getId(UPDATE update);

    protected abstract PERM_ID getPermId(PE entity);

    protected abstract void checkData(IOperationContext context, UPDATE update);

    protected abstract void checkAccess(IOperationContext context, ID id, PE entity);

    protected abstract void updateBatch(IOperationContext context, MapBatch<UPDATE, PE> batch);

    protected abstract void updateAll(IOperationContext context, MapBatch<UPDATE, PE> batch);

    protected abstract Map<ID, PE> map(IOperationContext context, Collection<ID> ids);

    protected abstract List<PE> list(IOperationContext context, Collection<Long> ids);

    protected abstract void save(IOperationContext context, List<PE> entities, boolean clearCache);

    protected abstract void handleException(DataAccessException e);

}
