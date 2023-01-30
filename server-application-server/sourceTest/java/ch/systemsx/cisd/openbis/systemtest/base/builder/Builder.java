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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * @author anttil
 */
public abstract class Builder<T>
{
    protected final ICommonServerForInternalUse commonServer;

    protected final IGenericServer genericServer;

    protected String sessionToken;

    public Builder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        this.commonServer = commonServer;
        this.genericServer = genericServer;
        sessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
    }

    public Builder<T> as(String token)
    {
        this.sessionToken = token;
        return this;
    }

    public abstract T create();
}
