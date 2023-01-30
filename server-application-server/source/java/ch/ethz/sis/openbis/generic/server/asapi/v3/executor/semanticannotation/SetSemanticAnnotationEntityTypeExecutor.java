/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
@Component
public class SetSemanticAnnotationEntityTypeExecutor
        extends AbstractSetEntityToOneRelationExecutor<SemanticAnnotationCreation, SemanticAnnotationPE, IEntityTypeId, EntityTypePE> implements
        ISetSemanticAnnotationEntityTypeExecutor
{

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "semantic-annotation-entity-type";
    }

    @Override
    protected IEntityTypeId getRelatedId(SemanticAnnotationCreation creation)
    {
        return creation.getEntityTypeId();
    }

    @Override
    protected Map<IEntityTypeId, EntityTypePE> map(IOperationContext context, List<IEntityTypeId> relatedIds)
    {
        return mapEntityTypeByIdExecutor.map(context, null, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, SemanticAnnotationPE entity, IEntityTypeId relatedId, EntityTypePE related)
    {
    }

    @Override
    protected void set(IOperationContext context, SemanticAnnotationPE entity, EntityTypePE related)
    {
        if (related != null)
        {
            if (related instanceof SampleTypePE)
            {
                entity.setSampleType((SampleTypePE) related);
            } else
            {
                throw new UserFailureException("Semantic annotations can be defined for sample entity types only.");
            }
        }
    }

}
