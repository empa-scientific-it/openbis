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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;

/**
 * Result of a search in a search domain.
 * 
 * @author Franz-Josef Elmer
 */
public class SearchDomainSearchResultWithFullEntity implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IEntityInformationHolderWithIdentifier entity;

    private SearchDomainSearchResult searchResult;

    public IEntityInformationHolderWithIdentifier getEntity()
    {
        return entity;
    }

    public void setEntity(IEntityInformationHolderWithIdentifier entity)
    {
        this.entity = entity;
    }

    public SearchDomainSearchResult getSearchResult()
    {
        return searchResult;
    }

    public void setSearchResult(SearchDomainSearchResult searchResult)
    {
        this.searchResult = searchResult;
    }

    @Override
    public String toString()
    {
        return searchResult.toString();
    }
}
