/*
 * Copyright 2014 ETH Zuerich, CISD
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IVerifyEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author Jakub Straszewski
 */
@Component
public class VerifyMaterialExecutor implements IVerifyMaterialExecutor
{

    @Autowired
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Autowired
    private IVerifyEntityPropertyExecutor verifyEntityPropertyExecutor;

    @Override
    public void verify(IOperationContext context, CollectionBatch<? extends IMaterialId> materialIds)
    {
        if (materialIds != null && false == materialIds.isEmpty())
        {
            Map<IMaterialId, MaterialPE> map = mapMaterialByIdExecutor.map(context, materialIds.getObjects());

            CollectionBatch<MaterialPE> materials =
                    new CollectionBatch<MaterialPE>(materialIds.getBatchIndex(), materialIds.getFromObjectIndex(),
                            materialIds.getToObjectIndex(), map.values(), materialIds.getTotalObjectCount());

            verifyEntityPropertyExecutor.verify(context, materials);
        }
    }

}
