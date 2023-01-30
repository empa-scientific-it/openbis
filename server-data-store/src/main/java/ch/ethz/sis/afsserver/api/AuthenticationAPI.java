package ch.ethz.sis.afsserver.api;

import lombok.NonNull;

public interface AuthenticationAPI {
    public String login(@NonNull String userId, @NonNull String password) throws Exception;

    public Boolean isSessionValid() throws Exception;

    public Boolean logout() throws Exception;
}
