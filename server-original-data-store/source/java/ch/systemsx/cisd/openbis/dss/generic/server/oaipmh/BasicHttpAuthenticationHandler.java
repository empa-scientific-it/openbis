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

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * <p>
 * Handler that performs Basic HTTP authentication as described here: http://en.wikipedia.org/wiki/Basic_access_authentication.
 * </p>
 * 
 * @author pkupczyk
 */
public class BasicHttpAuthenticationHandler implements IAuthenticationHandler
{

    @Override
    public void init(Properties properties)
    {

    }

    @Override
    public SessionContextDTO handle(HttpServletRequest req, HttpServletResponse resp)
    {
        try
        {
            String authorization = req.getHeader("Authorization");

            if (authorization != null && authorization.trim().length() > 0)
            {
                Pattern pattern = Pattern.compile("Basic (.+)");
                Matcher matcher = pattern.matcher(authorization);

                if (matcher.matches())
                {
                    String encoded = matcher.group(1);
                    String decoded = new String(Base64.decodeBase64(encoded.getBytes()));
                    String[] parts = decoded.split(":");

                    if (parts != null && parts.length == 2)
                    {
                        String user = parts[0];
                        String password = parts[1];

                        SessionContextDTO session = ServiceProvider.getOpenBISService().tryAuthenticate(user, password);

                        if (session == null)
                        {
                            resp.setHeader("WWW-Authenticate", "Basic realm=\"OAI-PMH\"");
                            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
                            return null;
                        } else
                        {
                            return session;
                        }
                    } else
                    {
                        throw new IllegalArgumentException("Authorization header had an incorrect value: " + authorization);
                    }
                } else
                {
                    throw new IllegalArgumentException("Authorization header had an incorrect value: " + authorization);
                }
            } else
            {
                resp.setHeader("WWW-Authenticate", "Basic realm=\"OAI-PMH\"");
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
                return null;
            }
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }
}
