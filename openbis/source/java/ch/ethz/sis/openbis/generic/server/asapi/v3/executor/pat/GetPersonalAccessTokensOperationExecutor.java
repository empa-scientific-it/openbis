/*
 * Copyright 2016 ETH Zuerich, CISD
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.get.GetPersonalAccessTokensOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.get.GetPersonalAccessTokensOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.pat.IPersonalAccessTokenTranslator;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
@Component
public class GetPersonalAccessTokensOperationExecutor
        extends OperationExecutor<GetPersonalAccessTokensOperation, GetPersonalAccessTokensOperationResult>
        implements IGetPersonalAccessTokensOperationExecutor
{

    @Autowired
    private IPersonalAccessTokenAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IPersonalAccessTokenTranslator translator;

    @Override protected Class<? extends GetPersonalAccessTokensOperation> getOperationClass()
    {
        return GetPersonalAccessTokensOperation.class;
    }

    @Override protected GetPersonalAccessTokensOperationResult doExecute(final IOperationContext context,
            final GetPersonalAccessTokensOperation operation)
    {
        authorizationExecutor.canGet(context);

        List<? extends IPersonalAccessTokenId> ids = operation.getObjectIds();
        PersonalAccessTokenFetchOptions fetchOptions = operation.getFetchOptions();

        if (ids == null)
        {
            throw new UserFailureException("Ids cannot be null");
        }
        if (fetchOptions == null)
        {
            throw new UserFailureException("Fetch options cannot be null");
        }

        List<PersonalAccessToken> pats = new ArrayList<>();

        for (IPersonalAccessTokenId id : ids)
        {
            if (id instanceof PersonalAccessTokenPermId)
            {
                PersonalAccessToken pat = daoFactory.getPersonalAccessTokenDAO().getTokenByHash(((PersonalAccessTokenPermId) id).getPermId());
                if (pat != null)
                {
                    pats.add(pat);
                }
            } else
            {
                throw new UserFailureException("Unsupported id: " + id.getClass());
            }
        }

        if (pats.isEmpty())
        {
            return new GetPersonalAccessTokensOperationResult(Collections.emptyMap());
        } else
        {
            TranslationContext translationContext = new TranslationContext(context.getSession());
            Map<PersonalAccessToken, ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken> patsV3 =
                    translator.translate(translationContext, pats, fetchOptions);

            Map<IPersonalAccessTokenId, ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken> result = new HashMap<>();
            for (ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken patV3 : patsV3.values())
            {
                result.put(patV3.getPermId(), patV3);
            }

            return new GetPersonalAccessTokensOperationResult(result);
        }
    }
}
