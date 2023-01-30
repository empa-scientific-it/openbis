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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * @author Kaloyan Enimanev
 */
public class PropertyTypeImmutable implements IPropertyTypeImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType propertyType;

    PropertyTypeImmutable(String code, DataType dataType)
    {
        this(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType());
        getPropertyType().setCode(code);

        final DataTypeCode typeCode = DataTypeCode.valueOf(dataType.name());
        getPropertyType().setDataType(
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType(typeCode));
    }

    PropertyTypeImmutable(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType getPropertyType()
    {
        return propertyType;
    }

    @Override
    public String getCode()
    {
        return getPropertyType().getCode();
    }

    @Override
    public String getDescription()
    {
        return getPropertyType().getDescription();
    }

    @Override
    public String getLabel()
    {
        return getPropertyType().getLabel();
    }

    @Override
    public DataType getDataType()
    {
        final String typeName = getPropertyType().getDataType().getCode().name();
        return DataType.valueOf(typeName);
    }

    @Override
    public IMaterialTypeImmutable getMaterialType()
    {
        MaterialType materialType = getPropertyType().getMaterialType();
        if (materialType != null)
        {
            return new MaterialTypeImmutable(materialType);
        }
        return null;
    }

    @Override
    public String getXmlSchema()
    {
        return getPropertyType().getSchema();
    }

    @Override
    public String getTransformation()
    {
        return getPropertyType().getTransformation();
    }

    @Override
    public boolean isManagedInternally()
    {
        return getPropertyType().isManagedInternally();
    }

    /**
     * @deprecated use {@link #isManagedInternally()}
     */
    @Override
    public boolean isInternalNamespace()
    {
        return isManagedInternally();
    }

    @Override
    public IVocabularyImmutable getVocabulary()
    {
        final Vocabulary vocabulary = getPropertyType().getVocabulary();
        if (vocabulary != null)
        {
            return new VocabularyImmutable(vocabulary);
        }
        return null;
    }

}
