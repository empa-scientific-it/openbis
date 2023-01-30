/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.asList;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.assertCodeNotFound;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.findCode;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectSpaceCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Test cases for {@link ISampleListingQuery}.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
{ SampleRecord.class, ExperimentProjectSpaceCodeRecord.class, ISampleListingQuery.class,
        SampleListerDAO.class, SampleRelationRecord.class })
@Test(groups =
{ "db", "sample" })
public class SampleListingQueryTest extends AbstractDAOTest
{

    private static final String MATERIAL_TYPE_CODE = "GENE";

    private static final String SHARED_MASTER_PLATE_CODE = "MP";

    private static final int SHARED_MASTER_PLATE_ID = 646;

    private static final String SAMPLE_TYPE_CODE_MASTER_PLATE = "MASTER_PLATE";

    private static final String SAMPLE_TYPE_CODE_CELL_PLATE = "CELL_PLATE";

    private static final String DEFAULT_SPACE_CODE = "CISD";

    private static final String DILUTION_PLATE_CODE_1 = "DP1-A";

    private static final String DILUTION_PLATE_CODE_2 = "DP1-B";

    private long dbInstanceId;

    private SpacePE group;

    private long groupId;

    private String groupCode;

    private SampleTypePE masterPlateType;

    private SamplePE sharedMasterPlate;

    private SamplePE dilutionPlate1;

    private SamplePE dilutionPlate2;

    private long parentChildRelationshipTypeId;

    private ISampleListingQuery query;

    @BeforeMethod(alwaysRun = true)
    public void init() throws SQLException
    {
        SampleListerDAO sampleListerDAO = createSampleListerDAO(daoFactory);
        dbInstanceId = sampleListerDAO.getDatabaseInstanceId();
        group =
                daoFactory.getSpaceDAO().tryFindSpaceByCode(DEFAULT_SPACE_CODE);
        groupId = group.getId();
        groupCode = group.getCode();
        masterPlateType =
                daoFactory.getSampleTypeDAO()
                        .tryFindSampleTypeByCode(SAMPLE_TYPE_CODE_MASTER_PLATE);
        sharedMasterPlate =
                daoFactory.getSampleDAO().tryFindByCodeAndDatabaseInstance(
                        SHARED_MASTER_PLATE_CODE);
        assertEquals(SHARED_MASTER_PLATE_ID, sharedMasterPlate.getId().longValue());
        query = sampleListerDAO.getQuery();
        parentChildRelationshipTypeId = query.getRelationshipTypeId("PARENT_CHILD", true);
    }

    public static SampleListerDAO createSampleListerDAO(IDAOFactory daoFactory)
    {
        ISampleListingQuery query =
                EntityListingTestUtils.createQuery(daoFactory, ISampleListingQuery.class);
        return SampleListerDAO.create(daoFactory, query);
    }

    @Test
    public void testSampleCount()
    {
        assertEquals(701, query.getSampleCount()); // without deleted
    }

    @Test
    public void testGetRelationshipTypeId()
    {
        assertEquals(1, query.getRelationshipTypeId("PARENT_CHILD", true));
        assertEquals(2, query.getRelationshipTypeId("PLATE_CONTROL_LAYOUT", true));
        assertRelationshipTypeNotExists("FAKE_RELATIONSHIP", true);
        assertRelationshipTypeNotExists("PARENT_CHILD", false);
        assertRelationshipTypeNotExists("PLATE_CONTROL_LAYOUT", false);
        assertRelationshipTypeNotExists("FAKE_RELATIONSHIP", false);
    }

    private void assertRelationshipTypeNotExists(String code, boolean managedInternally)
    {
        try
        {
            long id = query.getRelationshipTypeId(code, managedInternally);
            fail("unexpected " + (managedInternally ? "internal" : "user defined")
                    + " relationship found with code " + code + " and id " + id);
        } catch (NullPointerException e)
        {
        }
    }

