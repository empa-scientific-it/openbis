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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.security.TokenGenerator;

/**
 * Class managing DSS session tokens. Each invocation of {@link #drawSessionToken()} generates a new token. The last two tokens are valid. Invocation
 * of {@link #assertValidSessionToken(String)} will throw an {@link InvalidAuthenticationException} in case of an invalid token.
 * 
 * @author Franz-Josef Elmer
 */
public class SessionTokenManager
{
    private static final int MAX_NUMBER_OF_TOKENS = 2;

    private final List<String> sessionTokens = new ArrayList<String>();

    private final TokenGenerator tokenGenerator = new TokenGenerator();

    public synchronized String drawSessionToken()
    {
        String sessionToken = tokenGenerator.getNewToken(System.currentTimeMillis());
        sessionTokens.add(sessionToken);
        while (sessionTokens.size() > MAX_NUMBER_OF_TOKENS)
        {
            sessionTokens.remove(0);
        }
        return sessionToken;
    }

    public void assertValidSessionToken(String sessionToken)
    {
        if (sessionTokens.contains(sessionToken) == false)
        {
            throw new InvalidAuthenticationException("Invalid session token.");
        }
    }
}
