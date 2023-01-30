/*
 * Copyright 2018 ETH Zuerich, SIS
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabulariesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabulariesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.vocabulary.IVocabularyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class GetVocabulariesOperationExecutor
        extends GetObjectsPEOperationExecutor<IVocabularyId, VocabularyPE, Vocabulary, VocabularyFetchOptions>
        implements IGetVocabulariesOperationExecutor
{
    @Autowired
    private IMapVocabularyByIdExecutor mapExecutor;
    
    @Autowired
    private IVocabularyTranslator translator;
    
    @Override
    protected Class<? extends GetObjectsOperation<IVocabularyId, VocabularyFetchOptions>> getOperationClass()
    {
        return GetVocabulariesOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<IVocabularyId, VocabularyPE> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Vocabulary, VocabularyFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IVocabularyId, Vocabulary> getOperationResult(Map<IVocabularyId, Vocabulary> objectMap)
    {
        return new GetVocabulariesOperationResult(objectMap);
    }

}
