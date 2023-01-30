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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;

/**
 * Stores the {@link RoleWithHierarchy} and the "owner" to which this role is connected: database instance or a space.
 * <p>
 * Note that {@link #equals(Object)} resp. {@link #hashCode()} are not overridden and so do not consider the <code>owner</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class RoleWithIdentifier
{

    RoleWithHierarchy role;

    private final SpacePE spaceOrNull;

    private final ProjectPE projectOrNull;

    @Private
    RoleWithIdentifier(final RoleLevel roleLevel, final RoleCode roleName, final SpacePE spaceOrNull, final ProjectPE projectOrNull)
    {
        role = RoleWithHierarchy.valueOf(roleLevel, roleName);

        if (RoleLevel.SPACE.equals(roleLevel))
        {
            assert spaceOrNull != null : "Unspecified space";
        } else
        {
            assert spaceOrNull == null;
        }
        this.spaceOrNull = spaceOrNull;

        if (RoleLevel.PROJECT.equals(roleLevel))
        {
            assert projectOrNull != null : "Unspecified project";
        } else
        {
            assert projectOrNull == null;
        }
        this.projectOrNull = projectOrNull;
    }

    /**
     * This method can be called only if role is defined on the space level.
     * 
     * @return space to which the role is assigned
     */
    public final SpacePE getAssignedSpace()
    {
        assert spaceOrNull != null;
        return spaceOrNull;
    }

    /**
     * This method can be called only if role is defined on the project level.
     * 
     * @return project to which the role is assigned
     */
    public final ProjectPE getAssignedProject()
    {
        assert projectOrNull != null;
        return projectOrNull;
    }

    /**
     * Create a <code>RoleWithCode</code> from given <var>roleAssignment</var>.
     */
    public final static RoleWithIdentifier createRole(final RoleAssignmentPE roleAssignment)
    {
        assert roleAssignment != null : "Unspecified role assignment";
        final RoleLevel roleLevel = roleAssignment.getRoleLevel();
        final RoleCode roleName = roleAssignment.getRole();
        final SpacePE space = roleAssignment.getSpace();
        final ProjectPE project = roleAssignment.getProject();
        return new RoleWithIdentifier(roleLevel, roleName, space, project);
    }

    //
    // Role
    //

    @Override
    public final String toString()
    {
        final StringBuilder builder = new StringBuilder(super.toString());
        builder.append("<").append(createOwnerDescription()).append(">");
        return builder.toString();
    }

    private String createOwnerDescription()
    {
        if (projectOrNull != null)
        {
            return IdentifierHelper.createProjectIdentifier(projectOrNull).toString();
        } else if (spaceOrNull != null)
        {
            return IdentifierHelper.createGroupIdentifier(spaceOrNull).toString();
        } else
        {
            return RoleLevel.INSTANCE.name();
        }
    }

    public RoleLevel getRoleLevel()
    {
        return role.getRoleLevel();
    }

    public RoleCode getRoleName()
    {
        return role.getRoleCode();
    }

    public RoleWithHierarchy getRole()
    {
        return role;
    }
}
