/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * A validator of objects which are connected to a space. Note: we assume that we operate on _the only_ db instance (the home db)!
 * 
 * @author Tomasz Pylak
 */
abstract class SpaceValidator<T> extends AbstractValidator<T>
{
    abstract protected String getSpace(T value);

    @Override
    public boolean doValidation(PersonPE person, T value)
    {
        final String spaceCode = getSpace(value);
        return validateSpace(person, spaceCode);
    }

    private boolean validateSpace(PersonPE person, final String spaceCode)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            if (roleAssignment.getRoleWithHierarchy().isInstanceLevel())
            {
                // All roles on the db level allow full read access.
                // Note: Here we assume that we operate on _the only_ db instance (the home db)!
                return true;
            }
            final SpacePE group = roleAssignment.getSpace();
            if (group != null && group.getCode().equals(spaceCode))
            {
                return true;
            }
        }
        return false;
    }

}
