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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.HashMap;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDeletablePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * A generic translator for deleted entities. In the future, if we need to access specific fields of a deleted entity in the UI we'll have to create a
 * DTO layer of deleted object.
 * <p>
 * For now, a single DeletedEntity covers all required uses cases of the trash can.
 * 
 * @author Kaloyan Enimanev
 */
public class DeletedEntityTranslator
{

    public static IEntityInformationHolderWithIdentifier translate(final IDeletablePE deletedEntity)
    {
        Long id = deletedEntity.getId();
        String code = deletedEntity.getCode();
        String permId = deletedEntity.getPermId();
        String identifier = deletedEntity.getIdentifier();
        EntityKind entityKind = EntityKind.valueOf(deletedEntity.getEntityKind().name());
        BasicEntityType entityType = translateType(entityKind, deletedEntity.getEntityType());
        return new DeletedEntity(id, code, permId, identifier, entityType, entityKind);
    }

    private static BasicEntityType translateType(EntityKind entityKind, EntityTypePE entityType)
    {
        HashMap<MaterialTypePE, MaterialType> materialTypeCache = new HashMap<MaterialTypePE, MaterialType>();
        HashMap<PropertyTypePE, PropertyType> propertyMap =
                new HashMap<PropertyTypePE, PropertyType>();
        switch (entityKind)
        {
            case EXPERIMENT:
                return ExperimentTranslator.translate((ExperimentTypePE) entityType, materialTypeCache, propertyMap);

            case SAMPLE:
                return SampleTypeTranslator.translate((SampleTypePE) entityType, materialTypeCache, propertyMap);

            case DATA_SET:
                return DataSetTypeTranslator.translate((DataSetTypePE) entityType, materialTypeCache, propertyMap);

            default:
                throw new IllegalArgumentException("Invalid deleted entity" + entityKind);
        }
    }

}
