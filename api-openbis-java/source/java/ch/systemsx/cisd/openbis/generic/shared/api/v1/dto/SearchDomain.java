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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A class specifying the search domain of a {@link SearchDomainSearchResult}.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("SearchDomain")
public class SearchDomain implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String dataStoreCode;

    private String name;

    private String label;
    
    private String possibleSearchOptionsKey;
    
    private List<SearchDomainSearchOption> possibleSearchOptions;
    
    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLabel()
    {
        return label == null ? name : label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getPossibleSearchOptionsKey()
    {
        return possibleSearchOptionsKey;
    }

    public void setPossibleSearchOptionsKey(String availableSearchOptionsKey)
    {
        this.possibleSearchOptionsKey = availableSearchOptionsKey;
    }

    public List<SearchDomainSearchOption> getPossibleSearchOptions()
    {
        return possibleSearchOptions;
    }

    public void setPossibleSearchOptions(List<SearchDomainSearchOption> possibleSearchOptions)
    {
        this.possibleSearchOptions = possibleSearchOptions;
    }
    
    @Override
    public String toString()
    {
        return label == null ? name : label + " [" + name + "]";
    }
}
