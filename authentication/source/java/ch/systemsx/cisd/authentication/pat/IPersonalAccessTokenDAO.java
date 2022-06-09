package ch.systemsx.cisd.authentication.pat;

import java.util.List;

public interface IPersonalAccessTokenDAO
{

    List<PersonalAccessToken> listTokens();

    PersonalAccessToken getTokenByHash(String hash);

    void updateToken(PersonalAccessToken patToken);

    List<PersonalAccessTokenSession> listSessions();

    PersonalAccessTokenSession getSessionByUserIdAndSessionName(String userId, String sessionName);


}
