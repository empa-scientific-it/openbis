/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainService;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.service.fetchoptions.SearchDomainServiceFetchOptions")
public class SearchDomainServiceFetchOptions extends FetchOptions<SearchDomainService> implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty
    private SearchDomainServiceSortOptions sort;

    @Override
    public SortOptions<SearchDomainService> sortBy()
    {
        if (sort == null)
        {
            sort = new SearchDomainServiceSortOptions();
        }
        return sort;
    }

    @Override
    public SortOptions<SearchDomainService> getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("SearchDomainService", this);
        return f;
    }

}
