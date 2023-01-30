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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

import java.util.*;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;

public class PostgresAuthorisationInformationProviderDAO implements ISQLAuthorisationInformationProviderDAO
{

    private ISQLExecutor executor;

    public PostgresAuthorisationInformationProviderDAO(ISQLExecutor executor)
    {
        this.executor = executor;
    }

    @Override
    public Set<Long> getAuthorisedSamples(final Set<Long> requestedIDs, final AuthorisationInformation authInfo)
    {
        if (requestedIDs.isEmpty())
        {
            return requestedIDs;
        }

        final String query = SELECT + SP + ID_COLUMN + NL +
                FROM + SP + TableMapper.SAMPLE.getEntitiesTable() + NL +
                WHERE + SP + ID_COLUMN + SP + IN + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + AND + NL +
                LP + SPACE_COLUMN + SP + IN + SP + LP +
                SELECT + SP + UNNEST + LP + QU + RP + RP + SP + OR + NL +
                PROJECT_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + OR + SP +
                EXPERIMENT_COLUMN + SP + IN + SP + LP +
                        SELECT + SP + ID_COLUMN + SP +
                        FROM + SP + TableMapper.EXPERIMENT.getEntitiesTable() + SP +
                        WHERE + SP + PROJECT_COLUMN + SP + IN + SP + SELECT_UNNEST + RP + OR + NL +
                SPACE_COLUMN + SP + IS_NULL + SP + AND + SP + PROJECT_COLUMN + SP + IS_NULL + RP;
        final Long[] projectIds = authInfo.getProjectIds().toArray(new Long[0]);
        final List<Object> args = Arrays.asList(requestedIDs.toArray(new Long[0]), authInfo.getSpaceIds().toArray(new Long[0]),
                projectIds, projectIds);
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        return collectIDs(queryResultList);
    }

    @Override
    public Set<Long> getAuthorisedExperiments(final Set<Long> requestedIDs, final AuthorisationInformation authInfo)
    {
        if (requestedIDs.isEmpty())
        {
            return requestedIDs;
        }

        final String e = "e";
        final String p = "p";
        final String query = SELECT + SP + e + PERIOD + ID_COLUMN + NL +
                FROM + SP + TableMapper.EXPERIMENT.getEntitiesTable() + SP + e + NL +
                INNER_JOIN + SP + PROJECTS_TABLE + SP + p + SP + ON + SP + p + PERIOD + ID_COLUMN + SP + EQ + SP + e + PERIOD + PROJECT_COLUMN + SP +
                WHERE + SP + e + PERIOD + ID_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + AND +
                SP + LP + p + PERIOD + SPACE_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + OR +
                SP + e + PERIOD + PROJECT_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + RP;

        final List<Object> args = Arrays.asList(requestedIDs.toArray(new Long[0]), authInfo.getSpaceIds().toArray(new Long[0]),
                authInfo.getProjectIds().toArray(new Long[0]));
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        return collectIDs(queryResultList);
    }

    @Override
    public Set<Long> getAuthorisedProjects(final Set<Long> requestedIDs, final AuthorisationInformation authInfo)
    {
        if (requestedIDs.isEmpty())
        {
            return requestedIDs;
        }

        final String p = "p";
        final String query = SELECT + SP + p + PERIOD + ID_COLUMN + NL +
                FROM + SP + TableMapper.PROJECT.getEntitiesTable() + SP + p + NL +
                WHERE + SP + p + PERIOD + ID_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + AND +
                SP + LP + p + PERIOD + SPACE_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + OR +
                SP + p + PERIOD + ID_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + RP;

        final List<Object> args = Arrays.asList(requestedIDs.toArray(new Long[0]), authInfo.getSpaceIds().toArray(new Long[0]),
                authInfo.getProjectIds().toArray(new Long[0]));
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        return collectIDs(queryResultList);
    }

