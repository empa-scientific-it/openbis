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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;

/**
 * Interface for a search domain service. Implementing classes should have a public constructor with two arguments: First is an instance of
 * {@link Properties} and second is an instance of {@link File} which points to the root of the data set store.
 *
 * @author Franz-Josef Elmer
 */
public interface ISearchDomainService
{
    /**
     * Returns the label of this search domain service instance. It can be used for human readable output. Will be used to populated
     * {@link SearchDomain} instances of the search result..
     */
    public String getLabel();
    
    /**
     * Returns the key used in <code>optionalParametersOrNull</code> if this service supports 
     * different {@link SearchDomainSearchOption}s.
     * 
     * @return <code>null</code> if no such options supported.
     */
    public String getPossibleSearchOptionsKey();
    
    /**
     * Returns all possible search options. The first option is the default option.
     * 
     * @return an empty list if {@link #getPossibleSearchOptionsKey()} return <code>null</code>.
     */
    public List<SearchDomainSearchOption> getPossibleSearchOptions();

    /**
     * Returns <code>true</code> if this service is available. For example, a local BLAST sequence search service is available if the external BLAST
     * tools are available.
     */
    public boolean isAvailable();

    /**
     * Searches this service for the specified search string.
     * 
     * @param optionalParametersOrNull Optional parameters which might be used. Can be <code>null</code>.
     * @return an empty list if nothing be found.
     */
    public List<SearchDomainSearchResult> search(String searchString, Map<String, String> optionalParametersOrNull);
}
