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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.UpdateMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.UpdateMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.UpdateObjectsOperationExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateMaterialTypesOperationExecutor
        extends UpdateObjectsOperationExecutor<MaterialTypeUpdate, IEntityTypeId>
        implements IUpdateMaterialTypesOperationExecutor
{
    private static final List<DataTypeCode> INVALID_TYPES =
            Arrays.asList(DataTypeCode.ARRAY_INTEGER, DataTypeCode.ARRAY_STRING,
                    DataTypeCode.ARRAY_REAL,
                    DataTypeCode.ARRAY_TIMESTAMP, DataTypeCode.JSON);

    @Autowired
    private IUpdateMaterialTypeExecutor executor;

    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExecutor;

    @Override
    protected Class<? extends UpdateObjectsOperation<MaterialTypeUpdate>> getOperationClass()
    {
        return UpdateMaterialTypesOperation.class;
    }

    @Override
    protected UpdateObjectsOperationResult<? extends IEntityTypeId> doExecute(
            IOperationContext context,
            UpdateObjectsOperation<MaterialTypeUpdate> operation)
    {
        if (isValid(context, operation.getUpdates()))
        {
            return new UpdateMaterialTypesOperationResult(
                    executor.update(context, operation.getUpdates()));
        } else
        {
            throw new UserFailureException("Wrong property type has been provided!");
        }
    }

    public boolean isValid(IOperationContext context,
            List<MaterialTypeUpdate> materialTypeUpdates)
    {
        for (MaterialTypeUpdate materialTypeUpdate : materialTypeUpdates)
        {
            if (materialTypeUpdate.getPropertyAssignments() != null)
            {
                for (PropertyAssignmentCreation addedAssignments : materialTypeUpdate.getPropertyAssignments()
                        .getAdded())
                {
                    if (addedAssignments.getPropertyTypeId() != null)
                    {
                        PropertyTypePE type =
                                findPropertyType(context, addedAssignments.getPropertyTypeId());
                        if (type != null && type.getType() != null && INVALID_TYPES.contains(
                                type.getType().getCode()))
                        {
                            return false;
                        }
                    }
                }
                for (PropertyAssignmentCreation setAssignments : materialTypeUpdate.getPropertyAssignments()
                        .getSet())
                {
                    if (setAssignments.getPropertyTypeId() != null)
                    {
                        PropertyTypePE type =
                                findPropertyType(context, setAssignments.getPropertyTypeId());
                        if (type != null && type.getType() != null && INVALID_TYPES.contains(
                                type.getType().getCode()))
                        {
                            return false;
                        }
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
        return propertyTypePE;
    }

}