    @Override
    public Set<Long> getAuthorisedSpaces(final Set<Long> requestedIDs, final AuthorisationInformation authInfo)
    {
        if (requestedIDs.isEmpty())
        {
            return requestedIDs;
        }

        final String s = "s";
        final String p = "p";
        final String query = SELECT + SP + s + PERIOD + ID_COLUMN + NL +
                FROM + SP + TableMapper.SPACE.getEntitiesTable() + SP + s + NL +
                LEFT_JOIN + SP + TableMapper.PROJECT.getEntitiesTable() + SP + p + SP +
                ON + SP + p + PERIOD + SPACE_COLUMN + SP + EQ + SP + s + PERIOD + ID_COLUMN + NL +
                WHERE + SP + s + PERIOD + ID_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + AND +
                SP + LP + s + PERIOD + ID_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + OR +
                SP + p + PERIOD + ID_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + RP;

        final List<Object> args = Arrays.asList(requestedIDs.toArray(new Long[0]), authInfo.getSpaceIds().toArray(new Long[0]),
                authInfo.getProjectIds().toArray(new Long[0]));
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);
                                                                         
        return collectIDs(queryResultList);
    }

    @Override
    public Set<Long> getAuthorisedDatasets(final Set<Long> requestedIDs, final AuthorisationInformation authInfo)
    {
        if (requestedIDs.isEmpty())
        {
            return requestedIDs;
        }

        final Long[] projectIds = authInfo.getProjectIds().toArray(new Long[0]);
        final Long[] spaceIds = authInfo.getSpaceIds().toArray(new Long[0]);
        final String d = "d";
        final String ep = "ep";
        final String sp = "sp";
        final String exp = "exp";
        final String samp = "samp";
        final String query = SELECT + SP + d + PERIOD + ID_COLUMN + NL +
                FROM + SP + TableMapper.DATA_SET.getEntitiesTable() + SP + d + NL +
                LEFT_JOIN + SP + TableMapper.EXPERIMENT.getEntitiesTable() + SP + exp + SP +
                ON + SP + d + PERIOD + EXPERIMENT_COLUMN + SP + EQ + SP + exp + PERIOD + ID_COLUMN + NL +
                LEFT_JOIN + SP + TableMapper.PROJECT.getEntitiesTable() + SP + ep + SP +
                ON + SP + exp + PERIOD + PROJECT_COLUMN + SP + EQ + SP + ep + PERIOD + ID_COLUMN + NL +
                LEFT_JOIN + SP + TableMapper.SAMPLE.getEntitiesTable() + SP + samp + SP +
                ON + SP + d + PERIOD + SAMPLE_COLUMN + SP + EQ + SP + samp + PERIOD + ID_COLUMN + NL +
                LEFT_JOIN + SP + TableMapper.PROJECT.getEntitiesTable() + SP + sp + SP +
                ON + SP + samp + PERIOD + PROJECT_COLUMN + SP + EQ + SP + sp + PERIOD + ID_COLUMN + NL +
                WHERE + SP + d + PERIOD + ID_COLUMN + SP + IN + SP + SELECT_UNNEST + SP + AND + SP + LP +
                ep + PERIOD + ID_COLUMN + SP + IN + SP + SELECT_UNNEST + SP + OR + SP +
                sp + PERIOD + ID_COLUMN + SP + IN + SP + SELECT_UNNEST + SP + OR + NL +
                ep + PERIOD + SPACE_COLUMN + SP + IN + SP + SELECT_UNNEST + SP + OR + SP + 
                samp + PERIOD + SPACE_COLUMN + SP + IN + SP + SELECT_UNNEST + SP + OR + SP + 
                sp + PERIOD + SPACE_COLUMN + SP + IN + SP + SELECT_UNNEST + RP;

        final List<Object> args = Arrays.asList(requestedIDs.toArray(new Long[0]), projectIds, projectIds,
                spaceIds, spaceIds, spaceIds);
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        return collectIDs(queryResultList);
    }

    @Override
    public Set<Long> getTagsOfUser(final Set<Long> requestedIDs, final Long userID)
    {
        if (requestedIDs.isEmpty())
        {
            return requestedIDs;
        }

        final String query = SELECT + SP + ID_COLUMN + SP + NL +
                FROM + SP + METAPROJECTS_TABLE + NL +
                WHERE + SP + OWNER_COLUMN + SP + EQ + SP + QU + SP +
                AND + SP + ID_COLUMN + SP + IN + LP + SELECT + SP + UNNEST + LP + QU + RP + RP;
        final List<Object> args = Arrays.asList(userID, requestedIDs.toArray(new Long[0]));
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);
        return collectIDs(queryResultList);
    }

    private Set<Long> collectIDs(final List<Map<String, Object>> queryResultList)
    {
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(ID_COLUMN)).collect(Collectors.toSet());
    }

}
