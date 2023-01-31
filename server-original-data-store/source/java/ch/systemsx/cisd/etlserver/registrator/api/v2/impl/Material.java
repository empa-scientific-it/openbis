/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.ArrayList;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Kaloyan Enimanev
 */
public class Material extends MaterialImmutable implements IMaterial
{

    private static ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material buildMaterialWithCodeAndType(
            String code, String type)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material();
        material.setCode(code);
        MaterialType materialType = new MaterialType();
        materialType.setCode(type);
        material.setMaterialType(materialType);
        material.setProperties(new ArrayList<IEntityProperty>());

        return material;
    }

    public Material(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material)
    {
        super(material);
    }

    public Material(String code, String type)
    {
        super(buildMaterialWithCodeAndType(code, type), false);
    }

    @Override
    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        EntityHelper.createOrUpdateProperty(getMaterial(), propertyCode, propertyValue);
    }
}
