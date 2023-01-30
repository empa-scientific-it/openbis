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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceSearchOption;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchDomainServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.NameMatcher;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISearchDomainSearcher;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchOption;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchSearchDomainServiceExecutor
        extends AbstractSearchObjectManuallyExecutor<SearchDomainServiceSearchCriteria, SearchDomainService>
        implements ISearchSearchDomainServiceExecutor
{
    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private ISearchDomainServiceAuthorizationExecutor authorizationExecutor;

    @Override
    public List<SearchDomainService> search(IOperationContext context, SearchDomainServiceSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<SearchDomainService> listAll(IOperationContext context)
    {
        ISearchDomainSearcher searcher = businessObjectFactory.createSearchDomainSearcher(context.getSession());
        List<SearchDomain> searchDomains = searcher.listAvailableSearchDomains();
        List<SearchDomainService> result = new ArrayList<>();
        for (SearchDomain searchDomain : searchDomains)
        {
            SearchDomainService searchDomainService = new SearchDomainService();
            searchDomainService.setName(searchDomain.getName());
            DataStorePermId dataStoreId = new DataStorePermId(searchDomain.getDataStoreCode());
            searchDomainService.setPermId(new DssServicePermId(searchDomain.getName(), dataStoreId));
            searchDomainService.setLabel(searchDomain.getLabel());
            searchDomainService.setPossibleSearchOptionsKey(searchDomain.getPossibleSearchOptionsKey());
            List<SearchDomainServiceSearchOption> parameters = new ArrayList<>();
            for (SearchDomainSearchOption searchOption : searchDomain.getPossibleSearchOptions())
            {
                SearchDomainServiceSearchOption parameter = new SearchDomainServiceSearchOption();
                parameter.setCode(searchOption.getCode());
                parameter.setLabel(searchOption.getLabel());
                parameter.setDescription(searchOption.getDescription());
                parameters.add(parameter);
            }
            searchDomainService.setPossibleSearchOptions(parameters);
            result.add(searchDomainService);
        }
        return result;
    }

    @Override
    protected Matcher<SearchDomainService> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof NameSearchCriteria)
        {
            return new NameMatcher<SearchDomainService>();
        }
        throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
    }
    
    private static class IdMatcher extends Matcher<SearchDomainService>
    {
        @Override
        public List<SearchDomainService> getMatching(IOperationContext context, List<SearchDomainService> objects, ISearchCriteria criteria)
        {
            @SuppressWarnings("unchecked")
            IDssServiceId id = ((IdSearchCriteria<IDssServiceId>) criteria).getId();
            if (id == null)
            {
                return objects;
            }
            return objects.stream().filter(s -> s.getPermId().equals(id)).collect(Collectors.toList());
        }
    }
}
