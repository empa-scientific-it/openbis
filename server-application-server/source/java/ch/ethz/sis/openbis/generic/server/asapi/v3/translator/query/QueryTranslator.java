/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query;

import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProvider;

/**
 * @author pkupczyk
 */
@Component
public class QueryTranslator extends AbstractCachingTranslator<Long, Query, QueryFetchOptions> implements IQueryTranslator
{

    @Autowired
    private IQueryAuthorizationValidator authorizationValidator;

    @Autowired
    private IQueryRegistratorTranslator registratorTranslator;

    @Autowired
    private IQueryBaseTranslator baseTranslator;

    @Autowired
    private IQueryDatabaseDefinitionProvider databaseProvider;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> queryIds, QueryFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), queryIds);
    }

    @Override
    protected Query createObject(TranslationContext context, Long queryId, QueryFetchOptions fetchOptions)
    {
        Query query = new Query();
        query.setFetchOptions(new QueryFetchOptions());
        return query;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> queryIds, QueryFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IQueryBaseTranslator.class, baseTranslator.translate(context, queryIds, null));

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IQueryRegistratorTranslator.class, registratorTranslator.translate(context, queryIds, fetchOptions.withRegistrator()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long queryId, Query result, Object objectRelations, QueryFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        QueryBaseRecord baseRecord = relations.get(IQueryBaseTranslator.class, queryId);

        result.setPermId(new QueryTechId(baseRecord.id));
        result.setName(baseRecord.name);
        result.setDescription(baseRecord.description);
        result.setDatabaseId(new QueryDatabaseName(baseRecord.database));

        DatabaseDefinition database = databaseProvider.getDefinition(baseRecord.database);
        if (database != null)
        {
            result.setDatabaseLabel(database.getLabel());
        }

        result.setQueryType(QueryType.valueOf(baseRecord.queryType));
        result.setEntityTypeCodePattern(baseRecord.entityTypeCodePattern);
        result.setSql(baseRecord.sql);
        result.setPublic(baseRecord.isPublic);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setModificationDate(baseRecord.modificationDate);

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IQueryRegistratorTranslator.class, queryId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
    }

}
