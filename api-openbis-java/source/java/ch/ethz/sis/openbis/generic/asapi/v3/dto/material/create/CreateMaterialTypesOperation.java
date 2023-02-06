/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.material.create.CreateMaterialTypesOperation")
public class CreateMaterialTypesOperation extends CreateObjectsOperation<MaterialTypeCreation>
{
    private static final long serialVersionUID = 1L;

    private CreateMaterialTypesOperation()
    {
    }

    public CreateMaterialTypesOperation(MaterialTypeCreation... creations)
    {
        super(creations);
    }

    public CreateMaterialTypesOperation(List<MaterialTypeCreation> creations)
    {
        super(creations);
    }

}
