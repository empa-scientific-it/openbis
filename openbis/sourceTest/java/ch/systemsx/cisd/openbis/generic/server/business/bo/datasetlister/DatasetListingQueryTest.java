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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.asList;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.findCode;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.utilities.Counters;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectSpaceCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Test cases for {@link ISampleListingQuery}.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
{ DatasetRecord.class, ExperimentProjectSpaceCodeRecord.class, IDatasetListingQuery.class,
        DatasetListerDAO.class })
@Test(groups =
{ "db", "dataset" })
@Transactional
public class DatasetListingQueryTest extends AbstractDAOTest
{

    private long dbInstanceId;

    private DatabaseInstance dbInstance;

    private IDatasetListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        DatasetListerDAO dao = createDatasetListerDAO(daoFactory);
        dbInstanceId = dao.getDatabaseInstanceId();
        dbInstance = dao.getDatabaseInstance();

        query = dao.getQuery();
    }

    public static DatasetListerDAO createDatasetListerDAO(IDAOFactory daoFactory)
    {
        IDatasetListingQuery query =
                EntityListingTestUtils.createQuery(daoFactory, IDatasetListingQuery.class);
        return DatasetListerDAO.create(daoFactory, query);
    }

    @Test
    public void testGetDatasetById()
    {
        long datasetId = 5;
        DatasetRecord dataset = query.getDataset(datasetId);
        assertDatasetCorrect(dataset);
    }

    private void assertDatasetCorrect(DatasetRecord dataset)
    {
        assertNotNull(dataset);
        assertNotNull(dataset.location);
        assertNotNull(dataset.code);
        assertNotNull(dataset.registration_timestamp);
    }

    @Test
    public void testGetDeletedDatasetById()
    {
        long datasetId = 2;
        DatasetRecord dataset = query.getDataset(datasetId);
        assertNull(dataset);
    }

    @Test
    public void testDatasets()
    {
        List<DatasetRecord> datasets = asList(query.getDatasets());
        assertTrue(datasets.size() > 0);
        assertEqualWithFetchedById(datasets.get(0));
        assertEqualWithFetchedById(datasets.get(datasets.size() - 1));
    }

    private void assertEqualWithFetchedById(DatasetRecord datasetRecord)
    {
        DatasetRecord sameDataset = query.getDataset(datasetRecord.id);
        assertTrue(EqualsBuilder.reflectionEquals(sameDataset, datasetRecord));
    }

    @Test
    public void testDatasetsForExperiments()
    {
        ExperimentPE experiment1 =
                getExperiment(dbInstance.getCode(), "CISD", "NEMO", "EXP-TEST-1", daoFactory);
        List<DatasetRecord> datasets = asList(query.getDatasetsForExperiment(experiment1.getId()));
        Counters<Long> counters = new Counters<Long>();
        for (DatasetRecord record : datasets)
        {
            assertDatasetCorrect(record);
            assertEqualWithFetchedById(record);
            counters.count(record.expe_id);
        }

        assertEquals(1, counters.getCountOf(experiment1.getId()));
        assertEquals(1, datasets.size());
    }

    @Test
    public void testQueryForDatasetsForExperimentAndDescendentsReturnsNotNullPostRegistered()
    {
        ExperimentPE experiment1 =
                getExperiment(dbInstance.getCode(), "CISD", "NEMO", "EXP-TEST-1", daoFactory);

        Long relationshipTypeId = RelationshipUtils.getParentChildRelationshipType(daoFactory.getRelationshipTypeDAO()).getId();

        List<DatasetRecord> datasets = asList(query.getDataSetsForExperimentAndDescendents(experiment1.getId(), relationshipTypeId));

        for (DatasetRecord record : datasets)
        {
            assertTrue(record.is_post_registered != null);
        }
    }

    @Test
    public void testQueryUsingSelectAllReturnsNotNullPostRegistered()
    {
        ExperimentPE experiment1 =
                getExperiment(dbInstance.getCode(), "CISD", "NEMO", "EXP-TEST-1", daoFactory);
        List<DatasetRecord> datasets = asList(query.getDatasetsForExperiment(experiment1.getId()));
        Counters<Long> counters = new Counters<Long>();
        for (DatasetRecord record : datasets)
        {
            assertTrue(record.is_post_registered != null);
        }
    }

    @Test
    public void testQueryUsingSelectAllExternalDatasReturnsNotNullPostRegistered()
    {
        List<DatasetRecord> datasets = asList(query.getDatasetsByDataStoreId(1));
        for (DatasetRecord record : datasets)
        {
            assertTrue(record.is_post_registered != null);
        }
    }

    @Test
    public void testQueryUsingSelectAllExternalDatasReturnsCorrectPostRegistered()
    {
        List<DatasetRecord> datasets = asList(query.getDatasetsByDataStoreId(1));
        for (DatasetRecord record : datasets)
        {
            if (record.code.equals("COMPONENT_1A"))
            {
                assertTrue(record.is_post_registered == false);
            }
            else
            {
                assertTrue(record.is_post_registered == true);
            }
        }
    }

    @Test
    public void testDatasetsForSample()
    {
        SamplePE sample = getSample("CISD", "NEMO", "CP-TEST-1", dbInstanceId, daoFactory);
        long sampleId = sample.getId();
        List<DatasetRecord> datasets = asList(query.getDatasetsForSample(sampleId));
        assertTrue(datasets.size() > 0);
        for (DatasetRecord record : datasets)
        {
            assertDatasetCorrect(record);
            assertEqualWithFetchedById(record);
            assertEquals(sampleId, record.samp_id.longValue());
        }
        // check coherence between the method which fetches only ids for the sample datasets
        Set<Long> datasetIds = EntityListingTestUtils.asSet(query.getDatasetIdsForSample(sampleId));
        assertEquals(datasets.size(), datasetIds.size());
        for (DatasetRecord record : datasets)
        {
            assertTrue(datasetIds.contains(record.id));
        }

    }

    static ExperimentPE getExperiment(String dbInstanceCode, String groupCode, String projectCode,
            String expCode, IDAOFactory daoFactory)
    {
        ProjectPE project =
                daoFactory.getProjectDAO().tryFindProject(groupCode, projectCode);
        assertNotNull(project);
        ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryFindByCodeAndProject(project, expCode);
        assertNotNull(experiment);
        return experiment;
    }

    static SamplePE getSample(String spaceCode, String projectCode, String sampleCode, long dbInstanceId,
            IDAOFactory daoFactory)
    {
        SpacePE space = daoFactory.getSpaceDAO().tryFindSpaceByCode(spaceCode);
        assertNotNull(space);
        SamplePE sample;
        if (projectCode == null)
        {
            sample = daoFactory.getSampleDAO().tryFindByCodeAndSpace(sampleCode, space);
        } else
        {
            ProjectPE project = daoFactory.getProjectDAO().tryFindProject(spaceCode, projectCode);
            assertNotNull(project);
            sample = daoFactory.getSampleDAO().tryfindByCodeAndProject(sampleCode, project);
        }
        assertNotNull(sample);
        return sample;
    }

    @Test
    public void testParentDatasetsForChild()
    {
        long datasetId = 9;
        List<DatasetRecord> parents = asList(query.getParentsOf(new LongOpenHashSet(Arrays.asList(datasetId)), 1L));
        assertEquals(3, parents.size()); // the data set has 4 parents but 1 is deleted
    }

    @Test
    public void testChildDatasetsForParent()
    {
        LongSet datasetIds = new LongOpenHashSet(new long[]
        { 9 });
        List<DatasetRecord> children = asList(query.getChildrenOf(new LongOpenHashSet(datasetIds), 1L));
        assertEquals(2, children.size());
    }

    @Test
    public void testDatasetTypes()
    {
        CodeRecord[] datasetTypes = query.getDatasetTypes();
        assertEqualsOrGreater(3, datasetTypes.length);
        findCode(Arrays.asList(datasetTypes), "UNKNOWN");
    }

    @Test
    public void testDataStores()
    {
        CodeRecord[] codes = query.getDataStores();
        assertEqualsOrGreater(1, codes.length);
        findCode(Arrays.asList(codes), "STANDARD");
    }

    @Test
    public void testLocatorTypes()
    {
        CodeRecord[] codes = query.getLocatorTypes();
        assertEqualsOrGreater(1, codes.length);
        findCode(Arrays.asList(codes), "RELATIVE_LOCATION");
    }

    @Test
    public void testFileFormatTypes()
    {
        CodeRecord[] codes = query.getFileFormatTypes();
        assertEqualsOrGreater(8, codes.length);
        findCode(Arrays.asList(codes), "XML");
    }

}