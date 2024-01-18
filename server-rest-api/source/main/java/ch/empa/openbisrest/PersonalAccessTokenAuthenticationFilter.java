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

package ch.empa.openbisrest;

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