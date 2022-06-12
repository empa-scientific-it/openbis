package ch.systemsx.cisd.openbis.common.pat;

import java.util.Date;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.authentication.pat.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.authentication.pat.PersonalAccessTokenSession;

public abstract class PersonalAccessTokenInvocationHandler
{

    protected abstract IPersonalAccessTokenDAO getPersonalAccessTokenDAO();

    protected String toSessionToken(String sessionTokenOrPAT)
    {
        IPersonalAccessTokenDAO patDAO = getPersonalAccessTokenDAO();
        PersonalAccessToken patToken = patDAO.getTokenByHash(sessionTokenOrPAT);

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

            patToken.setLastAccessedAt(now);
            patDAO.updateToken(patToken);

            final PersonalAccessTokenSession patSession =
                    patDAO.getSessionByUserIdAndSessionName(patToken.getUserId(), patToken.getSessionName());

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
