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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Kaloyan Enimanev
 */
public class DeletionTestCase extends SystemTestCase
{
    private static final DefaultResultSetConfig<String, TableModelRowWithObject<Deletion>> FETCH_ALL =
            DefaultResultSetConfig.<String, TableModelRowWithObject<Deletion>> createFetchAll();

    private static final String REASON = "REASON";

    protected IDeletionDAO deletionDAO;

    private String sessionToken;

    private List<Experiment> registeredExperiments;

    private List<Sample> registeredSamples;

    private List<Sample> registeredSamplesThatShouldBeDeleted;

    private List<DeletionPE> preExistingDeletions;

    private Set<Long> preExistingDeletionIDs;

    @Autowired
    public final void setEtlService(IServiceForDataStoreServer etlService)
    {
        this.etlService = etlService;
    }

    @BeforeMethod
    public void setUp()
    {
        registeredExperiments = new ArrayList<Experiment>();
        registeredSamples = new ArrayList<Sample>();
        registeredSamplesThatShouldBeDeleted = new ArrayList<Sample>();

        SessionContext sessionContext = logIntoCommonClientService();
        sessionToken = sessionContext.getSessionID();
        deletionDAO = daoFactory.getDeletionDAO();

        preExistingDeletions = deletionDAO.listAllEntities();
        preExistingDeletionIDs = new HashSet<Long>();
        for (DeletionPE deletion : preExistingDeletions)
        {
            preExistingDeletionIDs.add(deletion.getId());
        }

        // experiments
        createExperiment("E1");
        createExperiment("E2");

        // samples
        createSample("E1", "S1");
        createComponentSample("E1", "S1", "S1.1");
        createComponentSample("E1", "S1", "S1.2");
        createComponentSample("E2", "S1", "S1.3");
        createChildSample("E1", "S1.2", "S1.2.1");
        createChildSample("E1", "S1.2", "S1.2.2");
        createChildSample("E1", "S1.2.1", "S1.2.1.1");
        createChildSample("E2", "S1.2.1", "S1.2.1.2", false);

        createChildSample("E2", "S1.3", "S1.3.1", false);
        createComponentSample("E1", "S1.3.1", "S1.3.1.1");
        createComponentSample("E1", "S1.3.1", "S1.3.1.2");
        createComponentSample("E2", "S1.3.1", "S1.3.1.3", false);

        // nested children
        createChildSample("E1", "S1", "S1.4");
        createChildSample("E1", "S1.4", "S1.4.1");
        createChildSample("E2", "S1.4.1", "S1.4.1.1", false);
        createChildSample("E1", "S1.4.1.1", "S1.4.1.1.1");
        createChildSample("E1", "S1.4.1.1", "S1.4.1.1.2");
        createChildSample("E2", "S1.4.1.1.1", "S1.4.1.1.1.1", false);

        // try also components under nested children
        createComponentSample("E1", "S1.4.1.1.1.1", "S1.4.1.1.1.1.1");
        createComponentSample("E1", "S1.4.1.1.1.1", "S1.4.1.1.1.1.2");
        createComponentSample("E1", "S1.4.1.1.1.1", "S1.4.1.1.1.1.3");
        createChildSample("E1", "S1.4.1.1.1.1.3", "S1.4.1.1.1.1.3.1");
        createChildSample("E1", "S1.4.1.1.1.1.3", "S1.4.1.1.1.1.3.2");

        // an empty samples
        createChildSample("E1", "S1", "S1.5");

        // some more samples in an "S2"-branch
        createSample("E1", "S2");
        createChildSample("E1", "S2", "S2.1");
        createChildSample("E2", "S2", "S2.2", false);
        createChildSample("E1", "S2", "S2.3");
        createComponentSample("E1", "S2.1", "S2.2");

    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        if (commonServer != null)
        {
            List<Experiment> existingExperiments = new ArrayList<Experiment>();
            if (registeredExperiments != null)
            {
                for (Experiment exp : registeredExperiments)
                {
                    if (isExistingExperiment(exp.getCode()))
                    {
                        existingExperiments.add(exp);
                    }
                }
            }
            flushAndClearHibernateSession();
            commonServer.deleteExperiments(sessionToken, TechId.createList(existingExperiments),
                    REASON, DeletionType.TRASH);
            commonServer.deletePermanently(sessionToken, TechId.createList(listDeletions()));
        }
    }

