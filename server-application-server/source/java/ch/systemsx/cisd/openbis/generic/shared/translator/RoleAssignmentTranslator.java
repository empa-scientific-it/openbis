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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * A {@link RoleAssignment} &lt;---&gt; {@link RoleAssignmentPE} translator.
 * 
 * @author Christian Ribeaud
 */
public final class RoleAssignmentTranslator
{
    private RoleAssignmentTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<RoleAssignment> translate(final List<RoleAssignmentPE> roles)
    {
        final List<RoleAssignment> result = new ArrayList<RoleAssignment>();
        for (final RoleAssignmentPE role : roles)
        {
            result.add(RoleAssignmentTranslator.translate(role));
        }
        return result;
    }

    public final static RoleAssignment translate(final RoleAssignmentPE role)
    {
        if (role == null)
        {
            return null;
        }
        final RoleAssignment result = new RoleAssignment();
        result.setSpace(SpaceTranslator.translate(role.getSpace()));
        result.setProject(ProjectTranslator.translate(role.getProject()));
        result.setPerson(PersonTranslator.translate(role.getPerson()));
        result.setAuthorizationGroup(AuthorizationGroupTranslator.translate(role
                .getAuthorizationGroup()));
        result.setRoleSetCode(role.getRoleWithHierarchy());
        return result;
    }

}
