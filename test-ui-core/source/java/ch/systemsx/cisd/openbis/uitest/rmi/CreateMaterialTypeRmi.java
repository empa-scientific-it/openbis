/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.MaterialType;

/**
 * @author anttil
 */
public class CreateMaterialTypeRmi implements Command<MaterialType>
{

    @Inject
    private String session;

    @Inject
    private ICommonServer commonServer;

    private MaterialType type;

    public CreateMaterialTypeRmi(MaterialType type)
    {
        this.type = type;
    }

    @Override
    public MaterialType execute()
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType materialType =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType();
        materialType.setCode(type.getCode());
        materialType.setDescription("");
        materialType.setMaterialTypePropertyTypes(new ArrayList<MaterialTypePropertyType>());
        commonServer.registerMaterialType(session, materialType);
        return type;
    }
}
