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

import static ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory.USE_NEW_SQL_ENGINE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author pkupczyk
 */
@Test(groups =
{ "db", "dataset" })
public class ETLServiceDatabaseTest extends AbstractDAOTest
{
    @Autowired
    private IServiceForDataStoreServer service;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryAuthenticate("test", "password").getSessionToken();
    }

    @Test
    public void testListExperimentsWithBasicFetchOptions()
    {
        List<ExperimentIdentifier> identifiers =
                Collections.singletonList(new ExperimentIdentifier("CISD", "NEMO", "EXP1"));

        List<Experiment> result =
                service.listExperiments(sessionToken, identifiers, new ExperimentFetchOptions());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(ExperimentFetchOption.BASIC));
    }

    @Test
    public void testListExperimentsWithAllFetchOptions()
    {
        List<ExperimentIdentifier> identifiers =
                Collections.singletonList(new ExperimentIdentifier("CISD", "NEMO", "EXP1"));

        List<Experiment> result =
                service.listExperiments(sessionToken, identifiers, new ExperimentFetchOptions(
                        ExperimentFetchOption.values()));

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(ExperimentFetchOption.values()));
    }

    @Test
    public void testListExperimentsForProjectsWithBasicFetchOptions()
    {
        List<ProjectIdentifier> identifiers =
                Collections.singletonList(new ProjectIdentifier("CISD", "NOE"));

        List<Experiment> result =
                service.listExperimentsForProjects(sessionToken, identifiers,
                        new ExperimentFetchOptions());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(ExperimentFetchOption.BASIC));
    }

    @Test
    public void testListExperimentsForProjectsWithAllFetchOptions()
    {
        List<ProjectIdentifier> identifiers =
                Collections.singletonList(new ProjectIdentifier("CISD", "NOE"));

        List<Experiment> result =
                service.listExperimentsForProjects(sessionToken, identifiers,
                        new ExperimentFetchOptions(ExperimentFetchOption.values()));

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(ExperimentFetchOption.values()));
    }

    @Test
    public void testPerformEntityOperationsUpdateSample()
    {
        Sample sample = findSampleByCode("3VCP7");

        // Update the comment
        String sampleComment = "This is a new comment for a sample.";
        updateEntityProperty(sample, "COMMENT", sampleComment);

        // Update the parents
        Sample parent = findSampleByCode("3V-126");
        updateEntityProperty(parent, "OFFSET", "43");
        assertEquals(1, sample.getParents().size());
        sample.addParent(parent);

        SampleUpdatesDTO parentUpdate = convertToSampleUpdateDTO(parent);
        SampleUpdatesDTO sampleUpdate = convertToSampleUpdateDTO(sample);
        performSampleUpdate(Arrays.asList(sampleUpdate, parentUpdate), 1);

        // Now retrieve the sample again and check that the properties were updated.
        Sample updatedSample = findSampleByCode("3VCP7");
        Sample updatedParent = findSampleByCode("3V-126");

        assertTrue("The modification date should have been updated", updatedSample
                .getModificationDate().compareTo(sample.getModificationDate()) > 0);
        assertTrue("The modification date should have been updated", updatedParent
                .getModificationDate().compareTo(sample.getModificationDate()) > 0);
        assertEquals(sampleComment, EntityHelper.tryFindPropertyValue(updatedSample, "COMMENT"));
        assertEquals("43", EntityHelper.tryFindPropertyValue(updatedParent, "OFFSET"));
        assertEquals(2, updatedSample.getParents().size());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPerformEntityOperationsUpdateStaleSample()
    {
        Sample sampleToUpdate = findSampleByCode("3VCP7");

        // Update the comment
        String newComment = "This is a new comment. This is not the old comment.";
        updateEntityProperty(sampleToUpdate, "COMMENT", newComment);

        SampleUpdatesDTO sampleUpdate = convertToSampleUpdateDTO(sampleToUpdate);
        sampleUpdate.setVersion(42);
        performSampleUpdate(Arrays.asList(sampleUpdate), 1);
    }

    @Test
    public void testPerformEntityOperationsCreateSample()
    {
        // Get the parents
        Sample parent = findSampleByCode("3VCP7");

        NewSample sampleToCreate = new NewSample();
        String newSampleIdentifier = "/" + parent.getSpace().getCode() + "/" + "NEW-SAMPLE";
        sampleToCreate.setIdentifier(newSampleIdentifier);
        sampleToCreate.setSampleType(parent.getSampleType());

        // Set the properties
        String comment = "This is a comment.";
        IEntityProperty commentProperty = EntityHelper.createNewProperty("COMMENT", comment);
        sampleToCreate.setProperties(new IEntityProperty[]
        { commentProperty });

        sampleToCreate.setParentsOrNull(new String[]
        { parent.getIdentifier() });

        performSampleCreation(sampleToCreate);

        // Now retrieve the sample again and check it was created
        ListSampleCriteria listCriteria = ListSampleCriteria.createForParent(TechId.create(parent));
        List<Sample> childSamples = service.listSamples(sessionToken, listCriteria);
        Sample createdSample = null;
        for (Sample child : childSamples)
        {
            if (child.getIdentifier().equals(sampleToCreate.getIdentifier()))
            {
                createdSample = child;
                break;
            }
        }
        assertTrue(createdSample != null);
        assertEquals(comment, EntityHelper.tryFindPropertyValue(createdSample, "COMMENT"));
        assertTrue(createdSample.getParents().contains(parent));
    }

    @Test
    public void testPerformEntityOperationsUpdateDataSet()
    {
        // Find the samples to add
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(
                MatchClauseAttribute.TYPE, "HCS_IMAGE"));
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch("COMMENT",
                "no comment"));
        List<AbstractExternalData> dataSetsToUpdate =
                service.searchForDataSets(sessionToken, searchCriteria);
        if (USE_NEW_SQL_ENGINE) {
            assertEquals(11, dataSetsToUpdate.size());
        } else {
            assertEquals(22, dataSetsToUpdate.size()); // The old search engine doesn't have real equals
        }
        Date now = daoFactory.getTransactionTimestamp();

        // Update the comment
        String newComment = "This is a new comment. This is not the old comment.";
        List<DataSetBatchUpdatesDTO> dataSetUpdates = new ArrayList<DataSetBatchUpdatesDTO>();
        for (AbstractExternalData dataSetToUpdate : dataSetsToUpdate)
        {
            assertTrue("The modification date should be in the distant past", dataSetToUpdate
                    .getModificationDate().compareTo(now) < 0);

            String oldComment = EntityHelper.tryFindPropertyValue(dataSetToUpdate, "COMMENT");
            assertFalse(newComment.equals(oldComment));
            DataSetBatchUpdatesDTO updates =
                    createDataSetUpdateDTO(dataSetToUpdate, "COMMENT", newComment);
            dataSetUpdates.add(updates);
        }

        performDataSetUpdates(dataSetUpdates, 1);

        // Now retrieve the sample again and check that the properties were updated.
        // Find the samples to add
        SearchCriteria searchCriteriaUpdated = new SearchCriteria();
        searchCriteriaUpdated.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(
                MatchClauseAttribute.TYPE, "HCS_IMAGE"));
        searchCriteriaUpdated.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch("COMMENT",
                newComment));

        List<AbstractExternalData> updatedDataSets =
                service.searchForDataSets(sessionToken, searchCriteriaUpdated);
        // The index has not been updated yet, so we have to group the items into those that
        // still have the old comment and those with the new comment

        List<AbstractExternalData> dataSetsWithOldValue = new ArrayList<AbstractExternalData>();
        List<AbstractExternalData> dataSetsWithNewValue = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData data : updatedDataSets)
        {
            String comment = EntityHelper.tryFindPropertyValue(data, "COMMENT");
            if (newComment.equals(comment))
            {
                dataSetsWithNewValue.add(data);
            }
        }

        assertEquals(0, dataSetsWithOldValue.size());
        assertEquals(dataSetsToUpdate.size(), dataSetsWithNewValue.size());

        for (AbstractExternalData dataSetWithNewValue : dataSetsWithNewValue)
        {
            Date modificationDate = dataSetWithNewValue.getModificationDate();
            assertTrue("The modification date (" + modificationDate + ") should be current (" + now + ")",
                    modificationDate.compareTo(now) >= 0);

            String savedComment = EntityHelper.tryFindPropertyValue(dataSetWithNewValue, "COMMENT");
            assertTrue(newComment.equals(savedComment));
        }

    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPerformEntityOperationsUpdateStaleDataSet()
    {
        AbstractExternalData dataSetToUpdate = findDatasetByCode("20081105092159188-3");

        DataSetBatchUpdatesDTO update =
                createDataSetUpdateDTO(dataSetToUpdate, "COMMENT",
                        "This is a new comment. This is not the old comment.");

        update.setVersion(123456);
        performDataSetUpdates(Arrays.asList(update), 1);
    }

    private void performDataSetUpdates(List<DataSetBatchUpdatesDTO> dataSetUpdates,
            Integer batchSizeOrNull)
    {
        TechId registrationid = new TechId(service.drawANewUniqueID(sessionToken));
        List<NewSpace> spaceRegistrations = Collections.emptyList();
        List<NewProject> projectRegistrations = Collections.emptyList();
        List<ProjectUpdatesDTO> projectUpdates = Collections.emptyList();
        List<NewExperiment> experimentRegistrations = Collections.emptyList();
        List<ExperimentUpdatesDTO> experimentUpdates =
                Collections.<ExperimentUpdatesDTO> emptyList();
        List<SampleUpdatesDTO> sampleUpdates = Collections.emptyList();
        List<NewSample> sampleRegistrations = Collections.emptyList();
        Map<String, List<NewMaterial>> materialRegistrations = Collections.emptyMap();
        List<MaterialUpdateDTO> materialUpdates = Collections.emptyList();
        List<? extends NewExternalData> dataSetRegistrations = Collections.emptyList();
        List<NewMetaproject> metaprojectRegistrations = Collections.emptyList();
        List<MetaprojectUpdatesDTO> metaprojectUpdates = Collections.emptyList();
        List<VocabularyUpdatesDTO> vocabularyUpdates = Collections.emptyList();

        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(registrationid, null, spaceRegistrations,
                        projectRegistrations, projectUpdates, experimentRegistrations,
                        experimentUpdates, sampleUpdates, sampleRegistrations,
                        materialRegistrations, materialUpdates, dataSetRegistrations,
                        dataSetUpdates, metaprojectRegistrations,
                        metaprojectUpdates, vocabularyUpdates, batchSizeOrNull);
        service.performEntityOperations(sessionToken, details);
    }

    private void performSampleUpdate(List<SampleUpdatesDTO> sampleUpdates, Integer batchSizeOrNull)
    {
        TechId registrationid = new TechId(service.drawANewUniqueID(sessionToken));
        List<NewSpace> spaceRegistrations = Collections.emptyList();
        List<NewProject> projectRegistrations = Collections.emptyList();
        List<ProjectUpdatesDTO> projectUpdates = Collections.emptyList();
        List<NewExperiment> experimentRegistrations = Collections.emptyList();
        List<ExperimentUpdatesDTO> experimentUpdates =
                Collections.<ExperimentUpdatesDTO> emptyList();

        List<NewSample> sampleRegistrations = Collections.emptyList();
        Map<String, List<NewMaterial>> materialRegistrations = Collections.emptyMap();
        List<MaterialUpdateDTO> materialUpdates = Collections.emptyList();
        List<? extends NewExternalData> dataSetRegistrations = Collections.emptyList();
        List<DataSetBatchUpdatesDTO> dataSetUpdates = Collections.emptyList();
        List<NewMetaproject> metaprojectRegistrations = Collections.emptyList();
        List<MetaprojectUpdatesDTO> metaprojectUpdates = Collections.emptyList();
        List<VocabularyUpdatesDTO> vocabularyUpdates = Collections.emptyList();

        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(registrationid, null, spaceRegistrations,
                        projectRegistrations, projectUpdates, experimentRegistrations,
                        experimentUpdates, sampleUpdates, sampleRegistrations,
                        materialRegistrations, materialUpdates,
                        dataSetRegistrations, dataSetUpdates, metaprojectRegistrations,
                        metaprojectUpdates, vocabularyUpdates, batchSizeOrNull);
        service.performEntityOperations(sessionToken, details);
    }

    private SampleUpdatesDTO convertToSampleUpdateDTO(Sample sample)
    {
        List<NewAttachment> attachments = Collections.emptyList();
        String containerIdentifier =
                (sample.getContainer() != null) ? sample.getContainer().getIdentifier() : null;
        Set<Sample> sampleParents = sample.getParents();
        String[] parentCodes = new String[sampleParents.size()];
        int i = 0;
        for (Sample parent : sampleParents)
        {
            parentCodes[i++] = parent.getCode();
        }
        SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(TechId.create(sample), // db id
                        sample.getProperties(), // List<IEntityProperty>
                        sample.getExperiment() == null ? null
                                : ExperimentIdentifierFactory.parse(sample.getExperiment()
                                        .getIdentifier()), // ExperimentIdentifier
                        null,
                        attachments, // Collection<NewAttachment>
                        sample.getVersion(), // Sample version
                        SampleIdentifierFactory.parse(sample.getIdentifier()), // Sample Identifier
                        containerIdentifier, // Container Identifier
                        parentCodes // Parent Identifiers
                );
        return sampleUpdate;
    }

    private DataSetBatchUpdatesDTO createDataSetUpdateDTO(AbstractExternalData dataSet,
            String propertyCode, String propertyValue)
    {
        // Create the initial information
        DataSetBatchUpdatesDTO updates = new DataSetBatchUpdatesDTO();
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updates.setDatasetCode(dataSet.getCode());
        updates.setDatasetId(TechId.create(dataSet));
        updates.setDetails(updateDetails);
        updates.setVersion(dataSet.getVersion());

        String identifierString = dataSet.getExperiment().getIdentifier();
        ExperimentIdentifier experimentIdentifier =
                ExperimentIdentifierFactory.parse(identifierString);
        updates.setExperimentIdentifierOrNull(experimentIdentifier);

        // Request a property update
        EntityHelper.createOrUpdateProperty(dataSet, propertyCode, propertyValue);
        updates.setProperties(dataSet.getProperties());
        Set<String> propertiesToUpdate = new HashSet<String>();
        propertiesToUpdate.add(propertyCode);
        updateDetails.setPropertiesToUpdate(propertiesToUpdate);
        return updates;
    }

    private void performSampleCreation(NewSample sampleToCreate)
    {
        TechId registrationid = new TechId(service.drawANewUniqueID(sessionToken));
        List<NewSpace> spaceRegistrations = Collections.emptyList();
        List<NewProject> projectRegistrations = Collections.emptyList();
        List<ProjectUpdatesDTO> projectUpdates = Collections.emptyList();
        List<NewExperiment> experimentRegistrations = Collections.emptyList();
        List<ExperimentUpdatesDTO> experimentUpdates =
                Collections.<ExperimentUpdatesDTO> emptyList();
        List<SampleUpdatesDTO> sampleUpdates = Collections.emptyList();
        List<NewSample> sampleRegistrations = Arrays.asList(sampleToCreate);
        Map<String, List<NewMaterial>> materialRegistrations = Collections.emptyMap();
        List<MaterialUpdateDTO> materialUpdates = Collections.emptyList();
        List<? extends NewExternalData> dataSetRegistrations = Collections.emptyList();
        List<DataSetBatchUpdatesDTO> dataSetUpdates = Collections.emptyList();
        List<NewMetaproject> metaprojectRegistrations = Collections.emptyList();
        List<MetaprojectUpdatesDTO> metaprojectUpdates = Collections.emptyList();
        List<VocabularyUpdatesDTO> vocabularyUpdates = Collections.emptyList();

        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(registrationid, null, spaceRegistrations,
                        projectRegistrations, projectUpdates, experimentRegistrations,
                        experimentUpdates, sampleUpdates, sampleRegistrations,
                        materialRegistrations, materialUpdates,
                        dataSetRegistrations, dataSetUpdates, metaprojectRegistrations,
                        metaprojectUpdates, vocabularyUpdates);
        service.performEntityOperations(sessionToken, details);
    }

    @Test
    public void testDoesUserHaveRole()
    {
        assertTrue(service.doesUserHaveRole(sessionToken, "observer", "OBSERVER", "TESTGROUP"));

        assertTrue(service.doesUserHaveRole(sessionToken, "test", "OBSERVER", null));
        assertTrue(service.doesUserHaveRole(sessionToken, "test", "ADMIN", null));
        assertTrue(service.doesUserHaveRole(sessionToken, "test", "OBSERVER", "TESTGROUP"));
        assertTrue(service.doesUserHaveRole(sessionToken, "test", "USER", "TESTGROUP"));
        assertTrue(service.doesUserHaveRole(sessionToken, "test", "POWER_USER", "TESTGROUP"));
        assertTrue(service.doesUserHaveRole(sessionToken, "test", "ADMIN", "TESTGROUP"));

        assertTrue(service.doesUserHaveRole(sessionToken, "test", "OBSERVER", "CISD"));
        assertTrue(service.doesUserHaveRole(sessionToken, "test", "USER", "CISD"));
        assertTrue(service.doesUserHaveRole(sessionToken, "test", "POWER_USER", "CISD"));
        assertTrue(service.doesUserHaveRole(sessionToken, "test", "ADMIN", "CISD"));

        assertFalse(service.doesUserHaveRole(sessionToken, "test_role", "OBSERVER", "TESTGROUP"));
        assertFalse(service.doesUserHaveRole(sessionToken, "test_role", "USER", "TESTGROUP"));
        assertFalse(service.doesUserHaveRole(sessionToken, "test_role", "POWER_USER", "TESTGROUP"));
        assertFalse(service.doesUserHaveRole(sessionToken, "test_role", "ADMIN", "TESTGROUP"));

        assertTrue(service.doesUserHaveRole(sessionToken, "test_role", "OBSERVER", "CISD"));
        assertTrue(service.doesUserHaveRole(sessionToken, "test_role", "USER", "CISD"));
        assertTrue(service.doesUserHaveRole(sessionToken, "test_role", "POWER_USER", "CISD"));
        assertFalse(service.doesUserHaveRole(sessionToken, "test_role", "ADMIN", "CISD"));

        assertFalse(service.doesUserHaveRole(sessionToken, "test_role", "OBSERVER", null));
        assertFalse(service.doesUserHaveRole(sessionToken, "test_role", "ADMIN", null));
    }

    public void testFilterDataSets()
    {
        LinkedList<String> dataSetCodesAll = new LinkedList<String>();
        dataSetCodesAll.add("20081105092259000-20");
        dataSetCodesAll.add("20081105092259000-21");
        dataSetCodesAll.add("20120619092259000-22");

        List<String> result =
                service.filterToVisibleDataSets(sessionToken, "test_role", dataSetCodesAll);

        assertEquals(2, result.size());

        assertTrue(result.contains("20081105092259000-20"));
        assertTrue(result.contains("20081105092259000-21"));

        assertFalse(result.contains("20120619092259000-22"));
    }

    public void testFilterExperiments()
    {
        LinkedList<String> experimentsAll = new LinkedList<String>();
        experimentsAll.add("/TESTGROUP/TESTPROJ/EXP-SPACE-TEST");
        experimentsAll.add("/CISD/NEMO/EXP1");
        experimentsAll.add("/CISD/NEMO/EXP10");

        List<String> result =
                service.filterToVisibleExperiments(sessionToken, "test_role", experimentsAll);

        assertEquals(2, result.size());

        assertTrue(result.contains("/CISD/NEMO/EXP1"));
        assertTrue(result.contains("/CISD/NEMO/EXP10"));

        assertFalse(result.contains("/TESTGROUP/TESTPROJ/EXP-SPACE-TEST"));
    }

    public void testFilterSamples()
    {
        LinkedList<String> samplesAll = new LinkedList<String>();
        samplesAll.add("/CISD/SAMPLE_EXAMPLE-1");
        samplesAll.add("/CISD/SAMPLE_EXAMPLE-2");
        samplesAll.add("/TESTGROUP/SAMPLE_EXAMPLE");

        List<String> result = service.filterToVisibleSamples(sessionToken, "test_role", samplesAll);

        assertEquals(2, result.size());

        assertTrue(result.contains("/CISD/SAMPLE_EXAMPLE-1"));
        assertTrue(result.contains("/CISD/SAMPLE_EXAMPLE-2"));

        assertFalse(result.contains("/TESTGROUP/SAMPLE_EXAMPLE"));
    }

    private Sample findSampleByCode(String code)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, code));
        List<Sample> samples = service.searchForSamples(sessionToken, searchCriteria);
        assertEquals(1, samples.size());
        return samples.get(0);
    }

    private AbstractExternalData findDatasetByCode(String code)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, code));
        List<AbstractExternalData> dataSets = service.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(1, dataSets.size());
        return dataSets.get(0);
    }

    private void updateEntityProperty(IEntityPropertiesHolder entity, String propertyName,
            String propertyValue)
    {
        String oldValue = EntityHelper.tryFindPropertyValue(entity, propertyName);
        assertFalse(propertyValue.equals(oldValue));
        EntityHelper.createOrUpdateProperty(entity, propertyName, propertyValue);
    }

}
