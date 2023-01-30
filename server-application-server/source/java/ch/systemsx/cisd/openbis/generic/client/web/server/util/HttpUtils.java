/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Some utilities around <i>Http</i>.
 * 
 * @author Tomasz Pylak
 */
public final class HttpUtils
{
    public static final OSKind figureOperatingSystemKind(final HttpServletRequest request)
    {
        String userAgent = request.getHeader("user-agent");
        return figureOperatingSystemKind(userAgent);
    }

    private static OSKind figureOperatingSystemKind(String userAgent)
    {
        if (userAgent.indexOf("Win") != -1)
        {
            return OSKind.WINDOWS;
        } else if (userAgent.indexOf("Mac") != -1)
        {
            return OSKind.MAC;
        } else if (userAgent.indexOf("OpenBSD") != -1)
        {
            return OSKind.UNIX;
        } else if (userAgent.indexOf("SunOS") != -1)
        {
            return OSKind.UNIX;
        } else if (userAgent.indexOf("Linux") != -1)
        {
            return OSKind.UNIX;
        } else if (userAgent.indexOf("X11") != -1)
        {
            return OSKind.UNIX;
        } else
        {
            return OSKind.OTHER;
        }
    }
}