    @Test
    public void testDeleteExperimentE1()
    {
        Experiment e1 = findExperimentByCode("E1");
        final TechId experimentId = new TechId(e1);
        // delete
        commonServer.deleteExperiments(sessionToken, Collections.singletonList(experimentId),
                REASON, DeletionType.TRASH);

        assertExperimentDoesNotExist(e1.getCode());

        assertSamplesDoNotExist(registeredSamplesThatShouldBeDeleted);

        List<DeletionPE> deletions = listDeletions();
        assertEquals(1, deletions.size());

        List<TableModelRowWithObject<Deletion>> deletionTable = getDeletionTable();
        List<ISerializableComparable> row = deletionTable.get(0).getValues();
        assertEquals("Experiment   /CISD/DEFAULT/E1 (COMPOUND_HCS)",
                row.get(2).toString().split("\n")[0]);
        assertEquals(REASON, row.get(3).toString());
        assertEquals(1, deletionTable.size());

        // revert
        final TechId deletionId1 = TechId.create(deletions.get(0));
        commonServer.revertDeletions(sessionToken, Collections.singletonList(deletionId1));

        assertExperimentExists(e1.getCode());
        assertSamplesExist(registeredSamples);

        // delete permanently
        flushAndClearHibernateSession();
        commonServer.deleteExperiments(sessionToken, Collections.singletonList(experimentId),
                REASON, DeletionType.TRASH);
        final TechId deletionId2 = TechId.create(listDeletions().get(0));
        commonServer.deletePermanently(sessionToken, Collections.singletonList(deletionId2));
        assertExperimentDoesNotExist(e1.getCode());
        assertSamplesDoNotExist(registeredSamplesThatShouldBeDeleted);
    }

    @Autowired
    SessionFactory sessionFactory;

    @Test
    public void testDeleteExperimentWithContainerDataSetWhichHasADataSetFromAnotherExperiment()
    {
        // Change experiment of data set 'COMPONENT_1B' to 'E1'
        TechId dataSetId = new TechId(34);
        PhysicalDataSet originalDataSet = genericServer.getDataSetInfo(sessionToken, dataSetId).tryGetAsDataSet();
        Experiment e1 = findExperimentByCode("E1");
        DataSetUpdatesDTO updatesDTO = new DataSetUpdatesDTO();
        updatesDTO.setDatasetId(dataSetId);
        updatesDTO.setExperimentIdentifierOrNull(new ExperimentIdentifier(e1));
        updatesDTO.setFileFormatTypeCode("XML");
        updatesDTO.setProperties(Arrays.<IEntityProperty> asList(new PropertyBuilder("COMMENT").value("hello").getProperty()));
        genericServer.updateDataSet(sessionToken, updatesDTO);

        // Delete experiment which has container data set 'CONTAINER_1' where 'COMPONENT_1B' is a component
        commonServer.deleteExperiments(sessionToken, Arrays.asList(new TechId(8)), REASON, DeletionType.TRASH);

        assertEquals(null, commonServer.getDataSetInfo(sessionToken, dataSetId).getDeletion());
        List<DeletionPE> deletions = listDeletions();
        assertEquals(1, deletions.size());

        // revert deletion and data set COMPONENT_1B in order to restore database state
        TechId deletionId1 = TechId.create(deletions.get(0));
        commonServer.revertDeletions(sessionToken, Collections.singletonList(deletionId1));
        updatesDTO = new DataSetUpdatesDTO();
        updatesDTO.setDatasetId(dataSetId);
        updatesDTO.setExperimentIdentifierOrNull(new ExperimentIdentifier(originalDataSet.getExperiment()));
        updatesDTO.setFileFormatTypeCode(originalDataSet.getFileFormatType().getCode());
        updatesDTO.setProperties(originalDataSet.getProperties());
        updatesDTO.setVersion(genericServer.getDataSetInfo(sessionToken, dataSetId).getVersion());
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        genericServer.updateDataSet(sessionToken, updatesDTO);
    }

