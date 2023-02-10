/*
 * Copyright 2007 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collection.CollectionStyle;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for corresponding {@link SampleDAO} class.
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
{ "db", "sample" })
@Friend(toClasses = SamplePE.class)
public final class SampleDAOTest extends AbstractDAOTest
{
    private static final String DILUTION_PLATE = "DILUTION_PLATE";

    private static final String MASTER_PLATE = "MASTER_PLATE";

    public final void testCodeUniqueness()
    {

    }

    @Test
    public final void testListSamplesFetchRelations()
    {
        final SampleTypePE type1 = getSampleType("MASTER_PLATE");
        final SampleTypePE type2 = getSampleType("DILUTION_PLATE");
        final SampleTypePE type3 = getSampleType("CELL_PLATE");
        type3.setContainerHierarchyDepth(1);
        type3.setGeneratedFromHierarchyDepth(1);
        SamplePE grandParent = createSample(type1, "grandParent");
        save(grandParent);
        SamplePE parent = createDerivedSample(type2, "parent", grandParent);
        save(parent);
        SamplePE child1 = createDerivedSample(type3, "child1", parent);
        save(child1);
        SamplePE child2 = createDerivedSample(type3, "child2", parent);
        save(child2);
        final SamplePE container = createSample(type2, "container");
        save(container);
        parent.setContainer(container);

        // clear session to avoid using samples from first level cache
        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();
        currentSession.clear();

        final List<SamplePE> children =
                daoFactory.getSampleDAO().listSamplesByGeneratedFrom(parent);
        child1 = findSample(child1, children);
        child2 = findSample(child2, children);
        assertTrue(HibernateUtils.isInitialized(child1.getGeneratedFrom()));
        assertTrue(HibernateUtils.isInitialized(child2.getGeneratedFrom()));
        assertEquals(child1.getGeneratedFrom(), child2.getGeneratedFrom());

        assertEquals(parent, child1.getGeneratedFrom());
        parent = child1.getGeneratedFrom();
        assertTrue(HibernateUtils.isInitialized(parent.getGeneratedFrom()));
        assertEquals(grandParent, parent.getGeneratedFrom());
        assertTrue(HibernateUtils.isInitialized(parent.getContainer()));
        assertEquals(container, parent.getContainer());
    }

    private final SamplePE findSample(final SamplePE sample, final List<SamplePE> samples)
    {
        final int sampleIx = samples.indexOf(sample);
        assert sampleIx != -1 : "sample not found " + sample;
        return samples.get(sampleIx);
    }

    @Test
    public final void testDefaultSampleCodeUniqueness()
    {
        final SampleTypePE containerType = getSampleType("DILUTION_PLATE");
        final SampleTypePE containedType = getSampleType("CELL_PLATE");
        containedType.setContainerHierarchyDepth(1);
        containedType.setGeneratedFromHierarchyDepth(1);
        containedType.setSubcodeUnique(false);
        final SamplePE container1 = createSample(containerType, "container1");
        final SamplePE container2 = createSample(containerType, "container2");
        save(container1, container2);
        final SamplePE well1_1 = createContainedSample(containedType, "well1", container1);
        final SamplePE well1_2 = createContainedSample(containedType, "well2", container1);
        final SamplePE well2_1 = createContainedSample(containedType, "well1", container2);
        final SamplePE well2_2 = createContainedSample(containedType, "well2", container2);
        save(well1_1, well1_2, well2_1, well2_2);

        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();

        try
        {
            final SamplePE duplicatedWell1_1 =
                    createContainedSample(containedType, "well1", container1);
            save(duplicatedWell1_1);
            currentSession.flush();
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            e.printStackTrace();
            assertEquals("Insert/Update of sample (code: WELL1) failed because "
                    + "sample with the same code already exists.", e.getMessage());
        }
    }

    @Test
    public final void testSampleSubcodeUniqueness()
    {
        final SampleTypePE containerType = getSampleType("DILUTION_PLATE");
        final SampleTypePE containedType = getSampleType("CELL_PLATE");
        containedType.setContainerHierarchyDepth(1);
        containedType.setGeneratedFromHierarchyDepth(1);
        containedType.setSubcodeUnique(true);
        final SamplePE container1 = createSample(containerType, "container1");
        final SamplePE container2 = createSample(containerType, "container2");
        save(container1, container2);
        final SamplePE well1_1 = createContainedSample(containedType, "well1", container1);
        save(well1_1);

        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();

        // subcode uniqueness should be checked
        try
        {
            final SamplePE well2_1 = createContainedSample(containedType, "well1", container2);
            save(well2_1);
            currentSession.flush();
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("Insert/Update of sample (code: WELL1) failed because "
                    + "sample of the same type with the same subcode already exists.",
                    e.getMessage());
        }
    }

    @Test
    public void testCreateSamplesForAnExperiment()
    {
        ExperimentPE experiment = daoFactory.getExperimentDAO().listExperiments().get(0);
        SampleTypePE type = getSampleType("CELL_PLATE");
        SamplePE s1 = createSample(type, "LINKED_SAMPLE1");
        s1.setExperiment(experiment);
        SamplePE s2 = createSample(type, "LINKED_SAMPLE2");
        s2.setExperiment(experiment);

        save(s1, s2);

        // clear session to avoid using samples from first level cache
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();
        currentSession.clear();
        ExperimentPE reloadedExperiment =
                daoFactory.getExperimentDAO().tryGetByPermID(experiment.getPermId());
        List<SamplePE> samples = reloadedExperiment.getSamples();
        List<String> identifiers = new ArrayList<String>();
        for (SamplePE sample : samples)
        {
            String identifier = sample.getIdentifier();
            if (identifier.contains("LINKED"))
            {
                identifiers.add(identifier);
            }
        }
        Collections.sort(identifiers);
        assertEquals("[/LINKED_SAMPLE1, /LINKED_SAMPLE2]", identifiers.toString());
    }

    @Test
    public void testCreateExperimentAndLinkedSamples()
    {
        IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        ExperimentPE experiment = experimentDAO.listExperiments().get(0);
        ExperimentPE newExperiment = new ExperimentPE();
        newExperiment.setProject(experiment.getProject());
        newExperiment.setExperimentType(experiment.getExperimentType());
        newExperiment.setCode(newExperiment.getCode() + "-2");
        newExperiment.setRegistrator(experiment.getRegistrator());
        newExperiment.setPermId("abc-2");
        PersonPE user = getSystemPerson();
        newExperiment.setModifier(user);
        newExperiment.setRegistrationDate(new Date());
        newExperiment.setModificationDate(new Date());
        experimentDAO.createOrUpdateExperiment(newExperiment, user);
        SampleTypePE type = getSampleType("CELL_PLATE");
        SamplePE s1 = createSample(type, "LINKED_SAMPLE1");
        s1.setExperiment(newExperiment);
        SamplePE s2 = createSample(type, "LINKED_SAMPLE2");
        s2.setExperiment(newExperiment);

        save(s1, s2);

        ExperimentPE reloadedExperiment = experimentDAO.tryGetByPermID(newExperiment.getPermId());
        List<SamplePE> samples = reloadedExperiment.getSamples();
        List<String> identifiers = new ArrayList<String>();
        for (SamplePE sample : samples)
        {
            identifiers.add(sample.getIdentifier());
        }
        Collections.sort(identifiers);
        assertEquals("[/LINKED_SAMPLE1, /LINKED_SAMPLE2]", identifiers.toString());
    }

    @Test
    public final void testSampleSubcodeUniquenessAcrossTypes()
    {
        final SampleTypePE type1 = getSampleType("DILUTION_PLATE");
        final SampleTypePE type2 = getSampleType("CELL_PLATE");
        type2.setSubcodeUnique(true);
        final SamplePE sampleT1 = createSample(type1, "S_CODE");
        save(sampleT1);

        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();

        // default uniqueness should still be checked across types
        try
        {
            final SamplePE sampleT2 = createSample(type2, "S_CODE");
            save(sampleT2);
            currentSession.flush();
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("Insert/Update of sample (code: S_CODE) failed because "
                    + "sample with the same code already exists.", e.getMessage());
        }
    }

    @Test
    public void testTryToFindByPermID()
    {
        String permID = "200811050919915-8";
        SamplePE sample = daoFactory.getSampleDAO().tryToFindByPermID(permID);

        assertEquals(permID, sample.getPermId());
        Set<SamplePropertyPE> properties = sample.getProperties();
        assertEquals(2, properties.size());
        Iterator<SamplePropertyPE> iterator = properties.iterator();
        SamplePropertyPE description = iterator.next();
        SamplePropertyPE plateGeometry = iterator.next();
        if (plateGeometry.getVocabularyTerm() == null)
        {
            SamplePropertyPE p = plateGeometry;
            plateGeometry = description;
            description = p;
        }
        assertEquals("DESCRIPTION", description.getEntityTypePropertyType().getPropertyType()
                .getCode());
        assertEquals("test control layout", description.getValue());
        assertEquals("$PLATE_GEOMETRY", plateGeometry.getEntityTypePropertyType().getPropertyType()
                .getCode());
        assertEquals("384_WELLS_16X24", plateGeometry.getVocabularyTerm().getCode());
    }

    @Test
    public final void testTryFindByCodeAndDatabaseInstance()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        boolean fail = true;
        try
        {
            sampleDAO.tryFindByCodeAndDatabaseInstance(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertNotNull(sampleDAO.tryFindByCodeAndDatabaseInstance("MP"));
        assertNull(sampleDAO.tryFindByCodeAndDatabaseInstance(""));
    }

    @Test
    public final void testTryFindByCodeAndSpace()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE sample = createSpaceSample();
        try
        {
            sampleDAO.tryFindByCodeAndSpace(null, null);
            fail("AssertionError expected");
        } catch (final AssertionError e)
        {
            // ignored
        }
        assertEquals(sample, sampleDAO.tryFindByCodeAndSpace(sample.getCode(), sample.getSpace()));
        assertNull(sampleDAO.tryFindByCodeAndSpace("", sample.getSpace()));
    }

    private final SamplePE findSample(String code, String projectCode, String spaceCode)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SpacePE space = findSpace(spaceCode);
        SamplePE sample;
        if (projectCode == null)
        {
            sample = sampleDAO.tryFindByCodeAndSpace(code, space);
        } else
        {
            ProjectPE project = daoFactory.getProjectDAO().tryFindProject(spaceCode, projectCode);
            sample = sampleDAO.tryfindByCodeAndProject(code, project);
        }
        assertNotNull(sample);

        return sample;
    }

    public final void testDeleteWithParentAndExperimentPreserved()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE sampleToDelete = findSample("3VCP5", "NEMO", "CISD");

        // Deleted sample should have all collections which prevent it from deletion empty.
        assertTrue(sampleToDelete.getAttachments().isEmpty());
        assertTrue(sampleToDelete.getDatasets().isEmpty());
        assertTrue(sampleToDelete.getGenerated().isEmpty());
        assertTrue(sampleToDelete.getContained().isEmpty());

        SamplePE generatedFrom = sampleToDelete.getGeneratedFrom();
        assertNotNull(generatedFrom);
        ExperimentPE experiment = sampleToDelete.getExperiment();
        assertNotNull(experiment);

        // delete
        deleteSamples(sampleToDelete);

        // test successful deletion of sample
        assertNull(sampleDAO.tryGetByTechId(TechId.create(sampleToDelete)));

        // deleted sample had objects connected that should not be deleted:
        // - a parent
        assertNotNull(sampleDAO.tryGetByTechId(new TechId(HibernateUtils.getId(generatedFrom))));
        // - an experiment
        assertNotNull(daoFactory.getExperimentDAO().tryGetByTechId(
                new TechId(HibernateUtils.getId(experiment))));
    }

    public final void testDeleteMultipleSamples()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE sample1 = findSample("3VCP5", "NEMO", "CISD");
        final SamplePE sample2 = findSample("EMPTY-MP", null, "CISD");

        // delete
        deleteSamples(new SamplePE[]
        { sample1, sample2 });

        // test successful deletion of sample
        assertNull(sampleDAO.tryGetByTechId(TechId.create(sample1)));
        assertNull(sampleDAO.tryGetByTechId(TechId.create(sample2)));
    }

    private void deleteSamples(SamplePE... samples)
    {
        // before deletion there shouldn't be any entry about deletion of the sample in event table
        for (SamplePE sample : samples)
        {
            assertNull(tryGetDeletionEvent(sample));
        }

        final PersonPE registrator = getSystemPerson();
        List<TechId> sampleIds = new ArrayList<TechId>();
        List<String> identifiers = new ArrayList<String>();

        for (SamplePE sample : samples)
        {
            identifiers.add(sample.getPermId());
            sampleIds.add(TechId.create(sample));
        }
        Collections.sort(identifiers);
        String commaSeparatedIdentifiers =
                CollectionUtils.abbreviate(identifiers, -1, CollectionStyle.NO_BOUNDARY);
        final String reason = "reason " + commaSeparatedIdentifiers;

        daoFactory.getSampleDAO().delete(sampleIds, registrator, reason);

        // after deletion there should be an entry about deletion of the sample in event table
        for (SamplePE sample : samples)
        {
            final EventPE event = tryGetDeletionEvent(sample);
            assertNotNull(event);
            assertEquals(reason, event.getReason());
            Collections.sort(event.getIdentifiers());
            String persistedIdentifiers =
                    CollectionUtils.abbreviate(event.getIdentifiers(), -1,
                            CollectionStyle.NO_BOUNDARY);
            assertEquals(commaSeparatedIdentifiers, persistedIdentifiers);
            assertEquals(registrator, event.getRegistrator());
        }
    }

    private EventPE tryGetDeletionEvent(SamplePE sample)
    {
        final IEventDAO eventDAO = daoFactory.getEventDAO();
        return eventDAO.tryFind(sample.getPermId(), EntityType.SAMPLE, EventType.DELETION);
    }

    @Test
    public final void testDeleteWithProperties()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE sampleToDelete = findSample("EMPTY-MP", null, "CISD");

        // Deleted sample should have all collections which prevent it from deletion empty.
        assertTrue(sampleToDelete.getAttachments().isEmpty());
        assertTrue(sampleToDelete.getDatasets().isEmpty());
        assertTrue(sampleToDelete.getGenerated().isEmpty());
        assertTrue(sampleToDelete.getContained().isEmpty());
        assertFalse(sampleToDelete.getProperties().isEmpty());

        // Remember how many rows are in the properties table before we delete
        int beforeDeletionPropertiesRowCount = countRowsInTable(TableNames.SAMPLE_PROPERTIES_TABLE);

        // delete
        deleteSamples(sampleToDelete);

        // test successful deletion of sample
        assertNull(sampleDAO.tryGetByTechId(TechId.create(sampleToDelete)));

        // test successful deletion of sample properties
        int afterDeletionPropertiesRowCount = countRowsInTable(TableNames.SAMPLE_PROPERTIES_TABLE);

        assertEquals(afterDeletionPropertiesRowCount, beforeDeletionPropertiesRowCount
                - sampleToDelete.getProperties().size());
    }

    private static final String ATT_CONTENTS_TABLE = "attachment_contents";

    @Test
    public final void testDeleteWithAttachments()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE sampleToDelete = findSample("3VCP6", "NEMO", "CISD");

        // Deleted sample should have attachments which should be deleted as well as the sample.
        assertFalse(sampleToDelete.getAttachments().isEmpty());
        List<TechId> attachmentIds = TechId.createList(sampleToDelete.getAttachments());
        // Other connections which would prevent sample deletion should be empty in this test.
        assertTrue(sampleToDelete.getDatasets().isEmpty());
        assertTrue(sampleToDelete.getGenerated().isEmpty());
        assertTrue(sampleToDelete.getContained().isEmpty());

        int rowsInAttachmentContents = countRowsInTable(ATT_CONTENTS_TABLE);

        // delete
        deleteSamples(sampleToDelete);

        // test successful deletion of sample
        assertNull(sampleDAO.tryGetByTechId(TechId.create(sampleToDelete)));

        // test successful deletion of attachments & their contents
        for (TechId attachmentId : attachmentIds)
        {
            assertNull(daoFactory.getAttachmentDAO().tryGetByTechId(attachmentId));
        }
        assertEquals(rowsInAttachmentContents, countRowsInTable(ATT_CONTENTS_TABLE));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithDatasets()
    {
        final SamplePE sampleToDelete = findSample("CP-TEST-1", "NEMO", "CISD");

        // Deleted sample should have data sets which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertTrue(sampleToDelete.getAttachments().isEmpty());
        assertFalse(sampleToDelete.getDatasets().isEmpty());
        assertTrue(sampleToDelete.getGenerated().isEmpty());
        assertTrue(sampleToDelete.getContained().isEmpty());

        // delete
        deleteSamples(sampleToDelete);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class, groups = "broken")
    // FIXME we expect permanent deletion of parent to fail if it has children
    public final void testDeleteWithGeneratedSamples()
    {
        final SamplePE sampleToDelete = findSample("3V-125", null, "CISD");

        // Deleted sample should have 'generated' samples which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertTrue(sampleToDelete.getAttachments().isEmpty());
        assertTrue(sampleToDelete.getDatasets().isEmpty());
        assertFalse(sampleToDelete.getGenerated().isEmpty());
        assertTrue(sampleToDelete.getContained().isEmpty());

        // delete
        deleteSamples(sampleToDelete);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithContainedSamples()
    {
        final SamplePE sampleToDelete = findSample("C1", null, "CISD");

        // Deleted sample should have 'contained' samples which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertTrue(sampleToDelete.getAttachments().isEmpty());
        assertTrue(sampleToDelete.getDatasets().isEmpty());
        assertTrue(sampleToDelete.getGenerated().isEmpty());
        assertFalse(sampleToDelete.getContained().isEmpty());

        // delete
        deleteSamples(sampleToDelete);
    }

    private SpacePE findSpace(String spaceCode)
    {
        SpacePE space =
                daoFactory.getSpaceDAO().tryFindSpaceByCode(spaceCode);
        assert space != null : "space not found: " + spaceCode;
        return space;
    }

    @Test
    public final void testTryFindCodeAndDatabaseInstanceWithUniqueResult()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE samplePE = new SamplePE();
        final String sampleCode = "A03";
        samplePE.setCode(sampleCode);
        samplePE.setPermId(daoFactory.getPermIdDAO().createPermId());
        samplePE.setSampleType(daoFactory.getSampleTypeDAO()
                .tryFindSampleTypeByCode(DILUTION_PLATE));
        samplePE.setRegistrator(getSystemPerson());
        sampleDAO.createOrUpdateSample(samplePE, getTestPerson());
        // Following line throws a NonUniqueResultException if sample code not unique.
        sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode);
    }

    //
    // Private methods
    //

    private final void save(final SamplePE... samples)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        if (samples.length > 1)
        {
            sampleDAO.createOrUpdateSamples(Arrays.asList(samples), getTestPerson(), true);
        } else
        {
            sampleDAO.createOrUpdateSample(samples[0], getTestPerson());
        }
    }

    private final SampleTypePE getSampleType(final String sampleTypeCode)
    {
        final SampleTypePE sampleType =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
        assert sampleType != null;
        return sampleType;
    }

    /**
     * Creates a space sample in the database.
     */
    private final SamplePE createSpaceSample()
    {
        final SampleTypePE sampleType = getSampleType(MASTER_PLATE);
        final SpacePE space = createSpace("xxx");
        final SamplePE sample =
                createSample(sampleType, "code", null, null, SampleOwner.createSpace(space));
        save(sample);
        assertNotNull(sample);
        assertNotNull(sample.getSampleType());
        assertNotNull(sample.getSpace());
        return sample;
    }

    private final SamplePE createSample(final SampleTypePE type, final String code)
    {
        final SampleOwner owner =
                SampleOwner.createDatabaseInstance();
        return createSample(type, code, null, null, owner);
    }

    private final SamplePE createContainedSample(final SampleTypePE type, final String code,
            final SamplePE containerOrNull)
    {
        final SampleOwner owner =
                SampleOwner.createDatabaseInstance();
        return createSample(type, code, null, containerOrNull, owner);
    }

    private final SamplePE createDerivedSample(final SampleTypePE type, final String code,
            final SamplePE generatorOrNull)
    {
        final SampleOwner owner =
                SampleOwner.createDatabaseInstance();
        return createSample(type, code, generatorOrNull, null, owner);
    }

    private final SamplePE createSample(final SampleTypePE type, final String code,
            final SamplePE generatorOrNull, final SamplePE containerOrNull,
            final SampleOwner sampleOwner)
    {
        final SamplePE sample = new SamplePE();
        sample.setRegistrator(getSystemPerson());
        sample.setCode(code);
        sample.setPermId(daoFactory.getPermIdDAO().createPermId());
        sample.setSampleType(type);
        sample.setSpace(sampleOwner.tryGetSpace());
        sample.setModificationDate(new Date());
        if (generatorOrNull != null)
        {
            sample.addParentRelationship(new SampleRelationshipPE(generatorOrNull, sample,
                    daoFactory.getRelationshipTypeDAO().tryFindRelationshipTypeByCode(
                            BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP), getTestPerson()));
        }
        sample.setContainer(containerOrNull);
        return sample;
    }

    @Test
    public void testLoadByPermId() throws Exception
    {
        SamplePE sample = daoFactory.getSampleDAO().listAllEntities().get(0);
        HashSet<String> keys = new HashSet<String>();
        keys.add(sample.getPermId());
        keys.add("nonexistent");
        List<SamplePE> result = daoFactory.getSampleDAO().listByPermID(keys);
        AssertJUnit.assertEquals(1, result.size());
        AssertJUnit.assertEquals(sample, result.get(0));
    }

    @Test
    public void testLoadByPermIdNoEntries() throws Exception
    {
        HashSet<String> keys = new HashSet<String>();
        List<SamplePE> result = daoFactory.getSampleDAO().listByPermID(keys);
        AssertJUnit.assertTrue(result.isEmpty());
    }
}
