/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.IMaterialTranslator;

/**
 * @author pkupczyk
 */
@Component
public class GetMaterialsOperationExecutor extends GetObjectsOperationExecutor<IMaterialId, Material, MaterialFetchOptions>
        implements IGetMaterialsOperationExecutor
{

    @Autowired
    private IMapMaterialTechIdByIdExecutor mapExecutor;

    @Autowired
    private IMaterialTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<IMaterialId, MaterialFetchOptions>> getOperationClass()
    {
        return GetMaterialsOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<IMaterialId, Long> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Material, MaterialFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IMaterialId, Material> getOperationResult(Map<IMaterialId, Material> objectMap)
    {
        return new GetMaterialsOperationResult(objectMap);
    }

}
