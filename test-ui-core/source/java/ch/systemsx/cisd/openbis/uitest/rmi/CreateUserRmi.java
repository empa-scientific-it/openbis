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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class CreateUserRmi implements Command<User>
{

    @Inject
    private String session;

    @Inject
    private ICommonServer commonServer;

    private User user;

    private Space homeSpace;

    private Collection<Space> adminOf;

    public CreateUserRmi(User user, Space homeSpace, Collection<Space> adminOf)
    {
        this.user = user;
        this.homeSpace = homeSpace;
        this.adminOf = adminOf;
    }

    @Override
    public User execute()
    {
        String userName = user.getName();
        commonServer.registerPerson(session, userName);
        if (adminOf.isEmpty() == false)
        {
            for (Space space : adminOf)
            {
                commonServer.registerSpaceRole(session, RoleCode.ADMIN, Identifiers.get(space),
                        Grantee
                                .createPerson(userName));
            }
        } else
        {
            commonServer.registerInstanceRole(session, RoleCode.ADMIN, Grantee
                    .createPerson(userName));
        }

        if (homeSpace != null)
        {
            String userSession =
                    commonServer.tryAuthenticate(userName, "pwd").getSessionToken();

            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space space : commonServer
                    .listSpaces(session))
            {
                if (space.getCode().equalsIgnoreCase(homeSpace.getCode()))
                {
                    commonServer.changeUserHomeSpace(userSession, new TechId(space.getId()));
                    return user;
                }
            }
            throw new IllegalArgumentException("Home space could not be set");
        }

        return user;
    }
}
