package ch.systemsx.cisd.openbis.generic.server.pat;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;

public class PersonalAccessTokenConverterFromDAO extends AbstractPersonalAccessTokenConverter
{

    private final IPersonalAccessTokenDAO dao;

    public PersonalAccessTokenConverterFromDAO(IPersonalAccessTokenDAO dao)
    {
        this.dao = dao;
    }

    @Override protected PersonalAccessToken getToken(final String tokenHash)
    {
        return dao.getTokenByHash(tokenHash);
    }

    @Override protected void touchToken(final String tokenHash, final Date date)
    {
        PersonalAccessToken token = dao.getTokenByHash(tokenHash);
        if (token != null)
        {
            token.setAccessDate(date);
            dao.updateToken(token);
        }
    }

    @Override protected String getSessionToken(final String userId, final String sessionName)
    {
        PersonalAccessTokenSession session = dao.getSessionByUserIdAndSessionName(userId, sessionName);
        if (session != null)
        {
            return session.getHash();
        } else
        {
            return null;
        }
    }
}
