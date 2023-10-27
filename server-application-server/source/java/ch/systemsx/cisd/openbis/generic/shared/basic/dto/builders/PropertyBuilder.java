/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.CANONICAL_DATE_FORMAT_PATTERN;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * Builder for an {@link EntityProperty} instance.
 * 
 * @author Franz-Josef Elmer
 */
public class PropertyBuilder
{
    private static final class ExtendedEntityProperty extends EntityProperty
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private boolean managed;

        @Override
        public boolean isManaged()
        {
            return managed;
        }

        void setManaged(boolean managed)
        {
            this.managed = managed;
        }

    }

    private final ExtendedEntityProperty property = new ExtendedEntityProperty();

    /**
     * Creates an instance for specified property type code which also the simple code. Data type is {@link DataTypeCode#VARCHAR}.
     */
    public PropertyBuilder(String key)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(key);
        propertyType.setSimpleCode(key);
        property.setPropertyType(propertyType);
        type(DataTypeCode.VARCHAR);
    }

    public PropertyBuilder type(DataTypeCode dataType)
    {
        property.getPropertyType().setDataType(new DataType(dataType));
        return this;
    }

    public PropertyBuilder label(String label)
    {
        property.getPropertyType().setLabel(label);
        return this;
    }

    /**
     * Sets property type to internal name space and adds a '$' code prefix.
     */
    public PropertyBuilder internal()
    {
        PropertyType propertyType = property.getPropertyType();
        propertyType.setManagedInternally(true);
        propertyType.setCode("$" + propertyType.getSimpleCode());
        return this;
    }

    public PropertyBuilder dynamic()
    {
        property.setDynamic(true);
        return this;
    }

    public PropertyBuilder managed()
    {
        property.setManaged(true);
        return this;
    }

    public PropertyBuilder value(String value)
    {
        property.setValue(value);
        return this;
    }

    public PropertyBuilder value(Serializable value)
    {
        property.setValue(value);
        return this;
    }

    public PropertyBuilder value(int value)
    {
        type(DataTypeCode.INTEGER);
        property.setValue(Integer.toString(value));
        return this;
    }

    public PropertyBuilder value(double value)
    {
        type(DataTypeCode.REAL);
        property.setValue(Double.toString(value));
        return this;
    }

    public PropertyBuilder value(Date value)
    {
        type(DataTypeCode.TIMESTAMP);
        String formatedDate = new SimpleDateFormat(CANONICAL_DATE_FORMAT_PATTERN).format(value);
        property.setValue(formatedDate);
        return this;
    }

    public PropertyBuilder value(MaterialBuilder builder)
    {
        return value(builder.getMaterial());
    }

    public PropertyBuilder value(Material value)
    {
        type(DataTypeCode.MATERIAL);
        property.setMaterial(value);
        return this;
    }

    public PropertyBuilder value(VocabularyTerm value)
    {
        type(DataTypeCode.CONTROLLEDVOCABULARY);
        property.setVocabularyTerm(value);
        return this;
    }

    public EntityProperty getProperty()
    {
        return property;
    }

}
