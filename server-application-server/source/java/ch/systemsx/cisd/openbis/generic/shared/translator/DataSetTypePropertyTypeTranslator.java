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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Translates {@link DataSetTypePropertyTypePE} to {@link DataSetTypePropertyType}.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetTypePropertyTypeTranslator
{

    static private class DataSetTypePropertyTypeTranslatorHelper
            extends
            AbstractEntityTypePropertyTypeTranslator<DataSetType, DataSetTypePropertyType, DataSetTypePropertyTypePE>
    {
        @Override
        DataSetType translate(EntityTypePE entityTypePE, Map<MaterialTypePE, MaterialType> materialTypeCache, 
                Map<PropertyTypePE, PropertyType> cacheOrNull)
        {
            return DataSetTypeTranslator.translate((DataSetTypePE) entityTypePE, materialTypeCache, cacheOrNull);
        }

        @Override
        DataSetTypePropertyType create()
        {
            return new DataSetTypePropertyType();
        }
    }

    public static List<DataSetTypePropertyType> translate(
            Set<DataSetTypePropertyTypePE> DataSetTypePropertyTypes, PropertyType result,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return new DataSetTypePropertyTypeTranslatorHelper().translate(DataSetTypePropertyTypes,
                result, materialTypeCache, cacheOrNull);
    }

    public static DataSetTypePropertyType translate(DataSetTypePropertyTypePE entityTypePropertyType)
    {
        return new DataSetTypePropertyTypeTranslatorHelper()
                .translate(entityTypePropertyType, null, null);
    }

    public static List<DataSetTypePropertyType> translate(
            Set<DataSetTypePropertyTypePE> DataSetTypePropertyTypes, DataSetType result,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return new DataSetTypePropertyTypeTranslatorHelper().translate(DataSetTypePropertyTypes,
                result, materialTypeCache, cacheOrNull);
    }

    public static final Transformer<EntityTypePropertyTypePE, DataSetTypePropertyType> TRANSFORMER =
            new Transformer<EntityTypePropertyTypePE, DataSetTypePropertyType>()
                {
                    @Override
                    public DataSetTypePropertyType transform(EntityTypePropertyTypePE input)
                    {
                        return translate((DataSetTypePropertyTypePE) input);
                    }
                };
}