    @Test
    public void testGetParentRelations()
    {
        dilutionPlate1 =
                daoFactory.getSampleDAO().tryFindByCodeAndSpace(DILUTION_PLATE_CODE_1, group);
        dilutionPlate2 =
                daoFactory.getSampleDAO().tryFindByCodeAndSpace(DILUTION_PLATE_CODE_2, group);
        final int children1 = 2;
        final int children2 = 1;
        assertEquals(children1, dilutionPlate1.getGenerated().size());
        assertEquals(children2, dilutionPlate2.getGenerated().size());
        LongSet dilutionPlateIdSet = new LongOpenHashSet(new long[]
        { dilutionPlate1.getId(), dilutionPlate2.getId() });
        LongSet childrenIds =
                new LongOpenHashSet(query.getChildrenIds(parentChildRelationshipTypeId,
                        dilutionPlateIdSet));
        assertEquals(children1 + children2, childrenIds.size());
        int sampleCount1 = 0;
        int sampleCount2 = 0;
        for (SampleRelationRecord sample : query.getParentRelations(parentChildRelationshipTypeId,
                childrenIds))
        {
            assertEquals(parentChildRelationshipTypeId, sample.relationship_id);
            if (dilutionPlate1.getId().equals(sample.sample_id_parent))
            {
                sampleCount1++;
            } else if (dilutionPlate2.getId().equals(sample.sample_id_parent))
            {
                sampleCount2++;
            } else
            {
                fail("unexpected sample parent id: " + sample.sample_id_parent + " for child id: "
                        + sample.sample_id_child);
            }
        }
        assertEquals(children1, sampleCount1);
        assertEquals(children2, sampleCount2);
    }

    @Test(groups = "slow")
    public void testQuerySamples()
    {
        final long listableSamplesInTestDB = query.getSampleCount();
        assertTrue(listableSamplesInTestDB > 0);
        int sampleCount = 0;
        for (SampleRecord sample : query.getSamples())
        {
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            final SamplePE samplePE = daoFactory.getSampleDAO().getByTechId(new TechId(sample.id));
            assertEquals(msg, samplePE.getCode(), sample.code);
            assertEquals(msg, samplePE.getPermId(), sample.perm_id);
            assertEquals(msg, samplePE.getRegistrator().getId().longValue(),
                    sample.pers_id_registerer.longValue());
            assertEquals(msg, samplePE.getRegistrationDate(), sample.registration_timestamp);
            assertEquals(msg, samplePE.getModificationDate(), sample.modification_timestamp);
            assertEquals(msg, samplePE.getSampleType().getId().longValue(), sample.saty_id);
            if (samplePE.getExperiment() == null)
            {
                assertNull(msg, sample.expe_id);
            } else
            {
                assertEquals(msg, samplePE.getExperiment().getId(), sample.expe_id);
            }
            if (samplePE.getDeletion() == null)
            {
                assertNull(msg, sample.del_id);
            } else
            {
                assertEquals(msg, samplePE.getDeletion().getId(), sample.del_id);
            }
            if (samplePE.getContainer() == null)
            {
                assertNull(msg, sample.samp_id_part_of);
            } else
            {
                assertNotNull(msg, samplePE.getContainer().getId());
                assertEquals(msg, samplePE.getContainer().getId(), sample.samp_id_part_of);
            }
            ++sampleCount;
        }
        assertEquals(listableSamplesInTestDB, sampleCount);
    }

    @Test
    public void testQueryGroupSamples()
    {
        int sampleCount = 0;
        for (SampleRecord sample : query.getListableSpaceSamples(groupCode))
        {
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
            assertEquals(msg, groupId, sample.space_id.longValue());
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            ++sampleCount;
        }
        assertEquals(31, sampleCount); // without deleted
    }

