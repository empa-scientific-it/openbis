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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author pkupczyk
 */
public class EntityTypeTranslator
{

    public static EntityType translate(EntityTypePE entityTypePE)
    {
        if (entityTypePE == null)
        {
            return null;
        }

        HashMap<MaterialTypePE, MaterialType> materialTypeCache = new HashMap<MaterialTypePE, MaterialType>();
        Map<PropertyTypePE, PropertyType> cache = new HashMap<PropertyTypePE, PropertyType>();

        switch (entityTypePE.getEntityKind())
        {
            case EXPERIMENT:
                ExperimentTypePE experimentTypePE = (ExperimentTypePE) entityTypePE;
                HibernateUtils.initialize(experimentTypePE.getExperimentTypePropertyTypes());
                return ExperimentTypeTranslator.translate(experimentTypePE, materialTypeCache, cache);
            case SAMPLE:
                SampleTypePE sampleTypePE = (SampleTypePE) entityTypePE;
                HibernateUtils.initialize(sampleTypePE.getSampleTypePropertyTypes());
                return SampleTypeTranslator.translate(sampleTypePE, materialTypeCache, cache);
            case DATA_SET:
                DataSetTypePE dataSetTypePE = (DataSetTypePE) entityTypePE;
                HibernateUtils.initialize(dataSetTypePE.getDataSetTypePropertyTypes());
                return DataSetTypeTranslator.translate(dataSetTypePE, materialTypeCache, cache);
            case MATERIAL:
                MaterialTypePE materialTypePE = (MaterialTypePE) entityTypePE;
                HibernateUtils.initialize(materialTypePE.getMaterialTypePropertyTypes());
                return MaterialTypeTranslator.translate(materialTypePE, materialTypeCache, cache);
            default:
                throw new IllegalArgumentException("Unsupported entity kind: "
                        + entityTypePE.getEntityKind());
        }

    }
}
