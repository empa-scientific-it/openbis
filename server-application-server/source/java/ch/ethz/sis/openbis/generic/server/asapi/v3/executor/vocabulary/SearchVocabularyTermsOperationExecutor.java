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

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.vocabulary.IVocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchVocabularyTermsOperationExecutor
        extends SearchObjectsPEOperationExecutor<VocabularyTerm, VocabularyTermPE, VocabularyTermSearchCriteria, VocabularyTermFetchOptions>
        implements ISearchVocabularyTermsOperationExecutor
{

    @Autowired
    private ISearchVocabularyTermExecutor searchExecutor;

    @Autowired
    private IVocabularyTermTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<VocabularyTermSearchCriteria, VocabularyTermFetchOptions>> getOperationClass()
    {
        return SearchVocabularyTermsOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<VocabularyTermSearchCriteria, VocabularyTermPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, VocabularyTerm, VocabularyTermFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<VocabularyTerm> getOperationResult(SearchResult<VocabularyTerm> searchResult)
    {
        return new SearchVocabularyTermsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<VocabularyTermSearchCriteria, VocabularyTerm, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}
