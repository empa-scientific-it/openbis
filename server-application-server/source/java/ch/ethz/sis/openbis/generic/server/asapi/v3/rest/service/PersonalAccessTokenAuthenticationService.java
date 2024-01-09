/*
 *
 *
 * Copyright 2024 Simone Baffelli (simone.baffelli@empa.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.rest.service;

import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.rest.PersonalAccessTokenAuthenticationToken;
import ch.ethz.sis.openbis.generic.server.asapi.v3.rest.configuration.TokenConfig;
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