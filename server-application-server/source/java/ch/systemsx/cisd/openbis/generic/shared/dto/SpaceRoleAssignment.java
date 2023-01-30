/*
 * Copyright 2013 ETH Zuerich, CISD
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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A DTO for communicating information about space role changes
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SpaceRoleAssignment implements Serializable
{
    private static final long serialVersionUID = 1L;

    private RoleCode roleCode;

    private SpaceIdentifier spaceIdentifier;

    private List<Grantee> grantees;

    public RoleCode getRoleCode()
    {
        return roleCode;
    }

    public void setRoleCode(RoleCode roleCode)
    {
        this.roleCode = roleCode;
    }

    public SpaceIdentifier getSpaceIdentifier()
    {
        return spaceIdentifier;
    }

    public void setSpaceIdentifier(SpaceIdentifier spaceIdentifier)
    {
        this.spaceIdentifier = spaceIdentifier;
    }

    public List<Grantee> getGrantees()
    {
        return grantees;
    }

    public void setGrantees(List<Grantee> grantee)
    {
        this.grantees = grantee;
    }
}
