/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp;

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A web application URL that will be used for displaying the web application in an IFRAME.
 * 
 * @author pkupczyk
 */
public class WebAppUrl
{

    private URLMethodWithParameters builder;

    public WebAppUrl(String openbisProtocol, String openbisHost, String url, String webAppCode,
            String sessionId)
    {
        if (openbisProtocol == null)
        {
            throw new IllegalArgumentException("OpenBIS protocol cannot be null");
        }
        if (openbisHost == null)
        {
            throw new IllegalArgumentException("OpenBIS host cannot be null");
        }
        if (url == null)
        {
            throw new IllegalArgumentException("OpenBIS applicationURL cannot be null");
        }
        if (webAppCode == null)
        {
            throw new IllegalArgumentException("Web application code cannot be null");
        }
        if (sessionId == null)
        {
            throw new IllegalArgumentException("Session id cannot be null");
        }
        String applicationURL = url;
        if (url.endsWith("/") == false)
        {
            int lastIndexFileDelim = url.lastIndexOf('/');
            applicationURL = url.substring(0, lastIndexFileDelim + 1);
        }
        builder =
                new URLMethodWithParameters(openbisProtocol + "//" + openbisHost + applicationURL
                        + "webapp/" + webAppCode + "/");
        builder.addParameter(WebAppUrlParameter.WEBAPP_CODE.getName(), webAppCode);
        builder.addParameter(WebAppUrlParameter.SESSION_ID.getName(), sessionId);
    }

    public void addEntityKind(EntityKind entityKind)
    {
        if (entityKind != null)
        {
            builder.addParameter(WebAppUrlParameter.ENTITY_KIND.getName(), entityKind.name());
        }
    }

    public void addEntityType(BasicEntityType entityType)
    {
        if (entityType != null)
        {
            builder.addParameter(WebAppUrlParameter.ENTITY_TYPE.getName(), entityType.getCode());
        }
    }

    public void addEntityIdentifier(String entityIdentifier)
    {
        if (entityIdentifier != null)
        {
            builder.addParameter(WebAppUrlParameter.ENTITY_IDENTIFIER.getName(), entityIdentifier);
        }
    }

    public void addEntityPermId(String entityPermId)
    {
        if (entityPermId != null)
        {
            builder.addParameter(WebAppUrlParameter.ENTITY_PERM_ID.getName(), entityPermId);
        }
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }
}
