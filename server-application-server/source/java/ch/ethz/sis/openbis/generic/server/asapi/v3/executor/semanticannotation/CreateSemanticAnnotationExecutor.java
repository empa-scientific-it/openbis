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
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
@Component
public class CreateSemanticAnnotationExecutor
        extends AbstractCreateEntityExecutor<SemanticAnnotationCreation, SemanticAnnotationPE, SemanticAnnotationPermId> implements
        ICreateSemanticAnnotationExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetSemanticAnnotationEntityTypeExecutor setEntityTypeExecutor;

    @Autowired
    private ISetSemanticAnnotationPropertyTypeExecutor setPropertyTypeExecutor;

    @Autowired
    private ISetSemanticAnnotationPropertyAssignmentExecutor setPropertyAssignmentExecutor;

    @Autowired
    private ISemanticAnnotationAuthorizationExecutor authorizationExecutor;

    @Override
    protected List<SemanticAnnotationPE> createEntities(IOperationContext context, CollectionBatch<SemanticAnnotationCreation> batch)
    {
        final List<SemanticAnnotationPE> annotations = new LinkedList<SemanticAnnotationPE>();

        new CollectionBatchProcessor<SemanticAnnotationCreation>(context, batch)
            {
                @Override
                public void process(SemanticAnnotationCreation object)
                {
                    SemanticAnnotationPE annotation = new SemanticAnnotationPE();
                    String createdPermId = daoFactory.getPermIdDAO().createPermId();
                    annotation.setPermId(createdPermId);
                    annotation.setPredicateOntologyId(object.getPredicateOntologyId());
                    annotation.setPredicateOntologyVersion(object.getPredicateOntologyVersion());
                    annotation.setPredicateAccessionId(object.getPredicateAccessionId());
                    annotation.setDescriptorOntologyId(object.getDescriptorOntologyId());
                    annotation.setDescriptorOntologyVersion(object.getDescriptorOntologyVersion());
                    annotation.setDescriptorAccessionId(object.getDescriptorAccessionId());
                    annotations.add(annotation);
                }

                @Override
                public IProgress createProgress(SemanticAnnotationCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return annotations;
    }

    @Override
    protected SemanticAnnotationPermId createPermId(IOperationContext context, SemanticAnnotationPE entity)
    {
        return new SemanticAnnotationPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, SemanticAnnotationCreation creation)
    {
        // data is checked in SemanticAnnotationDAO
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
    }

    @Override
    protected void checkAccess(IOperationContext context, SemanticAnnotationPE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<SemanticAnnotationCreation, SemanticAnnotationPE> batch)
    {
        setEntityTypeExecutor.set(context, batch);
        setPropertyTypeExecutor.set(context, batch);
        setPropertyAssignmentExecutor.set(context, batch);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<SemanticAnnotationCreation, SemanticAnnotationPE> batch)
    {
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

    @Override
    protected IObjectId getId(SemanticAnnotationPE entity)
    {
        return new SemanticAnnotationPermId(entity.getPermId());
    }

}
