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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * <i>Bean</i> for registering new role assignments.
 * <p>
 * It is used by the parser. This explains the {@link BeanProperty} annotations.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class NewRoleAssignment extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final NewRoleAssignment[] EMPTY_ARRAY = new NewRoleAssignment[0];

    private SpaceIdentifier spaceIdentifier;

    private ProjectIdentifier projectIdentifier;

    private Grantee grantee;

    private RoleCode role;

    public final RoleCode getRole()
    {
        return role;
    }

    @BeanProperty(label = "role")
    public final void setRole(final RoleCode role)
    {
        if (RoleCode.DISABLED.equals(role))
        {
            throw new IllegalArgumentException("Cannot assign DISABLED role to anyone");
        }
        this.role = role;
    }

    public final SpaceIdentifier getSpaceIdentifier()
    {
        return spaceIdentifier;
    }

    @BeanProperty(label = "space")
    public final void setSpaceIdentifier(final SpaceIdentifier spaceIdentifier)
    {
        this.spaceIdentifier = spaceIdentifier;
    }

    public final ProjectIdentifier getProjectIdentifier()
    {
        return projectIdentifier;
    }

    @BeanProperty(label = "project")
    public final void setProjectIdentifier(final ProjectIdentifier projectIdentifier)
    {
        this.projectIdentifier = projectIdentifier;
    }

    public final Grantee getGrantee()
    {
        return grantee;
    }

    @BeanProperty(label = "grantee")
    public final void setGrantee(final Grantee grantee)
    {
        this.grantee = grantee;
    }

    //
    // AbstractHashable
    //

    @Override
    public final String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(grantee).append("=").append(role).append("@");
        if (getSpaceIdentifier() != null)
        {
            builder.append(getSpaceIdentifier());
        }
        if (getProjectIdentifier() != null)
        {
            builder.append(getProjectIdentifier());
        }
        return builder.toString();
    }
}
