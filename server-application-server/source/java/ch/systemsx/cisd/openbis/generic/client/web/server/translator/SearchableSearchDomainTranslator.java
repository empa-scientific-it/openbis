/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;

/**
 * A {@link SearchableEntity} &lt;---&gt; {@link ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity} translator.
 * 
 * @author Christian Ribeaud
 */
public final class SearchableSearchDomainTranslator
{

    private SearchableSearchDomainTranslator()
    {
        // Can not be instantiated.
    }
    
    // if null all possible entities are returned
    public final static SearchDomain[] translate(
            final ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity searchableEntityOrNull,
            List<SearchDomain> availableSearchDomains)
    {
        SearchDomain[] matchingSearchDomains = {};
        
        //this case is not used yet because no "All" selection possibility
        if (searchableEntityOrNull == null)
        {
        	matchingSearchDomains = (SearchDomain[]) availableSearchDomains.toArray(); 
        } else
        {
			//translate here what the user selected into a search domain object!
			for(SearchDomain searchDomain : availableSearchDomains){
				if (searchableEntityOrNull.getName().equals(searchDomain.getName())){ 
		        	matchingSearchDomains = new SearchDomain[] { searchDomain };
				}
			}

        }
        return matchingSearchDomains;
    }

    
}
