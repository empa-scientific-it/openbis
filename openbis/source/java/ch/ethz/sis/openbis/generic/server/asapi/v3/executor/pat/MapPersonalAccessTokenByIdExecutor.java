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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.pat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query.IQueryAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.pat.ListPersonalAccessTokenByPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.query.ListQueryByName;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.query.ListQueryByTechId;
import ch.systemsx.cisd.authentication.pat.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
@Component
public class MapPersonalAccessTokenByIdExecutor extends AbstractMapObjectByIdExecutor<IPersonalAccessTokenId, PersonalAccessToken>
        implements IMapPersonalAccessTokenByIdExecutor
{

    private IPersonalAccessTokenDAO personalAccessTokenDAO;

    @Autowired
    private IPersonalAccessTokenAuthorizationExecutor authorizationExecutor;

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canGet(context);
    }

    @Override
    protected void addListers(IOperationContext context, List<IListObjectById<? extends IPersonalAccessTokenId, PersonalAccessToken>> listers)
    {
        listers.add(new ListPersonalAccessTokenByPermId(personalAccessTokenDAO));
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        personalAccessTokenDAO = daoFactory.getPersonalAccessTokenDAO();
    }

}
