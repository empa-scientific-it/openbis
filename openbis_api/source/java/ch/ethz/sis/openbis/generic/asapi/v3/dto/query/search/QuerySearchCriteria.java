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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DescriptionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.query.search.QuerySearchCriteria")
public class QuerySearchCriteria extends AbstractObjectSearchCriteria<IQueryId>
{

    private static final long serialVersionUID = 1L;

    public QuerySearchCriteria()
    {
    }

    public NameSearchCriteria withName()
    {
        return with(new NameSearchCriteria());
    }

    public QueryTypeSearchCriteria withQueryType()
    {
        return with(new QueryTypeSearchCriteria());
    }

    public DatabaseIdSearchCriteria withDatabaseId()
    {
        return with(new DatabaseIdSearchCriteria());
    }

    public DescriptionSearchCriteria withDescription()
    {
        return with(new DescriptionSearchCriteria());
    }

    public EntityTypeCodePatternSearchCriteria withEntityTypeCodePattern()
    {
        return with(new EntityTypeCodePatternSearchCriteria());
    }

    public SqlSearchCriteria withSql()
    {
        return with(new SqlSearchCriteria());
    }

    public QuerySearchCriteria withOrOperator()
    {
        return (QuerySearchCriteria) withOperator(SearchOperator.OR);
    }

    public QuerySearchCriteria withAndOperator()
    {
        return (QuerySearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("QUERY");
        return builder;
    }

}
