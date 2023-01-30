/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DescriptionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.DatabaseIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.EntityTypeCodePatternSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QuerySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QueryTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SqlSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchQueryExecutor extends AbstractSearchObjectManuallyExecutor<QuerySearchCriteria, QueryPE> implements
        ISearchQueryExecutor
{

    @Autowired
    private IQueryAuthorizationExecutor authorizationExecutor;

    @Override
    public List<QueryPE> search(IOperationContext context, QuerySearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<QueryPE> listAll()
    {
        return daoFactory.getQueryDAO().listAllEntities();
    }

    @Override
    protected Matcher<QueryPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof DatabaseIdSearchCriteria)
        {
            return new DatabaseIdMatcher();
        } else if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof NameSearchCriteria)
        {
            return new NameMatcher();
        } else if (criteria instanceof QueryTypeSearchCriteria)
        {
            return new QueryTypeMatcher();
        } else if (criteria instanceof DescriptionSearchCriteria)
        {
            return new DescriptionMatcher();
        } else if (criteria instanceof EntityTypeCodePatternSearchCriteria)
        {
            return new EntityTypeCodePatternMatcher();
        } else if (criteria instanceof SqlSearchCriteria)
        {
            return new SqlMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<QueryPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, QueryPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof QueryTechId)
            {
                return id.equals(new QueryTechId(object.getId()));
            } else if (id instanceof QueryName)
            {
                return id.equals(new QueryName(object.getName()));
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private class DatabaseIdMatcher extends SimpleFieldMatcher<QueryPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, QueryPE object, ISearchCriteria criteria)
        {
            Object id = ((DatabaseIdSearchCriteria) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof QueryDatabaseName)
            {
                return id.equals(new QueryDatabaseName(object.getQueryDatabaseKey()));
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private class NameMatcher extends StringFieldMatcher<QueryPE>
    {

        @Override
        protected String getFieldValue(QueryPE object)
        {
            return object.getName();
        }

    }

    private class QueryTypeMatcher extends SimpleFieldMatcher<QueryPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, QueryPE object, ISearchCriteria criteria)
        {
            QueryTypeSearchCriteria queryTypeCriteria = (QueryTypeSearchCriteria) criteria;

            if (queryTypeCriteria.getFieldValue() == null)
            {
                return true;
            } else
            {
                return queryTypeCriteria.getFieldValue().name().equals(object.getQueryType().name());
            }
        }

    }

    private class EntityTypeCodePatternMatcher extends StringFieldMatcher<QueryPE>
    {

        @Override
        protected String getFieldValue(QueryPE object)
        {
            return object.getEntityTypeCodePattern();
        }

    }

    private class DescriptionMatcher extends StringFieldMatcher<QueryPE>
    {

        @Override
        protected String getFieldValue(QueryPE object)
        {
            return object.getDescription();
        }

    }

    private class SqlMatcher extends StringFieldMatcher<QueryPE>
    {

        @Override
        protected String getFieldValue(QueryPE object)
        {
            return object.getExpression();
        }

    }

}
