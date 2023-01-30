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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityListingQueryTest;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * @author Piotr Buczek
 */
@Friend(toClasses =
{ MaterialRecord.class })
@Test(groups =
{ "db", "material" })
public class MaterialListerTest extends AbstractDAOTest
{
    private IMaterialLister lister;

    private static final long BACTERIUM_MATERIAL_TYPE = 6L;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        MaterialListerDAO materialListerDAO =
                MaterialListingQueryTest.createMaterialListerDAO(daoFactory);
        SecondaryEntityDAO secondaryEntityDAO =
                SecondaryEntityListingQueryTest.createSecondaryEntityDAO(daoFactory);
        lister = MaterialLister.create(materialListerDAO, secondaryEntityDAO, "url", null);
    }

    @Test
    public void testListByMaterialTypeWithProperties()
    {
        MaterialType materialType = createMaterialType();
        boolean withProperties = true;
        List<Material> materials =
                lister.list(ListMaterialCriteria.createFromMaterialType(materialType),
                        withProperties);
        assertEqualsOrGreater(4, materials.size());
        assertMaterialsProperlyFetched(materials, materialType, withProperties == false);

    }

    @Test
    public void testListByMaterialTypeWithoutProperties()
    {
        MaterialType materialType = createMaterialType();
        boolean withProperties = false;
        List<Material> materials =
                lister.list(ListMaterialCriteria.createFromMaterialType(materialType),
                        withProperties);
        assertEqualsOrGreater(4, materials.size());
        assertMaterialsProperlyFetched(materials, materialType, withProperties == false);
    }

    @Test
    public void testListByMaterialTypeAndMaterialIds()
    {
        MaterialType materialType = createMaterialType();
        Collection<Long> materialIds = Arrays.asList(new Long[]
        { 22L, 34L });
        boolean withProperties = true;
        List<Material> materials =
                lister.list(ListMaterialCriteria.createFromMaterialIds(materialIds), withProperties);
        assertEqualsOrGreater(2, materials.size());
        assertMaterialsProperlyFetched(materials, materialType, withProperties == false);
    }

    private void assertMaterialsProperlyFetched(List<Material> materials,
            MaterialType expectedType, boolean emptyProperties)
    {
        for (Material material : materials)
        {
            assertNotNull(material.getId());
            assertNotNull(material.getCode());
            assertNotNull(material.getRegistrator());
            assertNotNull(material.getRegistrationDate());
            assertNotNull(material.getModificationDate());
            assertEquals(expectedType.getCode(), material.getMaterialType().getCode());
            assertEquals(emptyProperties, material.getProperties().isEmpty());
        }
    }

    private MaterialType createMaterialType()
    {
        final MaterialType materialType = new MaterialType();
        materialType.setId(BACTERIUM_MATERIAL_TYPE);
        materialType.setCode("BACTERIUM");
        return materialType;
    }

}
