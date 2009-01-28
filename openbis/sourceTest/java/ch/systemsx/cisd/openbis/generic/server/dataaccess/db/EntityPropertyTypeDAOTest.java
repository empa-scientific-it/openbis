/*
 * Copyright 2008 ETH Zuerich, CISD
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

import junit.framework.Assert;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for {@link EntityPropertyTypeDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "property" })
public class EntityPropertyTypeDAOTest extends AbstractDAOTest
{

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeMandatory")
    public final void testTryFindAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
        EntityTypePropertyTypePE assignment =
                daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(entityType,
                        propertyType);
        Assert.assertEquals(true, assignment.isMandatory());
    }

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeNotAssigned")
    public final void testTryFindNonexistentAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
        EntityTypePropertyTypePE assignment =
                daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(entityType,
                        propertyType);
        Assert.assertNull(assignment);
    }

    @Test(groups = "broken", dataProvider = "entityKindsWithEntityTypeAndPropertyTypeNotAssigned")
    public void testCreateEntityPropertyTypeAssignment(EntityKind entityKind, String typeCode,
            String propertyCode) throws Exception
    {
        // prepare data
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
        // check assignment does not exist
        Assert.assertNull(daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(
                entityType, propertyType));
        // create assignment
        EntityTypePropertyTypePE entityPropertyTypeAssignement =
                createAssignment(entityKind, entityType, propertyType);
        daoFactory.getEntityPropertyTypeDAO(entityKind).createEntityPropertyTypeAssignment(
                entityPropertyTypeAssignement);
        // check assignment exists
        Assert.assertNotNull(daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(
                entityType, propertyType));

    }

    @Test
    public void testListEntities() throws Exception
    {
        EntityKind entityKind = EntityKind.EXPERIMENT;
        String typeCode = "SIRNA_HCS";
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        Assert.assertEquals(daoFactory.getExperimentDAO().listExperiments(), daoFactory
                .getEntityPropertyTypeDAO(entityKind).listEntities(entityType));
    }

    private EntityTypePropertyTypePE createAssignment(EntityKind entityKind,
            EntityTypePE entityType, PropertyTypePE propertyType)
    {
        EntityTypePropertyTypePE entityPropertyTypeAssignement =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        entityPropertyTypeAssignement.setEntityType(entityType);
        entityPropertyTypeAssignement.setPropertyType(propertyType);
        entityPropertyTypeAssignement.setRegistrator(getTestPerson());
        return entityPropertyTypeAssignement;
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] entityKindsWithEntityTypeAndPropertyTypeMandatory()
    {
        return new Object[][]
            {
                { EntityKind.EXPERIMENT, "SIRNA_HCS", "USER.DESCRIPTION" },
                { EntityKind.SAMPLE, "CONTROL_LAYOUT", "PLATE_GEOMETRY" },
                { EntityKind.MATERIAL, "BACTERIUM", "USER.DESCRIPTION" } };
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] entityKindsWithEntityTypeAndPropertyTypeNotAssigned()
    {
        return new Object[][]
            {
                { EntityKind.EXPERIMENT, "SIRNA_HCS", "USER.IS_VALID" },
                { EntityKind.SAMPLE, "CONTROL_LAYOUT", "USER.IS_VALID" },
                { EntityKind.MATERIAL, "BACTERIUM", "USER.IS_VALID" }, };
    }
}
