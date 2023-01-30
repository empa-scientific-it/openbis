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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;

/**
 * Builder class of {@link MaterialType} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class MaterialTypeBuilder extends AbstractEntityTypeBuilder<MaterialType>
{
    private MaterialType materialType = new MaterialType();

    public MaterialTypeBuilder()
    {
        materialType.setMaterialTypePropertyTypes(new ArrayList<MaterialTypePropertyType>());
    }

    public MaterialType getMaterialType()
    {
        return materialType;
    }

    public MaterialTypeBuilder code(String code)
    {
        materialType.setCode(code);
        return this;
    }

    public MaterialTypeBuilder propertyType(String code, String label, DataTypeCode dataType)
    {
        MaterialTypePropertyType entityTypePropertyType = new MaterialTypePropertyType();
        List<MaterialTypePropertyType> types = materialType.getAssignedPropertyTypes();
        fillEntityTypePropertyType(materialType, entityTypePropertyType, code, label, dataType);
        types.add(entityTypePropertyType);
        return this;
    }
}
