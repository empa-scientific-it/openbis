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

package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.createSet;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.findExactlyOneProperty;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.findProperties;
import static org.testng.AssertJUnit.assertEquals;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Test cases for {@link IMaterialListingQuery} queries.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
{ MaterialRecord.class, IMaterialListingQuery.class, MaterialListerDAO.class })
@Test(groups =
{ "db", "material" })
public class MaterialListingQueryTest extends AbstractDAOTest
{

    private static final long BACTERIUM_MATERIAL_TYPE = 6L;

    // ID of the BACTERIA which has ORGANISM property filled
    private static final long MATERIAL_ID_TEST_1 = 34;

    private static final long MATERIAL_ID_TEST_2 = 35;

    private IMaterialListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        MaterialListerDAO materialListerDAO = createMaterialListerDAO(daoFactory);
        query = materialListerDAO.getQuery();
    }

    public static MaterialListerDAO createMaterialListerDAO(IDAOFactory daoFactory)
    {
        IMaterialListingQuery query =
                EntityListingTestUtils.createQuery(daoFactory, IMaterialListingQuery.class);
        return MaterialListerDAO.create(daoFactory, query);
    }

    @Test
    public void testMaterialPropertyGenericValues()
    {
        LongSet ids = createSet(MATERIAL_ID_TEST_1, MATERIAL_ID_TEST_2);
        PropertyType propertyType =
                EntityListingTestUtils.findPropertyType(query.getPropertyTypes(), "description");
        Iterable<GenericEntityPropertyRecord> properties =
                query.getEntityPropertyGenericValues(ids);
        List<GenericEntityPropertyRecord> descriptions =
                findProperties(properties, propertyType.getId());
        assertEquals("There should be exactly one description for each material", ids.size(),
                descriptions.size());
        findExactlyOneProperty(descriptions, propertyType.getId(), MATERIAL_ID_TEST_1);
        findExactlyOneProperty(descriptions, propertyType.getId(), MATERIAL_ID_TEST_2);
        for (GenericEntityPropertyRecord desc : descriptions)
        {
            if (desc.entity_id == MATERIAL_ID_TEST_1)
            {
                assertEquals(desc.value, "test bacterium 1");
            } else if (desc.entity_id == MATERIAL_ID_TEST_2)
            {
                assertEquals(desc.value, "test bacterium 2");
            }
        }
    }

    @Test
    public void testListMaterialsByType()
    {
        Iterable<MaterialRecord> materials =
                query.getMaterialsForMaterialType(BACTERIUM_MATERIAL_TYPE);
        Set<String> remainingBacteriumCodes = new HashSet<String>(Arrays.asList(new String[]
        { "BACTERIUM-X", "BACTERIUM-Y", "BACTERIUM1", "BACTERIUM2" }));
        for (MaterialRecord materialRecord : materials)
        {
            assertEquals(BACTERIUM_MATERIAL_TYPE, materialRecord.maty_id);
            if (false == remainingBacteriumCodes.remove(materialRecord.code))
            {
                AssertJUnit.fail("Unexpected material code: '" + materialRecord.code + "'");
            }
        }
        assertEquals(0, remainingBacteriumCodes.size());
    }

    public void testListMaterialsByTypeAndId()
    {
        LongSet materialIds = new LongOpenHashSet(new long[]
        { 34L, 22L });
        Iterable<MaterialRecord> materials =
                query.getMaterialsForMaterialTypeWithIds(materialIds);
        Set<String> remainingBacteriumCodes = new HashSet<String>(Arrays.asList(new String[]
        { "BACTERIUM-X", "BACTERIUM1" }));
        for (MaterialRecord materialRecord : materials)
        {
            assertEquals(BACTERIUM_MATERIAL_TYPE, materialRecord.maty_id);
            if (false == remainingBacteriumCodes.remove(materialRecord.code))
            {
                AssertJUnit.fail("Unexpected material code: '" + materialRecord.code + "'");
            }
        }
        assertEquals(0, remainingBacteriumCodes.size());
    }
}