    @Test
    public void testDeletingChildMustNotBreakParent()
    {
        Sample s14 = findSampleByCode("S1.4");
        final TechId sampleId = new TechId(s14);
        // delete
        commonServer.deleteSamples(sessionToken, Collections.singletonList(sampleId), REASON,
                DeletionType.TRASH);

        Sample s1 = findSampleByCode("S1");
        SampleParentWithDerived retrievedFromDb =
                commonServer.getSampleInfo(sessionToken, new TechId(s1));
        assertNotNull(retrievedFromDb);
    }

    @Test
    public void testDeleteSampleS14()
    {
        Sample s14 = findSampleByCode("S1.4");

        CollectionUtils.filter(registeredSamples, new Predicate<Sample>()
            {
                @Override
                public boolean evaluate(Sample s)
                {
                    return !s.getCode().equals("S1.4");
                }
            });

        final TechId sampleId = new TechId(s14);
        // delete
        commonServer.deleteSamples(sessionToken, Collections.singletonList(sampleId), REASON,
                DeletionType.TRASH);

        assertExperimentExists("E1");
        assertSamplesDoNotExist(Collections.singletonList(s14));
        assertSamplesExist(registeredSamples);

        List<DeletionPE> deletions = listDeletions();
        assertEquals(1, deletions.size());

        List<TableModelRowWithObject<Deletion>> deletionTable = getDeletionTable();
        List<ISerializableComparable> row = deletionTable.get(0).getValues();
        assertEquals("Sample   /CISD/DEFAULT/S1.4 (CELL_PLATE)\n", row.get(2).toString());
        assertEquals(REASON, row.get(3).toString());
        assertEquals(1, deletionTable.size());

        // revert
        final TechId deletionId1 = TechId.create(deletions.get(0));
        commonServer.revertDeletions(sessionToken, Collections.singletonList(deletionId1));

        assertSamplesExist(Collections.singletonList(s14));

        // delete permanently
        commonServer.deleteSamples(sessionToken, Collections.singletonList(sampleId), REASON,
                DeletionType.TRASH);
        final TechId deletionId2 = TechId.create(listDeletions().get(0));
        commonServer.deletePermanently(sessionToken, Collections.singletonList(deletionId2));
        assertExperimentExists("E1");
        assertSamplesDoNotExist(Collections.singletonList(s14));
        assertSamplesExist(registeredSamples);
    }

    private List<TableModelRowWithObject<Deletion>> getDeletionTable()
    {
        List<TableModelRowWithObject<Deletion>> rows =
                new ArrayList<TableModelRowWithObject<Deletion>>();
        GridRowModels<TableModelRowWithObject<Deletion>> list =
                commonClientService.listDeletions(FETCH_ALL).getResultSet().getList();
        for (GridRowModel<TableModelRowWithObject<Deletion>> rowModel : list)
        {
            TableModelRowWithObject<Deletion> row = rowModel.getOriginalObject();
            if (preExistingDeletionIDs.contains(row.getObjectOrNull().getId()) == false)
            {
                rows.add(row);
            }
        }
        return rows;
    }

    private void assertExperimentExists(String expCode)
    {
        final String error = String.format("Experiment '%s' must exist", expCode);
        assertTrue(error, isExistingExperiment(expCode));
    }

    private void assertExperimentDoesNotExist(String expCode)
    {
        final String error = String.format("Experiment '%s' should not exist", expCode);
        assertFalse(error, isExistingExperiment(expCode));
    }

    private boolean isExistingExperiment(String expCode)
    {
        Experiment experiment = findExperimentByCode(expCode);
        try
        {
            commonServer.getExperimentInfo(sessionToken, new TechId(experiment));
            return true;
        } catch (UserFailureException ufe)
        {
            return false;
        }

    }

    private void assertSamplesExist(List<Sample> samples)
    {
        for (Sample sample : samples)
        {
            // will fail if sample is not found
            commonServer.getSampleInfo(sessionToken, new TechId(sample));
        }
    }

    private void assertSamplesDoNotExist(List<Sample> samples)
    {
        for (Sample sample : samples)
        {
            assertSampleDoNotExists(sample);
        }
    }

