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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author pkupczyk
 */
@Component
public class MapPropertyAssignmentByIdExecutor implements IMapPropertyAssignmentByIdExecutor
{

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExecutor;

    @Override
    public Map<IPropertyAssignmentId, EntityTypePropertyTypePE> map(IOperationContext context, Collection<? extends IPropertyAssignmentId> ids)
    {
        return map(context, ids, true);
    }

    @Override
    public Map<IPropertyAssignmentId, EntityTypePropertyTypePE> map(IOperationContext context, Collection<? extends IPropertyAssignmentId> ids,
            boolean checkAccess)
    {
        Collection<IEntityTypeId> entityTypeIds = new HashSet<IEntityTypeId>();
        Collection<IPropertyTypeId> propertyTypeIds = new HashSet<IPropertyTypeId>();

        for (IPropertyAssignmentId id : ids)
        {
            if (id instanceof PropertyAssignmentPermId)
            {
                PropertyAssignmentPermId permId = (PropertyAssignmentPermId) id;
                entityTypeIds.add(permId.getEntityTypeId());
                propertyTypeIds.add(permId.getPropertyTypeId());
            } else
            {
                throw new UnsupportedObjectIdException(id);
            }
        }

        Map<IEntityTypeId, EntityTypePE> entityTypes = mapEntityTypeByIdExecutor.map(context, null, entityTypeIds);
        Map<IPropertyTypeId, PropertyTypePE> propertyTypes = mapPropertyTypeByIdExecutor.map(context, propertyTypeIds, checkAccess);
        Map<IPropertyAssignmentId, EntityTypePropertyTypePE> propertyAssignments = new HashMap<IPropertyAssignmentId, EntityTypePropertyTypePE>();

        for (IPropertyAssignmentId id : ids)
        {
            PropertyAssignmentPermId permId = (PropertyAssignmentPermId) id;
            EntityTypePE entityType = entityTypes.get(permId.getEntityTypeId());
            PropertyTypePE propertyType = propertyTypes.get(permId.getPropertyTypeId());

            if (entityType != null && propertyType != null)
            {
                for (EntityTypePropertyTypePE propertyAssignment : entityType.getEntityTypePropertyTypes())
                {
                    if (propertyType.equals(propertyAssignment.getPropertyType()))
                    {
                        propertyAssignments.put(id, propertyAssignment);
                    }
                }
            }
        }

        return propertyAssignments;
    }

}
