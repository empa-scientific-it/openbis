/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Builder class of {@link DataSetType} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetTypeBuilder extends AbstractEntityTypeBuilder<DataSetType>
{
    private DataSetType dataSetType = new DataSetType();

    public DataSetTypeBuilder()
    {
        dataSetType.setDataSetTypePropertyTypes(new ArrayList<DataSetTypePropertyType>());
    }

    public DataSetType getDataSetType()
    {
        return dataSetType;
    }

    public DataSetTypeBuilder code(String code)
    {
        dataSetType.setCode(code);
        return this;
    }

    public DataSetTypeBuilder description(String description)
    {
        dataSetType.setDescription(description);
        return this;
    }

    public DataSetTypeBuilder validationPlugin(String name, String descriptionOrNull)
    {
        setValidationPlugin(dataSetType, name, descriptionOrNull);
        return this;
    }

    public DataSetTypeBuilder mainDataSetPattern(String pattern)
    {
        dataSetType.setMainDataSetPattern(pattern);
        return this;
    }

    public DataSetTypeBuilder mainDataSetPath(String path)
    {
        dataSetType.setMainDataSetPath(path);
        return this;
    }

    public DataSetTypeBuilder deletionDisallowed()
    {
        dataSetType.setDeletionDisallow(true);
        return this;
    }

    public DataSetTypeBuilder propertyType(String code, String label, DataTypeCode dataType)
    {
        DataSetTypePropertyType entityTypePropertyType = new DataSetTypePropertyType();
        List<DataSetTypePropertyType> types = dataSetType.getAssignedPropertyTypes();
        entityTypePropertyType.setOrdinal(Long.valueOf(types.size()));
        fillEntityTypePropertyType(dataSetType, entityTypePropertyType, code, label, dataType);
        types.add(entityTypePropertyType);
        return this;
    }

    public EntityTypePropertyTypeBuilder propertyType(PropertyType propertyType)
    {
        DataSetTypePropertyType entityTypePropertyType = new DataSetTypePropertyType();
        List<DataSetTypePropertyType> types = dataSetType.getAssignedPropertyTypes();
        entityTypePropertyType.setOrdinal(Long.valueOf(types.size()));
        fillEntityTypePropertyType(dataSetType, entityTypePropertyType, propertyType);
        types.add(entityTypePropertyType);
        return new EntityTypePropertyTypeBuilder(entityTypePropertyType);
    }
}
