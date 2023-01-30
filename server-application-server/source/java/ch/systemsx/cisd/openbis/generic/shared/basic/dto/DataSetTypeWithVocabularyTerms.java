/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetTypeWithVocabularyTerms implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DataSetType dataSetType;

    private List<PropertyTypeWithVocabulary> propertyTypes =
            new ArrayList<PropertyTypeWithVocabulary>();

    public DataSetType getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(DataSetType dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    public void addPropertyType(PropertyTypeWithVocabulary propertyType)
    {
        propertyTypes.add(propertyType);
    }

    public List<PropertyTypeWithVocabulary> getPropertyTypes()
    {
        return propertyTypes;
    }

}