    @Test
    public void testQueryGroupSamplesBySampleType()
    {
        long sampleTypeId = getSampleTypeId(SAMPLE_TYPE_CODE_CELL_PLATE);
        List<SampleRecord> samples =
                asList(query.getSpaceSamplesForSampleType(groupCode, sampleTypeId));
        assertTrue(samples.size() >= 11);
        SampleRecord sample = findCode(samples, "CP-TEST-1");
        assertEquals(18, sample.expe_id.longValue());
        assertEquals(1042, sample.id);
        assertEquals(2, sample.pers_id_registerer.longValue());
        assertEquals(sampleTypeId, sample.saty_id);
        assertNotNull(sample.perm_id);
        assertNull(sample.samp_id_part_of);

        assertCodeNotFound(samples, "3VCP1"); // deleted
    }

    private Long getSampleTypeId(String sampleTypeCode)
    {
        return daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode).getId();
    }

    @Test
    public void testQueryExperimentSamples()
    {
        final long experimentId = 8; // EXP-REUSE

        int sampleCount = 0;
        for (SampleRecord sample : query.getListableSamplesForExperiment(experimentId))
        {
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            assertEquals(msg, experimentId, sample.expe_id.longValue());
            ++sampleCount;
        }
        assertEquals(7, sampleCount);
    }

    @Test
    public void testQueryGroupWithExperimentSamples()
    {
        int sampleCount = 0;
        for (SampleRecord sample : query.getSpaceSamplesWithExperiment(groupCode))
        {
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            assertEquals(msg, groupId, sample.space_id.longValue());
            assertNotNull(msg, sample.expe_id);
            ++sampleCount;
        }
        assertEquals(16, sampleCount); // without deleted
    }

    @Test
    public void testQueryContainedSamples()
    {
        int sampleCount = 0;
        LongSet childrenIds = new LongOpenHashSet();
        childrenIds.add(sharedMasterPlate.getId());
        for (SampleRecord sample : query.getSamplesForContainer(childrenIds))
        {
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            assertEquals(msg, sharedMasterPlate.getId(), sample.samp_id_part_of);
            ++sampleCount;
        }
        assertEquals(320, sampleCount);
    }

    @Test
    public void testQuerySharedSamples()
    {
        List<SampleRecord> samples = asList(query.getListableSharedSamples());
        assertTrue(samples.size() > 0);
        SampleRecord sample = findCode(samples, SHARED_MASTER_PLATE_CODE);
        assertEquals(SHARED_MASTER_PLATE_ID, sample.id);
    }

    @Test
    public void testQuerySharedSamplesBySampleType()
    {
        List<SampleRecord> samples =
                asList(query.getSharedSamplesForSampleType(masterPlateType.getId()));
        assertTrue(samples.size() > 0);
        SampleRecord sample = findCode(samples, SHARED_MASTER_PLATE_CODE);
        assertEquals(SHARED_MASTER_PLATE_ID, sample.id);
    }

    @Test
    public void testSampleType()
    {
        SampleType sampleType = query.getSampleType(SAMPLE_TYPE_CODE_MASTER_PLATE);
        assertEquals(SAMPLE_TYPE_CODE_MASTER_PLATE, sampleType.getCode());
    }

    @Test
    public void testSampleTypes()
    {
        List<SampleType> sampleTypes = Arrays.asList(query.getSampleTypes());
        findCode(sampleTypes, SAMPLE_TYPE_CODE_MASTER_PLATE);
        findCode(sampleTypes, SAMPLE_TYPE_CODE_CELL_PLATE);
    }

    @Test
    public void testMaterialTypes()
    {
        CodeRecord[] materialTypes = query.getMaterialTypes();
        findCode(Arrays.asList(materialTypes), MATERIAL_TYPE_CODE);
    }

    @Test
    public void testPropertyTypes()
    {
        PropertyType[] propertyTypes = query.getPropertyTypes();
        PropertyType propertyType = findCode(Arrays.asList(propertyTypes), "COMMENT");
        assertEquals(propertyType.getDataType().getCode(), DataTypeCode.VARCHAR);
        assertEquals(propertyType.getLabel(), "Comment");
    }

    @Test
    public void testVocabuaryURLTemplates()
    {
        query.getVocabularyURLTemplates();
    }
}
