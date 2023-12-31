/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.delete.PersonalAccessTokenDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;

/**
 * @author pkupczyk
 */
@Component
public class DeletePersonalAccessTokenExecutor
        extends AbstractDeleteEntityExecutor<Void, IPersonalAccessTokenId, PersonalAccessToken, PersonalAccessTokenDeletionOptions>
        implements IDeletePersonalAccessTokenExecutor
{

    @Autowired
    private IMapPersonalAccessTokenByIdExecutor mapPersonalAccessTokenByIdExecutor;

    @Autowired
    private IPersonalAccessTokenAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IPersonalAccessTokenDAO personalAccessTokenDAO;

    @Override
    protected Map<IPersonalAccessTokenId, PersonalAccessToken> map(IOperationContext context, List<? extends IPersonalAccessTokenId> entityIds,
            PersonalAccessTokenDeletionOptions deletionOptions)
    {
        return mapPersonalAccessTokenByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IPersonalAccessTokenId entityId, PersonalAccessToken entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, PersonalAccessToken entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<PersonalAccessToken> tokens, PersonalAccessTokenDeletionOptions deletionOptions)
    {
        for (PersonalAccessToken token : tokens)
        {
            personalAccessTokenDAO.deleteToken(token.getHash());
        }

        return null;
    }

}