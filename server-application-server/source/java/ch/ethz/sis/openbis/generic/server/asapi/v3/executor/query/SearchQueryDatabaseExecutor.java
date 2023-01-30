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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QueryDatabaseSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProviderAutoInitialized;

/**
 * @author pkupczyk
 */
@Component
public class SearchQueryDatabaseExecutor extends AbstractSearchObjectManuallyExecutor<QueryDatabaseSearchCriteria, DatabaseDefinition> implements
        ISearchQueryDatabaseExecutor
{

    @Autowired
    private IQueryDatabaseAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IQueryDatabaseDefinitionProviderAutoInitialized databaseProvider;

    @Override
    public List<DatabaseDefinition> search(IOperationContext context, QueryDatabaseSearchCriteria criteria)
    {
        authorizationExecutor.canGet(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<DatabaseDefinition> listAll()
    {
        List<DatabaseDefinition> definitions = new ArrayList<DatabaseDefinition>(databaseProvider.getAllDefinitions());
        definitions.sort(new Comparator<DatabaseDefinition>()
            {
                @Override
                public int compare(DatabaseDefinition o1, DatabaseDefinition o2)
                {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
        return definitions;
    }

    @Override
    protected Matcher<DatabaseDefinition> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<DatabaseDefinition>
    {

        @Override
        protected boolean isMatching(IOperationContext context, DatabaseDefinition object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof QueryDatabaseName)
            {
                return id.equals(new QueryDatabaseName(object.getKey()));
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

}
