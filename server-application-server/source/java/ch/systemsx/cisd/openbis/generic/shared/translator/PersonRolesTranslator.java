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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PersonRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PersonRoles;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author pkupczyk
 */
public class PersonRolesTranslator
{

    private PersonRolesTranslator()
    {
        // Can not be instantiated.
    }

    public final static PersonRoles translate(final Collection<RoleAssignmentPE> roleAssignments)
    {
        final List<PersonRole> personRoles = new ArrayList<PersonRole>();

        if (roleAssignments != null)
        {
            for (final RoleAssignmentPE roleAssignment : roleAssignments)
            {
                PersonRole personRole = translate(roleAssignment);
                if (personRole != null)
                {
                    personRoles.add(personRole);
                }
            }
        }

        return new PersonRoles(personRoles);
    }

    private final static PersonRole translate(final RoleAssignmentPE roleAssignment)
    {
        if (roleAssignment == null)
        {
            return null;
        }

        Space space = SpaceTranslator.translate(roleAssignment.getSpace());

        return new PersonRole(roleAssignment.getRoleWithHierarchy(), space);
    }
}
