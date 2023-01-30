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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.delete.SemanticAnnotationDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteSemanticAnnotationExecutor
        extends AbstractDeleteEntityExecutor<Void, ISemanticAnnotationId, SemanticAnnotationPE, SemanticAnnotationDeletionOptions> implements
        IDeleteSemanticAnnotationExecutor
{

    @Autowired
    private IMapSemanticAnnotationByIdExecutor mapSemanticAnnotationByIdExecutor;

    @Autowired
    private ISemanticAnnotationAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<ISemanticAnnotationId, SemanticAnnotationPE> map(IOperationContext context, List<? extends ISemanticAnnotationId> entityIds,
            SemanticAnnotationDeletionOptions deletionOptions)
    {
        return mapSemanticAnnotationByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, ISemanticAnnotationId entityId, SemanticAnnotationPE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, SemanticAnnotationPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<SemanticAnnotationPE> annotations, SemanticAnnotationDeletionOptions deletionOptions)
    {
        for (SemanticAnnotationPE annotation : annotations)
        {
            daoFactory.getSemanticAnnotationDAO().delete(annotation);
        }

        return null;
    }

}
