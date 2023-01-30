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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DeletionValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletionTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class RevertDeletionExecutor implements IRevertDeletionExecutor
{

    @Autowired
    private IMapDeletionByIdExecutor mapDeletionByIdExecutor;

    @Autowired
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IDeletionAuthorizationExecutor authorizationExecutor;

    @Override
    public void revert(IOperationContext context, List<? extends IDeletionId> deletionIds)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (deletionIds == null)
        {
            throw new IllegalArgumentException("Deletion ids cannot be null");
        }

        ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());

        Set<TechId> deletedExperimentIds = new HashSet<TechId>();
        Set<TechId> deletedSampleIds = new HashSet<TechId>();
        Set<String> deletedDataSetCodes = new HashSet<String>();

        for (Deletion deletion : getDeletions(context, deletionIds))
        {
            List<IEntityInformationHolderWithIdentifier> deletedEntities =
                    deletion.getDeletedEntities();
            for (IEntityInformationHolderWithIdentifier deletedEntity : deletedEntities)
            {
                EntityKind entityKind = deletedEntity.getEntityKind();
                TechId entityId = new TechId(deletedEntity.getId());
                switch (entityKind)
                {
                    case EXPERIMENT:
                        deletedExperimentIds.add(entityId);
                        break;
                    case SAMPLE:
                        deletedSampleIds.add(entityId);
                        break;
                    case DATA_SET:
                        deletedDataSetCodes.add(deletedEntity.getCode());
                        break;
                    default:
                }
            }
            trashBO.revertDeletion(new TechId(deletion.getId()));
        }

        updateModificationDateAndModifierOfRelatedProjectsOfExperiments(context, deletedExperimentIds);
        updateModificationDateAndModifierOfRelatedEntitiesOfSamples(context, deletedSampleIds);
        updateModificationDateAndModifierOfRelatedEntitiesOfDataSets(context, deletedDataSetCodes);
    }

    private List<Deletion> getDeletions(IOperationContext context, List<? extends IDeletionId> deletionIds)
    {
        Map<IDeletionId, DeletionPE> map = mapDeletionByIdExecutor.map(context, deletionIds);
        List<Long> deletionTechIds = new LinkedList<Long>();

        try
        {
            authorizationExecutor.canRevert(context, deletionIds);
        } catch (AuthorizationFailureException ex)
        {
            throw new UnauthorizedObjectAccessException(deletionIds);
        }

        for (IDeletionId deletionId : deletionIds)
        {
            DeletionPE deletion = map.get(deletionId);

            if (deletion == null)
            {
                throw new ObjectNotFoundException(deletionId);
            } else
            {
                deletionTechIds.add(deletion.getId());
            }
        }

        IDeletionTable table = businessObjectFactory.createDeletionTable(context.getSession());
        table.load(deletionTechIds, true);
        List<Deletion> deletions = table.getDeletions();

        DeletionValidator validator = new DeletionValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        for (Deletion deletion : deletions)
        {
            if (false == validator.doValidation(context.getSession().tryGetPerson(), deletion))
            {
                throw new UnauthorizedObjectAccessException(new DeletionTechId(deletion.getId()));
            }
        }

        return deletions;
    }

    private void updateModificationDateAndModifierOfRelatedProjectsOfExperiments(IOperationContext context, Collection<TechId> experimentIds)
    {
        List<ExperimentPE> experiments =
                daoFactory.getExperimentDAO().listByIDs(TechId.asLongs(experimentIds));
        Session session = context.getSession();
        Date timeStamp = daoFactory.getTransactionTimestamp();
        for (ExperimentPE experiment : experiments)
        {
            RelationshipUtils.updateModificationDateAndModifier(experiment.getProject(), session, timeStamp);
        }
    }

    private void updateModificationDateAndModifierOfRelatedEntitiesOfSamples(IOperationContext context, Collection<TechId> sampleIds)
    {
        List<SamplePE> samples = daoFactory.getSampleDAO().listByIDs(TechId.asLongs(sampleIds));
        Session session = context.getSession();
        Date timeStamp = daoFactory.getTransactionTimestamp();
        for (SamplePE sample : samples)
        {
            ExperimentPE experiment = sample.getExperiment();
            if (experiment != null)
            {
                RelationshipUtils.updateModificationDateAndModifier(experiment, session, timeStamp);
            }
            SamplePE container = sample.getContainer();
            if (container != null)
            {
                RelationshipUtils.updateModificationDateAndModifier(container, session, timeStamp);
            }
            List<SamplePE> parents = sample.getParents();
            if (parents != null)
            {
                for (SamplePE parent : parents)
                {
                    RelationshipUtils.updateModificationDateAndModifier(parent, session, timeStamp);
                }
            }
            Set<SampleRelationshipPE> childRelationships = sample.getChildRelationships();
            if (childRelationships != null)
            {
                for (SampleRelationshipPE childRelationship : childRelationships)
                {
                    SamplePE childSample = childRelationship.getChildSample();
                    RelationshipUtils.updateModificationDateAndModifier(childSample, session, timeStamp);
                }
            }
        }
    }

    private void updateModificationDateAndModifierOfRelatedEntitiesOfDataSets(IOperationContext context, Collection<String> dataSetCodes)
    {
        List<DataPE> dataSets = daoFactory.getDataDAO().listByCode(new HashSet<String>(dataSetCodes));
        Session session = context.getSession();
        Date timeStamp = daoFactory.getTransactionTimestamp();
        for (DataPE dataSet : dataSets)
        {
            ExperimentPE experiment = dataSet.getExperiment();
            RelationshipUtils.updateModificationDateAndModifier(experiment, session, timeStamp);
            SamplePE sample = dataSet.tryGetSample();
            if (sample != null)
            {
                RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
            }
            updateModificationDateAndModifierOfDataSets(context, dataSet.getChildren());
            updateModificationDateAndModifierOfDataSets(context, dataSet.getParents());
            Set<DataSetRelationshipPE> relationships = dataSet.getParentRelationships();
            for (DataSetRelationshipPE relationship : RelationshipUtils.getContainerComponentRelationships(relationships))
            {
                RelationshipUtils.updateModificationDateAndModifier(relationship.getParentDataSet(), session, timeStamp);
            }
        }
    }

    private void updateModificationDateAndModifierOfDataSets(IOperationContext context, List<DataPE> dataSets)
    {
        if (dataSets != null)
        {
            Session session = context.getSession();
            Date timeStamp = daoFactory.getTransactionTimestamp();
            for (DataPE child : dataSets)
            {
                RelationshipUtils.updateModificationDateAndModifier(child, session, timeStamp);
            }
        }
    }

}
