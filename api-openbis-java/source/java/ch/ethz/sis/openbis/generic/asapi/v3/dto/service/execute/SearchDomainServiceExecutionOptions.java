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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.service.execute.SearchDomainServiceExecutionOptions")
public class SearchDomainServiceExecutionOptions extends AbstractExecutionOptionsWithParameters<SearchDomainServiceExecutionOptions, String>
{
    private static final long serialVersionUID = 1L;

    private String preferredSearchDomain;
    
    private String searchString;
    
    public SearchDomainServiceExecutionOptions withPreferredSearchDomain(String preferredSearchDomain)
    {
        this.preferredSearchDomain = preferredSearchDomain;
        return this;
    }
    
    public String getPreferredSearchDomain()
    {
        return preferredSearchDomain;
    }

    public SearchDomainServiceExecutionOptions withSearchString(String searchString)
    {
        this.searchString = searchString;
        return this;
    }
    
    public String getSearchString()
    {
        return searchString;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(super.toString());
        if (searchString != null)
        {
            builder.append(", search string='").append(searchString).append("'");
        }
        if (preferredSearchDomain != null)
        {
            builder.append(", preferred serach domain=").append(preferredSearchDomain);
        }
        return builder.toString();
    }

}
