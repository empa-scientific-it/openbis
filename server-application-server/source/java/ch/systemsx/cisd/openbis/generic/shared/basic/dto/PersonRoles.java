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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;

/**
 * @author pkupczyk
 */
public class PersonRoles implements Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<PersonRole> personRoles;

    // GWT
    @SuppressWarnings("unused")
    private PersonRoles()
    {
    }

    public PersonRoles(List<PersonRole> personRoles)
    {
        if (personRoles == null)
        {
            throw new IllegalArgumentException("Roles cannot be null");
        }
        this.personRoles = personRoles;
    }

    public boolean isAdmin()
    {
        for (PersonRole personRole : personRoles)
        {
            if (RoleCode.ADMIN.equals(personRole.getRole().getRoleCode()))
            {
                return true;
            }
        }
        return false;
    }
}
