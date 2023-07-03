/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link PropertyType} &lt;---&gt; {@link PropertyTypePE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeTranslator
{

    private PropertyTypeTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<PropertyType> translate(final List<PropertyTypePE> propertyTypes,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        final List<PropertyType> result = new ArrayList<PropertyType>();
        for (final PropertyTypePE propType : propertyTypes)
        {
            result.add(PropertyTypeTranslator.translate(propType, materialTypeCache, cacheOrNull));
        }
        return result;
    }

    public final static PropertyType translate(final PropertyTypePE propertyType,
            MaterialType materialType, Map<MaterialTypePE, MaterialType> materialTypeCache, 
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        final PropertyType cachedOrNull =
                (cacheOrNull == null) ? null : cacheOrNull.get(propertyType);
        if (cachedOrNull != null)
        {
            return cachedOrNull;
        }
        final PropertyType result = new PropertyType();
        if (cacheOrNull != null)
        {
            cacheOrNull.put(propertyType, result);
        }
        result.setId(HibernateUtils.getId(propertyType));
        result.setCode(propertyType.getCode());
        result.setSimpleCode(propertyType.getSimpleCode());
        result.setModificationDate(propertyType.getModificationDate());
        result.setRegistrator(PersonTranslator.translate(propertyType.getRegistrator()));
        result.setManagedInternally(propertyType.isManagedInternally());
        result.setLabel(propertyType.getLabel());
        result.setDataType(DataTypeTranslator.translate(propertyType.getType()));
        result.setVocabulary(VocabularyTranslator.translate(propertyType.getVocabulary()));
        result.setMaterialType(MaterialTypeTranslator.translate(propertyType.getMaterialType(),
                false, materialTypeCache, cacheOrNull));
        result.setSampleType(SampleTypeTranslator.translate(
                propertyType.getSampleType(), materialTypeCache, cacheOrNull));
        result.setDescription(propertyType.getDescription());
        result.setSampleTypePropertyTypes(SampleTypePropertyTypeTranslator.translate(
                propertyType.getSampleTypePropertyTypes(), result, materialTypeCache, cacheOrNull));
        result.setMaterialTypePropertyTypes(MaterialTypePropertyTypeTranslator.translate(
                propertyType.getMaterialTypePropertyTypes(), result, materialTypeCache, cacheOrNull));
        result.setExperimentTypePropertyTypes(ExperimentTypePropertyTypeTranslator.translate(
                propertyType.getExperimentTypePropertyTypes(), result, materialTypeCache, cacheOrNull));
        result.setDataSetTypePropertyTypes(DataSetTypePropertyTypeTranslator.translate(
                propertyType.getDataSetTypePropertyTypes(), result, materialTypeCache, cacheOrNull));
        result.setSchema(propertyType.getSchema());
        result.setTransformation(propertyType.getTransformation());
        result.setMultiValue(propertyType.isMultiValue());

        return result;
    }

    public final static PropertyType translate(final PropertyTypePE propertyType,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return translate(propertyType, null, materialTypeCache, cacheOrNull);
    }
}
