/*
 * Copyright 2010 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * Builder of a {@link Material} instance.
 * 
 * @author Franz-Josef Elmer
 */
public class MaterialBuilder
{
    private final Material material = new Material();

    public MaterialBuilder()
    {
        material.setProperties(new ArrayList<IEntityProperty>());
    }

    public final Material getMaterial()
    {
        return material;
    }

    public MaterialBuilder id(Long id)
    {
        material.setId(id);
        return this;
    }

    public MaterialBuilder code(String code)
    {
        material.setCode(code);
        return this;
    }

    public MaterialBuilder type(String type)
    {
        MaterialType materialType = new MaterialType();
        materialType.setCode(type);
        material.setMaterialType(materialType);
        return this;
    }

    public PropertyBuilder property(String key)
    {
        List<IEntityProperty> properties = material.getProperties();
        PropertyBuilder propertyBuilder = new PropertyBuilder(key);
        properties.add(propertyBuilder.getProperty());
        return propertyBuilder;
    }

    public MaterialBuilder property(String key, String value)
    {
        property(key).value(value);
        return this;
    }
}
