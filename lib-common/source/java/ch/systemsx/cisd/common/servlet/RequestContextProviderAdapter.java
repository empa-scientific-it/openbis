/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.server.IRemoteHostProvider;

/**
 * A <code>IRemoteHostProvider</code> implementation which adapts an encapsulated <code>IRequestContextProvider</code>.
 * 
 * @author Christian Ribeaud
 */
public final class RequestContextProviderAdapter implements IRemoteHostProvider
{
    private final IRequestContextProvider requestContextProvider;

    public RequestContextProviderAdapter(final IRequestContextProvider requestContextProvider)
    {
        assert requestContextProvider != null : "Undefined IRequestContextProvider.";
        this.requestContextProvider = requestContextProvider;
    }

    //
    // IRemoteHostProvider
    //

    @Override
    public final String getRemoteHost()
    {
        final HttpServletRequest request = requestContextProvider.getHttpServletRequest();
        if (request == null)
        {
            return UNKNOWN;
        }
        final String remoteHost = request.getRemoteHost();
        if (StringUtils.isEmpty(remoteHost))
        {
            return StringUtils.defaultIfEmpty(request.getRemoteAddr(), UNKNOWN);
        }
        return remoteHost;
    }
}