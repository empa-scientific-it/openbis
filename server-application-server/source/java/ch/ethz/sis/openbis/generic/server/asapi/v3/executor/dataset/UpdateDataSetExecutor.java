/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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

import java.util.*;
import java.util.Map.Entry;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.metadata.IUpdateMetaDataForEntityExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IEventExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingEvent;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingFlags;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetExecutor extends AbstractUpdateEntityExecutor<DataSetUpdate, DataPE, IDataSetId, DataSetPermId> implements
        IUpdateDataSetExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IDataSetAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IUpdateDataSetExperimentExecutor updateDataSetExperimentExecutor;

    @Autowired
    private IUpdateDataSetSampleExecutor updateDataSetSampleExecutor;

    @Autowired
    private IUpdateDataSetPhysicalDataExecutor updateDataSetPhysicalDataExecutor;

    @Autowired
    private IUpdateDataSetLinkedDataExecutor updateDataSetLinkedDataExecutor;

    @Autowired
    private IUpdateDataSetRelatedDataSetsExecutor updateDataSetRelatedDataSetsExecutor;

    @Autowired
    private IUpdateDataSetPropertyExecutor updateDataSetPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IEventExecutor eventExecutor;

    @Autowired
    private IUpdateMetaDataForEntityExecutor<DataSetUpdate, DataPE> updateMetaDataForEntityExecutor;

    @Override
    protected IDataSetId getId(DataSetUpdate update)
    {
        return update.getDataSetId();
    }

    @Override
    protected DataSetPermId getPermId(DataPE entity)
    {
        return new DataSetPermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, DataSetUpdate update)
    {
        if (update.getDataSetId() == null)
        {
            throw new UserFailureException("Data set id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IDataSetId id, DataPE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(final IOperationContext context, final MapBatch<DataSetUpdate, DataPE> batch)
    {
        updateDataSetPhysicalDataExecutor.update(context, batch);
        updateDataSetLinkedDataExecutor.update(context, batch);
        updateDataSetExperimentExecutor.update(context, batch);
        updateDataSetSampleExecutor.update(context, batch);
        updateDataSetPropertyExecutor.update(context, batch);
        updateTags(context, batch);
        updateMetaDataForEntityExecutor.update(context, batch);

        PersonPE person = context.getSession().tryGetPerson();
        Date timeStamp = daoFactory.getTransactionTimestamp();
        List<FreezingEvent> freezingEvents = new ArrayList<>();

        for (Entry<DataSetUpdate, DataPE> entry : batch.getObjects().entrySet())
        {
            DataSetUpdate update = entry.getKey();;
            DataPE entity = entry.getValue();
            FreezingFlags freezingFlags = new FreezingFlags();
            RelationshipUtils.updateModificationDateAndModifier(entity, person, timeStamp);
            if (update.shouldBeFrozen())
            {
                authorizationExecutor.canFreeze(context, entity);
                entity.setFrozen(true);
                freezingFlags.freeze();
            }
            if (update.shouldBeFrozenForChildren())
            {
                authorizationExecutor.canFreeze(context, entity);
                assertionOfNoDeletedEntityExecutor.assertDataSetHasNoDeletedChildren(update.getDataSetId());
                entity.setFrozenForChildren(true);
                freezingFlags.freezeForChildren();
            }
            if (update.shouldBeFrozenForParents())
            {
                authorizationExecutor.canFreeze(context, entity);
                assertionOfNoDeletedEntityExecutor.assertDataSetHasNoDeletedParents(update.getDataSetId());
                entity.setFrozenForParents(true);
                freezingFlags.freezeForParents();
            }
            if (update.shouldBeFrozenForComponents())
            {
                authorizationExecutor.canFreeze(context, entity);
                assertionOfNoDeletedEntityExecutor.assertDataSetHasNoDeletedComponents(update.getDataSetId());
                entity.setFrozenForComponents(true);
                freezingFlags.freezeForComponents();
            }
            if (update.shouldBeFrozenForContainers())
            {
                authorizationExecutor.canFreeze(context, entity);
                entity.setFrozenForContainers(true);
                freezingFlags.freezeForContainers();
            }
            if (freezingFlags.noFlags() == false)
            {
                freezingEvents.add(new FreezingEvent(entity.getIdentifier(), EventPE.EntityType.DATASET, freezingFlags));
            }
        }
        if (freezingEvents.isEmpty() == false)
        {
            eventExecutor.persist(context, freezingEvents);
        }
    }

    private void updateTags(final IOperationContext context, final MapBatch<DataSetUpdate, DataPE> batch)
    {
        new MapBatchProcessor<DataSetUpdate, DataPE>(context, batch)
            {
                @Override
                public void process(DataSetUpdate update, DataPE entity)
                {
                    if (update.getTagIds() != null && update.getTagIds().hasActions())
                    {
                        updateTagForEntityExecutor.update(context, entity, update.getTagIds());
                    }
                }

                @Override
                public IProgress createProgress(DataSetUpdate key, DataPE value, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(key, value, "dataset-tag", objectIndex, totalObjectCount);
                }
            };
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<DataSetUpdate, DataPE> batch)
    {
        updateDataSetRelatedDataSetsExecutor.update(context, batch);
    }

    @Override
    protected Map<IDataSetId, DataPE> map(IOperationContext context, Collection<IDataSetId> ids)
    {
        return mapDataSetByIdExecutor.map(context, ids);
    }

    @Override
    protected List<DataPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getDataDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<DataPE> entities, boolean clearCache)
    {
        daoFactory.getDataDAO().updateDataSets(entities, context.getSession().tryGetPerson());
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.DATA_SET.getLabel(), EntityKind.DATA_SET);
    }

}
