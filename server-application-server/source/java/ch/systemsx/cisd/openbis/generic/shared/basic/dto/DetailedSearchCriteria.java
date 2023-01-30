/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.List;

/**
 * Describes detailed search criteria specific to an entity.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchCriteria implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<DetailedSearchCriterion> criteria;

    private SearchCriteriaConnection connection;

    private boolean useWildcardSearchMode;

    private List<DetailedSearchSubCriteria> subCriterias =
            new ArrayList<DetailedSearchSubCriteria>();

    public DetailedSearchCriteria()
    {
    }

    public List<DetailedSearchCriterion> getCriteria()
    {
        return criteria;
    }

    public void setCriteria(List<DetailedSearchCriterion> criteria)
    {
        this.criteria = criteria;
    }

    public List<DetailedSearchSubCriteria> getSubCriterias()
    {
        return subCriterias;
    }

    public void setSubCriterias(List<DetailedSearchSubCriteria> subCriterias)
    {
        this.subCriterias = subCriterias;
    }

    public void addSubCriteria(DetailedSearchSubCriteria subCriteria)
    {
        subCriterias.add(subCriteria);
    }

    public SearchCriteriaConnection getConnection()
    {
        return connection;
    }

    public void setConnection(SearchCriteriaConnection connection)
    {
        this.connection = connection;
    }

    public boolean isUseWildcardSearchMode()
    {
        return useWildcardSearchMode;
    }

    public void setUseWildcardSearchMode(boolean useWildcardSearchMode)
    {
        this.useWildcardSearchMode = useWildcardSearchMode;
    }

    public boolean isEmpty()
    {
        return (criteria == null || criteria.isEmpty()) && subCriterias.isEmpty();
    }

    String toString(boolean withWildcardInfo)
    {
        final StringBuilder sb = new StringBuilder();
        if (criteria != null)
        {
            for (final DetailedSearchCriterion element : criteria)
            {
                if (sb.length() > 0)
                {
                    sb.append(" " + getConnection().getLabel() + " ");
                }
                sb.append(element);
            }
        }
        for (DetailedSearchSubCriteria subCriteria : subCriterias)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            sb.append("[" + subCriteria.toString() + "]");
        }
        if (withWildcardInfo)
        {
            sb.append(" (" + (isUseWildcardSearchMode() ? "with" : "without") + " wildcards)");
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return toString(true);
    }
}
