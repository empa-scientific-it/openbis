package ch.ethz.sis.afsserver.worker.providers.dummy;

import ch.ethz.sis.afsserver.worker.providers.AuthenticationInfoProvider;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DummyAuthenticationInfoProvider implements AuthenticationInfoProvider {
    private final Set<String> dummySessions;

    private DummyAuthenticationInfoProvider() {
        dummySessions = new HashSet<>();
    }

    @Override
    public void init(Configuration initParameter) throws Exception {
        // Do nothing
    }

    @Override
    public String login(String userId, String password) {
        String sessionToken = UUID.randomUUID().toString();
        dummySessions.add(sessionToken);
        return sessionToken;
    }

    @Override
    public Boolean isSessionValid(String sessionToken) {
        return dummySessions.contains(sessionToken);
    }

    @Override
    public Boolean logout(String sessionToken) {
        return dummySessions.remove(sessionToken);
    }
}
