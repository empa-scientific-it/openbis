/*
 * Copyright 2014 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;

/**
 * @author pkupczyk
 */
@Component
public class UpdatePersonalAccessTokenExecutor implements IUpdatePersonalAccessTokenExecutor
{

    @Autowired
    private IPersonalAccessTokenDAO personalAccessTokenDAO;

    @Autowired
    private IPersonalAccessTokenAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapPersonalAccessTokenByIdExecutor mapPersonalAccessTokenByIdExecutor;

    @Override public List<PersonalAccessTokenPermId> update(final IOperationContext context,
            final List<PersonalAccessTokenUpdate> updates)
    {
        if (updates == null)
        {
            throw new UserFailureException("Updates cannot be null");
        }

        final PersonPE person = context.getSession().tryGetPerson();
        final Date now = new Date();
        final List<PersonalAccessTokenPermId> ids = new ArrayList<>();

        for (PersonalAccessTokenUpdate update : updates)
        {
            checkData(context, update);

            Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                    mapPersonalAccessTokenByIdExecutor.map(context, Collections.singleton(update.getPersonalAccessTokenId()));
            PersonalAccessToken token = map.get(update.getPersonalAccessTokenId());

            if (token == null)
            {
                throw new ObjectNotFoundException(update.getPersonalAccessTokenId());
            }

            authorizationExecutor.canUpdate(context, update.getPersonalAccessTokenId(), token);

            if (update.getAccessDate().isModified())
            {
                token.setAccessDate(update.getAccessDate().getValue());
            }

            token.setModifierId(person.getUserId());
            token.setModificationDate(now);

            personalAccessTokenDAO.updateToken(token);

            ids.add(new PersonalAccessTokenPermId(token.getHash()));
        }

        return ids;
    }

    private void checkData(final IOperationContext context, final PersonalAccessTokenUpdate update)
    {
        if (update.getPersonalAccessTokenId() == null)
        {
            throw new UserFailureException("Personal access token id cannot be null.");
        }
    }

}
