/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.ExpressionUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProvider;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;

/**
 * A {@link QueryExpression} &lt;---&gt; {@link QueryPE} translator.
 * 
 * @author Piotr Buczek
 */
public final class QueryTranslator
{

    private QueryTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<QueryExpression> translate(final List<QueryPE> queries,
            IQueryDatabaseDefinitionProvider dbDefinitionProvider)
    {
        final List<QueryExpression> result = new ArrayList<QueryExpression>();
        for (final QueryPE query : queries)
        {
            final String dbKey = query.getQueryDatabaseKey();
            final DatabaseDefinition database = dbDefinitionProvider.getDefinition(dbKey);
            if (database != null)
            {
                result.add(QueryTranslator.translate(query, database));
            } else
            {
                UserFailureException.fromTemplate("Query database '%s' is not defined.", dbKey);
            }
        }
        return result;
    }

    public final static QueryExpression translate(final QueryPE original,
            final DatabaseDefinition database)
    {
        if (original == null)
        {
            return null;
        }
        final QueryExpression result = new QueryExpression();
        result.setName(original.getName());
        result.setQueryDatabase(new QueryDatabase(database.getKey(), database.getLabel()));
        result.setQueryType(original.getQueryType());
        result.setEntityTypeCode(original.getEntityTypeCodePattern());
        result.setupParameters(ExpressionUtil.extractParameters(original.getExpression()));

        GridCustomExpressionTranslator.translateExpression(original, result);
        return result;
    }
}
