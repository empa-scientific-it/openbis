package ch.systemsx.cisd.openbis.common.pat;

import java.util.Date;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.authentication.pat.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.authentication.pat.PersonalAccessTokenSession;

public class PersonalAccessTokenConverter
{

    private final IPersonalAccessTokenDAO dao;

    public PersonalAccessTokenConverter(IPersonalAccessTokenDAO dao)
    {
        this.dao = dao;
    }

    public String convert(String sessionTokenOrPAT)
    {
        PersonalAccessToken patToken = dao.getTokenByHash(sessionTokenOrPAT);

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
            dao.updateToken(patToken);

            final PersonalAccessTokenSession patSession =
                    dao.getSessionByUserIdAndSessionName(patToken.getUserId(), patToken.getSessionName());

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
