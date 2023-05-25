/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IMapPropertyTypeByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author pkupczyk
 */
@Component
public class CreateMaterialTypesOperationExecutor
        extends CreateObjectsOperationExecutor<MaterialTypeCreation, EntityTypePermId> implements
        ICreateMaterialTypesOperationExecutor
{

    private static final List<DataTypeCode> INVALID_TYPES =
            Arrays.asList(DataTypeCode.ARRAY_INTEGER, DataTypeCode.ARRAY_STRING,
                    DataTypeCode.ARRAY_REAL,
                    DataTypeCode.ARRAY_TIMESTAMP, DataTypeCode.JSON);

    @Autowired
    private ICreateMaterialTypeExecutor executor;

    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExecutor;

    @Override
    protected Class<? extends CreateObjectsOperation<MaterialTypeCreation>> getOperationClass()
    {
        return CreateMaterialTypesOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<EntityTypePermId> doExecute(IOperationContext context,
            CreateObjectsOperation<MaterialTypeCreation> operation)
    {
        if (isValid(context, operation.getCreations()))
        {
            return new CreateMaterialTypesOperationResult(
                    executor.create(context, operation.getCreations()));
        } else
        {
            throw new UserFailureException("Wrong property type has been provided!");
        }
    }

    public boolean isValid(IOperationContext context,
            List<MaterialTypeCreation> materialTypeCreations)
    {
        for (MaterialTypeCreation materialTypeCreation : materialTypeCreations)
        {
            if (materialTypeCreation.getPropertyAssignments() != null)
            {
                for (PropertyAssignmentCreation propertyAssignmentCreation : materialTypeCreation.getPropertyAssignments())
                {
                    PropertyTypePE type =
                            findPropertyType(context,
                                    propertyAssignmentCreation.getPropertyTypeId());
                    if (type.getType() != null && INVALID_TYPES.contains(type.getType().getCode()))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private PropertyTypePE findPropertyType(IOperationContext context,
            IPropertyTypeId propertyTypeId)
    {
        Map<IPropertyTypeId, PropertyTypePE> propertyTypePEMap =
                mapPropertyTypeByIdExecutor.map(context, Arrays.asList(propertyTypeId));
        PropertyTypePE propertyTypePE = propertyTypePEMap.get(propertyTypeId);

        if (propertyTypePE == null)
        {
            throw new ObjectNotFoundException(propertyTypeId);
        }
        return propertyTypePE;
    }

}
