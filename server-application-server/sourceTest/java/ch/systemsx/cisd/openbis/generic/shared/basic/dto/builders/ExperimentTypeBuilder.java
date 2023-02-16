/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentTypeBuilder extends AbstractEntityTypeBuilder<ExperimentType>
{
    private ExperimentType experimentType = new ExperimentType();

    public ExperimentTypeBuilder()
    {
        experimentType.setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());
    }

    public ExperimentTypeBuilder code(String code)
    {
        experimentType.setCode(code);
        return this;
    }

    public ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public ExperimentTypeBuilder propertyType(String code, String label, DataTypeCode dataType)
    {
        ExperimentTypePropertyType entityTypePropertyType = new ExperimentTypePropertyType();
        List<ExperimentTypePropertyType> types = experimentType.getAssignedPropertyTypes();
        fillEntityTypePropertyType(experimentType, entityTypePropertyType, code, label, dataType);
        types.add(entityTypePropertyType);
        return this;
    }

    public EntityTypePropertyTypeBuilder propertyType(PropertyType propertyType)
    {
        ExperimentTypePropertyType entityTypePropertyType = new ExperimentTypePropertyType();
        List<ExperimentTypePropertyType> types = experimentType.getAssignedPropertyTypes();
        entityTypePropertyType.setOrdinal(Long.valueOf(types.size()));
        fillEntityTypePropertyType(experimentType, entityTypePropertyType, propertyType);
        types.add(entityTypePropertyType);
        return new EntityTypePropertyTypeBuilder(entityTypePropertyType);
    }
}
