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
package ch.systemsx.cisd.openbis.generic.shared.pat;

import java.util.Date;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenHash;

public abstract class AbstractPersonalAccessTokenConverter implements IPersonalAccessTokenConverter
{

    abstract protected IPersonalAccessTokenConfig getConfig();

    abstract protected PersonalAccessToken getToken(String tokenHash);

    abstract protected void touchToken(String tokenHash, Date date);

    abstract protected String getSessionToken(String userId, String sessionName);

    public boolean shouldConvert(String sessionTokenOrPAT)
    {
        if (!getConfig().arePersonalAccessTokensEnabled() || !PersonalAccessTokenHash.isValid(sessionTokenOrPAT))
        {
            return false;
        }

        PersonalAccessToken patToken = getToken(sessionTokenOrPAT);
        return patToken != null;
    }

    public String convert(String sessionTokenOrPAT)
    {
        if (!getConfig().arePersonalAccessTokensEnabled() || !PersonalAccessTokenHash.isValid(sessionTokenOrPAT))
        {
            return sessionTokenOrPAT;
        }

        PersonalAccessToken patToken = getToken(sessionTokenOrPAT);

        if (patToken == null)
        {
            return sessionTokenOrPAT;
        } else
        {
            Date now = new Date();

            if (now.before(patToken.getValidFromDate()))
            {
                throw new InvalidSessionException("Personal access token is not yet valid.");
            }

            if (now.after(patToken.getValidToDate()))
            {
                throw new InvalidSessionException("Personal access token is no longer valid.");
            }

            touchToken(patToken.getHash(), now);

            final String patSessionToken =
                    getSessionToken(patToken.getOwnerId(), patToken.getSessionName());

            if (patSessionToken == null)
            {
                throw new InvalidSessionException("Personal access token session does not exist.");
            } else
            {
                return patSessionToken;
            }
        }
    }

}
