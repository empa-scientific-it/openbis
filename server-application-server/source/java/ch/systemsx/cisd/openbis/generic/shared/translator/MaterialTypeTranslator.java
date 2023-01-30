/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Translates {@link MaterialTypePE} to {@link MaterialType}.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialTypeTranslator
{

    private MaterialTypeTranslator()
    {
    }

    public static MaterialType translate(MaterialTypePE entityTypeOrNull,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return translate(entityTypeOrNull, true, materialTypeCache, cacheOrNull);
    }

    public static MaterialType translate(MaterialTypePE entityTypeOrNull, boolean withProperties,
            Map<MaterialTypePE, MaterialType> materialTypeCacheOrNull, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        if (entityTypeOrNull == null)
        {
            return null;
        }
        MaterialType materialType = tryGetMaterialTypeFromCache(entityTypeOrNull, materialTypeCacheOrNull);
        if (materialType != null)
        {
            return materialType;
        }
        final MaterialType result = translateSimple(entityTypeOrNull);
        addMaterialTypeToCache(materialTypeCacheOrNull, entityTypeOrNull, result);
        if (withProperties == false)
        {
            unsetMaterialTypes(entityTypeOrNull.getMaterialTypePropertyTypes());
        }

        result.setMaterialTypePropertyTypes(EntityType
                .sortedInternally(MaterialTypePropertyTypeTranslator.translate(
                        entityTypeOrNull.getMaterialTypePropertyTypes(), result, 
                        materialTypeCacheOrNull, cacheOrNull)));
        result.setModificationDate(entityTypeOrNull.getModificationDate());
        result.setValidationScript(ScriptTranslator.translate(entityTypeOrNull
                .getValidationScript()));
        return result;
    }

    private static MaterialType tryGetMaterialTypeFromCache(MaterialTypePE entityTypeOrNull, Map<MaterialTypePE, MaterialType> materialTypeCacheOrNull)
    {
        if (materialTypeCacheOrNull == null)
        {
            return null;
        }
        return materialTypeCacheOrNull.get(entityTypeOrNull);
    }
    
    private static void addMaterialTypeToCache(Map<MaterialTypePE, MaterialType> materialTypeCacheOrNull, MaterialTypePE entityTypeOrNull,
            final MaterialType result)
    {
        if (materialTypeCacheOrNull != null)
        {
            materialTypeCacheOrNull.put(entityTypeOrNull, result);
        }
    }

    /** translates basic information, without properties */
    public static MaterialType translateSimple(EntityTypePE entityTypeOrNull)
    {
        final MaterialType result = new MaterialType();
        result.setId(HibernateUtils.getId(entityTypeOrNull));
        result.setCode(entityTypeOrNull.getCode());
        result.setDescription(entityTypeOrNull.getDescription());
        return result;
    }

    private static void unsetMaterialTypes(Set<MaterialTypePropertyTypePE> materialTypePropertyTypes)
    {
        if ((HibernateUtils.isInitialized(materialTypePropertyTypes)))
            for (MaterialTypePropertyTypePE mtpt : materialTypePropertyTypes)
            {
                mtpt.getPropertyType().setMaterialType(null);
            }
    }

    public final static List<MaterialType> translate(final List<MaterialTypePE> materialTypes,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        final List<MaterialType> result = new ArrayList<MaterialType>(materialTypes.size());
        for (final MaterialTypePE materialType : materialTypes)
        {
            result.add(translate(materialType, materialTypeCache, cacheOrNull));
        }
        return result;
    }

    public static MaterialTypePE translate(MaterialType type)
    {
        final MaterialTypePE result = new MaterialTypePE();
        result.setCode(type.getCode());
        return result;
    }

}
