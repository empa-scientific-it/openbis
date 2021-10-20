/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.cache;

import java.io.Serializable;

/**
 * @author pkupczyk
 */
public class SearchCacheKey<CRITERIA, FETCH_OPTIONS> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String sessionToken;

    private CRITERIA criteria;

    private FETCH_OPTIONS fetchOptions;

    public SearchCacheKey(String sessionToken, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        this.sessionToken = sessionToken;
        this.criteria = criteria;
        this.fetchOptions = fetchOptions;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public CRITERIA getCriteria()
    {
        return criteria;
    }

    public FETCH_OPTIONS getFetchOptions()
    {
        return fetchOptions;
    }

    @Override
    public int hashCode()
    {
        return sessionToken.hashCode() + criteria.getClass().hashCode() + fetchOptions.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        SearchCacheKey<?, ?> other = (SearchCacheKey<?, ?>) obj;
        
        if (sessionToken.equals(other.sessionToken) && criteria.equals(other.criteria))
        {
            FetchOptionsMatcher matcher = new FetchOptionsMatcher();
            return matcher.areMatching(fetchOptions, other.fetchOptions);
        } else
        {
            return false;
        }
    }

    @Override public String toString()
    {
        return "SearchCacheKey{" +
                "sessionToken='" + sessionToken + '\'' +
                ", criteria=" + criteria +
                ", fetchOptions=" + fetchOptions +
                '}';
    }
}
