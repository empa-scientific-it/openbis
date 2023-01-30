/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.project;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ProjectTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewProjectPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectIdentifierExistingSpacePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPermIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPermIdStringPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author pkupczyk
 */
@Component
public class ProjectPredicateTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectPEPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectPEPredicate.class) ProjectPE projectPE)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testNewProjectPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = NewProjectPredicate.class) NewProject newProject)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectIdentifierPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) ProjectIdentifier projectIdentifier)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectPredicate(IAuthSessionProvider sessionProvider, @AuthorizationGuard(guardClass = ProjectPredicate.class) Project project)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectIdPredicate.class) IProjectId projectId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectAugmentedCodePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectAugmentedCodePredicate.class) String projectAugmentedCode)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectUpdatesPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectUpdatesPredicate.class) ProjectUpdatesDTO projectUpdatesDTO)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectTechIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectTechId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectPermIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectPermIdPredicate.class) PermId projectPermId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectPermIdStringPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectPermIdStringPredicate.class) String projectPermIdString)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectTechIdCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectTechIdCollectionPredicate.class) List<TechId> projectTechIds)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testProjectIdentifierExistingSpacePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ProjectIdentifierExistingSpacePredicate.class) ProjectIdentifier projectIdentifier)
    {
    }

}
