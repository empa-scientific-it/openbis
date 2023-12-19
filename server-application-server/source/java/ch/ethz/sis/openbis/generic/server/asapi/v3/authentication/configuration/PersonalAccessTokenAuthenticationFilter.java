package ch.ethz.sis.openbis.generic.server.asapi.v3.authentication.configuration;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

/**
 * Filter responsible for getting the api key off of incoming requests that need to be authorized.
 * Based on: https://stackoverflow.com/questions/48446708/securing-spring-boot-api-with-api-key-and-secret
 */
public class PersonalAccessTokenAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final String headerName;

    public PersonalAccessTokenAuthenticationFilter(String headerName) {
        this.headerName = headerName;
    }



    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return request.getHeader(headerName);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        // No credentials when using API key
        return null;
    }
}