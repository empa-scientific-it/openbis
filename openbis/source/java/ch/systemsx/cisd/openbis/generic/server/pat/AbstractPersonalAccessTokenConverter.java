package ch.systemsx.cisd.openbis.generic.server.pat;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

public abstract class AbstractPersonalAccessTokenConverter implements IPersonalAccessTokenConverter
{

    abstract protected IPersonalAccessTokenConfig getConfig();

    abstract protected PersonalAccessToken getToken(String tokenHash);

    abstract protected void touchToken(String tokenHash, Date date);

    abstract protected String getSessionToken(String userId, String sessionName);

    public boolean shouldConvert(String sessionTokenOrPAT)
    {
        if (!getConfig().arePersonalAccessTokensEnabled())
        {
            return false;
        }

        if (sessionTokenOrPAT == null)
        {
            return false;
        }

        PersonalAccessToken patToken = getToken(sessionTokenOrPAT);
        return patToken != null;
    }

    public String convert(String sessionTokenOrPAT)
    {
        if (!getConfig().arePersonalAccessTokensEnabled())
        {
            return sessionTokenOrPAT;
        }

        if (sessionTokenOrPAT == null)
        {
            return null;
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
