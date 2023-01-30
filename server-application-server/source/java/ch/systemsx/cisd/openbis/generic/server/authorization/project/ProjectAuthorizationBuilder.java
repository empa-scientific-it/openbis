/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.project;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.IObjectsProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.IRolesProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.IUserProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfigFacade;

/**
 * @author pkupczyk
 */
public class ProjectAuthorizationBuilder<O>
{

    private IAuthorizationDataProvider dataProvider;

    private IUserProvider userProvider;

    private IRolesProvider rolesProvider;

    private IObjectsProvider<O> objectsProvider;

    public ProjectAuthorizationBuilder()
    {
    }

    public ProjectAuthorizationBuilder<O> withData(IAuthorizationDataProvider provider)
    {
        this.dataProvider = provider;
        return this;
    }

    public ProjectAuthorizationBuilder<O> withUser(IUserProvider provider)
    {
        this.userProvider = provider;
        return this;
    }

    public ProjectAuthorizationBuilder<O> withRoles(IRolesProvider provider)
    {
        this.rolesProvider = provider;
        return this;
    }

    public ProjectAuthorizationBuilder<O> withObjects(IObjectsProvider<O> provider)
    {
        this.objectsProvider = provider;
        return this;
    }

    public IProjectAuthorization<O> build()
    {
        if (dataProvider == null)
        {
            throw new IllegalArgumentException("Data provider cannot be null");
        }
        if (userProvider == null)
        {
            throw new IllegalArgumentException("User provider cannot be null");
        }
        if (rolesProvider == null)
        {
            throw new IllegalArgumentException("Roles provider cannot be null");
        }
        if (objectsProvider == null)
        {
            throw new IllegalArgumentException("Objects provider cannot be null");
        }

        AuthorizationConfigFacade configFacade = new AuthorizationConfigFacade(dataProvider.getAuthorizationConfig());

        if (configFacade.isProjectLevelEnabled(userProvider.getUserId()))
        {
            return new ProjectAuthorizationEnabled<O>(dataProvider, rolesProvider, objectsProvider);
        } else
        {
            return new ProjectAuthorizationDisabled<O>(objectsProvider);
        }
    }

}
