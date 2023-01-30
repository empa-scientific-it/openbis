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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query.IQueryDatabaseAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space.ISpaceTranslator;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author pkupczyk
 */
@Component
public class QueryDatabaseTranslator extends AbstractCachingTranslator<DatabaseDefinition, QueryDatabase, QueryDatabaseFetchOptions>
        implements IQueryDatabaseTranslator
{

    @Autowired
    private IQueryDatabaseAuthorizationExecutor authorizationExecutor;

    @Autowired
    private ISpaceTranslator spaceTranslator;

    @Override
    protected Object getObjectId(DatabaseDefinition input)
    {
        return input.getKey();
    }

    @Override
    protected boolean shouldTranslate(TranslationContext context, DatabaseDefinition input, QueryDatabaseFetchOptions fetchOptions)
    {
        try
        {
            authorizationExecutor.canRead(new OperationContext(context.getSession()), input.getKey());
            return true;
        } catch (AuthorizationFailureException e)
        {
            return false;
        }
    }

    protected QueryDatabase createObject(TranslationContext context, DatabaseDefinition databaseDefinition, QueryDatabaseFetchOptions fetchOptions)
    {
        QueryDatabase database = new QueryDatabase();
        database.setFetchOptions(new QueryDatabaseFetchOptions());
        database.setPermId(new QueryDatabaseName(databaseDefinition.getKey()));
        database.setName(databaseDefinition.getKey());
        database.setLabel(databaseDefinition.getLabel());

        RoleWithHierarchy creatorMinimalRole = databaseDefinition.getCreatorMinimalRole();
        if (creatorMinimalRole != null)
        {
            database.setCreatorMinimalRole(Role.valueOf(creatorMinimalRole.getRoleCode().name()));
            database.setCreatorMinimalRoleLevel(RoleLevel.valueOf(creatorMinimalRole.getRoleLevel().name()));
        }

        return database;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<DatabaseDefinition> databaseDefinitions,
            QueryDatabaseFetchOptions fetchOptions)
    {
        Map<Class<?>, Object> relationsMap = new HashMap<>();

        if (fetchOptions.hasSpace())
        {
            Collection<Long> spaceIds = new HashSet<>();

            for (DatabaseDefinition databaseDefinition : databaseDefinitions)
            {
                if (databaseDefinition.tryGetDataSpace() != null)
                {
                    spaceIds.add(databaseDefinition.tryGetDataSpace().getId());
                }
            }

            Map<Long, Space> spaceMap = spaceTranslator.translate(context, spaceIds, fetchOptions.withSpace());
            relationsMap.put(Space.class, spaceMap);
        }

        return relationsMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateObject(TranslationContext context, DatabaseDefinition databaseDefinition, QueryDatabase result, Object objectRelations,
            QueryDatabaseFetchOptions fetchOptions)
    {
        Map<Class<?>, Object> relationsMap = (Map<Class<?>, Object>) objectRelations;

        if (fetchOptions.hasSpace())
        {
            Map<Long, Space> spaceMap = (Map<Long, Space>) relationsMap.get(Space.class);

            if (databaseDefinition.tryGetDataSpace() != null)
            {
                result.setSpace(spaceMap.get(databaseDefinition.tryGetDataSpace().getId()));
            }

            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }
    }

}
