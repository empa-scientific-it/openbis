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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IMapMaterialTechIdByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.material.IMaterialTranslator;

/**
 * @author pkupczyk
 */
@Component
public class GetMaterialMethodExecutor extends AbstractGetMethodExecutor<IMaterialId, Long, Material, MaterialFetchOptions> implements
        IGetMaterialMethodExecutor
{

    @Autowired
    private IMapMaterialTechIdByIdExecutor mapExecutor;

    @Autowired
    private IMaterialTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IMaterialId, Long> getMapExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Material, MaterialFetchOptions> getTranslator()
    {
        return translator;
    }

}