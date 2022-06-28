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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.pat;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.SearchPersonalAccessTokensOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.SearchPersonalAccessTokensOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.pat.IPersonalAccessTokenTranslator;

/**
 * @author pkupczyk
 */
@Component
public class SearchPersonalAccessTokensOperationExecutor extends
        AbstractSearchObjectsOperationExecutor<PersonalAccessToken, ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken, PersonalAccessTokenSearchCriteria, PersonalAccessTokenFetchOptions>
        implements ISearchPersonalAccessTokensOperationExecutor
{

    @Autowired
    private ISearchPersonalAccessTokenExecutor searchExecutor;

    @Autowired
    private IPersonalAccessTokenTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<PersonalAccessTokenSearchCriteria, PersonalAccessTokenFetchOptions>> getOperationClass()
    {
        return SearchPersonalAccessTokensOperation.class;
    }

    @Override protected List<ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken> doSearch(final IOperationContext context,
            final PersonalAccessTokenSearchCriteria criteria, final PersonalAccessTokenFetchOptions fetchOptions)
    {
        return searchExecutor.search(context, criteria);
    }

    @Override protected Map<ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken, PersonalAccessToken> doTranslate(
            final TranslationContext translationContext, final Collection<ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken> pats,
            final PersonalAccessTokenFetchOptions fetchOptions)
    {
        return translator.translate(translationContext, pats, fetchOptions);
    }

    @Override
    protected SearchObjectsOperationResult<PersonalAccessToken> getOperationResult(SearchResult<PersonalAccessToken> searchResult)
    {
        return new SearchPersonalAccessTokensOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<PersonalAccessTokenSearchCriteria, PersonalAccessToken, ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken> getSearchManager()
    {
        throw new RuntimeException("This method is not implemented yet.");
    }

}
