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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
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
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleExecutor extends AbstractUpdateEntityExecutor<SampleUpdate, SamplePE, ISampleId, SamplePermId> implements
        IUpdateSampleExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISampleAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private IUpdateSampleSpaceExecutor updateSampleSpaceExecutor;

    @Autowired
    private IUpdateSampleProjectExecutor updateSampleProjectExecutor;

    @Autowired
    private IUpdateSampleExperimentExecutor updateSampleExperimentExecutor;

    @Autowired
    private IUpdateSampleRelatedSamplesExecutor updateSampleRelatedSamplesExecutor;

    @Autowired
    private IUpdateSamplePropertyExecutor updateSamplePropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IUpdateSampleAttachmentExecutor updateSampleAttachmentExecutor;

    @Autowired
    protected IRelationshipService relationshipService;

    @Autowired
    private IEventExecutor eventExecutor;

    @Override
    protected ISampleId getId(SampleUpdate update)
    {
        return update.getSampleId();
    }

    @Override
    protected SamplePermId getPermId(SamplePE entity)
    {
        return new SamplePermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, SampleUpdate update)
    {
        if (update.getSampleId() == null)
        {
            throw new UserFailureException("Sample id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, ISampleId id, SamplePE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<SampleUpdate, SamplePE> batch)
    {
        Collection<SamplePE> experimentOrProjectSamples = new ArrayList<SamplePE>();
        List<FreezingEvent> freezingEvents = new ArrayList<>();

        for (Entry<SampleUpdate, SamplePE> entry : batch.getObjects().entrySet())
        {
            SampleUpdate update = entry.getKey();
            SamplePE entity = entry.getValue();
            FreezingFlags freezingFlags = new FreezingFlags();
            if (entity.getExperiment() != null || entity.getProject() != null)
            {
                experimentOrProjectSamples.add(entity);
            }
            if (update.shouldBeFrozen())
            {
                authorizationExecutor.canFreeze(context, entity);
                entity.setFrozen(true);
                freezingFlags.freeze();
            }
            if (update.shouldBeFrozenForComponents())
            {
                authorizationExecutor.canFreeze(context, entity);
                assertionOfNoDeletedEntityExecutor.assertSampleHasNoDeletedComponents(entity.getPermId());
                entity.setFrozenForComponent(true);
                freezingFlags.freezeForComponents();
            }
            if (update.shouldBeFrozenForChildren())
            {
                authorizationExecutor.canFreeze(context, entity);
                assertionOfNoDeletedEntityExecutor.assertSampleHasNoDeletedChildren(entity.getPermId());
                entity.setFrozenForChildren(true);
                freezingFlags.freezeForChildren();
            }
            if (update.shouldBeFrozenForParents())
            {
                authorizationExecutor.canFreeze(context, entity);
                assertionOfNoDeletedEntityExecutor.assertSampleHasNoDeletedParents(entity.getPermId());
                entity.setFrozenForParents(true);
                freezingFlags.freezeForParents();
            }
            if (update.shouldBeFrozenForDataSets())
            {
                authorizationExecutor.canFreeze(context, entity);
                assertionOfNoDeletedEntityExecutor.assertSampleHasNoDeletedDataSets(entity.getPermId());
                entity.setFrozenForDataSet(true);
                freezingFlags.freezeForDataSets();
            }
            if (freezingFlags.noFlags() == false)
            {
                freezingEvents.add(new FreezingEvent(entity.getIdentifier(), EventPE.EntityType.SAMPLE, freezingFlags));
            }
        }
        if (freezingEvents.isEmpty() == false)
        {
            eventExecutor.persist(context, freezingEvents);
        }

        updateSampleSpaceExecutor.update(context, batch);
        updateSampleExperimentExecutor.update(context, batch);
        updateSampleProjectExecutor.update(context, batch);
        updateSamplePropertyExecutor.update(context, batch);
        updateTags(context, batch);
        updateAttachments(context, batch);

        for (SamplePE entity : experimentOrProjectSamples)
        {
            if (entity.getExperiment() == null && entity.getProject() == null)
            {
                relationshipService.assignSampleToSpace(context.getSession(), entity, entity.getSpace());
            }
        }

        PersonPE person = context.getSession().tryGetPerson();
        Date timeStamp = daoFactory.getTransactionTimestamp();

        for (SamplePE entity : batch.getObjects().values())
        {
            RelationshipUtils.updateModificationDateAndModifier(entity, person, timeStamp);
        }
    }

    private void updateTags(final IOperationContext context, final MapBatch<SampleUpdate, SamplePE> batch)
    {
        new MapBatchProcessor<SampleUpdate, SamplePE>(context, batch)
        {
            @Override
            public void process(SampleUpdate update, SamplePE entity)
            {
                if (update.getTagIds() != null && update.getTagIds().hasActions())
                {
                    updateTagForEntityExecutor.update(context, entity, update.getTagIds());
                }
            }

            @Override
            public IProgress createProgress(SampleUpdate update, SamplePE entity, int objectIndex, int totalObjectCount)
            {
                return new UpdateRelationProgress(update, entity, "sample-tag", objectIndex, totalObjectCount);
            }
        };
    }

    private void updateAttachments(final IOperationContext context, final MapBatch<SampleUpdate, SamplePE> batch)
    {
        new MapBatchProcessor<SampleUpdate, SamplePE>(context, batch)
        {
            @Override
            public void process(SampleUpdate update, SamplePE entity)
            {
                if (update.getAttachments() != null && update.getAttachments().hasActions())
                {
                    updateSampleAttachmentExecutor.update(context, entity, update.getAttachments());
                }
            }

            @Override
            public IProgress createProgress(SampleUpdate update, SamplePE entity, int objectIndex, int totalObjectCount)
            {
                return new UpdateRelationProgress(update, entity, "sample-attachment", objectIndex, totalObjectCount);
            }
        };
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<SampleUpdate, SamplePE> batch)
    {
        updateSampleRelatedSamplesExecutor.update(context, batch);
    }

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, Collection<ISampleId> ids)
    {
        return mapSampleByIdExecutor.map(context, ids);
    }

    @Override
    protected List<SamplePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getSampleDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<SamplePE> entities, boolean clearCache)
    {
        daoFactory.getSampleDAO().createOrUpdateSamples(entities, context.getSession().tryGetPerson(), clearCache);
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.SAMPLE.getLabel(), EntityKind.SAMPLE);
    }

}
