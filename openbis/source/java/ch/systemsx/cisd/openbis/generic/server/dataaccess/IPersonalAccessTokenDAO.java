package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;

public interface IPersonalAccessTokenDAO
{
    PersonalAccessToken createToken(PersonalAccessToken creation);

    void updateToken(PersonalAccessToken update);

    void deleteToken(String hash);

    List<PersonalAccessToken> listTokens();

    PersonalAccessToken getTokenByHash(String hash);

    List<PersonalAccessTokenSession> listSessions();

    PersonalAccessTokenSession getSessionByUserIdAndSessionName(String userId, String sessionName);

}
