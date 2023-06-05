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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateExperimentExecutor extends AbstractCreateEntityExecutor<ExperimentCreation, ExperimentPE, ExperimentPermId> implements
        ICreateExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IExperimentAuthorizationExecutor authorizationExecutor;

    @Autowired
    private ISetExperimentTypeExecutor setExperimentTypeExecutor;

    @Autowired
    private ISetExperimentProjectExecutor setExperimentProjectExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Override
    protected List<ExperimentPE> createEntities(IOperationContext context, CollectionBatch<ExperimentCreation> batch)
    {
        final List<ExperimentPE> experiments = new LinkedList<ExperimentPE>();
        final PersonPE person = context.getSession().tryGetPerson();
        final Date timeStamp = daoFactory.getTransactionTimestamp();

        new CollectionBatchProcessor<ExperimentCreation>(context, batch)
            {
                @Override
                public void process(ExperimentCreation object)
                {
                    ExperimentPE experiment = new ExperimentPE();
                    experiment.setCode(object.getCode());
                    String createdPermId = daoFactory.getPermIdDAO().createPermId();
                    experiment.setPermId(createdPermId);
                    experiment.setRegistrator(person);
                    RelationshipUtils.updateModificationDateAndModifier(experiment, person, timeStamp);
                    experiment.setMetaData(object.getMetaData());
                    experiments.add(experiment);
                }

                @Override
                public IProgress createProgress(ExperimentCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return experiments;
    }

    @Override
    protected ExperimentPermId createPermId(IOperationContext context, ExperimentPE entity)
    {
        return new ExperimentPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, ExperimentCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }

        ExperimentIdentifierFactory.assertValidCode(creation.getCode());
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {

    }

    @Override
    protected void checkAccess(IOperationContext context, ExperimentPE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<ExperimentCreation, ExperimentPE> batch)
    {
        setExperimentProjectExecutor.set(context, batch);
        setExperimentTypeExecutor.set(context, batch);
        updateEntityPropertyExecutor.update(context, batch);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<ExperimentCreation, ExperimentPE> batch)
    {
        Map<AttachmentHolderPE, Collection<? extends AttachmentCreation>> attachmentMap =
                new HashMap<AttachmentHolderPE, Collection<? extends AttachmentCreation>>();
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<ExperimentCreation, ExperimentPE> entry : batch.getObjects().entrySet())
        {
            ExperimentCreation creation = entry.getKey();
            ExperimentPE entity = entry.getValue();
            attachmentMap.put(entity, creation.getAttachments());
            tagMap.put(entity, creation.getTagIds());
        }

        createAttachmentExecutor.create(context, attachmentMap);
        addTagToEntityExecutor.add(context, tagMap);
    }

    @Override
    protected List<ExperimentPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getExperimentDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<ExperimentPE> entities, boolean clearCache)
    {
        daoFactory.getExperimentDAO().createOrUpdateExperiments(entities, context.getSession().tryGetPerson(), clearCache);
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.EXPERIMENT.getLabel(), EntityKind.EXPERIMENT);
    }

    @Override
    protected IObjectId getId(ExperimentPE entity)
    {
        return new ExperimentIdentifier(entity.getIdentifier());
    }

}
