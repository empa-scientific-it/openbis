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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.session;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.SearchPersonalAccessTokensOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.SearchPersonalAccessTokensOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SearchSessionInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SearchSessionInformationOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.pat.ISearchPersonalAccessTokenExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query.ISearchQueriesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.pat.IPersonalAccessTokenTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.session.ISessionInformationTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
@Component
public class SearchSessionInformationOperationExecutor extends
        AbstractSearchObjectsOperationExecutor<SessionInformation, Session, SessionInformationSearchCriteria, SessionInformationFetchOptions>
        implements ISearchSessionInformationOperationExecutor
{

    @Autowired
    private ISearchSessionInformationExecutor searchExecutor;

    @Autowired
    private ISessionInformationTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<SessionInformationSearchCriteria, SessionInformationFetchOptions>> getOperationClass()
    {
        return SearchSessionInformationOperation.class;
    }

    @Override protected List<Session> doSearch(final IOperationContext context,
            final SessionInformationSearchCriteria criteria, final SessionInformationFetchOptions fetchOptions)
    {
        return searchExecutor.search(context, criteria);
    }

    @Override protected Map<Session, SessionInformation> doTranslate(
            final TranslationContext translationContext, final Collection<Session> sessions,
            final SessionInformationFetchOptions fetchOptions)
    {
        return translator.translate(translationContext, sessions, fetchOptions);
    }

    @Override
    protected SearchObjectsOperationResult<SessionInformation> getOperationResult(SearchResult<SessionInformation> searchResult)
    {
        return new SearchSessionInformationOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<SessionInformationSearchCriteria, SessionInformation, Session> getSearchManager()
    {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

