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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.get.GetPropertyTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.get.GetPropertyTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.IPropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class GetPropertyTypesOperationExecutor
        extends GetObjectsPEOperationExecutor<IPropertyTypeId, PropertyTypePE, PropertyType, PropertyTypeFetchOptions>
        implements IGetPropertyTypesOperationExecutor
{
    @Autowired
    private IMapPropertyTypeByIdExecutor mapExecutor;
    
    @Autowired
    private IPropertyTypeTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IPropertyTypeId, PropertyTypePE> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, PropertyType, PropertyTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IPropertyTypeId, PropertyType> getOperationResult(Map<IPropertyTypeId, PropertyType> objectMap)
    {
        return new GetPropertyTypesOperationResult(objectMap);
    }

    @Override
    protected Class<? extends GetObjectsOperation<IPropertyTypeId, PropertyTypeFetchOptions>> getOperationClass()
    {
        return GetPropertyTypesOperation.class;
    }

}
