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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import java.util.*;
import java.util.Map.Entry;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.metadata.IUpdateMetaDataForEntityExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateExperimentExecutor extends
        AbstractUpdateEntityExecutor<ExperimentUpdate, ExperimentPE, IExperimentId, ExperimentPermId>
        implements
        IUpdateExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IExperimentAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IUpdateExperimentProjectExecutor updateExperimentProjectExecutor;

    @Autowired
    private IUpdateExperimentPropertyExecutor updateExperimentPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IUpdateExperimentAttachmentExecutor updateExperimentAttachmentExecutor;

    @Autowired
    private IEventExecutor eventExecutor;

    @Autowired
    private IUpdateMetaDataForEntityExecutor<ExperimentUpdate, ExperimentPE>
            updateMetaDataForEntityExecutor;

    @Override
    protected IExperimentId getId(ExperimentUpdate update)
    {
        return update.getExperimentId();
    }

    @Override
    protected ExperimentPermId getPermId(ExperimentPE entity)
    {
        return new ExperimentPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, ExperimentUpdate update)
    {
        if (update.getExperimentId() == null)
        {
            throw new UserFailureException("Experiment id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IExperimentId id, ExperimentPE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(final IOperationContext context,
            final MapBatch<ExperimentUpdate, ExperimentPE> batch)
    {
        updateExperimentProjectExecutor.update(context, batch);
        updateExperimentPropertyExecutor.update(context, batch);
        updateTags(context, batch);
        updateAttachments(context, batch);
        updateMetaDataForEntityExecutor.update(context, batch);

        PersonPE person = context.getSession().tryGetPerson();
        Date timeStamp = daoFactory.getTransactionTimestamp();
        List<FreezingEvent> freezingEvents = new ArrayList<>();

        for (Entry<ExperimentUpdate, ExperimentPE> entry : batch.getObjects().entrySet())
        {
            ExperimentUpdate update = entry.getKey();
            ExperimentPE experiment = entry.getValue();
            FreezingFlags freezingFlags = new FreezingFlags();
            RelationshipUtils.updateModificationDateAndModifier(experiment, person, timeStamp);
            if (update.shouldBeFrozen())
            {
                authorizationExecutor.canFreeze(context, experiment);
                experiment.setFrozen(true);
                freezingFlags.freeze();
            }
            if (update.shouldBeFrozenForDataSets())
            {
                authorizationExecutor.canFreeze(context, experiment);
                assertionOfNoDeletedEntityExecutor.assertExperimentHasNoDeletedDataSets(
                        experiment.getPermId());
                experiment.setFrozenForDataSet(true);
                freezingFlags.freezeForDataSets();
            }
            if (update.shouldBeFrozenForSamples())
            {
                authorizationExecutor.canFreeze(context, experiment);
                assertionOfNoDeletedEntityExecutor.assertExperimentHasNoDeletedSamples(
                        experiment.getPermId());
                experiment.setFrozenForSample(true);
                freezingFlags.freezeForSamples();
            }
            if (freezingFlags.noFlags() == false)
            {
                freezingEvents.add(
                        new FreezingEvent(experiment.getIdentifier(), EventPE.EntityType.EXPERIMENT,
                                freezingFlags));
            }
        }
        if (freezingEvents.isEmpty() == false)
        {
            eventExecutor.persist(context, freezingEvents);
        }
    }

    private void updateTags(final IOperationContext context,
            final MapBatch<ExperimentUpdate, ExperimentPE> batch)
    {
        new MapBatchProcessor<ExperimentUpdate, ExperimentPE>(context, batch)
        {
            @Override
            public void process(ExperimentUpdate update, ExperimentPE entity)
            {
                if (update.getTagIds() != null && update.getTagIds().hasActions())
                {
                    updateTagForEntityExecutor.update(context, entity, update.getTagIds());
                }
            }

            @Override
            public IProgress createProgress(ExperimentUpdate update, ExperimentPE entity,
                    int objectIndex, int totalObjectCount)
            {
                return new UpdateRelationProgress(update, entity, "experiment-tag", objectIndex,
                        totalObjectCount);
            }
        };
    }

    private void updateAttachments(final IOperationContext context,
            final MapBatch<ExperimentUpdate, ExperimentPE> batch)
    {
        new MapBatchProcessor<ExperimentUpdate, ExperimentPE>(context, batch)
        {
            @Override
            public void process(ExperimentUpdate update, ExperimentPE entity)
            {
                if (update.getAttachments() != null && update.getAttachments().hasActions())
                {
                    updateExperimentAttachmentExecutor.update(context, entity,
                            update.getAttachments());
                }
            }

            @Override
            public IProgress createProgress(ExperimentUpdate update, ExperimentPE entity,
                    int objectIndex, int totalObjectCount)
            {
                return new UpdateRelationProgress(update, entity, "experiment-attachment",
                        objectIndex, totalObjectCount);
            }
        };
    }

    @Override
    protected void updateAll(IOperationContext context,
            MapBatch<ExperimentUpdate, ExperimentPE> batch)
    {
    }

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context,
            Collection<IExperimentId> ids)
    {
        return mapExperimentByIdExecutor.map(context, ids);
    }

    @Override
    protected List<ExperimentPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getExperimentDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<ExperimentPE> entities, boolean clearCache)
    {
        daoFactory.getExperimentDAO()
                .createOrUpdateExperiments(entities, context.getSession().tryGetPerson(),
                        clearCache);
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.EXPERIMENT.getLabel(),
                EntityKind.EXPERIMENT);
    }

}
