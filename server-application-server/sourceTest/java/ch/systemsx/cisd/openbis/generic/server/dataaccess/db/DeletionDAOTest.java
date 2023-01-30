/*
 * Copyright 2010 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Piotr Buczek
 */
@Test(groups = { "db" })
public class DeletionDAOTest extends AbstractDAOTest
{

    @Test
    public void testFindTrashedEntities() throws Exception
    {
        // simple test with db specific numbers
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();
        assertEquals(countRowsInTable(TableNames.DELETIONS_TABLE), allDeletions.size());
        assertEquals(4, allDeletions.size());

        int e0 = 0, s0 = 5, d0 = 1;
        checkTrashFor(allDeletions.get(0)).hasExperiments(e0).hasSamples(s0).hasDataSets(d0);
        int e1 = 0, s1 = 3, d1 = 0;
        checkTrashFor(allDeletions.get(1)).hasExperiments(e1).hasSamples(s1).hasDataSets(d1);
        int e2 = 0, s2 = 323, d2 = 0;
        checkTrashFor(allDeletions.get(2)).hasExperiments(e2).hasSamples(s2)
                .hasNonComponentSamples(3).hasComponentSamples(320).hasDataSets(d2);
        int e3 = 1, s3 = 0, d3 = 0;
        checkTrashFor(allDeletions.get(3)).hasExperiments(e3).hasSamples(s3).hasDataSets(d3);

        int eAll = e0 + e1 + e2 + e3, sAll = s0 + s1 + s2 + s3, dAll = d0 + d1 + d2 + d3;
        checkTrashFor(allDeletions).hasExperiments(eAll).hasSamples(sAll).hasDataSets(dAll);

        assertEquals(eAll, countRowsInTable(TableNames.DELETED_EXPERIMENTS_VIEW));
        assertEquals(sAll, countRowsInTable(TableNames.DELETED_SAMPLES_VIEW));
        assertEquals(dAll, countRowsInTable(TableNames.DELETED_DATA_VIEW));
    }

    @Test
    public void testViews()
    {
        // simple test checking that views for our entities sum up to the whole table

        assertEquals(countRowsInTable(TableNames.EXPERIMENTS_ALL_TABLE),
                countRowsInTable(TableNames.EXPERIMENTS_VIEW)
                        + countRowsInTable(TableNames.DELETED_EXPERIMENTS_VIEW));

        assertEquals(countRowsInTable(TableNames.SAMPLES_ALL_TABLE),
                countRowsInTable(TableNames.SAMPLES_VIEW)
                        + countRowsInTable(TableNames.DELETED_SAMPLES_VIEW));

        assertEquals(countRowsInTable(TableNames.DATA_ALL_TABLE),
                countRowsInTable(TableNames.DATA_VIEW)
                        + countRowsInTable(TableNames.DELETED_DATA_VIEW));
    }

    private TrashedEntityExpectations checkTrashFor(DeletionPE deletion)
    {
        return checkTrashFor(Collections.singletonList(deletion));
    }

    private TrashedEntityExpectations checkTrashFor(List<DeletionPE> deletions)
    {
        return new TrashedEntityExpectations(deletions);
    }

    @Test
    public void testCreateDeletion() throws InterruptedException
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();

        int rowsInDeletions = countRowsInTable(TableNames.DELETIONS_TABLE);

