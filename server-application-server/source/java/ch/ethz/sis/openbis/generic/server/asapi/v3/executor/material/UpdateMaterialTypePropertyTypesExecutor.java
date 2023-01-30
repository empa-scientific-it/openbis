/*
 * Copyright 2017 ETH Zuerich, SIS
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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityTypePropertyTypesExecutor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateMaterialTypePropertyTypesExecutor
        extends AbstractUpdateEntityTypePropertyTypesExecutor<MaterialTypeUpdate, MaterialTypePE, MaterialTypePropertyTypePE>
        implements IUpdateMaterialTypePropertyTypesExecutor
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }
}
