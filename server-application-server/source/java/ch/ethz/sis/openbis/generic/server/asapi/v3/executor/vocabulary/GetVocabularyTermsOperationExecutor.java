/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.vocabulary.IVocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class GetVocabularyTermsOperationExecutor
        extends GetObjectsPEOperationExecutor<IVocabularyTermId, VocabularyTermPE, VocabularyTerm, VocabularyTermFetchOptions>
        implements IGetVocabularyTermsOperationExecutor
{

    @Autowired
    private IMapVocabularyTermByIdExecutor mapExecutor;

    @Autowired
    private IVocabularyTermTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<IVocabularyTermId, VocabularyTermFetchOptions>> getOperationClass()
    {
        return GetVocabularyTermsOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<IVocabularyTermId, VocabularyTermPE> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, VocabularyTerm, VocabularyTermFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IVocabularyTermId, VocabularyTerm> getOperationResult(Map<IVocabularyTermId, VocabularyTerm> objectMap)
    {
        return new GetVocabularyTermsOperationResult(objectMap);
    }

}
