/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.pat;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;
import ch.systemsx.cisd.openbis.generic.shared.pat.AbstractPersonalAccessTokenConverter;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;

@Component
public class PersonalAccessTokenConverterFromDAO extends AbstractPersonalAccessTokenConverter
{

    @Autowired
    private IPersonalAccessTokenConfig config;

    @Autowired
    private IPersonalAccessTokenDAO personalAccessTokenDAO;

    @Override protected IPersonalAccessTokenConfig getConfig()
    {
        return config;
    }

    @Override protected PersonalAccessToken getToken(final String tokenHash)
    {
        return getPersonalAccessTokenDAO().getTokenByHash(tokenHash);
    }

    @Override protected void touchToken(final String tokenHash, final Date date)
    {
        PersonalAccessToken token = getPersonalAccessTokenDAO().getTokenByHash(tokenHash);
        if (token != null)
        {
            token.setAccessDate(date);
            getPersonalAccessTokenDAO().updateToken(token);
        }
    }

    @Override protected String getSessionToken(final String userId, final String sessionName)
    {
        PersonalAccessTokenSession session = getPersonalAccessTokenDAO().getSessionByUserIdAndSessionName(userId, sessionName);
        if (session != null)
        {
            return session.getHash();
        } else
        {
            return null;
        }
    }

    public IPersonalAccessTokenDAO getPersonalAccessTokenDAO()
    {
        return personalAccessTokenDAO;
    }
}
