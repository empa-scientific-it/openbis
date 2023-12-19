package ch.ethz.sis.openbis.generic.server.asapi.v3.authentication.configuration;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class PersonalAccessTokenAuthenticationToken extends AbstractAuthenticationToken {
    private final String personalAccessToken;
    public PersonalAccessTokenAuthenticationToken(String personalAccessToken, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.personalAccessToken = personalAccessToken;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