        DeletionPE deletion = new DeletionPE();
        deletion.setRegistrator(getTestPerson());
        deletion.setReason("example reason");
        Date dateBeforeRegistration = DateUtils.addDays(new Date(), -1);
        deletionDAO.create(deletion);
        Date dateAfterRegistration = DateUtils.addDays(new Date(), 1);

        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();
        assertEquals(rowsInDeletions + 1, countRowsInTable(TableNames.DELETIONS_TABLE));
        assertEquals(rowsInDeletions + 1, allDeletions.size());
        DeletionPE lastDeletion = allDeletions.get(allDeletions.size() - 1);
        assertEquals(deletion.getReason(), lastDeletion.getReason());
        assertEquals(deletion.getRegistrator(), lastDeletion.getRegistrator());
        assertTrue(dateBeforeRegistration + " < " + lastDeletion.getRegistrationDate(),
                dateBeforeRegistration.getTime() < lastDeletion.getRegistrationDate().getTime());
        assertTrue(lastDeletion.getRegistrationDate() + " > " + dateAfterRegistration, lastDeletion
                .getRegistrationDate().getTime() < dateAfterRegistration.getTime());
    }

    @Test
    public void testFindDeletedEntities()
    {
        assertExistsDeletedEntity(EntityKind.EXPERIMENT, "SIRNA_HCS");
        assertExistsDeletedEntity(EntityKind.SAMPLE, "WELL");
        assertExistsDeletedEntity(EntityKind.DATA_SET, "HCS_IMAGE");
    }

    private void assertExistsDeletedEntity(EntityKind entityKind, String entityTypeCode)
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();

        IEntityTypeDAO entityTypeDAO = daoFactory.getEntityTypeDAO(entityKind);
        EntityTypePE entityType = entityTypeDAO.tryToFindEntityTypeByCode(entityTypeCode);

        assertNotNull(
                String.format("%s '%s' does not exist", entityKind.getLabel(), entityTypeCode),
                entityType);

        final List<TechId> deletedEntities =
                deletionDAO.listDeletedEntitiesForType(entityKind, new TechId(entityType.getId()));

        assertTrue(String.format("At least one deleted %s of type '%s'"
                + " must exist in the test database", entityKind.getLabel(), entityTypeCode),
                deletedEntities.size() > 0);

    }

    @Test
    public void testRevertDeletion()
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();

        // - all deletions should be revertable,
        // - there are different connections in different deletions
        // (samples with data sets, experiments with samples...)
        for (DeletionPE deletion : allDeletions)
        {
            testRevertDeletion(deletionDAO, deletion);
        }
    }

    @Test
    public void testMetaprojectAssignmentsTrashing()
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();

        DeletionPE deletion = new DeletionPE();
        deletion.setRegistrator(getTestPerson());
        deletion.setReason("test metaproject assignments deletion");
        deletionDAO.create(deletion);

        deletionDAO
                .trash(EntityKind.EXPERIMENT, Collections.singletonList(new TechId(4)), deletion);
        deletionDAO.trash(EntityKind.SAMPLE, Collections.singletonList(new TechId(1055)), deletion);
        deletionDAO.trash(EntityKind.DATA_SET, Collections.singletonList(new TechId(22)), deletion);

        MetaprojectPE metaproject = daoFactory.getMetaprojectDAO().getByTechId(new TechId(1));
        Set<MetaprojectAssignmentPE> assignments = metaproject.getAssignments();
        assertEquals(2, assignments.size());
        for (MetaprojectAssignmentPE assignment : assignments)
        {
            if (assignment.getExperiment() != null)
            {
                assertEquals(23, assignment.getExperiment().getId().longValue());
            } else if (assignment.getMaterial() != null)
            {
                assertEquals(1, assignment.getMaterial().getId().longValue());
            } else
            {
                fail();
            }
        }
    }

    @Test
    public void testMetaprojectAssignmentsReverting()
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();

        DeletionPE deletion = new DeletionPE();
        PersonPE testPerson = getTestPerson();
        deletion.setRegistrator(testPerson);
        deletion.setReason("test metaproject assignments deletion reverting");
        deletionDAO.create(deletion);

        deletionDAO
                .trash(EntityKind.EXPERIMENT, Collections.singletonList(new TechId(4)), deletion);
        deletionDAO.trash(EntityKind.SAMPLE, Collections.singletonList(new TechId(1055)), deletion);
        deletionDAO.trash(EntityKind.DATA_SET, Collections.singletonList(new TechId(22)), deletion);

        deletionDAO.revert(deletion, testPerson);

        MetaprojectPE metaproject = daoFactory.getMetaprojectDAO().getByTechId(new TechId(1));
        Set<MetaprojectAssignmentPE> assignments = metaproject.getAssignments();
        assertEquals(5, assignments.size());
    }

    private void testRevertDeletion(IDeletionDAO deletionDAO, DeletionPE deletion)
    {
        List<TechId> deletionId = Collections.singletonList(TechId.create(deletion));
        List<TechId> experimentIds = deletionDAO.findTrashedExperimentIds(deletionId);
        List<TechId> sampleIds = deletionDAO.findTrashedSampleIds(deletionId);
        List<String> dataSetCodes = deletionDAO.findTrashedDataSetCodes(deletionId);

        assertExperimentsDeleted(true, experimentIds);
        assertSamplesDeleted(true, sampleIds);
        assertDataSetsDeleted(true, dataSetCodes);

        int rowsInDeletions = countRowsInTable(TableNames.DELETIONS_TABLE);
        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();

        deletionDAO.revert(deletion, getTestPerson());

        assertEquals(rowsInDeletions - 1, countRowsInTable(TableNames.DELETIONS_TABLE));
        assertEquals(allDeletions.size() - 1, deletionDAO.listAllEntities().size());

        assertEquals(0, deletionDAO.findTrashedExperimentIds(deletionId).size());
        assertEquals(0, deletionDAO.findTrashedSampleIds(deletionId).size());
        assertEquals(0, deletionDAO.findTrashedDataSetCodes(deletionId).size());
    }

    private void assertSamplesDeleted(boolean expectedDeleted, List<TechId> sampleIds)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        for (TechId id : sampleIds)
        {
            SamplePE sample = sampleDAO.tryGetByTechId(id);
            String errorMsg =
                    String.format("sample id=%s is expected %s be deleted;", id,
                            expectedDeleted ? "to" : "not to");
            assertEquals(errorMsg, expectedDeleted, sample == null);
        }
    }

    private void assertExperimentsDeleted(boolean expectedDeleted, List<TechId> experimentIds)
    {
        IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        for (TechId id : experimentIds)
        {
            ExperimentPE experiment = experimentDAO.tryGetByTechId(id);
            String errorMsg =
                    String.format("experiment id = %s is expected %s be deleted;", id,
                            expectedDeleted ? "to" : "not to");
            assertEquals(errorMsg, expectedDeleted, experiment == null);
        }
    }

    private void assertDataSetsDeleted(boolean expectedDeleted, List<String> dataSetCodes)
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        for (String code : dataSetCodes)
        {
            String errorMsg =
                    String.format("data set '%s' is expected %s be deleted;", code,
                            expectedDeleted ? "to" : "not to");
            assertEquals(errorMsg, expectedDeleted, dataDAO.tryToFindDataSetByCode(code) == null);
        }
    }

    private class TrashedEntityExpectations
    {
        private final List<TechId> deletionIds;

        private final IDeletionDAO deletionDAO;

        public TrashedEntityExpectations(List<DeletionPE> deletions)
        {
            deletionIds = TechId.createList(deletions);
            deletionDAO = daoFactory.getDeletionDAO();
        }

        public TrashedEntityExpectations hasExperiments(int expectedExperiments)
        {
            List<TechId> foundExperimentIds = deletionDAO.findTrashedExperimentIds(deletionIds);
            assertEquals(deletionIds.toString(), expectedExperiments, foundExperimentIds.size());
            assertExperimentsDeleted(true, foundExperimentIds);
            return this;
        }

        public TrashedEntityExpectations hasComponentSamples(int expectedSamples)
        {
            List<TechId> foundSampleIds = deletionDAO.findTrashedComponentSampleIds(deletionIds);
            assertEquals(deletionIds.toString(), expectedSamples, foundSampleIds.size());
            assertSamplesDeleted(true, foundSampleIds);
            return this;
        }

        public TrashedEntityExpectations hasNonComponentSamples(int expectedSamples)
        {
            List<TechId> foundSampleIds = deletionDAO.findTrashedNonComponentSampleIds(deletionIds);
            assertEquals(deletionIds.toString(), expectedSamples, foundSampleIds.size());
            assertSamplesDeleted(true, foundSampleIds);
            return this;
        }

        public TrashedEntityExpectations hasSamples(int expectedSamples)
        {
            List<TechId> foundSampleIds = deletionDAO.findTrashedSampleIds(deletionIds);
            assertEquals(deletionIds.toString(), expectedSamples, foundSampleIds.size());
            assertSamplesDeleted(true, foundSampleIds);
            return this;
        }

        public TrashedEntityExpectations hasDataSets(int expectedDataSets)
        {
            List<String> foundDataSetCodes = deletionDAO.findTrashedDataSetCodes(deletionIds);
            assertEquals(deletionIds.toString(), expectedDataSets, foundDataSetCodes.size());
            assertDataSetsDeleted(true, foundDataSetCodes);
            return this;
        }
    }
}
