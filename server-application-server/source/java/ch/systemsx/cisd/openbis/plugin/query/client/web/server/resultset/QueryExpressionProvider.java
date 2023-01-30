/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.ENTITY_TYPE;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.IS_PUBLIC;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.NAME;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.QUERY_DATABASE;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.QUERY_TYPE;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.plugin.query.client.web.client.dto.QueryBrowserGridColumnIDs.SQL_QUERY;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;

/**
 * Provider of {@link QueryExpression} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class QueryExpressionProvider extends AbstractTableModelProvider<QueryExpression>
{
    private final IQueryServer server;

    private final String sessionToken;

    public QueryExpressionProvider(IQueryServer server, String sessionToken)
    {
        this.server = server;
        this.sessionToken = sessionToken;
    }

    @Override
    protected TypedTableModel<QueryExpression> createTableModel()
    {
        List<QueryExpression> expressions =
                server.listQueries(sessionToken, QueryType.UNSPECIFIED, BasicEntityType.UNSPECIFIED);
        TypedTableModelBuilder<QueryExpression> builder =
                new TypedTableModelBuilder<QueryExpression>();
        builder.addColumn(NAME);
        builder.addColumn(DESCRIPTION);
        builder.addColumn(SQL_QUERY).hideByDefault();
        builder.addColumn(IS_PUBLIC).hideByDefault();
        builder.addColumn(QUERY_TYPE).hideByDefault();
        builder.addColumn(ENTITY_TYPE).hideByDefault();
        builder.addColumn(QUERY_DATABASE).hideByDefault();
        builder.addColumn(REGISTRATOR).hideByDefault();
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        for (QueryExpression expression : expressions)
        {
            builder.addRow(expression);
            builder.column(NAME).addString(expression.getName());
            builder.column(DESCRIPTION).addString(expression.getDescription());
            builder.column(SQL_QUERY).addString(expression.getExpression());
            builder.column(IS_PUBLIC).addString(SimpleYesNoRenderer.render(expression.isPublic()));
            builder.column(QUERY_TYPE).addString(expression.getQueryType().name());
            builder.column(ENTITY_TYPE).addString(expression.getEntityTypeCode());
            builder.column(QUERY_DATABASE).addString(expression.getQueryDatabaseLabel());
            builder.column(REGISTRATOR).addPerson(expression.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(expression.getRegistrationDate());
            builder.column(MODIFICATION_DATE).addDate(expression.getModificationDate());
        }
        return builder.getModel();
    }

}
