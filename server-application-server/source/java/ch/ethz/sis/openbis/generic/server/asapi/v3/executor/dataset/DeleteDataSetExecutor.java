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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class DeleteDataSetExecutor extends AbstractDeleteEntityExecutor<IDeletionId, IDataSetId, DataPE, DataSetDeletionOptions> implements
        IDeleteDataSetExecutor
{

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IDataSetAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<IDataSetId, DataPE> map(IOperationContext context, List<? extends IDataSetId> entityIds, DataSetDeletionOptions deletionOptions)
    {
        return mapDataSetByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IDataSetId entityId, DataPE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, DataPE dataSet)
    {
        Date timeStamp = daoFactory.getTransactionTimestamp();
        Session session = context.getSession();
        RelationshipUtils.updateModificationDateAndModifier(dataSet.getExperiment(), session, timeStamp);
        RelationshipUtils.updateModificationDateAndModifier(dataSet.tryGetSample(), session, timeStamp);
        updateModificationDateAndModifierOfRelatedDataSets(context, dataSet.getChildren());
        updateModificationDateAndModifierOfRelatedDataSets(context, dataSet.getParents());
        Set<DataSetRelationshipPE> relationships = dataSet.getParentRelationships();
        for (DataSetRelationshipPE relationship : RelationshipUtils.getContainerComponentRelationships(relationships))
        {
            RelationshipUtils.updateModificationDateAndModifier(relationship.getParentDataSet(), session, timeStamp);
        }
    }

    private void updateModificationDateAndModifierOfRelatedDataSets(IOperationContext context, List<DataPE> dataSets)
    {
        if (dataSets != null)
        {
            Date timeStamp = daoFactory.getTransactionTimestamp();
            for (DataPE child : dataSets)
            {
                RelationshipUtils.updateModificationDateAndModifier(child, context.getSession(), timeStamp);
            }
        }
    }

    @Override
    protected IDeletionId delete(IOperationContext context, Collection<DataPE> dataSets, DataSetDeletionOptions deletionOptions)
    {
        try
        {
            ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());
            trashBO.createDeletion(deletionOptions.getReason());
            trashBO.trashDataSets(asTechIds(dataSets));
            DeletionPE deletion = trashBO.getDeletion();
            return new DeletionTechId(deletion.getId());
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "deletion", null);
            return null; // never called
        }
    }

}
