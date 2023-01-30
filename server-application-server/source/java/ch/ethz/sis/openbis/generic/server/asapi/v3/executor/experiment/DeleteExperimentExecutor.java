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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class DeleteExperimentExecutor extends AbstractDeleteEntityExecutor<IDeletionId, IExperimentId, ExperimentPE, ExperimentDeletionOptions>
        implements IDeleteExperimentExecutor
{

    @Autowired
    private IExperimentAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context, List<? extends IExperimentId> entityIds,
            ExperimentDeletionOptions deletionOptions)
    {
        return mapExperimentByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IExperimentId entityId, ExperimentPE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, ExperimentPE entity)
    {
        Session session = context.getSession();
        Date timeStamp = daoFactory.getTransactionTimestamp();
        RelationshipUtils.updateModificationDateAndModifier(entity.getProject(), session, timeStamp);
    }

    @Override
    protected IDeletionId delete(IOperationContext context, Collection<ExperimentPE> entities, ExperimentDeletionOptions deletionOptions)
    {
        try
        {
            ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());
            trashBO.createDeletion(deletionOptions.getReason());
            trashBO.trashExperiments(asTechIds(entities));
            DeletionPE deletion = trashBO.getDeletion();
            return new DeletionTechId(deletion.getId());
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "deletion", null);
            return null; // never called
        }
    }

}
