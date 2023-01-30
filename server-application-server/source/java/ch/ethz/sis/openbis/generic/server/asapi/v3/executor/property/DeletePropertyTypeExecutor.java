/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class DeletePropertyTypeExecutor
        extends AbstractDeleteEntityExecutor<Void, IPropertyTypeId, PropertyTypePE, PropertyTypeDeletionOptions>
        implements IDeletePropertyTypeExecutor
{
    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IPropertyTypeAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExeceutor;

    @Override
    protected Map<IPropertyTypeId, PropertyTypePE> map(IOperationContext context, List<? extends IPropertyTypeId> entityIds,
            PropertyTypeDeletionOptions deletionOptions)
    {
        return mapPropertyTypeByIdExeceutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IPropertyTypeId entityId, PropertyTypePE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, PropertyTypePE entity)
    {
    }

    @Override
    protected Void delete(IOperationContext context, Collection<PropertyTypePE> entities, PropertyTypeDeletionOptions deletionOptions)
    {
        IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(context.getSession());
        String reason = deletionOptions.getReason();
        for (PropertyTypePE propertyType : entities)
        {
            propertyTypeBO.deleteByTechId(new TechId(propertyType.getId()), reason);
        }
        return null;
    }

}
