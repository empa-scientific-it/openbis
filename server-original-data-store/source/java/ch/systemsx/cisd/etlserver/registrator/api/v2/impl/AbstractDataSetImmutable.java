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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * Abstract super class of all {@link IDataSetImmutable} implementations.
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractDataSetImmutable implements IDataSetImmutable
{
    protected final IEncapsulatedBasicOpenBISService service;

    AbstractDataSetImmutable(IEncapsulatedBasicOpenBISService service)
    {
        this.service = service;
    }

    protected DataSetType getDataSetTypeWithPropertyTypes(String dataSetTypeCode)
    {
        DataSetTypeWithVocabularyTerms dataSetType = service.getDataSetType(dataSetTypeCode);
        HashMap<Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> termsMap =
                new HashMap<Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>>();
        List<DataSetTypePropertyType> propertyTypes =
                dataSetType.getDataSetType().getAssignedPropertyTypes();
        for (DataSetTypePropertyType dataSetTypePropertyType : propertyTypes)
        {
            Vocabulary vocabulary = dataSetTypePropertyType.getPropertyType().getVocabulary();
            if (vocabulary != null)
            {
                Collection<VocabularyTerm> vocabularyTerms =
                        service.listVocabularyTerms(vocabulary.getCode());
                termsMap.put(vocabulary, Translator.translatePropertyTypeTerms(vocabularyTerms));
            }
        }
        return Translator.translate(dataSetType.getDataSetType(), termsMap);
    }

}
