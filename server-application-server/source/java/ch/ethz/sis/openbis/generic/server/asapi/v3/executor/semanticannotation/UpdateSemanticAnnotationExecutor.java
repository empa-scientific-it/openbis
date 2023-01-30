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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update.SemanticAnnotationUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSemanticAnnotationExecutor extends
        AbstractUpdateEntityExecutor<SemanticAnnotationUpdate, SemanticAnnotationPE, ISemanticAnnotationId, SemanticAnnotationPermId> implements
        IUpdateSemanticAnnotationExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapSemanticAnnotationByIdExecutor mapSemanticAnnotationByIdExecutor;

    @Autowired
    private ISemanticAnnotationAuthorizationExecutor authorizationExecutor;

    @Override
    protected ISemanticAnnotationId getId(SemanticAnnotationUpdate update)
    {
        return update.getSemanticAnnotationId();
    }

    @Override
    protected SemanticAnnotationPermId getPermId(SemanticAnnotationPE entity)
    {
        return new SemanticAnnotationPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, SemanticAnnotationUpdate update)
    {
        if (update.getSemanticAnnotationId() == null)
        {
            throw new UserFailureException("Semantic annotation id cannot be null.");
        }

        // data is checked in SemanticAnnotationDAO
    }

    @Override
    protected void checkAccess(IOperationContext context, ISemanticAnnotationId id, SemanticAnnotationPE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<SemanticAnnotationUpdate, SemanticAnnotationPE> batch)
    {
        for (Map.Entry<SemanticAnnotationUpdate, SemanticAnnotationPE> entry : batch.getObjects().entrySet())
        {
            SemanticAnnotationUpdate update = entry.getKey();
            SemanticAnnotationPE semanticAnnotation = entry.getValue();

            if (update.getPredicateOntologyId() != null && update.getPredicateOntologyId().isModified())
            {
                semanticAnnotation.setPredicateOntologyId(update.getPredicateOntologyId().getValue());
            }
            if (update.getPredicateOntologyVersion() != null && update.getPredicateOntologyVersion().isModified())
            {
                semanticAnnotation.setPredicateOntologyVersion(update.getPredicateOntologyVersion().getValue());
            }
            if (update.getPredicateAccessionId() != null && update.getPredicateAccessionId().isModified())
            {
                semanticAnnotation.setPredicateAccessionId(update.getPredicateAccessionId().getValue());
            }
            if (update.getDescriptorOntologyId() != null && update.getDescriptorOntologyId().isModified())
            {
                semanticAnnotation.setDescriptorOntologyId(update.getDescriptorOntologyId().getValue());
            }
            if (update.getDescriptorOntologyVersion() != null && update.getDescriptorOntologyVersion().isModified())
            {
                semanticAnnotation.setDescriptorOntologyVersion(update.getDescriptorOntologyVersion().getValue());
            }
            if (update.getDescriptorAccessionId() != null && update.getDescriptorAccessionId().isModified())
            {
                semanticAnnotation.setDescriptorAccessionId(update.getDescriptorAccessionId().getValue());
            }
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<SemanticAnnotationUpdate, SemanticAnnotationPE> batch)
    {
        // nothing to do
    }

    @Override
    protected Map<ISemanticAnnotationId, SemanticAnnotationPE> map(IOperationContext context, Collection<ISemanticAnnotationId> ids)
    {
        return mapSemanticAnnotationByIdExecutor.map(context, ids);
    }

    @Override
    protected List<SemanticAnnotationPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getSemanticAnnotationDAO().findByIds(ids);
    }

    @Override
    protected void save(IOperationContext context, List<SemanticAnnotationPE> entities, boolean clearCache)
    {
        for (SemanticAnnotationPE entity : entities)
        {
            daoFactory.getSemanticAnnotationDAO().createOrUpdate(entity);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "semantic annotation", null);
    }

}
