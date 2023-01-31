/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * <p>
 * Handler that is responsible for authenticating a user for an OAI-PMH request.
 * </p>
 * 
 * @author pkupczyk
 */
public interface IAuthenticationHandler extends IConfigurable
{

    /**
     * <p>
     * Authenticates a user.
     * </p>
     * 
     * @param request An HTTP OAI-PMH request
     * @param response An HTTP OAI-PMH response
     * @return A session of an authenticated user. Returns null when a user could not be authenticated and the OAI-PMH request must not be further
     *         handled. For example, on the first request a handler can generate a response with a login page and return null to block further
     *         processing. Similarly when a user id or password is incorrect a handler can generate a response with an error message and return null.
     */
    public SessionContextDTO handle(HttpServletRequest request, HttpServletResponse response);

}
