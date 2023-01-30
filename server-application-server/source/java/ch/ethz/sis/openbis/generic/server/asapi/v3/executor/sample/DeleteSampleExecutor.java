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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateJdbcException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class DeleteSampleExecutor extends AbstractDeleteEntityExecutor<IDeletionId, ISampleId, SamplePE, SampleDeletionOptions> implements
        IDeleteSampleExecutor
{

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private ISampleAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, List<? extends ISampleId> entityIds, SampleDeletionOptions deletionOptions)
    {
        return mapSampleByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, ISampleId entityId, SamplePE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, SamplePE sample)
    {
        Session session = context.getSession();
        Date timeStamp = daoFactory.getTransactionTimestamp();
        ExperimentPE experiment = sample.getExperiment();
        if (experiment != null)
        {
            RelationshipUtils.updateModificationDateAndModifier(experiment, session, timeStamp);
        }
        ProjectPE project = sample.getProject();
        if (project != null)
        {
            RelationshipUtils.updateModificationDateAndModifier(project, session, timeStamp);
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

    @Override
    protected IDeletionId delete(IOperationContext context, Collection<SamplePE> entities, SampleDeletionOptions deletionOptions)
    {
        try
        {
            ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());
            trashBO.createDeletion(deletionOptions.getReason());
            trashBO.trashSamples(asTechIds(entities));
            DeletionPE deletion = trashBO.getDeletion();
            return new DeletionTechId(deletion.getId());
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "deletion", null);
            return null; // never called
        }
    }

}
