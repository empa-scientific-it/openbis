package ch.systemsx.cisd.openbis.generic.server.pat;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;

@Component
public class PersonalAccessTokenConverterFromDAO extends AbstractPersonalAccessTokenConverter
{

    @Autowired
    private IPersonalAccessTokenConfig config;

    @Autowired
    private IDAOFactory daoFactory;

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
        return daoFactory.getPersonalAccessTokenDAO();
    }
}
