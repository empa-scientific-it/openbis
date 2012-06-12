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
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;

/**
 * System tests for
 * {@link IETLLIMSService#performEntityOperations(String, AtomicEntityOperationDetails)}
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class EntityOperationTest extends SystemTestCase
{
    private static final String PREFIX = "EO_";

    private static final String SPACE_ETL_SERVER_FOR_A = PREFIX + "S_ETL_A";

    private static final String SPACE_ETL_SERVER_FOR_B = PREFIX + "S_ETL_B";

    private static final String INSTANCE_ETL_SERVER = PREFIX + "I_ETL";

    private static final String INSTANCE_ADMIN = PREFIX + "I_A";

    private static final SpaceIdentifier SPACE_A = new SpaceIdentifier("CISD", "CISD");

    private static final SpaceIdentifier SPACE_B = new SpaceIdentifier("CISD", "TESTGROUP");

    private static final class EntityOperationBuilder
    {
        private static long counter = 1000;

        private final List<NewSpace> spaces = new ArrayList<NewSpace>();

        private final List<NewProject> projects = new ArrayList<NewProject>();

        private final List<NewExperiment> experiments = new ArrayList<NewExperiment>();

        private final List<NewSample> samples = new ArrayList<NewSample>();

        private final List<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

        private final List<? extends NewExternalData> dataSets = new ArrayList<NewExternalData>();

        private final List<DataSetUpdatesDTO> dataSetUpdates = new ArrayList<DataSetUpdatesDTO>();

        private final Map<String, List<NewMaterial>> materials =
                new HashMap<String, List<NewMaterial>>();

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

        AtomicEntityOperationDetails create()
        {
            return new AtomicEntityOperationDetails(registrationID, userID, spaces, projects,
                    experiments, sampleUpdates, samples, materials, dataSets, dataSetUpdates);
        }

    }

    @BeforeClass
    public void createTestUsers()
    {
        assignSpaceRole(registerPerson(SPACE_ETL_SERVER_FOR_A), RoleCode.ETL_SERVER, SPACE_A);
        assignSpaceRole(registerPerson(SPACE_ETL_SERVER_FOR_B), RoleCode.ETL_SERVER, SPACE_B);
        assignInstanceRole(registerPerson(INSTANCE_ADMIN), RoleCode.ADMIN);
        assignInstanceRole(registerPerson(INSTANCE_ETL_SERVER), RoleCode.ETL_SERVER);
    }

    @Test
    public void testCreateSpaceAsInstanceAdmin()
    {
        String sessionToken = authenticateAs(INSTANCE_ADMIN);
        AtomicEntityOperationDetails eo = new EntityOperationBuilder().space("TEST_SPACE").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);

        assertEquals("[CISD/TEST_SPACE]", result.getSpacesCreated().toString());
    }

    @Test
    public void testCreateSpaceAsInstanceAdminButLoginAsSpaceETLServer()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).space("TEST_SPACE").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);

        assertEquals("[CISD/TEST_SPACE]", result.getSpacesCreated().toString());
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

        assertEquals("[ALPHA (GENE)]", result.getMaterialsCreated().toString());
        assertEquals("[GENE_SYMBOL: 42]", result.getMaterialsCreated().get(0).getProperties()
                .toString());
    }

    @Test
    public void testCreateMaterialAsInstanceAdminButLoginAsSpaceETLServer()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .material(
                                "GENE",
                                new MaterialBuilder().code("ALPHA").property("GENE_SYMBOL", "42")
                                        .getMaterial()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);

        assertEquals("[ALPHA (GENE)]", result.getMaterialsCreated().toString());
        assertEquals("[GENE_SYMBOL: 42]", result.getMaterialsCreated().get(0).getProperties()
                .toString());
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

        assertEquals("[/" + SPACE_A.getSpaceCode() + "/P1]", result.getProjectsCreated().toString());
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
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().experiment(
                        new ExperimentBuilder().identifier("/CISD/NEMO/E1").type("SIRNA_HCS")
                                .property("DESCRIPTION", "hello").getExperiment()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);

        assertEquals("/CISD/NEMO/E1", result.getExperimentsCreated().get(0).getIdentifier());
        assertEquals("SIRNA_HCS", result.getExperimentsCreated().get(0).getExperimentType()
                .getCode());
        assertEquals("[DESCRIPTION: hello]", result.getExperimentsCreated().get(0).getProperties()
                .toString());
        assertEquals(1, result.getExperimentsCreated().size());
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
