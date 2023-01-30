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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * Abstract super class of builder of subclasses of {@link EntityType}.
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractEntityTypeBuilder<E extends EntityType>
{
    protected void setValidationPlugin(E entityType, String name, String descriptionOrNull)
    {
        Script validationScript = new Script();
        validationScript.setName(name);
        validationScript.setDescription(descriptionOrNull);
        entityType.setValidationScript(validationScript);

    }

    protected void fillEntityTypePropertyType(E entityType,
            EntityTypePropertyType<E> entityTypePropertyType, String code, String label,
            DataTypeCode type)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        propertyType.setSimpleCode(code);
        propertyType.setLabel(label);
        propertyType.setDataType(new DataType(type));
        fillEntityTypePropertyType(entityType, entityTypePropertyType, propertyType);
    }

    protected void fillEntityTypePropertyType(E entityType,
            EntityTypePropertyType<E> entityTypePropertyType, PropertyType propertyType)
    {
        entityTypePropertyType.setPropertyType(propertyType);
        entityTypePropertyType.setEntityType(entityType);
    }

}