    private void assertSampleDoNotExists(Sample sample)
    {
        try
        {
            commonServer.getSampleInfo(sessionToken, new TechId(sample));
            final String error =
                    String.format("Sample '%s' should not exist", sample.getIdentifier());
            fail(error);
        } catch (UserFailureException ufe)
        {
            // OK
        }
    }

    private void createExperiment(String code)
    {
        ExperimentIdentifier expIdentifier =
                new ExperimentIdentifier("CISD", "DEFAULT", code);
        NewExperiment experiment = new NewExperiment(expIdentifier.toString(), "COMPOUND_HCS");
        final GenericEntityProperty property = createDescriptionProperty();
        experiment.setProperties(new IEntityProperty[] { property });
        long id = etlService.registerExperiment(sessionToken, experiment);

        Experiment exp = commonServer.getExperimentInfo(sessionToken, new TechId(id));
        assertNotNull(exp);
        registeredExperiments.add(exp);
    }

    private void createSample(String experimentCode, NewSample newSample, boolean shouldBeDeleted)
    {
        Experiment exp = findExperimentByCode(experimentCode);
        newSample.setExperimentIdentifier(exp.getIdentifier());
        long id = etlService.registerSample(sessionToken, newSample, null);

        SampleParentWithDerived sampParentAndDerived =
                commonServer.getSampleInfo(sessionToken, new TechId(id));
        final Sample sample = sampParentAndDerived.getParent();
        assertNotNull(sample);
        registeredSamples.add(sample);
        if (shouldBeDeleted)
        {
            registeredSamplesThatShouldBeDeleted.add(sample);
        }
    }

    private void createSample(String experimentCode, String sampleCode)
    {
        NewSample newSample = createNewSample(sampleCode);
        createSample(experimentCode, newSample, true);
    }

    private void createChildSample(String experimentCode, String parentCode, String sampleCode)
    {
        createChildSample(experimentCode, parentCode, sampleCode, true);
    }

    private void createChildSample(String experimentCode, String parentCode, String sampleCode,
            boolean shouldBeDeleted)
    {
        NewSample newSample = createNewSample(sampleCode);
        newSample.setParentsOrNull(new String[] { "/CISD/DEFAULT/" + parentCode });
        createSample(experimentCode, newSample, shouldBeDeleted);
    }

    private void createComponentSample(String experimentCode, String containerCode,
            String sampleCode)
    {
        createComponentSample(experimentCode, containerCode, sampleCode, true);
    }

    private void createComponentSample(String experimentCode, String containerCode,
            String sampleCode, boolean shouldBeDeleted)
    {
        NewSample newSample = createNewSample(sampleCode);
        Sample container = findSampleByCode(containerCode);
        newSample.setContainerIdentifier(container.getIdentifier());
        createSample(experimentCode, newSample, shouldBeDeleted);
    }

    private NewSample createNewSample(String sampleCode)
    {
        NewSample newSample = new NewSample();
        newSample.setIdentifier("/CISD/DEFAULT/" + sampleCode);
        newSample.setSampleType(new SampleType());
        newSample.getSampleType().setCode("CELL_PLATE");
        return newSample;
    }

    private Experiment findExperimentByCode(String experimentCode)
    {
        for (Experiment experiment : registeredExperiments)
        {
            if (experiment.getCode().equals(experimentCode))
            {
                return experiment;
            }
        }
        return null;
    }

    private Sample findSampleByCode(String sampleCode)
    {
        for (Sample sample : registeredSamples)
        {
            if (sample.getCode().equals(sampleCode))
            {
                return sample;
            }
        }
        return null;
    }

    private GenericEntityProperty createDescriptionProperty()
    {
        final GenericEntityProperty property = new GenericEntityProperty();
        property.setPropertyType(new PropertyType());
        property.getPropertyType().setCode("DESCRIPTION");
        property.setValue("some test description");
        return property;
    }

    private List<DeletionPE> listDeletions()
    {
        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();
        ArrayList<DeletionPE> result = new ArrayList<DeletionPE>();
        for (DeletionPE deletion : allDeletions)
        {
            if (preExistingDeletions.contains(deletion))
            {
                // ignore pre-existing deletions
            } else
            {
                result.add(deletion);
            }
        }
        return result;
    }

    private void flushAndClearHibernateSession()
    {
        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
        currentSession.flush();
        currentSession.clear();
    }

}
