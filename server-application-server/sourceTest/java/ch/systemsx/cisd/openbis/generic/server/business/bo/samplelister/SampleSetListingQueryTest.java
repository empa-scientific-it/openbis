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
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.createSet;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.findExactlyOneProperty;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.findProperties;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Test cases for {@link ISampleListingQuery} set queries.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
{ SampleRecord.class, ISampleListingQuery.class })
@Test(groups =
{ "db", "sample" })
public class SampleSetListingQueryTest extends AbstractDAOTest
{

    private static final int CELL_PLATE_ID_CP_TEST_1 = 1042; // CP-TEST-1

    private static final int CELL_PLATE_ID_CP_TEST_2 = 1043; // CP-TEST-2

    private ISampleListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        SampleListerDAO sampleListerDAO = SampleListingQueryTest.createSampleListerDAO(daoFactory);
        query = sampleListerDAO.getQuery();
    }

    @Test
    public void testQuerySamples()
    {
        LongSet ids = createSet(CELL_PLATE_ID_CP_TEST_1, CELL_PLATE_ID_CP_TEST_2);
        List<SampleRecord> samples = asList(query.getSamples(ids));
        assertEquals(ids.size(), samples.size());
        for (SampleRecord sampleRowVO : samples)
        {
            assertTrue(ids.contains(sampleRowVO.id));
            SampleRecord sameSample = query.getSample(sampleRowVO.id);
            assertTrue(EqualsBuilder.reflectionEquals(sampleRowVO, sameSample));
        }
    }

    @Test
    public void testSamplePropertyGenericValues()
    {
        LongSet ids = createSet(CELL_PLATE_ID_CP_TEST_1, CELL_PLATE_ID_CP_TEST_2);
        PropertyType propertyType =
                EntityListingTestUtils.findPropertyType(query.getPropertyTypes(), "comment");
        Iterable<GenericEntityPropertyRecord> properties =
                query.getEntityPropertyGenericValues(ids);
        List<GenericEntityPropertyRecord> comments =
                findProperties(properties, propertyType.getId());
        assertEquals("There should be exactly one comment for each sample", ids.size(), comments
                .size());
        findExactlyOneProperty(comments, propertyType.getId(), CELL_PLATE_ID_CP_TEST_1);
        findExactlyOneProperty(comments, propertyType.getId(), CELL_PLATE_ID_CP_TEST_2);
        for (GenericEntityPropertyRecord comment : comments)
        {
            if (comment.entity_id == CELL_PLATE_ID_CP_TEST_1)
            {
                assertEquals(comment.value, "very advanced stuff");
            } else if (comment.entity_id == CELL_PLATE_ID_CP_TEST_2)
            {
                assertEquals(comment.value, "extremely simple stuff");
            }
        }
    }

}
