/*
 * Copyright 2013 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * Builder of {@link PropertyType} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class PropertyTypeBuilder
{
    private final PropertyType propertyType = new PropertyType();

    public PropertyTypeBuilder(String label)
    {
        propertyType.setLabel(label);
        String code = label.toUpperCase();
        propertyType.setCode(code);
        propertyType.setSimpleCode(code);
        propertyType.setDataType(new DataType(DataTypeCode.VARCHAR));
    }

    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    public PropertyTypeBuilder code(String code)
    {
        propertyType.setCode(code);
        return this;
    }

    public PropertyTypeBuilder dataType(DataTypeCode dataType)
    {
        propertyType.setDataType(new DataType(dataType));
        return this;
    }

    public PropertyTypeBuilder vocabulary(String vocabularyCode, String... terms)
    {
        propertyType.setDataType(new DataType(DataTypeCode.CONTROLLEDVOCABULARY));
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setCode(vocabularyCode);
        List<VocabularyTerm> vocaTerms = new ArrayList<VocabularyTerm>();
        for (String term : terms)
        {
            vocaTerms.add(new VocabularyTermBuilder(term.toUpperCase()).label(term).getTerm());
        }
        vocabulary.setTerms(vocaTerms);
        propertyType.setVocabulary(vocabulary);
        return this;
    }
}
