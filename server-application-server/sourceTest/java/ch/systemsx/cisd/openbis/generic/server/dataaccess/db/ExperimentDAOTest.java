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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.validation.ValidationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ExperimentTypeCode;

/**
 * Test cases for {@link ExperimentDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups = { "db", "experiment" })
public class ExperimentDAOTest extends AbstractDAOTest
{
    private static final String MODIFIED = "_MODIFIED";

    //
    // Experiments existing in the test database
    //
    private static final String CISD_CISD_DEFAULT_EXP_REUSE = "/CISD/DEFAULT/EXP-REUSE";

    private static final String CISD_CISD_DEFAULT_EXP_X = "/CISD/DEFAULT/EXP-X";

    private static final String CISD_CISD_NEMO_EXP1 = "/CISD/NEMO/EXP1";

    private static final String CISD_CISD_NEMO_EXP10 = "/CISD/NEMO/EXP10";

    private static final String CISD_CISD_NEMO_EXP11 = "/CISD/NEMO/EXP11";

    @Test
    public void testListSpacesByExperimentIds()
    {
        List<SpacePE> spaces =
                daoFactory.getExperimentDAO().listSpacesByExperimentIds(
                        Arrays.asList(21L, 22L, 23L));

        Collections.sort(spaces);
        assertEquals("CISD", spaces.get(0).getCode());
        assertEquals("TEST-SPACE", spaces.get(1).getCode());
        assertEquals(2, spaces.size());
    }

    @Test
    public void testListExperiments() throws Exception
    {
        final List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(7, experiments.size());
        assertExperimentIdentifierPresent(CISD_CISD_DEFAULT_EXP_REUSE, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP1, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP10, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);
        assertExperimentIdentifierNotPresent(CISD_CISD_DEFAULT_EXP_X, experiments); // deleted
    }

    @Test
    public void testListExperimentsWithPropertiesForEmptySet() throws Exception
    {
        List<ExperimentPE> list =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(
                        Collections.<Long> emptySet());
        assertEquals(0, list.size());
    }

    @Test
    public void testListByPermIDForEmptySet() throws Exception
    {
        List<ExperimentPE> list =
                daoFactory.getExperimentDAO().listByPermID(Collections.<String> emptySet());
        assertEquals(0, list.size());
    }

    @Test
    public void testListExperimentsFromProject() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(7, experiments.size());
        final ExperimentPE expInNemo =
                assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);

        final ProjectPE projectNemo = expInNemo.getProject();
        assertEquals(ProjectDAOTest.NEMO, projectNemo.getCode());

        final ExperimentTypePE expType = expInNemo.getExperimentType();
        assertEquals(ExperimentTypeCode.SIRNA_HCS.getCode(), expType.getCode());

        experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(expType, projectNemo,
                        null);
        Collections.sort(experiments);
        assertEquals(4, experiments.size());
        ExperimentPE exp1 = assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP1, experiments);
        List<DataPE> dataSets = exp1.getDataSets();
        assertEquals(1, dataSets.size());
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP10, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);
    }

    @Test
    public void testListExperimentsFromAnotherProject() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(7, experiments.size());
        final ExperimentPE expInDefault = experiments.get(0);
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, expInDefault.getIdentifier());

        final ProjectPE projectDefault = expInDefault.getProject();
        assertEquals(ProjectDAOTest.DEFAULT, projectDefault.getCode());

        final ExperimentTypePE expType = expInDefault.getExperimentType();
        assertEquals(ExperimentTypeCode.SIRNA_HCS.getCode(), expType.getCode());

        experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(expType,
                        projectDefault, null);
        Collections.sort(experiments);
        assertEquals(3, experiments.size());
        assertContains(experiments, CISD_CISD_DEFAULT_EXP_REUSE);
        assertNotContains(experiments, CISD_CISD_DEFAULT_EXP_X);
    }

    @Test
    public void testListExperimentsFromSpace() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(7, experiments.size());
        final ExperimentPE expInCisd =
                assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);

        final SpacePE spaceCisd = expInCisd.getProject().getSpace();
        assertEquals("CISD", spaceCisd.getCode());

        final ExperimentTypePE expType = expInCisd.getExperimentType();
        assertEquals(ExperimentTypeCode.SIRNA_HCS.getCode(), expType.getCode());

        experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(expType, null,
                        spaceCisd);
        Collections.sort(experiments);
        assertEquals(7, experiments.size());
        assertContains(experiments, CISD_CISD_NEMO_EXP10);
        assertContains(experiments, CISD_CISD_NEMO_EXP11);
        assertContains(experiments, CISD_CISD_DEFAULT_EXP_REUSE);
        assertNotContains(experiments, CISD_CISD_DEFAULT_EXP_X);
    }

    @Test
    public void testListExperimentsOfAnotherType() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(7, experiments.size());
        final ExperimentPE exp = experiments.get(0);
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, exp.getIdentifier());

        final SpacePE spaceCisd = exp.getProject().getSpace();
        assertEquals("CISD", spaceCisd.getCode());
        final ProjectPE projectDefault = exp.getProject();
        assertEquals(ProjectDAOTest.DEFAULT, projectDefault.getCode());

        final List<EntityTypePE> types =
                daoFactory.getEntityTypeDAO(EntityKind.EXPERIMENT).listEntityTypes();
        Collections.sort(types);
        assertEquals(3, types.size());
        final ExperimentTypePE expType = (ExperimentTypePE) types.get(0);
        assertEquals(ExperimentTypeCode.COMPOUND_HCS.getCode(), expType.getCode());

        experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(expType,
                        projectDefault, null);
        Collections.sort(experiments);
        assertEquals(0, experiments.size());

        experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(expType, null,
                        spaceCisd);
        Collections.sort(experiments);
        assertEquals(2, experiments.size());
    }

    @Test
    public void testTryFindByCodeAndProject()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(7, experiments.size());
        final ExperimentPE templateExp =
                assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP1, experiments);

        ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryFindByCodeAndProject(templateExp.getProject(),
                        templateExp.getCode());

        assertEquals(CISD_CISD_NEMO_EXP1, experiment.getIdentifier());
        assertEquals(1, experiment.getDataSets().size());
    }

    private final ExperimentPE findExperimentByIdentifier(String identifier)
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        List<ExperimentPE> experiments = experimentDAO.listExperiments();

        final ExperimentPE experiment = assertExperimentIdentifierPresent(identifier, experiments);

        return experiment;
    }

    @Test
    public final void testDeleteWithProperties()
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        final ExperimentPE deletedExperiment = findExperimentByIdentifier("/CISD/DEFAULT/EXP-Y");

        int propertiesCount = deletedExperiment.getProperties().size();

        // Deleted experiment should have all collections which prevent it from deletion empty.
        assertTrue(deletedExperiment.getDataSets().isEmpty());
        assertTrue(deletedExperiment.getSamples().isEmpty());

        // Remember how many rows are in the properties table before we delete
        int beforeDeletionPropertiesRowCount =
                countRowsInTable(TableNames.EXPERIMENT_PROPERTIES_TABLE);

        // delete
        experimentDAO.delete(deletedExperiment);

        // test successful deletion of experiment
        assertNull(experimentDAO.tryGetByTechId(TechId.create(deletedExperiment)));

        int afterDeletionPropertiesRowCount =
                countRowsInTable(TableNames.EXPERIMENT_PROPERTIES_TABLE);
        assertEquals(beforeDeletionPropertiesRowCount - propertiesCount, afterDeletionPropertiesRowCount);
    }

    private static final String ATT_CONTENTS_TABLE = "attachment_contents";

    public final void testDeleteWithAttachments()
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        final ExperimentPE deletedExperiment = findExperimentByIdentifier("/CISD/DEFAULT/EXP-Y");

        // Deleted experiment should have attachments which prevent it from deletion.
        // Other connections which also prevent experiment deletion should be empty in this test.

        // Currently there is no such experiment in test DB so we first add an attachment
        // to an empty experiment (with no connections).
        int rowsInAttachmentContents = countRowsInTable(ATT_CONTENTS_TABLE);

        AttachmentPE attachment = CommonTestUtils.createAttachment();
        attachment.setRegistrator(deletedExperiment.getRegistrator());
        daoFactory.getAttachmentDAO().createAttachment(attachment, deletedExperiment);

        // We just added an attachment to the experiment
        assertEquals(rowsInAttachmentContents + 1, countRowsInTable(ATT_CONTENTS_TABLE));

        assertFalse(deletedExperiment.getAttachments().isEmpty());
        assertTrue(deletedExperiment.getDataSets().isEmpty());
        assertTrue(deletedExperiment.getSamples().isEmpty());

        // delete
        experimentDAO.delete(deletedExperiment);

        // test successful deletion of experiment, attachment & content
        assertNull(experimentDAO.tryGetByTechId(TechId.create(deletedExperiment)));
        assertNull(daoFactory.getAttachmentDAO().tryGetByTechId(TechId.create(attachment)));

        // We deleted the attachment we added as well as the one that was already connected to the
        // experiment
        assertEquals(rowsInAttachmentContents + 1, countRowsInTable(ATT_CONTENTS_TABLE));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithDataSets()
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();

        // Deleted experiment should have data sets which prevent it from deletion.
        // Other connections which also prevent experiment deletion should be empty in this test.

        // Currently there is no such experiment in test DB so we first create an experiment
        // with no connections and then connect a data set to it.
        // to an empty experiment (with no connections).
        ExperimentPE experiment =
                createExperiment("CISD", "DEFAULT", "EXP-13", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, getTestPerson());

        final ExperimentPE deletedExperiment = findExperimentByIdentifier("/CISD/DEFAULT/EXP-13");
        final ExternalDataPE dataSet = findExternalData("20110805092359990-17");
        dataSet.setExperiment(deletedExperiment);
        daoFactory.getDataDAO().validateAndSaveUpdatedEntity(dataSet);

        assertTrue(deletedExperiment.getAttachments().isEmpty());
        assertFalse(deletedExperiment.getDataSets().isEmpty());
        assertTrue(deletedExperiment.getSamples().isEmpty());

        // delete
        experimentDAO.delete(deletedExperiment);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithSamples()
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        final ExperimentPE deletedExperiment = findExperimentByIdentifier("/CISD/NEMO/EXP10");

        // Deleted experiment should have samples which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertTrue(deletedExperiment.getAttachments().isEmpty());
        assertTrue(deletedExperiment.getDataSets().isEmpty());
        assertFalse(deletedExperiment.getSamples().isEmpty());

        // delete
        experimentDAO.delete(deletedExperiment);
    }

    @Test
    public void testTryFindByCodeAndProjectNonexistent()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(7, experiments.size());
        final ExperimentPE templateExp =
                assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);

        AssertJUnit.assertNull(daoFactory.getExperimentDAO().tryFindByCodeAndProject(
                templateExp.getProject(), "nonexistent"));
    }

    @Test
    public void testCreateExperiment() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        int sizeBefore = experimentsBefore.size();
        assertEqualsOrGreater(7, sizeBefore);

        List<DynamicPropertyEvaluationOperation> threadOperations =
                DynamicPropertyEvaluationScheduler.getThreadOperations();
        assertEquals(0, threadOperations.size());

        ExperimentPE experiment = createExperiment("CISD", "NEMO", "THE-EXP12", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, getTestPerson());

        assertEquals(2, threadOperations.size());
        assertEquals(asDynamicPropertyEvaluationOperation(experiment), threadOperations.get(0));

        List<ExperimentPE> experimentsAfter = daoFactory.getExperimentDAO().listExperiments();
        assertEquals(sizeBefore + 1, experimentsAfter.size());
        Collections.sort(experimentsAfter);
        assertExperimentsEqual(experiment, experimentsAfter.get(sizeBefore));
    }

    @Test
    public void testUpdateExperiment() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        int sizeBefore = experimentsBefore.size();
        Collections.sort(experimentsBefore);
        assertEqualsOrGreater(7, sizeBefore);

        ExperimentPE experiment = experimentsBefore.get(0);
        String codeBefore = experiment.getCode();
        String codeModified = codeBefore + MODIFIED;
        experiment.setCode(codeModified);
        experiment.setPermId(daoFactory.getPermIdDAO().createPermId());
        final Date modificationTimestamp = experiment.getModificationDate();

        List<DynamicPropertyEvaluationOperation> threadOperations =
                DynamicPropertyEvaluationScheduler.getThreadOperations();
        assertEquals(0, threadOperations.size());

        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, getTestPerson());

        assertEquals(2, threadOperations.size());
        assertEquals(asDynamicPropertyEvaluationOperation(experiment), threadOperations.get(0));

        List<ExperimentPE> experimentsAfter = daoFactory.getExperimentDAO().listExperiments();
        assertEquals(sizeBefore, experimentsAfter.size());
        Collections.sort(experimentsAfter);
        final ExperimentPE experimentFound = experimentsAfter.get(0);
        assertEquals(codeModified, experimentFound.getCode());
        assertEquals(modificationTimestamp, experimentFound.getModificationDate());
    }

    @Test
    public void testCreateExperimentsOfDifferentTypes() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        int sizeBefore = experimentsBefore.size();
        assertEqualsOrGreater(7, sizeBefore);

        List<DynamicPropertyEvaluationOperation> threadOperations =
                DynamicPropertyEvaluationScheduler.getThreadOperations();
        assertEquals(0, threadOperations.size());

        ExperimentPE experiment = createExperiment("CISD", "NEMO", "THE-EXP13", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, getTestPerson());

        assertEquals(2, threadOperations.size());
        assertEquals(asDynamicPropertyEvaluationOperation(experiment), threadOperations.get(0));

        ExperimentPE experiment2 =
                createExperiment("CISD", "NEMO", "THE-EXP12", "COMPOUND_HCS");
        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment2, getTestPerson());

        assertEquals(4, threadOperations.size());
        assertEquals(asDynamicPropertyEvaluationOperation(experiment), threadOperations.get(0));
        assertEquals(asDynamicPropertyEvaluationOperation(experiment2), threadOperations.get(2));

        List<ExperimentPE> experimentsAfter = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experimentsAfter);
        assertEquals(sizeBefore + 2, experimentsAfter.size());
        assertExperimentsEqual(experiment, experimentsAfter.get(sizeBefore + 1));
        assertEquals(experiment.getExperimentType().getCode(), experimentsAfter.get(sizeBefore + 1)
                .getExperimentType().getCode());
        assertExperimentsEqual(experiment2, experimentsAfter.get(sizeBefore));
        assertEquals(experiment2.getExperimentType().getCode(), experimentsAfter.get(sizeBefore)
                .getExperimentType().getCode());
    }

    @Test
    public void testTryCreateExperimentWithExistingIdentifier() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experimentsBefore);
        assertEqualsOrGreater(7, experimentsBefore.size());
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experimentsBefore);

        ExperimentPE experiment = createExperiment("CISD", "NEMO", "EXP11", "SIRNA_HCS");
        boolean exceptionThrown = false;
        try
        {
            daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, getTestPerson());
        } catch (DataIntegrityViolationException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    private static ExperimentPE assertExperimentIdentifierPresent(String experimentIdentifier,
            List<ExperimentPE> experiments)
    {
        for (ExperimentPE experiment : experiments)
        {
            if (experiment.getIdentifier().equals(experimentIdentifier))
            {
                return experiment;
            }
        }
        fail("Experiment with the identifier '" + experimentIdentifier
                + "' expected, but not found.");
        return null; // never reached
    }

    private static void assertExperimentIdentifierNotPresent(String experimentIdentifier,
            List<ExperimentPE> experiments)
    {
        for (ExperimentPE experiment : experiments)
        {
            if (experiment.getIdentifier().equals(experimentIdentifier))
            {
                fail("Experiment with the identifier '" + experimentIdentifier
                        + "' was not expected, but was found.");
            }
        }
    }

    @Test(dataProvider = "illegalCodesProvider")
    public final void testCreateExperimentWithIllegalCode(String code)
    {
        final ExperimentPE experiment = createExperiment("CISD", "NEMO", code, "SIRNA_HCS");
        boolean exceptionThrown = false;

        try
        {
            daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, getTestPerson());
        } catch (final DataIntegrityViolationException ex)
        {
            exceptionThrown = true;
        } catch (final ValidationException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public final void testListExperimentsBySimpleProperty()
    {
        final IExperimentDAO dao = daoFactory.getExperimentDAO();
        ProjectPE project =
                findProject("CISD", "NEMO");

        List<ExperimentPE> entities =
                dao.listExperimentsByProjectAndProperty("DESCRIPTION", "A simple experiment",
                        project);

        assertEquals(3, entities.size());
        assertContains(entities, CISD_CISD_NEMO_EXP1);
        assertContains(entities, CISD_CISD_NEMO_EXP10);
        assertContains(entities, CISD_CISD_NEMO_EXP11);
    }

    @Test
    public final void testListExperimentsByVocabularyTermProperty()
    {
        final IExperimentDAO dao = daoFactory.getExperimentDAO();
        ProjectPE project =
                findProject("CISD", "NEMO");

        List<ExperimentPE> entities =
                dao.listExperimentsByProjectAndProperty("GENDER", "MALE", project);

        assertEquals(3, entities.size());
    }

    private void assertExperimentsEqual(ExperimentPE e1, ExperimentPE e2)
    {
        assertEquals(e1.getCode(), e2.getCode());
        assertEquals(e1.getExperimentType(), e2.getExperimentType());
        assertEquals(e1.getProject(), e2.getProject());
        assertEquals(e1.getRegistrator(), e2.getRegistrator());
        assertEquals(e1.getRegistrationDate(), e2.getRegistrationDate());
    }

    @DataProvider
    private final static Object[][] illegalCodesProvider()
    {
        return new Object[][] {
                { EXCEED_CODE_LENGTH_CHARACTERS },
                { "" },
                { null },
                { "@XPERIMENT" }, };
    }

    @Test
    public void testLoadByPermId() throws Exception
    {
        ExperimentPE exp = daoFactory.getExperimentDAO().listAllEntities().get(0);
        HashSet<String> keys = new HashSet<String>();
        keys.add(exp.getPermId());
        keys.add("nonexistent");
        List<ExperimentPE> result = daoFactory.getExperimentDAO().listByPermID(keys);
        AssertJUnit.assertEquals(1, result.size());
        AssertJUnit.assertEquals(exp, result.get(0));
    }

    @Test
    public void testLoadByPermIdNoEntries() throws Exception
    {
        HashSet<String> keys = new HashSet<String>();
        List<ExperimentPE> result = daoFactory.getExperimentDAO().listByPermID(keys);
        AssertJUnit.assertTrue(result.isEmpty());
    }

    private DynamicPropertyEvaluationOperation asDynamicPropertyEvaluationOperation(
            ExperimentPE experiment)
    {
        return DynamicPropertyEvaluationOperation.evaluate(ExperimentPE.class,
                Collections.singletonList(experiment.getId()));
    }

    private void assertContains(List<ExperimentPE> experiments, String identifier)
    {
        for (ExperimentPE exp : experiments)
        {
            if (identifier.equals(exp.getIdentifier()))
            {
                return;
            }
        }
        fail("Failed to find expected experiment with identifier=" + identifier);
    }

    private void assertNotContains(List<ExperimentPE> experiments, String identifier)
    {
        for (ExperimentPE exp : experiments)
        {
            if (identifier.equals(exp.getIdentifier()))
            {
                fail("Experiment with identifier=" + identifier + " is not expected");
            }
        }
    }

}
