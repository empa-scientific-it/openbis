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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
class PropertyTypeDsl extends PropertyType
{
    private final String code;

    private String label;

    private String description;

    private PropertyTypeDataType dataType;

    private Vocabulary vocabulary;

    PropertyTypeDsl(String code, String label, String description, PropertyTypeDataType dataType,
            Vocabulary vocabulary)
    {
        this.code = code;
        this.label = label;
        this.description = description;
        this.dataType = dataType;
        this.vocabulary = vocabulary;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public PropertyTypeDataType getDataType()
    {
        return dataType;
    }

    @Override
    public Vocabulary getVocabulary()
    {
        return vocabulary;
    }

    void setLabel(String label)
    {
        this.label = label;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setDataType(PropertyTypeDataType dataType)
    {
        this.dataType = dataType;
    }

    void setVocabulary(Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    @Override
    public String toString()
    {
        return "PropertyType " + code + " of type " + dataType;
    }
}
