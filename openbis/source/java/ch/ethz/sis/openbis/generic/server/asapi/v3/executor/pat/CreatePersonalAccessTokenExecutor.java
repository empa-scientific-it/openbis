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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.IMapPersonByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.security.TokenGenerator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
@Component
public class CreatePersonalAccessTokenExecutor implements ICreatePersonalAccessTokenExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IPersonalAccessTokenAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapPersonByIdExecutor mapPersonByIdExecutor;

    @Override public List<PersonalAccessTokenPermId> create(final IOperationContext context,
            final List<PersonalAccessTokenCreation> creations)
    {
        if (creations == null)
        {
            throw new UserFailureException("Creations cannot be null");
        }

        final PersonPE person = context.getSession().tryGetPerson();
        final Date now = new Date();
        final List<PersonalAccessTokenPermId> ids = new ArrayList<>();

        for (PersonalAccessTokenCreation creation : creations)
        {
            checkData(creation);

            PersonalAccessToken token = new PersonalAccessToken();
            token.setHash(new TokenGenerator().getNewToken(now.getTime()));
            token.setSessionName(creation.getSessionName());

            if (creation.getOwnerId() == null)
            {
                token.setOwnerId(person.getUserId());
            } else
            {
                Map<IPersonId, PersonPE> map = mapPersonByIdExecutor.map(context, Collections.singleton(creation.getOwnerId()));
                PersonPE owner = map.get(creation.getOwnerId());

                if (owner == null)
                {
                    throw new ObjectNotFoundException(creation.getOwnerId());
                } else
                {
                    token.setOwnerId(owner.getUserId());
                }
            }

            token.setRegistratorId(person.getUserId());
            token.setModifierId(person.getUserId());
            token.setValidFromDate(creation.getValidFromDate());
            token.setValidToDate(creation.getValidToDate());
            token.setRegistrationDate(now);
            token.setModificationDate(now);

            authorizationExecutor.canCreate(context, token);

            daoFactory.getPersonalAccessTokenDAO().createToken(token);

            ids.add(new PersonalAccessTokenPermId(token.getHash()));
        }

        return ids;
    }

    private void checkData(final PersonalAccessTokenCreation creation)
    {
        if (creation.getSessionName() == null)
        {
            throw new UserFailureException("Session name cannot be null.");
        }

        if (creation.getValidFromDate() == null)
        {
            throw new UserFailureException("Valid from date cannot be null.");
        }

        if (creation.getValidToDate() == null)
        {
            throw new UserFailureException("Valid to date cannot be null.");
        }

        if (creation.getValidFromDate().after(creation.getValidToDate()))
        {
            throw new UserFailureException("Valid from date cannot be after valid to date.");
        }
    }

}
