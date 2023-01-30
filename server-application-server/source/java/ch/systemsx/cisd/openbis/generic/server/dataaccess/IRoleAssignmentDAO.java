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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * <i>Data Access Object</i> for {@link RoleAssignmentPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IRoleAssignmentDAO extends IGenericDAO<RoleAssignmentPE>
{
    /**
     * Lists all role assignments found in the database.
     */
    public List<RoleAssignmentPE> listRoleAssignments() throws DataAccessException;

    /**
     * Creates a new role assignment in the database.
     * 
     * @param roleAssignment {@link RoleAssignmentPE} which should be stored in database.
     * @throws DataIntegrityViolationException if given role assignment already exists.
     */
    public void createRoleAssignment(final RoleAssignmentPE roleAssignment)
            throws DataAccessException;

    /**
     * Deletes given <code>RoleAssignmentPE</code> from the database.
     */
    public void deleteRoleAssignment(final RoleAssignmentPE roleAssignment)
            throws DataAccessException;

    /**
     * Lists all role assignments found in the database for given <var>personId</var>.
     */
    public List<RoleAssignmentPE> listRoleAssignmentsByPerson(final PersonPE person);

    /**
     * Returns a {@link RoleAssignmentPE} described by given role, project identifier and grantee.
     */
    public RoleAssignmentPE tryFindProjectRoleAssignment(RoleCode role, ProjectIdentifier projectIdentifier, Grantee grantee);

    /**
     * Returns a {@link RoleAssignmentPE} described by given role, space code and grantee.
     */
    public RoleAssignmentPE tryFindSpaceRoleAssignment(RoleCode role, String space, Grantee grantee);

    /**
     * Returns a {@link RoleAssignmentPE} described by given role and grantee.
     */
    public RoleAssignmentPE tryFindInstanceRoleAssignment(RoleCode role, Grantee grantee);

    /**
     * Lists all role assignments found in the database for given authorization group.
     */
    public List<RoleAssignmentPE> listRoleAssignmentsByAuthorizationGroup(
            AuthorizationGroupPE authGroup);
}
