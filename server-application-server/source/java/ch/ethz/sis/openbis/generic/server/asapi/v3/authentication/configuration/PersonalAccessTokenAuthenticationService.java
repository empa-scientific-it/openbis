package ch.ethz.sis.openbis.generic.server.asapi.v3.authentication.configuration;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public class PersonalAccessTokenAuthenticationService {

    private static final String AUTH_TOKEN_HEADER_NAME = TokenConfig.TOKEN_HEADER;
    @Resource(name = ApplicationServerApi.INTERNAL_SERVICE_NAME)
    private static final IApplicationServerInternalApi service = null;

    public static Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);

        if (apiKey == null || !service.isSessionActive(apiKey)) {
            throw new BadCredentialsException("Invalid API Key");
        }

        return new PersonalAccessTokenAuthenticationToken(apiKey, AuthorityUtils.NO_AUTHORITIES);
    }
}