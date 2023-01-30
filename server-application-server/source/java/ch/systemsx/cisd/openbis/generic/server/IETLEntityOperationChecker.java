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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetUpdatesCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExternalDataPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewProjectPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleUpdatesCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Checking methods to be invoked to check authorization in context of
 * {@link ServiceForDataStoreServer#performEntityOperations(String, ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails)} .
 * 
 * @author Franz-Josef Elmer
 */
public interface IETLEntityOperationChecker
{
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("CREATE_SPACES_VIA_DSS")
    public void assertSpaceCreationAllowed(IAuthSession session, List<NewSpace> newSpaces);

    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    @Capability("CREATE_MATERIALS_VIA_DSS")
    public void assertMaterialCreationAllowed(IAuthSession session,
            Map<String, List<NewMaterial>> materials);

    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    @Capability("UPDATE_MATERIALS_VIA_DSS")
    public void assertMaterialUpdateAllowed(IAuthSession session, List<MaterialUpdateDTO> materials);

    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_PROJECTS_VIA_DSS")
    public void assertProjectCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewProjectPredicate.class) List<NewProject> newProjects);

    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_PROJECTS_VIA_DSS")
    public void assertProjectUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = ProjectUpdatesPredicate.class) List<ProjectUpdatesDTO> projectsToUpdate);

    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_EXPERIMENTS_VIA_DSS")
    public void assertExperimentCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class) List<NewExperiment> newExperiments);

    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_EXPERIMENTS_VIA_DSS")
    public void assertExperimentUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = ExperimentUpdatesPredicate.class) List<ExperimentUpdatesDTO> experimentUpdates);

    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    @Capability("CREATE_INSTANCE_SAMPLES_VIA_DSS")
    public void assertInstanceSampleCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class) List<NewSample> instanceSamples);

    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_SPACE_SAMPLES_VIA_DSS")
    public void assertSpaceSampleCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class) List<NewSample> spaceSamples);

    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    @Capability("UPDATE_INSTANCE_SAMPLES_VIA_DSS")
    public void assertInstanceSampleUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleUpdatesCollectionPredicate.class) List<SampleUpdatesDTO> instanceSamples);

    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_SPACE_SAMPLES_VIA_DSS")
    public void assertSpaceSampleUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleUpdatesCollectionPredicate.class) List<SampleUpdatesDTO> spaceSamples);

    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_DATA_SETS_VIA_DSS")
    public void assertDataSetCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewExternalDataPredicate.class) List<? extends NewExternalData> dataSets);

    @RolesAllowed({ RoleWithHierarchy.PROJECT_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_DATA_SETS_VIA_DSS")
    public void assertDataSetUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = DataSetUpdatesCollectionPredicate.class) List<DataSetBatchUpdatesDTO> dataSets);

    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("ASSIGN_ROLE_TO_SPACE_VIA_DSS")
    public void assertSpaceRoleAssignmentAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) SpaceIdentifier space);

    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("UPDATE_VOCABULARY_VIA_DSS")
    public void assertVocabularyUpdateAllowed(IAuthSession session);
}
