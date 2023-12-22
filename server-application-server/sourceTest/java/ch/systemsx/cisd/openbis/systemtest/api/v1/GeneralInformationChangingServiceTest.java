/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.systemtest.api.v1;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.OpenBISHibernateTransactionManager;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialCodeAndTypeCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.systemtest.PropertyHistory;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.util.GeneralInformationServiceUtil;
import junit.framework.Assert;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class GeneralInformationChangingServiceTest extends SystemTestCase
{
    @Autowired
    private ICommonServer localCommonServer;

    @Autowired
    private IGeneralInformationService generalInformationService;

    @Autowired
    private IGeneralInformationChangingService generalInformationChangingService;

    private String sessionToken;

    @BeforeMethod
    public void beforeMethod()
    {
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
    }

    @Test
    public void testUpdateExperimentProperties()
    {
    }

    @Test
    public void testUpdateSampleProperties()
    {
        TechId id = new TechId(1043L);
        localCommonServer.assignPropertyType(sessionToken,
                new NewETPTAssignment(EntityKind.SAMPLE, "DESCRIPTION", "CELL_PLATE", false, null,
                        null, 1L, false, false, null, true, false));
        localCommonServer.assignPropertyType(sessionToken, new NewETPTAssignment(EntityKind.SAMPLE,
                "GENDER", "CELL_PLATE", false, null, null, 1L, false, false, null, true, false));
        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                        + "COMMENT: extremely simple stuff, ORGANISM: GORILLA, SIZE: 321]",
                localCommonServer.getSampleInfo(sessionToken, id).getParent());
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("SIZE", "42");
        properties.put("any_material", "1 (GENE)");
        properties.put("Organism", "DOG");
        properties.put("DESCRIPTION", "hello example");
        properties.put("gender", "FEMALE");

        generalInformationChangingService.updateSampleProperties(sessionToken, id.getId(),
                properties);

        assertProperties("[ANY_MATERIAL: 1 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                + "COMMENT: extremely simple stuff, DESCRIPTION: hello example, GENDER: FEMALE, "
                + "ORGANISM: DOG, SIZE: 42]", localCommonServer.getSampleInfo(sessionToken, id)
                .getParent());

        List<PropertyHistory> history = getSamplePropertiesHistory(id.getId());
        assertEquals(
                "[ANY_MATERIAL: material:2 [GENE]<a:1>, ORGANISM: term:GORILLA [ORGANISM]<a:1>, SIZE: 321<a:1>]",
                history.toString());
    }

    @SuppressWarnings("null")
    @Test
    public void testVocabularyAdditionAndReplacement()
    {
        TechId id = new TechId(1043L);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleBefore = localCommonServer.getSampleInfo(sessionToken, id).getParent();
        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                        + "COMMENT: extremely simple stuff, ORGANISM: GORILLA, SIZE: 321]",
                sampleBefore);
        NewETPTAssignment newETPTAssignment = new NewETPTAssignment();
        newETPTAssignment.setEntityKind(EntityKind.SAMPLE);
        newETPTAssignment.setDynamic(false);
        newETPTAssignment.setMandatory(true);
        newETPTAssignment.setDefaultValue("FEMALE");
        newETPTAssignment.setPropertyTypeCode("GENDER");
        newETPTAssignment.setOrdinal(1L);
        newETPTAssignment.setEntityTypeCode("CELL_PLATE");
        newETPTAssignment.setShownInEditView(true);
        newETPTAssignment.setShowRawValue(true);
        localCommonServer.assignPropertyType(sessionToken, newETPTAssignment);

        Session hibernateSession = getHibernateSession();
        hibernateSession.clear();
        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                        + "COMMENT: extremely simple stuff, GENDER: FEMALE, ORGANISM: GORILLA, SIZE: 321]",
                localCommonServer.getSampleInfo(sessionToken, id).getParent());

        List<Vocabulary> listVocabularies = generalInformationService.listVocabularies(sessionToken);
        Vocabulary genderVocab = null;
        for (Vocabulary vocabulary : listVocabularies)
        {
            if (vocabulary.getCode().equals("GENDER"))
            {
                genderVocab = vocabulary;
            }
        }
        assertNotNull(genderVocab);

        VocabularyTermReplacement replacement = new VocabularyTermReplacement();
        VocabularyTerm vocabularyTerm = new VocabularyTerm();
        vocabularyTerm.setCode("FEMALE");
        vocabularyTerm.setId(12l);
        vocabularyTerm.setOrdinal(1L);
        replacement.setTerm(vocabularyTerm);
        replacement.setReplacementCode("MALE");

        localCommonServer.deleteVocabularyTerms(sessionToken, new TechId(genderVocab.getId()),
                Collections.<VocabularyTerm>emptyList(), Arrays.asList(replacement));

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleAfter = localCommonServer.getSampleInfo(sessionToken, id)
                .getParent();
        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                + "COMMENT: extremely simple stuff, GENDER: MALE, ORGANISM: GORILLA, SIZE: 321]", sampleAfter);

        hibernateSession.clear();

        assertFalse(sampleBefore.getModificationDate().equals(sampleAfter.getModificationDate()));
    }

    @Test
    public void testAssignMandatoryProperty()
    {
        TechId id = new TechId(1043L);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleBefore = localCommonServer.getSampleInfo(sessionToken, id).getParent();
        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                        + "COMMENT: extremely simple stuff, ORGANISM: GORILLA, SIZE: 321]",
                sampleBefore);
        NewETPTAssignment newETPTAssignment = new NewETPTAssignment();
        newETPTAssignment.setEntityKind(EntityKind.SAMPLE);
        newETPTAssignment.setDynamic(false);
        newETPTAssignment.setMandatory(true);
        newETPTAssignment.setDefaultValue("BMP_15");
        newETPTAssignment.setPropertyTypeCode("GENE_SYMBOL");
        newETPTAssignment.setOrdinal(1L);
        newETPTAssignment.setEntityTypeCode("CELL_PLATE");
        newETPTAssignment.setShownInEditView(true);
        newETPTAssignment.setShowRawValue(true);
        localCommonServer.assignPropertyType(sessionToken,
                newETPTAssignment);
        // we clear the hibernateSession because assignPropertyType
        // method executes sqls outside hibernate session
        Session hibernateSession = getHibernateSession();
        hibernateSession.clear();

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleAfter = localCommonServer.getSampleInfo(sessionToken, id).getParent();

        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                + "COMMENT: extremely simple stuff, GENE_SYMBOL: BMP_15, "
                + "ORGANISM: GORILLA, SIZE: 321]", sampleAfter);
        assertFalse(sampleBefore.getModificationDate().equals(sampleAfter.getModificationDate()));
    }

    @Test
    public void testUnassignProperty()
    {
        TechId id = new TechId(1043L);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleBefore = localCommonServer.getSampleInfo(sessionToken, id).getParent();
        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                        + "COMMENT: extremely simple stuff, ORGANISM: GORILLA, SIZE: 321]",
                sampleBefore);
        localCommonServer.unassignPropertyType(sessionToken, EntityKind.SAMPLE, "COMMENT", "CELL_PLATE");
        getHibernateSession().clear();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleAfter =
                localCommonServer.getSampleInfo(sessionToken, id).getParent();
        assertFalse(sampleBefore.getModificationDate().equals(sampleAfter.getModificationDate()));
    }

    private Session getHibernateSession()
    {
        OpenBISHibernateTransactionManager bean = (OpenBISHibernateTransactionManager) applicationContext.getBean("transaction-manager");
        return bean.getSessionFactory().getCurrentSession();
    }

    @Test
    public void testCreateMetaproject()
    {
        String name = "BRAND_NEW_METAPROJECT";
        String description = "I'm brand new";

        List<String> beforeNames = getUtil().listMetaprojectNames(sessionToken);
        assertFalse(beforeNames.contains(name));

        Metaproject metaproject =
                generalInformationChangingService
                        .createMetaproject(sessionToken, name, description);

        List<String> afterNames = getUtil().listMetaprojectNames(sessionToken);
        assertTrue(afterNames.contains(name));

        assertEquals(beforeNames.size() + 1, afterNames.size());
        afterNames.remove(name);
        assertEquals(beforeNames, afterNames);

        assertNotNull(metaproject.getId());
        assertEquals("test", metaproject.getOwnerId());
        assertEquals("/test/" + name, metaproject.getIdentifier());
        assertEquals(name, metaproject.getName());
        assertEquals(description, metaproject.getDescription());
        assertNotNull(metaproject.getCreationDate());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateMetaprojectWithNameDuplicatedForSameOwner()
    {
        generalInformationChangingService.createMetaproject(sessionToken, "TEST_METAPROJECTS",
                "My name is already used by the same owner");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateMetaprojectWithNameThatIsUsedInDifferentCaseForSameOwner()
    {
        generalInformationChangingService.createMetaproject(sessionToken, "TEST_metaprojects",
                "My name is already used by the same owner but in a different case");
    }

    @Test
    public void testCreateMetaprojectWithNameDuplicatedForDifferentOwner()
    {
        generalInformationChangingService.createMetaproject(sessionToken, "TEST_METAPROJECTS_2",
                "My name is already used by a different owner");
    }

    @Test
    public void testCreateMetaprojectWithNameThatIsUsedInDifferentCaseForDifferentOwner()
    {
        generalInformationChangingService.createMetaproject(sessionToken, "TEST_metaprojects_2",
                "My name is already used by a different owner in a different case");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateMetaprojectWithEmptyName()
    {
        generalInformationChangingService.createMetaproject(sessionToken, null, "My name is empty");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateMetaprojectWithNameThatContainsDisallowedCharacters()
    {
        generalInformationChangingService.createMetaproject(sessionToken,
                "THIS NAME IS DISALLOWED", "My name is not allowed");
    }

    @Test
    public void testCreateMetaprojectWithNameThatUsesVariedCaseMaintainsTheCase()
    {
        String name = "NameWithVariedCase";

        Metaproject metaproject =
                generalInformationChangingService.createMetaproject(sessionToken, name,
                        "My name uses varied case");
        assertEquals(name, metaproject.getName());
        List<String> names = getUtil().listMetaprojectNames(sessionToken);
        assertTrue(names.contains(name));
    }

    @Test
    public void testCreateMetaprojectWithEmptyDescription()
    {
        String name = "METAPROJECT_WITH_EMPTY_DESCRIPTION";

        Metaproject metaproject =
                generalInformationChangingService.createMetaproject(sessionToken, name, null);
        assertEquals(name, metaproject.getName());
        assertNull(metaproject.getDescription());
    }

    @Test
    public void testUpdateMetaproject()
    {
        String beforeName = "TEST_METAPROJECTS";
        String afterName = "TEST_METAPROJECTS_UPDATED";
        String description = "My description is brand new";

        List<String> beforeNames = getUtil().listMetaprojectNames(sessionToken);
        assertTrue(beforeNames.contains(beforeName));
        assertFalse(beforeNames.contains(afterName));

        Metaproject metaproject =
                generalInformationChangingService.updateMetaproject(sessionToken,
                        new MetaprojectIdentifierId("/test/" + beforeName), afterName, description);

        List<String> afterNames = getUtil().listMetaprojectNames(sessionToken);
        assertFalse(afterNames.contains(beforeName));
        assertTrue(afterNames.contains(afterName));

        assertEquals(beforeNames.size(), afterNames.size());
        beforeNames.remove(beforeName);
        afterNames.remove(afterName);
        assertEquals(beforeNames, afterNames);

        assertNotNull(metaproject.getId());
        assertEquals("test", metaproject.getOwnerId());
        assertEquals("/test/" + afterName, metaproject.getIdentifier());
        assertEquals(afterName, metaproject.getName());
        assertEquals(description, metaproject.getDescription());
        assertNotNull(metaproject.getCreationDate());
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testUpdateMetaprojectOwnedBySomebodyElse()
    {
        generalInformationChangingService.updateMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test_role/TEST_METAPROJECTS"), "SOME_NEW_NAME",
                "some new description");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testUpdateMetaprojectWithEmptyId()
    {
        generalInformationChangingService.updateMetaproject(sessionToken, null, "SOME_NEW_NAME",
                "some new description");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testUpdateMetaprojectThatDoesntExist()
    {
        generalInformationChangingService.updateMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/METAPROJECT_THAT_DOESNT_EXIST"),
                "SOME_NEW_NAME", "some new description");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testUpdateMetaprojectToEmptyName()
    {
        generalInformationChangingService.updateMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"), null,
                "some new description");
    }

    @Test
    public void testUpdateMetaprojectToEmptyDescription()
    {
        generalInformationChangingService.updateMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"), "TEST_METAPROJECTS", null);
    }

    @Test
    public void testDeleteMetaproject()
    {
        String name = "TEST_METAPROJECTS";

        List<String> beforeNames = getUtil().listMetaprojectNames(sessionToken);
        assertTrue(beforeNames.contains(name));

        generalInformationChangingService.deleteMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/" + name));

        List<String> afterNames = getUtil().listMetaprojectNames(sessionToken);
        assertFalse(afterNames.contains(name));

        assertEquals(beforeNames.size() - 1, afterNames.size());
        beforeNames.remove(name);
        assertEquals(beforeNames, afterNames);
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testDeleteMetaprojectOwnedBySomebodyElse()
    {
        generalInformationChangingService.deleteMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test_role/TEST_METAPROJECTS"));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testDeleteMetaprojectWithEmptyId()
    {
        generalInformationChangingService.deleteMetaproject(sessionToken, null);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testDeleteMetaprojectThatDoesntExist()
    {
        generalInformationChangingService.deleteMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/METAPROJECT_THAT_DOESNT_EXIST"));
    }

    @Test
    public void testAddToMetaproject()
    {
        Metaproject metaproject = getUtil().createMetaprojectWithAssignments(sessionToken);

        MetaprojectAssignments assignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        metaproject.getIdentifier()));

        List<Long> experimentIds = getUtil().getObjectIds(assignments.getExperiments());
        assertEquals(3, experimentIds.size());
        assertTrue(experimentIds.contains(2L));
        assertTrue(experimentIds.contains(22L));
        assertTrue(experimentIds.contains(23L));

        List<Long> samplesIds = getUtil().getObjectIds(assignments.getSamples());
        assertEquals(4, samplesIds.size());
        assertTrue(samplesIds.contains(647L));
        assertTrue(samplesIds.contains(602L));
        assertTrue(samplesIds.contains(340L));
        assertTrue(samplesIds.contains(342L));

        List<Long> dataSetIds = getUtil().getObjectIds(assignments.getDataSets());
        assertEquals(2, dataSetIds.size());
        assertTrue(dataSetIds.contains(8L));
        assertTrue(dataSetIds.contains(12L));

        List<Long> materialIds = getUtil().getObjectIds(assignments.getMaterials());
        assertEquals(2, materialIds.size());
        assertTrue(materialIds.contains(18L));
        assertTrue(materialIds.contains(8L));
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testAddToMetaprojectOwnedBySomebodyElse()
    {
        generalInformationChangingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test_role/TEST_METAPROJECTS"),
                new MetaprojectAssignmentsIds());
    }

    @Test
    public void testAddToMetaprojectAssignmentsThatAlreadyExist()
    {
        Metaproject metaproject =
                generalInformationChangingService.createMetaproject(sessionToken,
                        "BRAND_NEW_METAPROJECT", null);

        MetaprojectAssignmentsIds assignmentsToAdd = new MetaprojectAssignmentsIds();
        assignmentsToAdd.addExperiment(new ExperimentIdentifierId("/CISD/NEMO/EXP1"));

        generalInformationChangingService.addToMetaproject(sessionToken, new MetaprojectTechIdId(
                metaproject.getId()), assignmentsToAdd);
        generalInformationChangingService.addToMetaproject(sessionToken, new MetaprojectTechIdId(
                metaproject.getId()), assignmentsToAdd);

        MetaprojectAssignments assignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectTechIdId(
                        metaproject.getId()));
        assertEquals(1, assignments.getExperiments().size());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testAddToMetaprojectWithEmptyId()
    {
        generalInformationChangingService.addToMetaproject(sessionToken, null,
                new MetaprojectAssignmentsIds());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testAddToMetaprojectWithNullAssignments()
    {
        generalInformationChangingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"), null);
    }

    @Test
    public void testAddToMetaprojectWithEmptyAssignments()
    {
        generalInformationChangingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"),
                new MetaprojectAssignmentsIds());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testAddToMetaprojectThatDoesntExist()
    {
        generalInformationChangingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/METAPROJECT_THAT_DOESNT_EXIST"),
                new MetaprojectAssignmentsIds());
    }

    @Test
    public void testRemoveFromMetaproject()
    {
        Metaproject metaproject = getUtil().createMetaprojectWithAssignments(sessionToken);

        MetaprojectAssignmentsIds assignmentsToRemove = new MetaprojectAssignmentsIds();
        assignmentsToRemove.addExperiment(new ExperimentIdentifierId("/CISD/NEMO/EXP1"));
        assignmentsToRemove.addSample(new SampleIdentifierId("/A03"));
        assignmentsToRemove.addDataSet(new DataSetCodeId("20081105092259000-8"));
        assignmentsToRemove.addMaterial(new MaterialCodeAndTypeCodeId("GFP", "CONTROL"));

        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId(metaproject.getIdentifier()), assignmentsToRemove);

        MetaprojectAssignments assignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        metaproject.getIdentifier()));

        List<Long> experimentIds = getUtil().getObjectIds(assignments.getExperiments());
        assertEquals(2, experimentIds.size());
        assertTrue(experimentIds.contains(22L));
        assertTrue(experimentIds.contains(23L));

        List<Long> samplesIds = getUtil().getObjectIds(assignments.getSamples());
        assertEquals(3, samplesIds.size());
        assertTrue(samplesIds.contains(602L));
        assertTrue(samplesIds.contains(340L));
        assertTrue(samplesIds.contains(342L));

        List<Long> dataSetIds = getUtil().getObjectIds(assignments.getDataSets());
        assertEquals(1, dataSetIds.size());
        assertTrue(dataSetIds.contains(12L));

        List<Long> materialIds = getUtil().getObjectIds(assignments.getMaterials());
        assertEquals(1, materialIds.size());
        assertTrue(materialIds.contains(8L));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testRemoveFromMetaprojectOwnedBySomebodyElse()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test_role/TEST_METAPROJECTS"),
                new MetaprojectAssignmentsIds());
    }

    public void testRemoveFromMetaprojectAssignmentsThatDoNotExist()
    {
        Metaproject metaproject = getUtil().createMetaprojectWithAssignments(sessionToken);
        MetaprojectAssignmentsIds assignmentsToRemove = new MetaprojectAssignmentsIds();
        assignmentsToRemove.addExperiment(new ExperimentIdentifierId("/CISD/NEMO/EXP10"));

        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId(metaproject.getIdentifier()), assignmentsToRemove);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testRemoveFromMetaprojectWithEmptyId()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken, null,
                new MetaprojectAssignmentsIds());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testRemoveFromMetaprojectWithNullAssignments()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"), null);
    }

    @Test
    public void testRemoveFromMetaprojectWithEmptyAssignments()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"),
                new MetaprojectAssignmentsIds());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testRemoveFromMetaprojectThatDoesntExist()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/METAPROJECT_THAT_DOESNT_EXIST"),
                new MetaprojectAssignmentsIds());
    }

    private GeneralInformationServiceUtil getUtil()
    {
        return new GeneralInformationServiceUtil(generalInformationService,
                generalInformationChangingService);
    }

    @Test
    public void testDeleteProject()
    {
        testDeletePermanent(new ProjectDeleteAction(), new ProjectListDeletedAction());
    }

    @Test
    public void testDeleteExperientPermanent()
    {
        testDeletePermanent(new ExperimentDeleteAction(), new ExperimentListDeletedAction());
    }

    @Test
    public void testDeleteExperientTrash()
    {
        testDeleteTrash(new ExperimentDeleteAction(), new ExperimentListDeletedAction());
    }

    @Test
    public void testDeleteSamplePermanent()
    {
        testDeletePermanent(new SampleDeleteAction(), new SampleListDeletedAction());
    }

    @Test
    public void testDeleteSampleTrash()
    {
        testDeleteTrash(new SampleDeleteAction(), new SampleListDeletedAction());
    }

    @Test
    public void testDeleteDataSetPermanent()
    {
        testDeletePermanent(new DataSetDeleteAction(), new DataSetListDeletedAction());
    }

    @Test
    public void testDeleteDataSetTrash()
    {
        testDeleteTrash(new DataSetDeleteAction(), new DataSetListDeletedAction());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String adminSession = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";
        String reason = "test reason";

        List<DataSet> dataSets = generalInformationService.getDataSetMetaData(adminSession, Arrays.asList(dataSetCode));
        assertEquals(1, dataSets.size());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.deleteDataSets(session, Arrays.asList(dataSetCode), reason, DeletionType.TRASH);

            try
            {
                dataSets = generalInformationService.getDataSetMetaData(adminSession, Arrays.asList(dataSetCode));
            } catch (UserFailureException e)
            {
                assertEquals("Unknown data set " + dataSetCode, e.getMessage());
            }
        } else
        {
            try
            {
                generalInformationChangingService.deleteDataSets(session, Arrays.asList(dataSetCode), reason, DeletionType.TRASH);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateSamplePropertiesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        long sampleId = 1054L; // /TEST-SPACE/FV-TEST
        String propertyCode = "COMMENT";
        String propertyValue = "test comment";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            SampleParentWithDerived sample = commonServer.getSampleInfo(session, new TechId(sampleId));
            assertEquals(sample.getParent().getProperties().size(), 1);

            generalInformationChangingService.updateSampleProperties(session, sampleId, Collections.singletonMap(propertyCode, propertyValue));

            sample = commonServer.getSampleInfo(session, new TechId(sampleId));
            assertEquals(sample.getParent().getProperties().size(), 2);
        } else
        {
            try
            {
                generalInformationChangingService.updateSampleProperties(session, sampleId, Collections.singletonMap(propertyCode, propertyValue));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testAddToMetaprojectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        // intentionally create a metaproject bypassing authorization as we want to
        // concentrate on addToMetaproject method authorization here

        PersonPE owner = daoFactory.getPersonDAO().tryFindPersonByUserId(user.getUserId());

        MetaprojectPE metaproject = new MetaprojectPE();
        metaproject.setName("TEST_ADD_TO_METAPROJECT");
        metaproject.setOwner(owner);

        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(metaproject, owner);

        IMetaprojectId metaprojectId = new MetaprojectTechIdId(metaproject.getId());

        MetaprojectAssignmentsIds assignments = new MetaprojectAssignmentsIds();
        assignments.addExperiment(new ExperimentIdentifierId("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.addToMetaproject(session, metaprojectId, assignments);
        } else
        {
            try
            {
                generalInformationChangingService.addToMetaproject(session, metaprojectId, assignments);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRemoveFromMetaprojectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        // intentionally create a metaproject and an assignment bypassing authorization as we want to
        // concentrate on removeFromMetaproject method authorization here

        PersonPE owner = daoFactory.getPersonDAO().tryFindPersonByUserId(user.getUserId());
        ExperimentPE experiment = daoFactory.getExperimentDAO().tryGetByPermID("201206190940555-1032"); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        MetaprojectPE metaproject = new MetaprojectPE();
        metaproject.setName("TEST_REMOVE_FROM_METAPROJECT");
        metaproject.setOwner(owner);

        MetaprojectAssignmentPE assignment = new MetaprojectAssignmentPE();
        assignment.setExperiment(experiment);
        assignment.setMetaproject(metaproject);

        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(metaproject, owner);

        IMetaprojectId metaprojectId = new MetaprojectTechIdId(metaproject.getId());

        MetaprojectAssignmentsIds assignments = new MetaprojectAssignmentsIds();
        assignments.addExperiment(new ExperimentIdentifierId("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationChangingService.removeFromMetaproject(session, metaprojectId, assignments);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            generalInformationChangingService.removeFromMetaproject(session, metaprojectId, assignments);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRegisterSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContext session = commonClientService.tryToLogin(user.getUserId(), PASSWORD);

        String sampleType = "CELL_PLATE";

        uploadFile(session.getSessionID(), "testRegisterSamplesWithProjectAuthorization.txt",
                "identifier\texperiment\tCOMMENT\n"
                        + "/TEST-SPACE/PA_UPLOAD\t/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST\ttest comment\n");

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationChangingService.registerSamples(session.getSessionID(), sampleType, SESSION_KEY, null);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.registerSamples(session.getSessionID(), sampleType, SESSION_KEY, null);
        } else
        {
            try
            {
                generalInformationChangingService.registerSamples(session.getSessionID(), sampleType, SESSION_KEY, null);
                fail();
            } catch (ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException e)
            {
                AssertionUtil.assertMatches(".*does not have enough privileges.*", e.getMessage());
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContext session = commonClientService.tryToLogin(user.getUserId(), PASSWORD);

        String sampleType = "CELL_PLATE";

        uploadFile(session.getSessionID(), "testUpdateSamplesWithProjectAuthorization.txt",
                "identifier\tCOMMENT\n"
                        + "/TEST-SPACE/TEST-PROJECT/FV-TEST\tupdated comment\n");

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationChangingService.updateSamples(session.getSessionID(), sampleType, SESSION_KEY, null);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.updateSamples(session.getSessionID(), sampleType, SESSION_KEY, null);
        } else
        {
            try
            {
                generalInformationChangingService.updateSamples(session.getSessionID(), sampleType, SESSION_KEY, null);
                fail();
            } catch (ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException e)
            {
                AssertionUtil.assertMatches(".*does not have enough privileges.*", e.getMessage());
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUploadedSamplesInfoWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContext session = commonClientService.tryToLogin(user.getUserId(), PASSWORD);

        String sampleType = "CELL_PLATE";

        uploadFile(session.getSessionID(), "testUploadedSamplesInfoWithProjectAuthorization.txt",
                "identifier\texperiment\tCOMMENT\n"
                        + "/TEST-SPACE/PA_UPLOAD\t/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST\ttest comment\n");

        // there are no checks on what has been uploaded before the uploaded data is actually used in the registration or update

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationChangingService.uploadedSamplesInfo(session.getSessionID(), sampleType, SESSION_KEY);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            generalInformationChangingService.uploadedSamplesInfo(session.getSessionID(), sampleType, SESSION_KEY);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteProjectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        Long projectId = 7L; // /TEST-SPACE/PROJECT-TO-DELETE

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.deleteProjects(session, Arrays.asList(projectId), "test reason");
        } else
        {
            try
            {
                generalInformationChangingService.deleteProjects(session, Arrays.asList(projectId), "test reason");
                fail();
            } catch (AuthorizationFailureException e)
            {
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteExperimentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String adminSession = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        NewExperiment newExperiment = new NewExperiment();
        newExperiment.setExperimentTypeCode("DELETION_TEST");
        newExperiment.setIdentifier("/TEST-SPACE/TEST-PROJECT/EXPERIMENT-TO-DELETE");

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                genericServer.registerExperiment(adminSession, newExperiment, Arrays.asList());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.deleteExperiments(session, Arrays.asList(experiment.getId()), "test reason", DeletionType.TRASH);
        } else
        {
            try
            {
                generalInformationChangingService.deleteExperiments(session, Arrays.asList(experiment.getId()), "test reason", DeletionType.TRASH);
                fail();
            } catch (AuthorizationFailureException e)
            {
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        Long sampleId = 1061L; // /TEST-SPACE/SAMPLE-TO-DELETE

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.deleteSamples(session, Arrays.asList(sampleId), "test reason", DeletionType.TRASH);
        } else
        {
            try
            {
                generalInformationChangingService.deleteSamples(session, Arrays.asList(sampleId), "test reason", DeletionType.TRASH);
                fail();
            } catch (AuthorizationFailureException e)
            {
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRevertDeletionsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String adminSession = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        Long sampleId = 1061L; // /TEST-SPACE/SAMPLE-TO-DELETE

        generalInformationChangingService.deleteSamples(adminSession, Arrays.asList(sampleId), "test reason", DeletionType.TRASH);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion> deletions = commonServer.listDeletions(adminSession, true);

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion deletion = deletions.get(deletions.size() - 1);
        assertEquals("SAMPLE-TO-DELETE", deletion.getDeletedEntities().get(0).getCode());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.revertDeletions(session, Arrays.asList(deletion.getId()));
        } else
        {
            try
            {
                generalInformationChangingService.revertDeletions(session, Arrays.asList(deletion.getId()));
                fail();
            } catch (UserFailureException e)
            {
                if (e.getCause() instanceof AuthorizationFailureException || e.getCause() instanceof UnauthorizedObjectAccessException)
                {
                    // expected
                } else
                {
                    throw e;
                }
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeletePermanentlyWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String adminSession = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        Long sampleId = 1061L; // /TEST-SPACE/SAMPLE-TO-DELETE

        generalInformationChangingService.deleteSamples(adminSession, Arrays.asList(sampleId), "test reason", DeletionType.TRASH);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion> deletions = commonServer.listDeletions(adminSession, true);

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion deletion = deletions.get(deletions.size() - 1);
        assertEquals("SAMPLE-TO-DELETE", deletion.getDeletedEntities().get(0).getCode());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            generalInformationChangingService.deletePermanently(session, Arrays.asList(deletion.getId()));
        } else
        {
            try
            {
                generalInformationChangingService.deletePermanently(session, Arrays.asList(deletion.getId()));
                fail();
            } catch (AuthorizationFailureException e)
            {
            }
        }
    }

    @Test
    public void testSetWebAppSettings()
    {
        String webAppId1 = UUID.randomUUID().toString();
        String webAppId2 = UUID.randomUUID().toString();
        String adminSessionToken = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        // get empty settings 1 and 2

        WebAppSettings settings1 = generalInformationChangingService.getWebAppSettings(adminSessionToken, webAppId1);
        assertEquals(webAppId1, settings1.getWebAppId());
        assertEquals(Collections.emptyMap(), settings1.getSettings());

        WebAppSettings settings2 = generalInformationChangingService.getWebAppSettings(adminSessionToken, webAppId2);
        assertEquals(webAppId2, settings2.getWebAppId());
        assertEquals(Collections.emptyMap(), settings2.getSettings());

        // set settings 1

        WebAppSettings settings1Creation = new WebAppSettings(webAppId1, Collections.singletonMap("test-key-1", "test-value-1"));
        generalInformationChangingService.setWebAppSettings(adminSessionToken, settings1Creation);

        settings1 = generalInformationChangingService.getWebAppSettings(adminSessionToken, webAppId1);
        assertEquals(webAppId1, settings1.getWebAppId());
        assertEquals(settings1Creation.getSettings(), settings1.getSettings());

        settings2 = generalInformationChangingService.getWebAppSettings(adminSessionToken, webAppId2);
        assertEquals(webAppId2, settings2.getWebAppId());
        assertEquals(Collections.emptyMap(), settings2.getSettings());

        // set settings 2

        WebAppSettings settings2Creation = new WebAppSettings(webAppId2, Collections.singletonMap("test-key-2", "test-value-2"));
        generalInformationChangingService.setWebAppSettings(adminSessionToken, settings2Creation);

        settings1 = generalInformationChangingService.getWebAppSettings(adminSessionToken, webAppId1);
        assertEquals(webAppId1, settings1.getWebAppId());
        assertEquals(settings1Creation.getSettings(), settings1.getSettings());

        settings2 = generalInformationChangingService.getWebAppSettings(adminSessionToken, webAppId2);
        assertEquals(webAppId2, settings2.getWebAppId());
        assertEquals(settings2.getSettings(), settings2.getSettings());
    }

    private class ProjectDeleteAction implements DeleteAction<Project>
    {
        @Override
        public void delete(Project project, String reason, DeletionType deletionType)
        {
            generalInformationChangingService.deleteProjects(sessionToken, Collections.singletonList(project.getId()), reason);
        }
    }

    private class ProjectListDeletedAction implements ListDeletedAction<Project>
    {
        @Override
        public List<Project> list()
        {
            List<Project> projects = generalInformationService.listProjects(sessionToken);
            for (Project project : projects)
            {
                if (project.getIdentifier().equals("/TEST-SPACE/PROJECT-TO-DELETE"))
                {
                    return Collections.singletonList(project);
                }
            }
            return Collections.emptyList();
        }
    }

    private class ExperimentDeleteAction implements DeleteAction<Experiment>
    {
        @Override
        public void delete(Experiment experiment, String reason, DeletionType deletionType)
        {
            generalInformationChangingService.deleteExperiments(sessionToken, Collections.singletonList(experiment.getId()), reason,
                    deletionType);
        }
    }

    private class ExperimentListDeletedAction implements ListDeletedAction<Experiment>
    {
        @Override
        public List<Experiment> list()
        {
            return generalInformationService.listExperiments(sessionToken, Collections.singletonList("/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE"));
        }
    }

    private class SampleDeleteAction implements DeleteAction<Sample>
    {
        @Override
        public void delete(Sample sample, String reason, DeletionType deletionType)
        {
            generalInformationChangingService.deleteSamples(sessionToken, Collections.singletonList(sample.getId()), reason,
                    deletionType);
        }
    }

    private class SampleListDeletedAction implements ListDeletedAction<Sample>
    {
        @Override
        public List<Sample> list()
        {
            SearchCriteria criteria = new SearchCriteria();
            criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "SAMPLE-TO-DELETE"));
            return generalInformationService.searchForSamples(sessionToken, criteria);
        }
    }

    private class DataSetDeleteAction implements DeleteAction<DataSet>
    {
        @Override
        public void delete(DataSet dataSet, String reason, DeletionType deletionType)
        {
            generalInformationChangingService.deleteDataSets(sessionToken, Collections.singletonList(dataSet.getCode()), reason,
                    deletionType);
        }
    }

    private class DataSetListDeletedAction implements ListDeletedAction<DataSet>
    {
        @Override
        public List<DataSet> list()
        {
            SearchCriteria criteria = new SearchCriteria();
            criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "DATASET-TO-DELETE"));
            return generalInformationService.searchForDataSets(sessionToken, criteria);
        }
    }

    private <T> void testDeleteTrash(DeleteAction<T> deleteAction, ListDeletedAction<T> listAction)
    {
        List<T> beforeObjects = listAction.list();
        List<Deletion> beforeDeletions = generalInformationService.listDeletions(sessionToken, null);

        Assert.assertEquals(1, beforeObjects.size());

        // move an object to trash
        T deletedObject = beforeObjects.get(0);
        String reason = "Test delete to trash: " + deletedObject;
        deleteAction.delete(deletedObject, reason, DeletionType.TRASH);

        List<T> afterObjects = listAction.list();
        List<Deletion> afterDeletions = generalInformationService.listDeletions(sessionToken, null);
        Deletion deletion = afterDeletions.get(afterDeletions.size() - 1);

        // the object should not be listed and a new deletion should be appear
        Assert.assertEquals(0, afterObjects.size());
        Assert.assertEquals(beforeDeletions.size() + 1, afterDeletions.size());
        Assert.assertEquals(reason, deletion.getReasonOrNull());

        // revert the logical deletion
        generalInformationChangingService.revertDeletions(sessionToken, Collections.singletonList(deletion.getId()));

        afterObjects = listAction.list();
        afterDeletions = generalInformationService.listDeletions(sessionToken, null);

        // the object should be listed again and the deletion should be gone
        Assert.assertEquals(1, afterObjects.size());
        Assert.assertEquals(beforeDeletions.size(), afterDeletions.size());

        // move the object again to trash
        deleteAction.delete(deletedObject, reason, DeletionType.TRASH);

        afterObjects = listAction.list();
        afterDeletions = generalInformationService.listDeletions(sessionToken, null);
        deletion = afterDeletions.get(afterDeletions.size() - 1);

        // the object should be gone again and a new deletion should appear
        Assert.assertEquals(0, afterObjects.size());
        Assert.assertEquals(beforeDeletions.size() + 1, afterDeletions.size());
        Assert.assertEquals(reason, deletion.getReasonOrNull());

        // permanently confirm the deletion
        generalInformationChangingService.deletePermanently(sessionToken, Collections.singletonList(deletion.getId()));

        afterObjects = listAction.list();
        afterDeletions = generalInformationService.listDeletions(sessionToken, null);

        // both the object and the deletion should be gone
        Assert.assertEquals(0, afterObjects.size());
        Assert.assertEquals(beforeDeletions.size(), afterDeletions.size());
    }

    private <T> void testDeletePermanent(DeleteAction<T> deleteAction, ListDeletedAction<T> listAction)
    {
        List<T> beforeObjects = listAction.list();
        List<Deletion> beforeDeletions = generalInformationService.listDeletions(sessionToken, null);

        Assert.assertEquals(1, beforeObjects.size());

        // delete an object permanently
        T deletedObject = beforeObjects.get(0);
        deleteAction.delete(deletedObject, "Test delete permanently: " + deletedObject, DeletionType.PERMANENT);

        List<T> afterObjects = listAction.list();
        List<Deletion> afterDeletions = generalInformationService.listDeletions(sessionToken, null);

        // the object should be gone and no new deletions should appear
        Assert.assertEquals(0, afterObjects.size());
        Assert.assertEquals(beforeDeletions.size(), afterDeletions.size());
    }

    private interface DeleteAction<T>
    {
        public void delete(T object, String reason, DeletionType deletionType);
    }

    private interface ListDeletedAction<T>
    {
        public List<T> list();
    }

}
