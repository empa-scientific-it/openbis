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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IMapPropertyTypeByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
@Component
public class SetSemanticAnnotationPropertyTypeExecutor
        extends AbstractSetEntityToOneRelationExecutor<SemanticAnnotationCreation, SemanticAnnotationPE, IPropertyTypeId, PropertyTypePE> implements
        ISetSemanticAnnotationPropertyTypeExecutor
{

    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "semantic-annotation-property-type";
    }

    @Override
    protected IPropertyTypeId getRelatedId(SemanticAnnotationCreation creation)
    {
        return creation.getPropertyTypeId();
    }

    @Override
    protected Map<IPropertyTypeId, PropertyTypePE> map(IOperationContext context, List<IPropertyTypeId> relatedIds)
    {
        return mapPropertyTypeByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, SemanticAnnotationPE entity, IPropertyTypeId relatedId, PropertyTypePE related)
    {
    }

    @Override
    protected void set(IOperationContext context, SemanticAnnotationPE entity, PropertyTypePE related)
    {
        if (related != null)
        {
            entity.setPropertyType(related);
        }
    }

}
