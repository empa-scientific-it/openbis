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

package ch.systemsx.cisd.openbis.generic.server;

import org.springframework.beans.factory.FactoryBean;

import ch.systemsx.cisd.authentication.IAuthenticationService;

/**
 * A {@link IAuthenticationService} holder.
 * 
 * @author Christian Ribeaud
 */
public final class AuthenticationServiceHolder implements FactoryBean
{
    private final IAuthenticationService authenticationService;

    public AuthenticationServiceHolder(final IAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public final IAuthenticationService getAuthenticationService()
    {
        return authenticationService;
    }

    //
    // FactoryBean
    //

    @Override
    public final Object getObject() throws Exception
    {
        return authenticationService;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final Class getObjectType()
    {
        return IAuthenticationService.class;
    }

    @Override
    public final boolean isSingleton()
    {
        return true;
    }
}
