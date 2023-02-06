/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.search.AbstractCompositeSearchCriteria")
public abstract class AbstractCompositeSearchCriteria extends AbstractSearchCriteria
{

    private static final long serialVersionUID = 1L;

    protected Collection<ISearchCriteria> criteria = new LinkedList<ISearchCriteria>();

    protected SearchOperator operator = SearchOperator.AND;

    public Collection<ISearchCriteria> getCriteria()
    {
        return Collections.<ISearchCriteria> unmodifiableCollection(criteria);
    }

    public void setCriteria(Collection<ISearchCriteria> criteria)
    {
        if (criteria == null)
        {
            this.criteria.clear();
        } else
        {
            this.criteria = criteria;
        }
    }

    protected <T extends ISearchCriteria> T with(T criterion)
    {
        criteria.add(criterion);
        return criterion;
    }

    public AbstractCompositeSearchCriteria withOperator(SearchOperator anOperator)
    {
        this.operator = anOperator;
        return this;
    }

    public SearchOperator getOperator()
    {
        return operator;
    }

    @Override
    public String toString()
    {
        return toString("");
    }

    protected final String toString(String indentation)
    {
        return createBuilder().toString(indentation);
    }

    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = new SearchCriteriaToStringBuilder();
        builder.setCriteria(criteria);
        builder.setOperator(operator);
        return builder;
    }

}
