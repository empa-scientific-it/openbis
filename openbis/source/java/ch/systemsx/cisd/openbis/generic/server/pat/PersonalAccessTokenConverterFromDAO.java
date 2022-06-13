package ch.systemsx.cisd.openbis.generic.server.pat;

import java.util.Date;

import ch.systemsx.cisd.authentication.pat.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.authentication.pat.PersonalAccessTokenSession;

public class PersonalAccessTokenConverterFromDAO extends AbstractPersonalAccessTokenConverter
{

    private final IPersonalAccessTokenDAO dao;

    public PersonalAccessTokenConverterFromDAO(IPersonalAccessTokenDAO dao)
    {
        this.dao = dao;
    }

    @Override protected PersonalAccessToken getTokenByHash(final String tokenHash)
    {
        return dao.getTokenByHash(tokenHash);
    }

    @Override protected void touchToken(final String tokenHash, final Date date)
    {
        PersonalAccessToken token = dao.getTokenByHash(tokenHash);
        if (token != null)
        {
            token.setLastAccessedAt(date);
            dao.updateToken(token);
        }
    }

    @Override protected PersonalAccessTokenSession getSessionByUserIdAndSessionName(final String userId, final String sessionName)
    {
        return dao.getSessionByUserIdAndSessionName(userId, sessionName);
    }
}
