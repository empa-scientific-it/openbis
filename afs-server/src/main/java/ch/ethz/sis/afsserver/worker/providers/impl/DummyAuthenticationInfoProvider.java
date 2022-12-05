package ch.ethz.sis.afsserver.worker.providers.impl;

import ch.ethz.sis.afsserver.worker.providers.AuthenticationInfoProvider;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.UUID;

public class DummyAuthenticationInfoProvider implements AuthenticationInfoProvider {

    @Override
    public void init(Configuration initParameter) throws Exception {
        // Do nothing
    }

    @Override
    public String login(String userId, String password) {
        return UUID.randomUUID().toString();
    }

    @Override
    public Boolean isSessionValid(String sessionToken) {
        return sessionToken != null;
    }

    @Override
    public Boolean logout(String sessionToken) {
        return sessionToken != null;
    }
}
