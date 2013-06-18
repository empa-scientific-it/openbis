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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpaceRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;

/**
 * System tests for {@link IServiceForDataStoreServer#performEntityOperations(String, AtomicEntityOperationDetails)}
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class EntityOperationTest extends SystemTestCase
{
    private static final String PREFIX = "EO_";

    private static final String SPACE_ETL_SERVER_FOR_A = PREFIX + "S_ETL_A";

    private static final String SPACE_ETL_SERVER_FOR_B = PREFIX + "S_ETL_B";

    private static final String SIMPLE_USER = PREFIX + "SIMPLE";

    private static final String SPACE_ADMIN_USER = PREFIX + "SPACE";

    private static final String AUTHORIZATION_GROUP = PREFIX + "GROUP";

    private static final String INSTANCE_ETL_SERVER = PREFIX + "I_ETL";

    private static final String INSTANCE_ADMIN = PREFIX + "I_A";

    private static final SpaceIdentifier SPACE_A = new SpaceIdentifier("CISD", "CISD");

    private static final SpaceIdentifier SPACE_B = new SpaceIdentifier("CISD", "TESTGROUP");

    private static final class EntityOperationBuilder
    {
        private static long counter = 1000;

        private final List<NewSpace> spaces = new ArrayList<NewSpace>();

        private final List<NewProject> projects = new ArrayList<NewProject>();

        private final List<ProjectUpdatesDTO> projectUpdates = new ArrayList<ProjectUpdatesDTO>();

        private final List<NewExperiment> experiments = new ArrayList<NewExperiment>();

        private final List<ExperimentUpdatesDTO> experimentUpdates =
                new ArrayList<ExperimentUpdatesDTO>();

        private final List<NewSample> samples = new ArrayList<NewSample>();

        private final List<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

        private final List<NewExternalData> dataSets = new ArrayList<NewExternalData>();

        private final List<DataSetBatchUpdatesDTO> dataSetUpdates =
                new ArrayList<DataSetBatchUpdatesDTO>();

        private final Map<String, List<NewMaterial>> materials =
                new HashMap<String, List<NewMaterial>>();

        private final List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();

        private final List<NewMetaproject> metaprojectRegistrations =
                new ArrayList<NewMetaproject>();

        private final List<MetaprojectUpdatesDTO> metaprojectUpdates =
                new ArrayList<MetaprojectUpdatesDTO>();

        private final List<VocabularyUpdatesDTO> vocabularyUpdates =
                new ArrayList<VocabularyUpdatesDTO>();

        private final List<SpaceRoleAssignment> spaceRoleAssignments = new ArrayList<SpaceRoleAssignment>();

        private final List<SpaceRoleAssignment> spaceRoleRevocations = new ArrayList<SpaceRoleAssignment>();

        private TechId registrationID = new TechId(counter++);

        private String userID;

        EntityOperationBuilder user(String user)
        {
            this.userID = user;
            return this;
        }

        EntityOperationBuilder space(String code)
        {
            return space(new NewSpace(code, null, null));
        }

        EntityOperationBuilder space(NewSpace space)
        {
            spaces.add(space);
            return this;
        }

        EntityOperationBuilder material(String materialTypeCode, Material material)
        {
            List<NewMaterial> list = materials.get(materialTypeCode);
            if (list == null)
            {
                list = new ArrayList<NewMaterial>();
                materials.put(materialTypeCode, list);
            }
            list.add(MaterialTranslator.translateToNewMaterial(material));
            return this;
        }

        EntityOperationBuilder project(SpaceIdentifier spaceIdentifier, String projectCode)
        {
            String projectIdentifier =
                    new ProjectIdentifier(spaceIdentifier, projectCode).toString();
            return project(new NewProject(projectIdentifier, null));
        }

        EntityOperationBuilder project(NewProject project)
        {
            projects.add(project);
            return this;
        }

        @SuppressWarnings("unused")
        EntityOperationBuilder project(ProjectUpdatesDTO project)
        {
            projectUpdates.add(project);
            return this;
        }

        EntityOperationBuilder experiment(Experiment experiment)
        {
            NewExperiment newExperiment =
                    new NewExperiment(experiment.getIdentifier(), experiment.getEntityType()
                            .getCode());
            newExperiment.setPermID(experiment.getPermId());
            newExperiment.setProperties(experiment.getProperties().toArray(new IEntityProperty[0]));
            experiments.add(newExperiment);
            return this;
        }

        EntityOperationBuilder sample(Sample sample)
        {
            NewSample newSample = new NewSample();
            newSample.setIdentifier(sample.getIdentifier());
            newSample.setSampleType(sample.getSampleType());
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                newSample.setExperimentIdentifier(experiment.getIdentifier());
            }
            newSample.setProperties(sample.getProperties().toArray(new IEntityProperty[0]));
            samples.add(newSample);
            return this;
        }

        EntityOperationBuilder sampleUpdate(Sample sample)
        {
            sampleUpdates.add(new SampleUpdatesDTO(new TechId(sample), sample.getProperties(),
                    null, null, sample.getVersion(), SampleIdentifierFactory.parse(sample
                            .getIdentifier()), null, null));
            return this;
        }

        EntityOperationBuilder dataSet(AbstractExternalData dataSet)
        {
            NewExternalData newExternalData = new NewExternalData();
            newExternalData.setCode(dataSet.getCode());
            newExternalData.setDataSetType(dataSet.getDataSetType());
            newExternalData.setDataStoreCode(dataSet.getDataStore().getCode());
            if (dataSet instanceof PhysicalDataSet)
            {
                PhysicalDataSet realDataSet = (PhysicalDataSet) dataSet;
                newExternalData.setFileFormatType(realDataSet.getFileFormatType());
                newExternalData.setLocation(realDataSet.getLocation());
                newExternalData.setLocatorType(realDataSet.getLocatorType());
            }
            newExternalData.setStorageFormat(StorageFormat.PROPRIETARY);
            List<IEntityProperty> properties = dataSet.getProperties();
            List<NewProperty> newProperties = new ArrayList<NewProperty>();
            for (IEntityProperty property : properties)
            {
                newProperties.add(new NewProperty(property.getPropertyType().getCode(), property
                        .tryGetAsString()));
            }
            newExternalData.setDataSetProperties(newProperties);
            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                newExternalData.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample
                        .getIdentifier()));
            }
            Experiment experiment = dataSet.getExperiment();
            if (experiment != null)
            {
                newExternalData.setExperimentIdentifierOrNull(ExperimentIdentifierFactory
                        .parse(experiment.getIdentifier()));
            }
            dataSets.add(newExternalData);
            return this;
        }

        EntityOperationBuilder dataSetUpdate(AbstractExternalData dataSet)
        {
            DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
            dataSetUpdate.setDetails(new DataSetBatchUpdateDetails());
            dataSetUpdate.setDatasetId(new TechId(dataSet));
            dataSetUpdate.setDatasetCode(dataSet.getCode());
            dataSetUpdate.setVersion(dataSet.getVersion());
            if (dataSet instanceof PhysicalDataSet)
            {
                PhysicalDataSet realDataSet = (PhysicalDataSet) dataSet;
                dataSetUpdate.setFileFormatTypeCode(realDataSet.getFileFormatType().getCode());
            }
            dataSetUpdate.setProperties(dataSet.getProperties());

            // Request an update of all properties
            HashSet<String> propertiesToUpdate = new HashSet<String>();
            for (IEntityProperty property : dataSet.getProperties())
            {
                propertiesToUpdate.add(property.getPropertyType().getCode());
            }
            dataSetUpdate.getDetails().setPropertiesToUpdate(propertiesToUpdate);

            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                dataSetUpdate.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample
                        .getIdentifier()));
            }
            Experiment experiment = dataSet.getExperiment();
            if (experiment != null)
            {
                dataSetUpdate.setExperimentIdentifierOrNull(ExperimentIdentifierFactory
                        .parse(experiment.getIdentifier()));
            }
            dataSetUpdates.add(dataSetUpdate);
            return this;
        }

        EntityOperationBuilder assignRoleToSpace(RoleCode roleCode, String spaceCode, List<String> userIds, List<String> groupCodes)
        {
            SpaceRoleAssignment assignment = createSpaceRoleAssignment(roleCode, spaceCode, userIds, groupCodes);
            spaceRoleAssignments.add(assignment);
            return this;
        }

        EntityOperationBuilder revokeRoleFromSpace(RoleCode roleCode, String spaceCode, List<String> userIds, List<String> groupCodes)
        {
            SpaceRoleAssignment assignment = createSpaceRoleAssignment(roleCode, spaceCode, userIds, groupCodes);
            spaceRoleRevocations.add(assignment);
            return this;
        }

        private SpaceRoleAssignment createSpaceRoleAssignment(RoleCode roleCode, String spaceIdentifier, List<String> userIds, List<String> groupCodes)
        {
            SpaceRoleAssignment assignment = new SpaceRoleAssignment();
            assignment.setRoleCode(roleCode);
            assignment.setSpaceIdentifier(new SpaceIdentifierFactory(spaceIdentifier).createIdentifier());
            ArrayList<Grantee> grantees = new ArrayList<Grantee>();
            for (String userId : userIds)
            {
                grantees.add(Grantee.createPerson(userId));
            }
            for (String code : groupCodes)
            {
                grantees.add(Grantee.createAuthorizationGroup(code));
            }
            assignment.setGrantees(grantees);
            return assignment;
        }

        AtomicEntityOperationDetails create()
        {
            return new AtomicEntityOperationDetails(registrationID, userID, spaces, projects,
                    projectUpdates, experiments, experimentUpdates, sampleUpdates, samples,
                    materials, materialUpdates, dataSets, dataSetUpdates, metaprojectRegistrations,
                    metaprojectUpdates, vocabularyUpdates, spaceRoleAssignments, spaceRoleRevocations);
        }

    }

    @BeforeClass
    public void createTestUsers()
    {
        assignSpaceRole(registerPerson(SPACE_ETL_SERVER_FOR_A), RoleCode.ETL_SERVER, SPACE_A);
        assignSpaceRole(registerPerson(SPACE_ETL_SERVER_FOR_B), RoleCode.ETL_SERVER, SPACE_B);
        assignSpaceRole(registerPerson(SPACE_ADMIN_USER), RoleCode.ADMIN, SPACE_A);
        assignInstanceRole(registerPerson(INSTANCE_ADMIN), RoleCode.ADMIN);
        assignInstanceRole(registerPerson(INSTANCE_ETL_SERVER), RoleCode.ETL_SERVER);

        String simpleUser = registerPerson(SIMPLE_USER);
        String authorizationGroup = registerAuthorizationGroupWithUsers(AUTHORIZATION_GROUP, Arrays.asList(simpleUser));
        assignSpaceRoleToGroup(authorizationGroup, RoleCode.USER, SPACE_A);
    }

    @Test
    public void testCreateSpaceAsInstanceAdmin()
    {
        String sessionToken = authenticateAs(INSTANCE_ADMIN);
        AtomicEntityOperationDetails eo = new EntityOperationBuilder().space("TEST_SPACE").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSpacesCreatedCount());

        Space space = etlService.tryGetSpace(sessionToken, new SpaceIdentifier("TEST_SPACE"));
        assertEquals("CISD/TEST_SPACE", space.toString());
    }

    @Test
    public void testCreateSpaceAndAssignAndRevokeRoles()
    {

        String sessionToken = authenticateAs(INSTANCE_ADMIN);
        String spaceCode = "TEST_SPACE_WITH_ROLES";
        String spaceIdentifier = "/TEST_SPACE_WITH_ROLES";

        // Create a space and assign roles to it
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .space(spaceCode)
                        .assignRoleToSpace(RoleCode.ADMIN, spaceIdentifier, Arrays.asList(SPACE_ETL_SERVER_FOR_A), Arrays.asList(AUTHORIZATION_GROUP))
                        .create();

        List<RoleAssignment> beforeRoleAssignments = etlService.listRoleAssignments(sessionToken);
        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSpacesCreatedCount());
        assertEquals(2, result.getSpaceRolesAssignedCount());
        assertEquals(0, result.getSpaceRolesRevokedCount());

        List<RoleAssignment> afterRoleAssignments = etlService.listRoleAssignments(sessionToken);
        assertEquals(2, afterRoleAssignments.size() - beforeRoleAssignments.size());

        // Revoke the role assignments
        eo =
                new EntityOperationBuilder()
                        .revokeRoleFromSpace(RoleCode.ADMIN, spaceIdentifier, Arrays.asList(SPACE_ETL_SERVER_FOR_A),
                                Arrays.asList(AUTHORIZATION_GROUP))
                        .create();

        result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(0, result.getSpacesCreatedCount());
        assertEquals(0, result.getSpaceRolesAssignedCount());
        assertEquals(2, result.getSpaceRolesRevokedCount());
        List<RoleAssignment> afterRoleRevocations = etlService.listRoleAssignments(sessionToken);
        assertEquals(afterRoleRevocations.size(), beforeRoleAssignments.size());
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testAssignSpaceRolesAsSpaceUserFails()
    {

        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        String spaceIdentifier = SPACE_A.toString();

        // Create a space and assign roles to it
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(SIMPLE_USER)
                        .assignRoleToSpace(RoleCode.ADMIN, spaceIdentifier, Arrays.asList(SIMPLE_USER), Arrays.asList(AUTHORIZATION_GROUP))
                        .create();

        etlService.performEntityOperations(sessionToken, eo);
    }

    @Test
    public void testAssignSpaceRolesAsSpaceAdminSucceedsOnOwnSpace()
    {

        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        String spaceIdentifier = SPACE_A.toString();

        List<String> groups = Collections.emptyList();

        // Assign roles to a space I admin
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(SPACE_ADMIN_USER)
                        .assignRoleToSpace(RoleCode.POWER_USER, spaceIdentifier, Arrays.asList(SIMPLE_USER), groups)
                        .create();

        List<RoleAssignment> beforeRoleAssignments = etlService.listRoleAssignments(sessionToken);
        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(0, result.getSpacesCreatedCount());
        assertEquals(1, result.getSpaceRolesAssignedCount());
        assertEquals(0, result.getSpaceRolesRevokedCount());

        List<RoleAssignment> afterRoleAssignments = etlService.listRoleAssignments(sessionToken);
        assertEquals(1, afterRoleAssignments.size() - beforeRoleAssignments.size());

    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testAssignSpaceRolesAsSpaceAdminFailsOnOtherSpace()
    {

        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        String spaceIdentifier = SPACE_B.toString();

        List<String> groups = Collections.emptyList();

        // Assign roles to a space I do not admin
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(SPACE_ADMIN_USER)
                        .assignRoleToSpace(RoleCode.POWER_USER, spaceIdentifier, Arrays.asList(SIMPLE_USER), groups)
                        .create();

        etlService.performEntityOperations(sessionToken, eo);
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testCreateSpaceAsInstanceAdminButLoginAsSpaceETLServerFails()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).space("TEST_SPACE").create();

        etlService.performEntityOperations(sessionToken, eo);
    }

    @Test
    public void testCreateSpaceAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo = new EntityOperationBuilder().space("TEST_SPACE").create();

        performFailungEntityOperations(sessionToken, eo,
                "Authorization failure: ERROR: \"None of method roles "
                        + "'[INSTANCE_ETL_SERVER, INSTANCE_ADMIN]' "
                        + "could be found in roles of user '" + SPACE_ETL_SERVER_FOR_A + "'.\".");
    }

    @Test
    public void testCreateMaterialAsInstanceETLServer()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().material(
                        "GENE",
                        new MaterialBuilder().code("ALPHA").property("GENE_SYMBOL", "42")
                                .getMaterial()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getMaterialsCreatedCount());

        Material material =
                etlService.tryGetMaterial(sessionToken, new MaterialIdentifier("ALPHA", "GENE"));
        assertEquals("ALPHA (GENE)", material.toString());
        assertEquals("[GENE_SYMBOL: 42]", material.getProperties().toString());
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testCreateMaterialAsInstanceAdminButLoginAsSpaceETLServerFails()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .material(
                                "GENE",
                                new MaterialBuilder().code("ALPHA").property("GENE_SYMBOL", "42")
                                        .getMaterial()).create();

        etlService.performEntityOperations(sessionToken, eo);
    }

    @Test
    public void testCreateMaterialAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().material("GENE",
                        new MaterialBuilder().code("ALPHA").getMaterial()).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: "
                + "ERROR: \"None of method roles '[INSTANCE_ETL_SERVER, INSTANCE_ADMIN]' "
                + "could be found in roles of user '" + SPACE_ETL_SERVER_FOR_A + "'.\".");
    }

    @Test
    public void testCreateProjectAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().project(SPACE_A, "P1").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getProjectsCreatedCount());

        Project project =
                etlService.tryGetProject(sessionToken, new ProjectIdentifier(SPACE_A, "P1"));
        assertEquals("/" + SPACE_A.getSpaceCode() + "/P1", project.toString());
    }

    @Test
    public void testCreateProjectAsInstanceAdminButLoginAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).project(SPACE_A, "P1").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getProjectsCreatedCount());

        Project project =
                etlService.tryGetProject(sessionToken, new ProjectIdentifier(SPACE_A, "P1"));
        assertEquals("/" + SPACE_A.getSpaceCode() + "/P1", project.toString());
    }

    @Test
    public void testCreateProjectAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().project(SPACE_B, "P1").create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_A + "' does not have enough privileges.\".");
    }

    @Test
    public void testCreateExperimentAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String experimentIdentifier = "/CISD/NEMO/E1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().experiment(
                        new ExperimentBuilder().identifier(experimentIdentifier).type("SIRNA_HCS")
                                .property("DESCRIPTION", "hello").getExperiment()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getExperimentsCreatedCount());

        // Need to make an additional call to get the experiment from the DB
        Experiment experiment =
                etlService.tryGetExperiment(sessionToken,
                        ExperimentIdentifierFactory.parse(experimentIdentifier));

        assertEquals("/CISD/NEMO/E1", experiment.getIdentifier());
        assertEquals("SIRNA_HCS", experiment.getExperimentType().getCode());
        assertEquals("[DESCRIPTION: hello]", experiment.getProperties().toString());
    }

    @Test
    public void testCreateExperimentAsInstanceAdminButLoginAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String experimentIdentifier = "/CISD/NEMO/E1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .experiment(
                                new ExperimentBuilder().identifier(experimentIdentifier)
                                        .type("SIRNA_HCS").property("DESCRIPTION", "hello")
                                        .getExperiment()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getExperimentsCreatedCount());

        // Need to make an additional call to get the experiment from the DB
        Experiment experiment =
                etlService.tryGetExperiment(sessionToken,
                        ExperimentIdentifierFactory.parse(experimentIdentifier));

        assertEquals("/CISD/NEMO/E1", experiment.getIdentifier());
        assertEquals("SIRNA_HCS", experiment.getExperimentType().getCode());
        assertEquals("[DESCRIPTION: hello]", experiment.getProperties().toString());
    }

    @Test
    public void testCreateExperimentAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_B);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().experiment(
                        new ExperimentBuilder().identifier("/CISD/NEMO/E1").type("SIRNA_HCS")
                                .getExperiment()).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testCreateInstanceSampleAsInstanceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        String sampleIdentifier = "/S1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sample(
                        new SampleBuilder().identifier(sampleIdentifier).type("MASTER_PLATE")
                                .property("$PLATE_GEOMETRY", "96_WELLS_8X12").getSample()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesCreatedCount());

        Sample sample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdentifier));
        assertEquals(sampleIdentifier, sample.getIdentifier());
        assertEquals("MASTER_PLATE", sample.getSampleType().getCode());
        assertEquals("[$PLATE_GEOMETRY: 96_WELLS_8X12]", sample.getProperties().toString());
    }

    @Test
    public void testCreateInstanceSampleAsInstanceAdminButLoginAsInstanceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        String sampleIdentifier = "/S1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .sample(new SampleBuilder().identifier(sampleIdentifier)
                                .type("MASTER_PLATE").property("$PLATE_GEOMETRY", "96_WELLS_8X12")
                                .getSample()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesCreatedCount());

        Sample sample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdentifier));
        assertEquals(sampleIdentifier, sample.getIdentifier());
        assertEquals("MASTER_PLATE", sample.getSampleType().getCode());
        assertEquals("[$PLATE_GEOMETRY: 96_WELLS_8X12]", sample.getProperties().toString());
    }

    @Test
    public void testCreateInstanceSampleAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sample(
                        new SampleBuilder().identifier("/S1").type("MASTER_PLATE")
                                .property("$PLATE_GEOMETRY", "96_WELLS_8X12").getSample()).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_A + "' does not have enough privileges "
                + "to modify database instance 'CISD'.\".");
    }

    @Test
    public void testCreateSpaceSampleAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String sampleIdentifier = "/CISD/S1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sample(
                        new SampleBuilder()
                                .identifier(sampleIdentifier)
                                .type("CELL_PLATE")
                                .property("COMMENT", "hello")
                                .experiment(
                                        new ExperimentBuilder().identifier("/CISD/NEMO/EXP1")
                                                .getExperiment()).getSample()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesCreatedCount());

        Sample sample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdentifier));
        assertEquals(sampleIdentifier, sample.getIdentifier());
        assertEquals("CELL_PLATE", sample.getSampleType().getCode());
        assertEquals("[COMMENT: hello]", sample.getProperties().toString());
        assertEquals("/CISD/NEMO/EXP1", sample.getExperiment().getIdentifier());
    }

    @Test
    public void testCreateSpaceSampleAsSpaceETLServerButLoginAsInstanceAdminSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ADMIN);
        String sampleIdentifier = "/CISD/S1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(SPACE_ETL_SERVER_FOR_A)
                        .sample(new SampleBuilder()
                                .identifier(sampleIdentifier)
                                .type("CELL_PLATE")
                                .property("COMMENT", "hello")
                                .experiment(
                                        new ExperimentBuilder().identifier("/CISD/NEMO/EXP1")
                                                .getExperiment()).getSample()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesCreatedCount());

        Sample sample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdentifier));
        assertEquals(sampleIdentifier, sample.getIdentifier());
        assertEquals("CELL_PLATE", sample.getSampleType().getCode());
        assertEquals("[COMMENT: hello]", sample.getProperties().toString());
        assertEquals("/CISD/NEMO/EXP1", sample.getExperiment().getIdentifier());
    }

    @Test
    public void testCreateSpaceSampleAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_B);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sample(
                        new SampleBuilder().identifier("/CISD/S1").type("CELL_PLATE").getSample())
                        .create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testUpdateInstanceSampleAsInstanceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(646)).getParent();
        List<IEntityProperty> properties = sample.getProperties();
        assertEquals("[$PLATE_GEOMETRY: 384_WELLS_16X24]", properties.toString());
        sample.setProperties(new SampleBuilder().property("$PLATE_GEOMETRY", "96_WELLS_8X12")
                .getSample().getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sampleUpdate(sample).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesUpdatedCount());

        Sample updatedSample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sample.getIdentifier()));

        assertEquals(new Long(646), updatedSample.getId());
        assertEquals("/MP", updatedSample.getIdentifier());
        assertEquals("MASTER_PLATE", updatedSample.getSampleType().getCode());
        assertEquals("[$PLATE_GEOMETRY: 96_WELLS_8X12]", updatedSample.getProperties().toString());
    }

    @Test
    public void testUpdateInstanceSampleAsInstanceAdminButLoginAsInstanceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(646)).getParent();
        List<IEntityProperty> properties = sample.getProperties();
        assertEquals("[$PLATE_GEOMETRY: 384_WELLS_16X24]", properties.toString());
        sample.setProperties(new SampleBuilder().property("$PLATE_GEOMETRY", "96_WELLS_8X12")
                .getSample().getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).sampleUpdate(sample).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesUpdatedCount());

        Sample updatedSample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sample.getIdentifier()));

        assertEquals(new Long(646), updatedSample.getId());
        assertEquals("/MP", updatedSample.getIdentifier());
        assertEquals("MASTER_PLATE", updatedSample.getSampleType().getCode());
        assertEquals("[$PLATE_GEOMETRY: 96_WELLS_8X12]", updatedSample.getProperties().toString());
    }

    @Test
    public void testUpdateInstanceSampleAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(646)).getParent();
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sampleUpdate(sample).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: "
                + "\"User '" + SPACE_ETL_SERVER_FOR_A
                + "' does not have enough privileges to modify database instance 'CISD'.\".");
    }

    @Test
    public void testUpdateSpaceSampleAsSpaceETLServerButLoginAsInstanceAdminSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ADMIN);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(986)).getParent();
        List<IEntityProperty> properties = sample.getProperties();
        assertEquals("[]", properties.toString());
        sample.setProperties(new SampleBuilder().property("COMMENT", "hello").getSample()
                .getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(SPACE_ETL_SERVER_FOR_A).sampleUpdate(sample)
                        .create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesUpdatedCount());

        Sample updatedSample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sample.getIdentifier()));
        assertEquals(new Long(986), updatedSample.getId());
        assertEquals("/CISD/3VCP5", updatedSample.getIdentifier());
        assertEquals("CELL_PLATE", updatedSample.getSampleType().getCode());
        assertEquals("[COMMENT: hello]", updatedSample.getProperties().toString());
    }

    @Test
    public void testUpdateSpaceSampleAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(986)).getParent();
        List<IEntityProperty> properties = sample.getProperties();
        assertEquals("[]", properties.toString());
        sample.setProperties(new SampleBuilder().property("COMMENT", "hello").getSample()
                .getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sampleUpdate(sample).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesUpdatedCount());

        Sample updatedSample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sample.getIdentifier()));
        assertEquals(new Long(986), updatedSample.getId());
        assertEquals("/CISD/3VCP5", updatedSample.getIdentifier());
        assertEquals("CELL_PLATE", updatedSample.getSampleType().getCode());
        assertEquals("[COMMENT: hello]", updatedSample.getProperties().toString());
    }

    @Test
    public void testUpdateSpaceSampleAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_B);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(986)).getParent();
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sampleUpdate(sample).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testCreateDataSetAsSpaceETLServerSucessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String dataSetCode = "DS-1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().dataSet(
                        new DataSetBuilder().code(dataSetCode).type("HCS_IMAGE")
                                .store(new DataStoreBuilder("STANDARD").getStore())
                                .fileFormat("XML").location("a/b/c").property("COMMENT", "my data")
                                .sample(new SampleBuilder().identifier("/CISD/CP1-A1").getSample())
                                .getDataSet()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getDataSetsCreatedCount());
        AbstractExternalData dataSet = etlService.tryGetDataSet(sessionToken, dataSetCode);

        assertEquals(dataSetCode, dataSet.getCode());
        assertEquals("HCS_IMAGE", dataSet.getDataSetType().getCode());
        assertEquals("[COMMENT: my data]", dataSet.getProperties().toString());
    }

    @Test
    public void testCreateDataSetAsInstanceAdminButLoginAsSpaceETLServerSucessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String dataSetCode = "DS-1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .dataSet(
                                new DataSetBuilder()
                                        .code(dataSetCode)
                                        .type("HCS_IMAGE")
                                        .store(new DataStoreBuilder("STANDARD").getStore())
                                        .fileFormat("XML")
                                        .location("a/b/c")
                                        .property("COMMENT", "my data")
                                        .sample(new SampleBuilder().identifier("/CISD/CP1-A1")
                                                .getSample()).getDataSet()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getDataSetsCreatedCount());
        AbstractExternalData dataSet = etlService.tryGetDataSet(sessionToken, dataSetCode);

        assertEquals(dataSetCode, dataSet.getCode());
        assertEquals("HCS_IMAGE", dataSet.getDataSetType().getCode());
        assertEquals("[COMMENT: my data]", dataSet.getProperties().toString());
    }

    @Test
    public void testCreateDataSetAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(SPACE_ETL_SERVER_FOR_B)
                        .dataSet(
                                new DataSetBuilder()
                                        .code("DS-1")
                                        .type("UNKNOWN")
                                        .store(new DataStoreBuilder("STANDARD").getStore())
                                        .fileFormat("XML")
                                        .location("a/b/c")
                                        .experiment(
                                                new ExperimentBuilder().identifier(
                                                        "/CISD/NEMO/EXP1").getExperiment())
                                        .getDataSet()).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testUpdateDataSetAsSpaceETLServerSucessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(4));
        dataSet.setDataSetProperties(new DataSetBuilder().property("COMMENT", "hello").getDataSet()
                .getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().dataSetUpdate(dataSet).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getDataSetsUpdatedCount());

        AbstractExternalData updatedDataSet = etlService.tryGetDataSet(sessionToken, dataSet.getCode());
        assertEquals(new Long(4), updatedDataSet.getId());
        assertEquals("[COMMENT: hello]", updatedDataSet.getProperties().toString());
    }

    @Test
    public void testUpdateDataSetAsInstanceAdminButLoginAsSpaceETLServerSucessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(4));
        dataSet.setDataSetProperties(new DataSetBuilder().property("COMMENT", "hello").getDataSet()
                .getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).dataSetUpdate(dataSet).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getDataSetsUpdatedCount());

        AbstractExternalData updatedDataSet = etlService.tryGetDataSet(sessionToken, dataSet.getCode());
        assertEquals(new Long(4), updatedDataSet.getId());
        assertEquals("[COMMENT: hello]", updatedDataSet.getProperties().toString());
    }

    @Test
    public void testUpdateDataSetAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(4));
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(SPACE_ETL_SERVER_FOR_B).dataSetUpdate(dataSet)
                        .create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testListAuthorizationGroups()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        List<AuthorizationGroup> result = etlService.listAuthorizationGroups(sessionToken);
        assertEquals("listAuthorizationGroups should return a list with two group, not " + result, 2, result.size());
    }

    @Test
    public void testListAuthorizationGroupsForUser()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        List<AuthorizationGroup> result = etlService.listAuthorizationGroupsForUser(sessionToken, SIMPLE_USER);
        assertEquals("listAuthorizationGroupsForUser(" + SIMPLE_USER + ") should return a list with one group, not " + result, 1, result.size());
    }

    @Test
    public void testListUsersForAuthorizationGroup()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        List<AuthorizationGroup> authorizationGroups = etlService.listAuthorizationGroups(sessionToken);

        AuthorizationGroup group = null;
        for (AuthorizationGroup aGroup : authorizationGroups)
        {
            if (AUTHORIZATION_GROUP.equals(aGroup.getCode()))
            {
                group = aGroup;
            }
        }
        if (null == group)
        {
            assertNotNull("There should be an authorization gropu with code " + AUTHORIZATION_GROUP, group);
            return;
        }
        List<Person> users = etlService.listUsersForAuthorizationGroup(sessionToken, TechId.create(group));
        assertEquals("Authorization group " + group.getCode() + " should have only 1 user, not : " + users, 1, users.size());
        Person user = users.get(0);
        assertEquals("The user " + SIMPLE_USER + " should be in the group " + AUTHORIZATION_GROUP + ", not " + user.getUserId(), SIMPLE_USER,
                user.getUserId());
    }

    @Test
    public void testListRoleAssignments()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        List<RoleAssignment> roleAssignments = etlService.listRoleAssignments(sessionToken);
        assertEquals("There should be more than 5 role assignments", true, roleAssignments.size() > 5);
    }

    private void performFailungEntityOperations(String sessionToken,
            AtomicEntityOperationDetails eo, String expectedMessage)
    {
        try
        {
            etlService.performEntityOperations(sessionToken, eo);
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(expectedMessage, ex.getMessage());
        }
    }
}
