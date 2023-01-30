package ch.ethz.sis.afsserver.worker.providers;

import ch.ethz.sis.shared.startup.Configuration;

public interface AuthenticationInfoProvider {
    void init(Configuration initParameter) throws Exception;

    String login(String userId, String password);

    Boolean isSessionValid(String sessionToken);

    Boolean logout(String sessionToken);
}
