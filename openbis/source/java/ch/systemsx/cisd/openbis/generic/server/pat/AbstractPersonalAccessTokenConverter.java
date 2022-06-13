package ch.systemsx.cisd.openbis.generic.server.pat;

import java.util.Date;

import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.authentication.pat.PersonalAccessTokenSession;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

public abstract class AbstractPersonalAccessTokenConverter
{

    abstract protected PersonalAccessToken getTokenByHash(String tokenHash);

    abstract protected void touchToken(String tokenHash, Date date);

    abstract protected PersonalAccessTokenSession getSessionByUserIdAndSessionName(String userId, String sessionName);

    public String convert(String sessionTokenOrPAT)
    {
        PersonalAccessToken patToken = getTokenByHash(sessionTokenOrPAT);

        if (patToken == null)
        {
            return sessionTokenOrPAT;
        } else
        {
            Date now = new Date();

            if (now.before(patToken.getValidFrom()))
            {
                throw new InvalidSessionException("Personal access token is not yet valid.");
            }

            if (now.after(patToken.getValidUntil()))
            {
                throw new InvalidSessionException("Personal access token is no longer valid.");
            }

            touchToken(patToken.getHash(), now);

            final PersonalAccessTokenSession patSession =
                    getSessionByUserIdAndSessionName(patToken.getUserId(), patToken.getSessionName());

            if (patSession == null)
            {
                throw new InvalidSessionException("Personal access token session does not exist.");
            } else
            {
                return patSession.getHash();
            }
        }
    }

}
