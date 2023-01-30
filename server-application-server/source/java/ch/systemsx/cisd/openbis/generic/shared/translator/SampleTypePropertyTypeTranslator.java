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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.Transformer;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;

/**
 * A {@link SampleTypePropertyType} &lt;---&gt; {@link SampleTypePropertyTypePE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class SampleTypePropertyTypeTranslator

{
    private SampleTypePropertyTypeTranslator()
    {
        // Can not be instantiated.
    }

    static private class SampleTypePropertyTypeTranslatorHelper
            extends
            AbstractEntityTypePropertyTypeTranslator<SampleType, SampleTypePropertyType, SampleTypePropertyTypePE>
    {

        @Override
        SampleType translate(EntityTypePE entityTypePE, Map<MaterialTypePE, MaterialType> materialTypeCache, 
                Map<PropertyTypePE, PropertyType> cacheOrNull)
        {
            return SampleTypeTranslator.translate((SampleTypePE) entityTypePE, materialTypeCache, cacheOrNull);
        }

        @Override
        SampleTypePropertyType create()
        {
            return new SampleTypePropertyType();
        }

    }

    public static List<SampleTypePropertyType> translate(
            Set<SampleTypePropertyTypePE> sampleTypePropertyTypes, SampleType result,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return new SampleTypePropertyTypeTranslatorHelper().translate(sampleTypePropertyTypes,
                result, materialTypeCache, cacheOrNull);
    }

    public static SampleTypePropertyType translate(SampleTypePropertyTypePE entityTypePropertyType,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return new SampleTypePropertyTypeTranslatorHelper().translate(entityTypePropertyType,
                materialTypeCache, cacheOrNull);
    }

    public static List<SampleTypePropertyType> translate(
            Set<SampleTypePropertyTypePE> sampleTypePropertyTypes, PropertyType result,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return new SampleTypePropertyTypeTranslatorHelper().translate(sampleTypePropertyTypes,
                result, materialTypeCache, cacheOrNull);
    }

    public static final Transformer<EntityTypePropertyTypePE, SampleTypePropertyType> TRANSFORMER =
            new Transformer<EntityTypePropertyTypePE, SampleTypePropertyType>()
                {
                    @Override
                    public SampleTypePropertyType transform(EntityTypePropertyTypePE input)
                    {
                        return translate((SampleTypePropertyTypePE) input, null, null);
                    }
                };
}
