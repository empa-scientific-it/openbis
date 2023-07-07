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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto;


/**
 * A {@link IEntityProperty} class that only stores the vocabulary term value, but not a generic value or a material value.
 * 
 * @author Bernd Rinn
 */
public class VocabularyTermEntityProperty extends AbstractEntityProperty
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private VocabularyTerm vocabularyTermOrNull;

    @Override
    public void setPropertyType(PropertyType propertyType)
    {
        if (DataTypeCode.CONTROLLEDVOCABULARY.equals(propertyType.getDataType().getCode()) == false)
        {
            throw new IllegalArgumentException(
                    "Only property types with data type CONTROLLEDVOCABULARY supported, found '"
                            + propertyType.getDataType().getCode() + "'.");
        }
        super.setPropertyType(propertyType);
    }

    @Override
    public VocabularyTerm getVocabularyTerm()
    {
        return vocabularyTermOrNull;
    }

    @Override
    public void setVocabularyTerm(VocabularyTerm vocabularyTerm)
    {
        this.vocabularyTermOrNull = vocabularyTerm;
    }

}
