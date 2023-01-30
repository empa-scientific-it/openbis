/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IWebInformationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.SessionConstants;

/**
 * @author Piotr Kupczyk
 */
@Component(WebInformationServiceResourceNames.WEB_INFORMATION_SERVICE_SERVER)
public class WebInformationService implements IWebInformationService
{

    @Resource(name = "request-context-provider")
    @Private
    public IRequestContextProvider requestContextProvider;

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public String getSessionToken()
    {
        HttpSession httpSession = requestContextProvider.getHttpServletRequest().getSession(false);
        if (httpSession != null)
        {
            return (String) httpSession
                    .getAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
        } else
        {
            return null;
        }
    }

}
