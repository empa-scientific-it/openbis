package ch.systemsx.cisd.authentication.pat;

import java.util.List;

public interface IPersonalAccessTokenDAO
{

    List<PersonalAccessToken> listTokens();

    PersonalAccessToken getTokenByHash(String hash);

    void createToken(PersonalAccessToken token);

    void updateToken(PersonalAccessToken token);

    void deleteToken(String hash);

    List<PersonalAccessTokenSession> listSessions();

    PersonalAccessTokenSession getSessionByUserIdAndSessionName(String userId, String sessionName);

}
