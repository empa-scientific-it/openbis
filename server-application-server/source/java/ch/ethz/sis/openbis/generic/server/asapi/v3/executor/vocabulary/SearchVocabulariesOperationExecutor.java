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

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabulariesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabulariesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.vocabulary.IVocabularyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SearchVocabulariesOperationExecutor
        extends SearchObjectsPEOperationExecutor<Vocabulary, VocabularyPE, VocabularySearchCriteria, VocabularyFetchOptions>
        implements ISearchVocabulariesOperationExecutor
{
    @Autowired
    private ISearchVocabularyExecutor searchExecutor;
    
    @Autowired
    private IVocabularyTranslator translator;
    
    @Override
    protected Class<? extends SearchObjectsOperation<VocabularySearchCriteria, VocabularyFetchOptions>> getOperationClass()
    {
        return SearchVocabulariesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<VocabularySearchCriteria, VocabularyPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Vocabulary, VocabularyFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<Vocabulary> getOperationResult(SearchResult<Vocabulary> searchResult)
    {
        return new SearchVocabulariesOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<VocabularySearchCriteria, Vocabulary, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}
