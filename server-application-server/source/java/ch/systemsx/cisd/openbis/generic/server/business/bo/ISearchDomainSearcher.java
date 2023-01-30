/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;

/**
 * Business object for searching in search domains.
 *
 * @author Franz-Josef Elmer
 */
public interface ISearchDomainSearcher
{
    /**
     * Lists all available sequence databases.
     */
    List<SearchDomain> listAvailableSearchDomains();

    /**
     * Searches for entities with sequences. The result is sorted by score in descending order.
     */
    List<SearchDomainSearchResultWithFullEntity> searchForEntitiesWithSequences(String preferredSearchDomainOrNull,
            String sequenceSnippet, Map<String, String> optionalParametersOrNull);

}
