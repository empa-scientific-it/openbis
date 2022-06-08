package ch.systemsx.cisd.authentication.pat;

import java.util.List;

public interface IPersonalAccessTokenDAO
{

    List<PersonalAccessToken> listTokens();

    PersonalAccessToken getTokenByHash(String hash);

    void createToken(PersonalAccessToken creation);

    void updateToken(PersonalAccessToken update);

    void deleteToken(String hash);

    PersonalAccessTokenSession getSessionByHash(String hash);

    PersonalAccessTokenSession getSessionByUserIdAndSessionName(String userId, String sessionName);

    void createSession(PersonalAccessTokenSession creation);

    void updateSession(PersonalAccessTokenSession update);

    void deleteSession(String hash);

}
