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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.AuthorizationService;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.SearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IRoleAssignmentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISpaceImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CreateSpacesFromDssSystemTest extends SystemTestCase
{
    // for jython script go to
    // sourceTest/core-plugins/generic-test/1/dss/drop-boxes/create-spaces/create-spaces-data-set-handler.py

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-create-spaces");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 280;
    }

    @Test
    public void testCreateSpaces() throws Exception
    {

        File exampleDataSet = new File(workingDirectory, "space-name");
        exampleDataSet.mkdirs();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();

        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        assertSpace(openBISService);
        assertRole(openBISService);
    }

    private void assertSpace(IEncapsulatedOpenBISService openBISService)
    {

        SearchService searchService = new SearchService(openBISService);
        ISpaceImmutable space = searchService.getSpace("SPACE-NAME");
        assertNotNull(space);
        assertEquals("Space from dropbox", space.getDescription());
    }

    private void assertRole(IEncapsulatedOpenBISService openBISService)
    {
        AuthorizationService authorizationService = new AuthorizationService(openBISService);
        List<IRoleAssignmentImmutable> roles = authorizationService.listRoleAssignments();
        IRoleAssignmentImmutable groupRoleForNewSpace = getGroupRole(roles);
        assertNotNull(groupRoleForNewSpace);
        assertEquals(RoleWithHierarchy.SPACE_POWER_USER, groupRoleForNewSpace.getRoleSetCode());
        assertEquals("AGROUP", groupRoleForNewSpace.getAuthorizationGroup().getCode());

        IRoleAssignmentImmutable userRoleForNewSpace = getUserRole(roles);
        assertNotNull(userRoleForNewSpace);
        assertEquals(RoleWithHierarchy.SPACE_ADMIN, userRoleForNewSpace.getRoleSetCode());
        assertEquals("test_space", userRoleForNewSpace.getUser().getUserId());
    }

    private IRoleAssignmentImmutable getGroupRole(List<IRoleAssignmentImmutable> roles)
    {
        for (IRoleAssignmentImmutable role : roles)
        {
            ISpaceImmutable roleSpace = role.getSpace();
            if (roleSpace != null && roleSpace.getSpaceCode().equals("SPACE-NAME") && role.getAuthorizationGroup() != null)
            {
                return role;
            }
        }
        return null;
    }

    private IRoleAssignmentImmutable getUserRole(List<IRoleAssignmentImmutable> roles)
    {
        for (IRoleAssignmentImmutable role : roles)
        {
            ISpaceImmutable roleSpace = role.getSpace();
            if (roleSpace != null && roleSpace.getSpaceCode().equals("SPACE-NAME") && role.getUser() != null)
            {
                return role;
            }
        }
        return null;
    }

}
