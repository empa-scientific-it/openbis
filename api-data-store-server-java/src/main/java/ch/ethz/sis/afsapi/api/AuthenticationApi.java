package ch.ethz.sis.afsapi.api;

import lombok.NonNull;

public interface AuthenticationApi
{

    @NonNull
    String login(@NonNull String userId, @NonNull String password) throws Exception;

    @NonNull
    Boolean isSessionValid() throws Exception;

    @NonNull
    Boolean logout() throws Exception;

}
