/*
 * Copyright 2016 ETH Zuerich, CISD
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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractGetEntityTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.IMaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class GetMaterialTypesOperationExecutor extends AbstractGetEntityTypesOperationExecutor<MaterialType, MaterialTypeFetchOptions>
        implements IGetMaterialTypesOperationExecutor
{

    @Autowired
    private IMaterialTypeTranslator translator;

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    protected Class<? extends GetObjectsOperation<IEntityTypeId, MaterialTypeFetchOptions>> getOperationClass()
    {
        return GetMaterialTypesOperation.class;
    }

    @Override
    protected ITranslator<Long, MaterialType, MaterialTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IEntityTypeId, MaterialType> getOperationResult(Map<IEntityTypeId, MaterialType> objectMap)
    {
        return new GetMaterialTypesOperationResult(objectMap);
    }

}
