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

package ch.systemsx.cisd.openbis.plugin.query.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.QueryAccessController;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.IQueryUpdates;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @see QueryAccessController for authorization
 * @author Franz-Josef Elmer
 */
public interface IQueryServer extends IServer
{

    @Transactional(readOnly = true)
    public int initDatabases(String sessionToken);

    @Transactional(readOnly = true)
    public List<QueryDatabase> listQueryDatabases(String sessionToken);

    @Transactional(readOnly = true)
    public TableModel queryDatabase(String sessionToken, QueryDatabase database, String sqlQuery,
            QueryParameterBindings bindings, boolean onlyPerform);

    @Transactional(readOnly = true)
    public TableModel queryDatabase(String sessionToken, TechId queryId,
            QueryParameterBindings bindings);

    @Transactional(readOnly = true)
    public List<QueryExpression> listQueries(String sessionToken, QueryType queryType,
            BasicEntityType entityTypeOrNull);

    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.QUERY)
    public void registerQuery(String sessionToken, NewQuery expression);

    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.QUERY)
    public void deleteQueries(String sessionToken, List<TechId> queryIds);

    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.QUERY)
    public void updateQuery(String sessionToken, IQueryUpdates updates);
}
