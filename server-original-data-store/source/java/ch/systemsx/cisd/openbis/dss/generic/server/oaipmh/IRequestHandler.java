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
 * Handler that is responsible for generating a response for an OAI-PMH request.
 * </p>
 * 
 * @author pkupczyk
 */
public interface IRequestHandler extends IConfigurable
{

    /**
     * <p>
     * Generates a response.
     * </p>
     * 
     * @param session A session of an authenticated user
     * @param request An HTTP OAI-PMH request
     * @param response An HTTP OAI-PMH response
     */
    public void handle(SessionContextDTO session, HttpServletRequest request, HttpServletResponse response);

}
