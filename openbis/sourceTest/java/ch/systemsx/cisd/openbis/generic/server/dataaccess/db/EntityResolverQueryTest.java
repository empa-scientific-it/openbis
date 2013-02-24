/*
 * Copyright 2013 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertNull;

import java.sql.SQLException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the simple entity resolver.
 * 
 * @author Bernd Rinn
 */
@Test(groups =
    { "db" })
public class EntityResolverQueryTest extends AbstractDAOTest
{
    private IEntityResolverQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        query = EntityResolverQueryFactory.create();
    }

    @Test
    public void testResolveSpace()
    {
        assertEquals(new Long(1), query.tryResolveSpaceIdByCode("CISD"));
    }

    @Test
    public void testResolveProjectByCode()
    {
        assertEquals(new Long(5), query.tryResolveProjectIdByCode("TEST-SPACE", "TEST-PROJECT"));
    }

    @Test
    public void testResolveProjectByPermId()
    {
        assertEquals(new Long(4), query.tryResolveProjectIdByPermId("20120814110011738-104"));
    }

    @Test
    public void testResolveExperimentByCode()
    {
        assertEquals(new Long(23),
                query.tryResolveExperimentIdByCode("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST"));
    }

    @Test
    public void testResolveExperimentByPermId()
    {
        assertEquals(new Long(8), query.tryResolveExperimentIdByPermId("200811050940555-1032"));
    }

    @Test
    public void testResolveSampleByCode()
    {
        assertEquals(new Long(1054),
                query.tryResolveSampleIdByCode("TEST-SPACE", "FV-TEST"));
    }

    @Test
    public void testResolveSampleByPermId()
    {
        assertEquals(new Long(1019), query.tryResolveSampleIdByPermId("200811050929035-1014"));
    }

    @Test
    public void testResolveMaterialByCode()
    {
        assertEquals(new Long(36),
                query.tryResolveMaterialIdByCode("GENE", "MYGENE1"));
    }

    @Test
    public void testResolveDatasetByCode()
    {
        assertEquals(new Long(13),
                query.tryResolveDatasetIdByCode("20110509092359990-10"));
    }

    @Test
    public void testResolveNonExistentDatasetByCode()
    {
        assertNull(query.tryResolveDatasetIdByCode("NONEXISTENT"));
    }

}
