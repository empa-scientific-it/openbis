/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Franz-Josef Elmer
 */
public class SimpleSpaceValidator extends AbstractValidator<ICodeHolder>
{

    @Override
    public boolean doValidation(PersonPE person, ICodeHolder value)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        String spaceCode = value.getCode();
        if (spaceCode == null && roleAssignments.isEmpty() == false)
        {
            return true;
        }
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            if (roleAssignment.getRoleWithHierarchy().isInstanceLevel())
            {
                return true;
            }

            final SpacePE space = roleAssignment.getSpace();

            if (space != null && space.getCode().equals(spaceCode))
            {
                return true;
            }
        }
        return false;
    }

}
